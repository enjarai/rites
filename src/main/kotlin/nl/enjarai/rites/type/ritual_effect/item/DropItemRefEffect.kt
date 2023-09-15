package nl.enjarai.rites.type.ritual_effect.item

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.ItemEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.InterpretedVec3
import nl.enjarai.rites.type.ritual_effect.RitualEffect
import java.util.*

class DropItemRefEffect(val ref: String, pos: Optional<InterpretedVec3>) : RitualEffect(CODEC) {
    val pos: InterpretedVec3? = pos.orElse(null)
    companion object {
        val CODEC: Codec<DropItemRefEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("ref").forGetter { it.ref },
                InterpretedVec3.CODEC.optionalFieldOf("pos").forGetter { Optional.ofNullable(it.pos) }
            ).apply(instance, ::DropItemRefEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val item = ctx.addressableItems[ref] ?: return false
        ctx.addressableItems.remove(ref)
        val spawnPos = this.pos?.interpret(ctx) ?: Vec3d.ofBottomCenter(ctx.realPos)
        return ctx.world.spawnEntity(ItemEntity(ctx.world, spawnPos.x, spawnPos.y, spawnPos.z, item))
    }
}