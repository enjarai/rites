package nl.enjarai.rites.type.ritual_effect.special.focus

import com.mojang.serialization.Codec
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class InternalizeFocusEffect(val focusRef: String) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<InternalizeFocusEffect> = Codec.STRING
            .xmap(::InternalizeFocusEffect, InternalizeFocusEffect::focusRef).fieldOf("focus_ref").codec()
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val focus = ctx.addressableItems[focusRef]?.copy() ?: return false
        val nbt = focus.orCreateNbt
        if (nbt.contains("riteData")) return false

        val ritualNbt = nbt.getCompound("riteData")

        ritualNbt.put("rituals", NbtList().apply {
            ctx.rituals.slice(0 until ctx.rituals.size - 1).forEach {
                add(NbtString.of(it.ritual.id.toString()))
            }
        })
        ritualNbt.put("storedItems", NbtList().apply {
            ctx.storedItems.forEach {
                add(it.writeNbt(NbtCompound()))
            }
        })

        nbt.put("riteData", ritualNbt)
        ctx.returnableItems += focus

        return true
    }
}