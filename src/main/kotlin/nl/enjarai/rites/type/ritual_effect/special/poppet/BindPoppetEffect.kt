package nl.enjarai.rites.type.ritual_effect.special.poppet

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.item.ModItems
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.InterpretedString
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class BindPoppetEffect(
    val ref: InterpretedString,
    val targetEntity: InterpretedString,
) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<BindPoppetEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                InterpretedString.CODEC.fieldOf("ref").forGetter { it.ref },
                InterpretedString.CODEC.fieldOf("target_entity").forGetter { it.targetEntity },
            ).apply(instance, ::BindPoppetEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val poppetRef = ref.interpret(ctx)
        val poppetStack = ctx.addressableItems[poppetRef]
        if (poppetStack?.isOf(ModItems.POPPET) != true) return false

        val entity = selectEntity(ctx, targetEntity.interpret(ctx)) ?: return false
        if (entity !is ServerPlayerEntity) return false

        val poppet = poppetStack.copyWithCount(1)
        val nbt = poppet.orCreateNbt
        nbt.put("Owner", NbtHelper.writeGameProfile(NbtCompound(), entity.gameProfile))

        for (i in 0 until poppetStack.count) {
            ctx.returnableItems += poppet.copy()
        }
        ctx.addressableItems.remove(poppetRef)

        return true
    }
}