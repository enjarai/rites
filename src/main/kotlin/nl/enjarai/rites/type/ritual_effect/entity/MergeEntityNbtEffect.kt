package nl.enjarai.rites.type.ritual_effect.entity

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.command.EntityDataObject
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.InterpretedString
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class MergeEntityNbtEffect(val selector: InterpretedString, val nbt: InterpretedString) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<MergeEntityNbtEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                InterpretedString.CODEC.fieldOf("selector").forGetter { it.selector },
                InterpretedString.CODEC.fieldOf("nbt").forGetter { it.nbt }
            ).apply(instance, ::MergeEntityNbtEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        selectEntities(ctx, selector.interpret(ctx))?.forEach {
            val dataObject = EntityDataObject(it)
            val nbt = dataObject.nbt
            nbt.copyFrom(this.nbt.interpretAsNbt(ctx))
            dataObject.nbt = nbt
        } ?: return false
        return true
    }
}