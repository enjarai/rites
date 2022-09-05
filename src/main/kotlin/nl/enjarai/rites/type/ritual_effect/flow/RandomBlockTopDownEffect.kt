package nl.enjarai.rites.type.ritual_effect.flow

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class RandomBlockTopDownEffect : RitualEffect() {
    @FromJson
    private val range: InterpretedNumber = ConstantNumber(0)
    @FromJson
    private val vertical_range: InterpretedNumber = range
    @FromJson
    private val pos_offset: List<InterpretedNumber> = listOf(ConstantNumber(0), ConstantNumber(0), ConstantNumber(0))
    @FromJson
    private lateinit var effects: List<RitualEffect>

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        // TODO if range 0, range = ctx.range
        val range = range.interpretAsInt(ctx)
        val verticalRange = vertical_range.interpretAsInt(ctx)

        val center = pos.add(pos_offset[0].interpretAsInt(ctx), pos_offset[1].interpretAsInt(ctx), pos_offset[2].interpretAsInt(ctx))
        val box = Box(
            center.add(-range, -verticalRange, -range),
            center.add(range, verticalRange, range)
        )

        return true
    }
}