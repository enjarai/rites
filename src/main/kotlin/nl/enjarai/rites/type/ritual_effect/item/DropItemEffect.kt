package nl.enjarai.rites.type.ritual_effect.item

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.ItemEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.ConstantString
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedString
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class DropItemEffect(
    val item: Item,
    val count: InterpretedNumber,
    val nbt: InterpretedString
) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<DropItemEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                Registries.ITEM.codec.fieldOf("item").forGetter { it.item },
                InterpretedNumber.CODEC.optionalFieldOf("count", ConstantNumber(1)).forGetter { it.count },
                InterpretedString.CODEC.optionalFieldOf("nbt", ConstantString("{}")).forGetter { it.nbt }
            ).apply(instance, ::DropItemEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val spawnPos = Vec3d.ofBottomCenter(pos)
        val itemStack = if (item != Items.AIR) item.defaultStack else return false

        itemStack.count = count.interpretAsInt(ctx)
        itemStack.orCreateNbt.copyFrom(nbt.interpretAsNbt(ctx) ?: return false)
        return ctx.world.spawnEntity(ItemEntity(ctx.world, spawnPos.x, spawnPos.y, spawnPos.z, itemStack))
    }
}