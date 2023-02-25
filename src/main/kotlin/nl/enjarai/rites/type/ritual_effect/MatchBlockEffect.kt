package nl.enjarai.rites.type.ritual_effect

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantVec3
import nl.enjarai.rites.type.interpreted_value.InterpretedVec3
import nl.enjarai.rites.type.predicate.BlockStatePredicate

class MatchBlockEffect(
    val block: BlockStatePredicate,
    val posOffset: InterpretedVec3
) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<MatchBlockEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                BlockStatePredicate.CODEC.fieldOf("block").forGetter { it.block },
                InterpretedVec3.CODEC.optionalFieldOf("pos_offset", ConstantVec3(.0, .0, .0))
                    .forGetter { it.posOffset }
            ).apply(instance, ::MatchBlockEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val blockPos = pos.add(posOffset.interpretAsBlockPos(ctx))
        return block.test(ctx.world.getBlockState(blockPos))
    }
}