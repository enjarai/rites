package nl.enjarai.rites.block

import eu.pb4.polymer.api.utils.PolymerObject
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.resource.Rituals
import nl.enjarai.rites.type.RitualResult
import nl.enjarai.rites.util.Visuals

class RiteFocusBlockEntity(pos: BlockPos, state: BlockState) : RiteRunningBlockEntity(ModBlocks.RITE_FOCUS_ENTITY, pos, state), PolymerObject {
    var rituals = listOf<Identifier>()

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)

        rituals = nbt.getList("rituals", NbtCompound.STRING_TYPE.toInt()).map { Identifier(it.asString()) }
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)

        nbt.put("rituals", NbtList().apply {
            rituals.forEach { add(NbtString.of(it.toString())) }
        })
    }

    fun tryStartRituals() {
        rituals.forEach {
            Rituals.getById(it)?.let { it1 -> startRitual(it1) }
        }
        if (rituals.isNotEmpty()) RiteFocusBlock.setActive(world!!, pos, true)
    }

    override fun endAllRituals(success: Boolean) {
        super.endAllRituals(success)

        RiteFocusBlock.setActive(world!!, pos, false)
    }

    override fun ritualActivatingTick() {
        if (ritualContext!!.activateRitual(storedItems) != RitualResult.FAIL) {
            storedItems = arrayOf()
        } else {
            endAllRituals(false)
            return
        }

        // Deactivate right away if the ritual we just activated isn't ticking
        if (ritualContext!!.getSelectedRitual()?.ritual?.shouldKeepRunning != true) {
            endAllRituals(true)
        }
    }

    override fun tick() {
        val world = getWorld()
        if (world is ServerWorld) {
            if (cachedState.get(RiteFocusBlock.ACTIVE)) Visuals.runningFocus(world, pos)
        }
        super.tick()
    }
}