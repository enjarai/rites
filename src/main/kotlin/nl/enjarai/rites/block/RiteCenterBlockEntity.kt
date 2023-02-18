package nl.enjarai.rites.block

import eu.pb4.polymer.core.api.utils.PolymerObject
import net.minecraft.block.BlockState
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.resource.Rituals
import nl.enjarai.rites.type.RitualResult
import nl.enjarai.rites.type.predicate.Ingredient
import nl.enjarai.rites.util.Visuals

open class RiteCenterBlockEntity(pos: BlockPos, state: BlockState) : RiteRunningBlockEntity(ModBlocks.RITE_CENTER_ENTITY, pos, state), PolymerObject {
    companion object {
        const val COOLDOWN = 30
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

            if (circles.isNotEmpty() && ritual.requiredItemsNearby(getWorld()!!, getPos(), circles)) {
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

    private fun setPower(value: Int) {
        ModBlocks.RITE_CENTER.setPower(getWorld()!!, getPos(), cachedState, value)
    }
}