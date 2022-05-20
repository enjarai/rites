package nl.enjarai.rites.block

import eu.pb4.polymer.api.utils.PolymerObject
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.resource.Rituals
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.RitualResult
import nl.enjarai.rites.util.Visuals

class RiteCenterBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlocks.RITE_CENTER_ENTITY, pos, state), PolymerObject
{
    companion object {
        const val COOLDOWN = 30
    }

    private var tickCooldown = 0
    private var storedItems = arrayOf<ItemStack>()
    private var ritualContext: RitualContext? = null

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        tickCooldown = nbt.getInt("tickCooldown")

        storedItems = nbt.getList("storedItems", NbtList.COMPOUND_TYPE.toInt()).map {
            ItemStack.fromNbt(it as NbtCompound)
        }.toTypedArray()

        val ritualNbt = nbt.getCompound("ritualContext")
        if (!ritualNbt.isEmpty) {
            ritualContext = RitualContext({ getWorld()!! }, getPos(), ritualNbt)
        }
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.putInt("tickCooldown", tickCooldown)

        val items = NbtList()
        for (stack in storedItems) {
            val entry = NbtCompound()
            items.add(stack.writeNbt(entry))
            items.add(entry)
        }
        nbt.put("storedItems", items)

        if (ritualContext != null) nbt.put("ritualContext", ritualContext!!.toNbt())
    }

    fun onUse(player: PlayerEntity) {
        if (player.isSneaking) {
            ritualContext?.cycleRituals()
        } else {
            if (ritualContext?.getSelectedRitual() == null) {
                val success = startRitual()
                setPower(if (success) 15 else 7)
                if (!success) {
                    Visuals.failParticles(getWorld() as ServerWorld, Vec3d.ofBottomCenter(getPos()))
                }
            } else {
                endAllRituals(!(ritualContext?.hasActivating() ?: true))
            }
        }
    }

    fun tick() {
        if (getWorld() is ServerWorld && ritualContext != null) {
            ritualContext!!.drawParticleEffects()
            Visuals.hum(getWorld()!! as ServerWorld, getPos(),
                if (ritualContext!!.hasActivating()) 0.8f else 0.2f)

            tickCooldown -= 1
            if (tickCooldown < 0) {
                tickCooldown = COOLDOWN

                // Make sure we get saved properly
                markDirty()

                // Check validity of ritual circle
                if (!ritualContext!!.canMaintain()) {
                    endAllRituals(false)
                    return
                }

                // If ritual hasn't activated yet, try absorbing new items
                if (ritualContext!!.hasActivating()) {
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
                    if (!ritualContext!!.getSelectedRitual()!!.ritual.hasTickingEffects) {
                        endAllRituals(true)
                    }
                    return
                }
            }

            // Tick rituals with lasting effects
            if (!ritualContext!!.hasActivating() && ritualContext!!.tickAll() == RitualResult.FAIL) {
                endAllRituals(false)
                return
            }
        }
    }

    /**
     * Runs when a ritual is initiated,
     * selects the ritual to be used based on nearby
     * items and circle and adds it to the stack
     */
    private fun startRitual(): Boolean {
        Rituals.values.values.forEach { ritual ->
            val circles = ritual.isValid(getWorld()!!, getPos(), ritualContext)

            if (hasContext()) circles.forEach circles@{
                if (this.ritualContext?.canAddCircle(it.size) == false) return@forEach
            }

            if (circles.isNotEmpty() && ritual.requiredItemsNearby(getWorld()!!, getPos())) {
                initContext()
                this.ritualContext?.appendRitual(ritual)
                this.ritualContext?.appendCircles(circles)
                tickCooldown = COOLDOWN
                return true
            }
        }
        return false
    }

    private fun endAllRituals(success: Boolean) {
        // Stop rituals and store results
        storedItems = ritualContext?.stopAllRituals(success) ?: arrayOf()

        // Reset all state
        ritualContext = null
        setPower(0)

        // Drop currently stored items, if available
        val pos = Vec3d.ofBottomCenter(getPos())
        storedItems.forEach {
            getWorld()?.spawnEntity(ItemEntity(getWorld(), pos.x, pos.y, pos.z, it))
        }
        storedItems = arrayOf()

        // Save the fact that the rituals have stopped
        markDirty()
    }

    private fun hasContext(): Boolean {
        return ritualContext != null
    }

    private fun initContext() {
        if (!hasContext()) {
            ritualContext = RitualContext({ getWorld()!! }, getPos())
        }
    }

    private fun getMissingItems(): Map<Item, Int> {
        return ritualContext?.getSelectedRitual()?.ritual?.ingredients?.mapValues { entry ->
            var amountNeeded = entry.value
            storedItems.forEach {
                if (it.isOf(entry.key)) amountNeeded -= it.count
            }
            return@mapValues amountNeeded.coerceAtLeast(0)
        }?.filter { entry -> entry.value > 0 } ?: mapOf()
    }

    private fun tryAbsorbMissing(missingItems: Map<Item, Int>): Boolean {
        // Find all item entities within the circle
        ritualContext?.getSelectedRitual()?.ritual?.getItemsInRange(getWorld()!!, getPos())?.forEach { itemEntity ->

            // Absorb if possible
            val stack = itemEntity.stack
            missingItems.forEach {
                if (it.key == stack.item) {
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

    private fun setPower(value: Int) {
        ModBlocks.RITE_CENTER.setPower(getWorld()!!, getPos(), cachedState, value)
    }
}