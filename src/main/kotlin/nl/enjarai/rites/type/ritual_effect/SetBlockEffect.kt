package nl.enjarai.rites.type.ritual_effect

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.resource.serialization.Codecs
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantVec3
import nl.enjarai.rites.type.interpreted_value.InterpretedVec3

class SetBlockEffect(
    val block: BlockState,
    val posOffset: InterpretedVec3,
    val dropItems: Boolean,
    val showParticles: Boolean
) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<SetBlockEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codecs.BLOCK_STATE.fieldOf("block").forGetter { it.block },
                InterpretedVec3.CODEC.optionalFieldOf("pos_offset", ConstantVec3(.0, .0, .0))
                    .forGetter { it.posOffset },
                Codec.BOOL.optionalFieldOf("drop_items", false).forGetter { it.dropItems },
                Codec.BOOL.optionalFieldOf("show_particles", true).forGetter { it.showParticles }
            ).apply(instance, ::SetBlockEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val world = ctx.world
        val blockPos = pos.add(posOffset.interpretAsBlockPos(ctx))

        if (showParticles) world.breakBlock(blockPos, dropItems)
        world.setBlockState(blockPos, block)

        return true
    }
}