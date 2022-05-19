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
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.resource.Rituals
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.util.RitualContext
import nl.enjarai.rites.util.Visuals

class RiteCenterBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlocks.RITE_CENTER_ENTITY, pos, state), PolymerObject
{
    companion object {
        val COOLDOWN = 30
    }

    private var tickCooldown = 0
    private var ritual: Ritual? = null
    private var hadAllItems = false
    private var isActive = false
    private var storedItems = arrayOf<ItemStack>()
    private var ritualContext: RitualContext? = null

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        tickCooldown = nbt.getInt("tickCooldown")
        ritual = Rituals.values[Identifier.tryParse(nbt.getString("ritual"))]
        hadAllItems = nbt.getBoolean("hadAllItems")
        isActive = nbt.getBoolean("isActive")

        storedItems = nbt.getList("storedItems", NbtList.COMPOUND_TYPE.toInt()).map {
            ItemStack.fromNbt(it as NbtCompound)
        }.toTypedArray()

        if (ritual != null) {
            ritualContext = RitualContext({getWorld()!!}, getPos(), ritual!!, nbt.getCompound("ritualContext"))
        }
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.putInt("tickCooldown", tickCooldown)
        val ritualId = ritual?.id?.toString()
        if (ritualId != null) nbt.putString("ritual", ritualId)
        nbt.putBoolean("hadAllItems", hadAllItems)
        nbt.putBoolean("isActive", isActive)

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
        if (ritual == null) {
            val success = startRitual()
            setPower(if (success) 15 else 7)
            if (!success) {
                Visuals.failParticles(getWorld() as ServerWorld, Vec3d.ofBottomCenter(getPos()))
            }
        } else {
            stopRitual(isActive)
        }
    }

    fun tick() {
        if (getWorld() is ServerWorld && ritual != null) {
            ritual!!.drawParticleEffects(getWorld()!!, getPos())
            Visuals.hum(getWorld()!! as ServerWorld, getPos(),
                if (isActive) 0.1f else 1.0f)

            tickCooldown -= 1
            if (tickCooldown < 0) {
                tickCooldown = COOLDOWN

                // Make sure we get saved properly
                markDirty()

                // Check validity of ritual circle
                if (!ritual!!.isValid(getWorld()!!, getPos())) {
                    stopRitual(false)
                    return
                }

                // If ritual hasn't activated yet, try absorbing new items
                if (!isActive) {
                    val missingItems = getMissingItems()
                    if (missingItems.isNotEmpty()) {
                        if (!tryAbsorbMissing(missingItems)) {
                            stopRitual(false)
                        }
                        return
                    }
                }

                // Deactivate the ritual after activation if activation fails, or ritual has no lasting effects
                if (!hadAllItems) {
                    hadAllItems = true
                    if (!activateRitual() || !ritual!!.hasTickingEffects) {
                        stopRitual(!ritual!!.hasTickingEffects)
                    }
                    return
                }
            }

            // Tick rituals with lasting effects
            if (ritualContext != null && !ritual!!.tick(ritualContext!!)) {
                stopRitual(false)
                return
            }
        }
    }

    /**
     * Runs when a ritual is initiated, selects the ritual to be used based on nearby items and circles
     */
    private fun startRitual(): Boolean {
        Rituals.values.values.forEach { ritual ->
            if (ritual.isValid(getWorld()!!, getPos()) && ritual.requiredItemsNearby(getWorld()!!, getPos())) {
                this.ritual = ritual
                tickCooldown = COOLDOWN
                return true
            }
        }
        return false
    }

    /**
     * Runs when ritual is done absorbing items
     */
    private fun activateRitual(): Boolean {
        ritualContext = RitualContext({getWorld()!!}, getPos(), ritual!!)
        ritualContext!!.storedItems = storedItems
        val success = ritual?.activate(ritualContext!!) ?: false
        if (success) {
            Visuals.activate(getWorld()!! as ServerWorld, getPos())
            storedItems = arrayOf()
            isActive = true
        }
        return success
    }

    /**
     * Safely stops any running ritual
     */
    private fun stopRitual(success: Boolean) {
        if (!success) {
            Visuals.failParticles(getWorld() as ServerWorld, Vec3d.ofBottomCenter(getPos()))
        } else {
            storedItems += ritualContext!!.returnableItems
        }

        // Reset all state
        ritualContext = null
        hadAllItems = false
        isActive = false
        ritual = null
        setPower(0)

        // Drop currently stored items, if available
        val pos = Vec3d.ofBottomCenter(getPos())
        storedItems.forEach {
            getWorld()?.spawnEntity(ItemEntity(world, pos.x, pos.y, pos.z, it))
        }
        storedItems = arrayOf()

        // Save the fact that the ritual stopped
        markDirty()
    }

    private fun getMissingItems(): Map<Item, Int> {
        return ritual?.ingredients?.mapValues { entry ->
            var amountNeeded = entry.value
            storedItems.forEach {
                if (it.isOf(entry.key)) amountNeeded -= it.count
            }
            return@mapValues amountNeeded.coerceAtLeast(0)
        }?.filter { entry -> entry.value > 0 } ?: mapOf()
    }

    private fun tryAbsorbMissing(missingItems: Map<Item, Int>): Boolean {
        // Find all item entities within the circle
        ritual?.getItemsInRange(getWorld()!!, getPos())?.forEach { itemEntity ->

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