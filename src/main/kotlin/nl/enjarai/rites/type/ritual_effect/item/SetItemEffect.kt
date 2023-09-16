package nl.enjarai.rites.type.ritual_effect.item

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.ConstantString
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedString
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class SetItemEffect(
    val ref: InterpretedString,
    val item: Item,
    val count: InterpretedNumber,
    val nbt: InterpretedString
) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<SetItemEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                InterpretedString.CODEC.fieldOf("ref").forGetter { it.ref },
                Registries.ITEM.codec.fieldOf("item").forGetter { it.item },
                InterpretedNumber.CODEC.optionalFieldOf("count", ConstantNumber(1)).forGetter { it.count },
                InterpretedString.CODEC.optionalFieldOf("nbt", ConstantString("{}")).forGetter { it.nbt }
            ).apply(instance, ::SetItemEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val ref = ref.interpret(ctx)
        val item = item.defaultStack
        item.count = count.interpretAsInt(ctx)
        val nbt = nbt.interpretAsNbt(ctx) ?: return false
        if (!nbt.isEmpty) {
            item.orCreateNbt.copyFrom(nbt)
        }
        ctx.addressableItems[ref] = item
        return true
    }
}