package nl.enjarai.rites.type.ritual_effect

import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.predicate.BlockStatePredicate

class MatchBlockEffect : RitualEffect() {
    @FromJson
    private lateinit var block: BlockStatePredicate
    @FromJson
    private val pos_offset: List<InterpretedNumber> = listOf(ConstantNumber(.0), ConstantNumber(.0), ConstantNumber(.0))

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val offset = pos_offset.map { it.interpretAsInt(ctx) }
        val blockPos = BlockPos(pos.x + offset[0], pos.y + offset[1], pos.z + offset[2])
        return block.test(ctx.world.getBlockState(blockPos))
    }
}