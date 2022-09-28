package nl.enjarai.rites.type.ritual_effect

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber

class SetBlockEffect : RitualEffect() {
    @FromJson
    private lateinit var block: BlockState
    @FromJson
    private val pos_offset: List<InterpretedNumber> = listOf(ConstantNumber(.0), ConstantNumber(.0), ConstantNumber(.0))
    @FromJson
    private val drop_items: Boolean = false

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val world = ctx.world
        val blockPos = pos.add(pos_offset[0].interpretAsInt(ctx), pos_offset[1].interpretAsInt(ctx), pos_offset[2].interpretAsInt(ctx))

        world.breakBlock(blockPos, drop_items)
        world.setBlockState(blockPos, block)

        return true
    }
}