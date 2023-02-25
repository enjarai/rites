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

class SpawnParticlesEffect(
    val particle: ParticleType<*>,
    val posOffset: InterpretedVec3,
    val delta: InterpretedVec3,
    val count: InterpretedNumber,
    val speed: InterpretedNumber
) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<SpawnParticlesEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                Registries.PARTICLE_TYPE.codec.fieldOf("particle").forGetter { it.particle },
                InterpretedVec3.CODEC.optionalFieldOf("pos_offset", ConstantVec3(.0, .0, .0))
                    .forGetter { it.posOffset },
                InterpretedVec3.CODEC.optionalFieldOf("delta", ConstantVec3(.0, .0, .0))
                    .forGetter { it.delta },
                InterpretedNumber.CODEC.optionalFieldOf("count", ConstantNumber(1)).forGetter { it.count },
                InterpretedNumber.CODEC.optionalFieldOf("speed", ConstantNumber(.0)).forGetter { it.speed }
            ).apply(instance, ::SpawnParticlesEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val dPos = Vec3d.ofBottomCenter(pos).add(posOffset.interpret(ctx))
        val delta = delta.interpret(ctx)

        (ctx.world as ServerWorld).spawnParticles(
            (particle as? ParticleEffect) ?: return false,
            dPos.getX(), dPos.getY(), dPos.getZ(),
            count.interpretAsInt(ctx),
            delta.getX(), delta.getY(), delta.getZ(),
            speed.interpret(ctx)
        )

        return true
    }
}