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

class RiteCenterBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlocks.RITE_CENTER_ENTITY, pos, state), PolymerObject
{
    companion object {
        val COOLDOWN = 20
    }
    var tickCooldown = 0
    var ritual: Ritual? = null
    var hadAllItems = false
    var isRunning = false
    var storedItems = arrayOf<ItemStack>()

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        tickCooldown = nbt.getInt("tickCooldown")
        ritual = Rituals.values[Identifier.tryParse(nbt.getString("ritual"))]
        hadAllItems = nbt.getBoolean("hadAllItems")
        isRunning = nbt.getBoolean("isRunning")

        storedItems = nbt.getList("storedItems", NbtList.COMPOUND_TYPE.toInt()).map {
            ItemStack.fromNbt(it as NbtCompound)
        }.toTypedArray()
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.putInt("tickCooldown", tickCooldown)
        val ritualId = ritual?.id?.toString()
        if (ritualId != null) nbt.putString("ritual", ritualId)
        nbt.putBoolean("hadAllItems", hadAllItems)
        nbt.putBoolean("isRunning", isRunning)

        val items = NbtList()
        for (stack in storedItems) {
            val entry = NbtCompound()
            items.add(stack.writeNbt(entry))
            items.add(entry)
        }
        nbt.put("storedItems", items)
    }

    fun onUse(player: PlayerEntity) {
        if (ritual == null) {
            startRitual()
        } else {
            stopRitual()
        }
    }

    fun tick() {
        tickCooldown -= 1
        if (getWorld() is ServerWorld && tickCooldown < 0) {
            tickCooldown = COOLDOWN

            if (ritual != null) {
                // Check validity of ritual circle
                if (!ritual!!.isValid(getWorld()!!, getPos())) {
                    stopRitual()
                    return
                }

                // If ritual hasn't activated yet, try absorbing new items
                if (!isRunning) {
                    val missingItems = getMissingItems()
                    if (missingItems.isNotEmpty()) {
                        if (!tryAbsorbMissing(missingItems)) {
                            stopRitual()
                        }
                        return
                    }
                }

                // Deactivate the ritual after activation if activation fails, or ritual has no lasting effects
                if (!hadAllItems) {
                    hadAllItems = true
                    if (!activateRitual() || !ritual!!.hasTickingEffects) {
                        stopRitual()
                    }
                    return
                }

                // Tick rituals with lasting effects
                if (!ritual!!.tick(getWorld()!!, getPos())) {
                    stopRitual()
                    return
                }
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
                setPower(15)
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
        val success = ritual?.activate(getWorld()!!, getPos()) ?: false
        if (success) {
            storedItems = arrayOf()
        }
        return success
    }

    /**
     * Safely stops any running ritual
     */
    private fun stopRitual() {
        hadAllItems = false
        ritual = null
        setPower(0)

        val pos = Vec3d.ofBottomCenter(getPos())
        storedItems.forEach {
            getWorld()?.spawnEntity(ItemEntity(world, pos.x, pos.y, pos.z, it))
        }
        storedItems = arrayOf()
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