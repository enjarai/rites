package nl.enjarai.rites.type.ritual_effect.flow

import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class ExtractItemNbtEffect : RitualEffect() {
    @FromJson
    private val source_ingredient: InterpretedNumber = ConstantNumber(1)
    @FromJson
    private lateinit var nbt_path: String
    @FromJson
    private lateinit var target_variable: String

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        TODO("Not yet implemented")
    }
}