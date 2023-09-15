package nl.enjarai.rites.block

import eu.pb4.polymer.core.api.utils.PolymerObject
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.*
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

    private val ingredientsFullFilled = mutableListOf<Int>()

    constructor(pos: BlockPos, state: BlockState) : this(ModBlocks.RITE_CENTER_ENTITY, pos, state)

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)

        val ingredientsFullFilled = NbtList()
        for (ingredient in this.ingredientsFullFilled) {
            ingredientsFullFilled.add(NbtInt.of(ingredient))
        }
        nbt.put("ingredientsFullFilled", ingredientsFullFilled)
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)

        val ingredientsFullFilled = nbt.getList("ingredientsFullFilled", NbtElement.INT_TYPE.toInt())
        for (ingredient in ingredientsFullFilled) {
            this.ingredientsFullFilled.add((ingredient as NbtInt).intValue())
        }
    }

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

        ingredientsFullFilled.clear()

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
        return ritualContext?.getSelectedRitual()?.ritual?.ingredients?.mapIndexedNotNull { i, ingredient ->
            if (ingredientsFullFilled.contains(i)) return@mapIndexedNotNull null
            ingredient to ingredient.amount.min
        }?.associate { it } ?: mapOf()
    }

    private fun tryAbsorbMissing(missingItems: Map<Ingredient, Int>): Boolean {
        // Find all item entities within the circle, match them to ingredients and absorb one
        ritualContext?.getItemsInRange()?.let {
            Ritual.requiredItemsNearby(missingItems, it)?.entries?.toList()?.get(0)?.let { (ingredient, itemEntity) ->
                Visuals.absorb(getWorld()!! as ServerWorld, itemEntity.pos)

                val stack = itemEntity.stack
                val absorbAmount = ingredient.amount.max.coerceAtMost(stack.count)
                val insertableStack = stack.split(absorbAmount);
                storedItems += insertableStack
                ritualContext?.maybeAddAddressableItem(ingredient, insertableStack)
                if (stack.isEmpty) {
                    itemEntity.discard()
                }
                ingredientsFullFilled.add(ritualContext!!.getSelectedRitual()!!.ritual.ingredients.indexOf(ingredient))
                return true
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

            // Transfer ref items
            val ownItems = ritualContext!!.rawAddressableItems
            val otherItems = it.ritualContext!!.rawAddressableItems
            val overlappingKeys = otherItems.keys.intersect(ownItems.keys)
            it.ritualContext!!.rawAddressableItems = otherItems.filter { (key, stack) ->
                if (key !in overlappingKeys) {
                    ownItems[key] = stack
                    return@filter false
                }
                return@filter true
            }.toMutableMap()
        }
    }

    override fun startRitual(ritual: Ritual, circles: List<CircleType>) {
        ingredientsFullFilled.clear()

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
        ingredientsFullFilled.clear()

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