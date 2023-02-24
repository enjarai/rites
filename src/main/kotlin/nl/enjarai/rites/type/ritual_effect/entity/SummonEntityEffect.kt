package nl.enjarai.rites.type.ritual_effect.entity

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.*
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class SummonEntityEffect(
    val entity: Identifier,
    val nbt: InterpretedString,
    val posOffset: InterpretedPosition
) : RitualEffect() {
    companion object {
        val CODEC: Codec<SummonEntityEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                Identifier.CODEC.fieldOf("entity").forGetter { it.entity },
                InterpretedString.CODEC.optionalFieldOf("nbt", ConstantString("{}")).forGetter { it.nbt },
                InterpretedPosition.CODEC.optionalFieldOf("pos_offset", ConstantPosition(Vec3d.ZERO)).forGetter { it.posOffset }
            ).apply(instance, ::SummonEntityEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val entityPos = Vec3d.ofBottomCenter(pos)
            .add(.0, .01, .0)
            .add(posOffset.interpret(ctx))
        val entityNbt = nbt.interpretAsNbt(ctx) ?: return false
        entityNbt.putString("id", entity.toString())

        val entityObj = EntityType.loadEntityWithPassengers(
            entityNbt, ctx.world
        ) { entity: Entity ->
            entity.refreshPositionAndAngles(
                entityPos.getX(), entityPos.getY(), entityPos.getZ(),
                entity.yaw, entity.pitch
            )
            entity
        } ?: return false
        entityObj.setPos(entityPos.getX(), entityPos.getY(), entityPos.getZ())

        return (ctx.world as ServerWorld).spawnNewEntityAndPassengers(entityObj)
    }

    override fun getCodec(): Codec<out RitualEffect> {
        return CODEC
    }
}