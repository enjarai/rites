package nl.enjarai.rites.type.ritual_effect

import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.resource.CircleTypes
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber

class MatchBlockEffect : RitualEffect() {
    @FromJson
    private lateinit var block: CircleTypes.BlockStatePredicate
    @FromJson
    private val pos_offset: List<InterpretedNumber> = listOf(ConstantNumber(.0), ConstantNumber(.0), ConstantNumber(.0))

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val offset = pos_offset.map { it.interpretAsInt(ctx) }
        val blockPos = BlockPos(ctx.pos.x + offset[0], ctx.pos.y + offset[1], ctx.pos.z + offset[2])
        return block.test(ctx.world.getBlockState(blockPos))
    }
}