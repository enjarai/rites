package nl.enjarai.rites.block

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.resource.Rituals
import nl.enjarai.rites.type.CircleType
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.RitualResult
import nl.enjarai.rites.util.Visuals

abstract class RiteRunningBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : BlockEntity(type, pos, state) {
    private var tickCooldown = 0
    var storedItems = arrayOf<ItemStack>()
    var ritualContext: RitualContext? = null

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

    open fun tick() {
        if (getWorld() is ServerWorld && ritualContext != null) {
            ritualContext!!.drawParticleEffects()
            Visuals.hum(
                getWorld()!! as ServerWorld, getPos(),
                if (ritualContext!!.hasActivating()) 0.8f else 0.2f
            )

            tickCooldown -= 1
            if (tickCooldown < 0) {
                tickCooldown = RiteCenterBlockEntity.COOLDOWN

                // Make sure we get saved properly
                markDirty()

                // Check validity of ritual circle
                if (!ritualContext!!.canMaintain()) {
                    endAllRituals(false)
                    return
                }

                // If ritual hasn't activated yet, try absorbing new items
                if (ritualContext!!.hasActivating()) {
                    ritualActivatingTick()
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

    abstract fun ritualActivatingTick()

    protected fun startRitual(ritual: Ritual, circles: List<CircleType> = listOf()) {
        initContext()
        ritualContext?.appendRitual(ritual)
        ritualContext?.appendCircles(circles)
        tickCooldown = RiteCenterBlockEntity.COOLDOWN
    }

    open fun endAllRituals(success: Boolean) {
        // Stop rituals and store results
        storedItems = ritualContext?.stopAllRituals(success) ?: arrayOf()

        // Reset all state
        ritualContext = null

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
}