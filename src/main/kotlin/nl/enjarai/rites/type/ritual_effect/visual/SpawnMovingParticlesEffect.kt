package nl.enjarai.rites.type.ritual_effect.visual

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleType
import net.minecraft.registry.Registries
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.ConstantVec3
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedVec3
import nl.enjarai.rites.type.ritual_effect.RitualEffect
import nl.enjarai.rites.util.Visuals

class SpawnMovingParticlesEffect(
    val particle: ParticleType<*>,
    val posOffset: InterpretedVec3,
    val delta: InterpretedVec3,
    val directionVector: InterpretedVec3,
    val count: InterpretedNumber
) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<SpawnMovingParticlesEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                Registries.PARTICLE_TYPE.codec.fieldOf("particle").forGetter { it.particle },
                InterpretedVec3.CODEC.optionalFieldOf("pos_offset", ConstantVec3(.0, .0, .0))
                    .forGetter { it.posOffset },
                InterpretedVec3.CODEC.optionalFieldOf("delta", ConstantVec3(.0, .0, .0))
                    .forGetter { it.delta },
                InterpretedVec3.CODEC.optionalFieldOf("direction_vector", ConstantVec3(.0, .0, .0))
                    .forGetter { it.directionVector },
                InterpretedNumber.CODEC.optionalFieldOf("count", ConstantNumber(1)).forGetter { it.count }
            ).apply(instance, ::SpawnMovingParticlesEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val dPos = Vec3d.ofBottomCenter(pos).add(posOffset.interpret(ctx))

        Visuals.movingParticleCloud(
            ctx.world as ServerWorld,
            (particle as? ParticleEffect) ?: return false,
            dPos,
            delta.interpret(ctx),
            directionVector.interpret(ctx),
            count.interpretAsInt(ctx)
        )

        return true
    }
}