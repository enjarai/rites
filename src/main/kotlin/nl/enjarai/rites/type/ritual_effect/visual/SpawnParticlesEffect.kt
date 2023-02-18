package nl.enjarai.rites.type.ritual_effect.visual

import net.minecraft.particle.ParticleEffect
import net.minecraft.registry.Registries
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class SpawnParticlesEffect : RitualEffect() {
    @FromJson
    private lateinit var particle: Identifier
    @FromJson
    private val pos_offset: List<InterpretedNumber> = listOf(ConstantNumber(.0), ConstantNumber(.0), ConstantNumber(.0))
    @FromJson
    private val delta: List<InterpretedNumber> = listOf(ConstantNumber(.0), ConstantNumber(.0), ConstantNumber(.0))
    @FromJson
    private val count: InterpretedNumber = ConstantNumber(1)
    @FromJson
    private val speed: InterpretedNumber = ConstantNumber(.0)

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        if (pos_offset.size != 3) return false
        if (delta.size != 3) return false
        val dPos = Vec3d.ofBottomCenter(pos)

        (ctx.world as ServerWorld).spawnParticles(
            (Registries.PARTICLE_TYPE.get(particle) as? ParticleEffect) ?: return false,
            dPos.getX() + pos_offset[0].interpret(ctx),
            dPos.getY() + pos_offset[1].interpret(ctx),
            dPos.getZ() + pos_offset[2].interpret(ctx),
            count.interpretAsInt(ctx),
            delta[0].interpret(ctx), delta[1].interpret(ctx), delta[2].interpret(ctx),
            speed.interpret(ctx)
        )

        return true
    }
}