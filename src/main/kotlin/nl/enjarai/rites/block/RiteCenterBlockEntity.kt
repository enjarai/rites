package nl.enjarai.rites.block

import eu.pb4.polymer.core.api.utils.PolymerObject
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.resource.Rituals
import nl.enjarai.rites.type.CircleType
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualResult
import nl.enjarai.rites.type.predicate.Ingredient
import nl.enjarai.rites.util.Visuals

open class RiteCenterBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : RiteRunningBlockEntity(type, pos, state), PolymerObject {
    companion object {
        const val COOLDOWN = 30
    }

    constructor(pos: BlockPos, state: BlockState) : this(ModBlocks.RITE_CENTER_ENTITY, pos, state)

    fun onUse(player: PlayerEntity) {
        if (player.isSneaking) {
            endAllRituals(!(ritualContext?.hasActivating() ?: true))
        } else {
            val success = findAndStartRitual()

            if (ritualContext?.getSelectedRitual() == null) {
                setPower(if (success) 15 else 7)
            }

            if (!success) {
                Visuals.failParticles(getWorld() as ServerWorld, Vec3d.ofBottomCenter(getPos()))
            }
        }
    }

    /**
     * Runs when a ritual is initiated,
     * selects the ritual to be used based on nearby
     * items and circle and adds it to the stack
     */
    private fun findAndStartRitual(): Boolean {
        Rituals.values.values.forEach { ritual ->
            val circles = ritual.isValid(getWorld()!!, getPos(), ritualContext)

//            if (hasContext()) circles.forEach circles@{
//                if (this.ritualContext?.canAddCircle(it.size) == false) return@forEach
//            }

            if (circles.isNotEmpty() && ritual.requiredItemsNearby(getWorld()!!, getPos(), circles) && canRunRitual(ritual, circles)) {
                startRitual(ritual, circles)
                return true
            }
        }
        return false
    }

    override fun ritualActivatingTick() {
        val missingItems = getMissingItems()
        if (missingItems.isNotEmpty()) {
            if (!tryAbsorbMissing(missingItems)) {
                endAllRituals(false)
            }
            return
        }

        // Deactivate the ritual after activation if activation fails, or ritual has no lasting effects
        if (ritualContext!!.activateRitual(storedItems) != RitualResult.FAIL) {
            storedItems = arrayOf()
        } else {
            endAllRituals(false)
            return
        }

        // Deactivate right away if the ritual we just activated isn't ticking
        if (!ritualContext!!.getSelectedRitual()!!.ritual.shouldKeepRunning) {
            endAllRituals(true)
        }
    }

    private fun getMissingItems(): Map<Ingredient, Int> {
        return ritualContext?.getSelectedRitual()?.ritual?.ingredients?.associate {
            it to it.amount
        }?.mapValues { entry ->
            var amountNeeded = entry.value
            storedItems.forEach {
                if (entry.key.test(it)) {
                    amountNeeded -= it.count
                }
            }
            return@mapValues amountNeeded
        }?.filter { entry -> entry.value > 0 } ?: mapOf()
    }

    private fun tryAbsorbMissing(missingItems: Map<Ingredient, Int>): Boolean {
        // Find all item entities within the circle
        ritualContext?.getItemsInRange()?.forEach { itemEntity ->

            // Absorb if possible
            val stack = itemEntity.stack
            missingItems.forEach {
                if (it.key.test(stack)) {
                    Visuals.absorb(getWorld()!! as ServerWorld, itemEntity.pos)

                    val absorbAmount = it.value.coerceAtMost(stack.count)
                    storedItems += stack.split(absorbAmount)
                    if (stack.isEmpty) {
                        itemEntity.discard()
                    }
                    return true
                }
            }
        }
        return false
    }

    /**
     * Finds the centers of all circles that intersect with this circle
     */
    private fun findIntersectingSubCircles(centerType: Block): List<RiteCenterBlockEntity> {
        val subCenters = mutableListOf<RiteCenterBlockEntity>()

        val circlePos = getPos()
        val circleRadius = ritualContext?.range ?: return subCenters

        val minX = circlePos.x - circleRadius * 2
        val maxX = circlePos.x + circleRadius * 2
        val minZ = circlePos.z - circleRadius * 2
        val maxZ = circlePos.z + circleRadius * 2

        val world = getWorld() ?: return subCenters

        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                if (x == circlePos.x && z == circlePos.z) continue
                val pos = BlockPos(x, circlePos.y, z)
                val state = world.getBlockState(pos)
                if (state.isOf(centerType)) {
                    val entity = world.getBlockEntity(pos) as? RiteCenterBlockEntity
                    if (entity?.ritualContext?.overlapsWith(ritualContext!!) == true) {
                        subCenters.add(entity)
                    }
                }
            }
        }

        return subCenters
    }

    private fun grabVariablesFromSubCircles() {
        findIntersectingSubCircles(ModBlocks.RITE_SUBCENTER).forEach {
            val subCenter = it as? RiteSubCenterBlockEntity ?: return@forEach
            if (subCenter.linkedCenter != null) return@forEach
            subCenter.linkedCenter = getPos()
            if (it.ritualContext?.hasActivating() != false) return@forEach
            ritualContext!!.variables += it.ritualContext?.variables ?: return@forEach
        }
    }

    override fun startRitual(ritual: Ritual, circles: List<CircleType>) {
        super.startRitual(ritual, circles)

        grabVariablesFromSubCircles()
    }

    override fun slowTick(world: ServerWorld) {
        if (ritualContext != null) {
            grabVariablesFromSubCircles()
        }
    }

    override fun endAllRituals(success: Boolean) {
        super.endAllRituals(success)

        setPower(0)

        // Drop currently stored items, if available
        val pos = Vec3d.ofBottomCenter(getPos())
        storedItems.forEach {
            getWorld()?.spawnEntity(ItemEntity(getWorld(), pos.x, pos.y, pos.z, it))
        }
        storedItems = arrayOf()

        markDirty()
    }

    open fun canRunRitual(ritual: Ritual, circles: List<CircleType>): Boolean {
        return true
    }

    private fun setPower(value: Int) {
        ModBlocks.RITE_CENTER.setPower(getWorld()!!, getPos(), cachedState, value)
    }
}