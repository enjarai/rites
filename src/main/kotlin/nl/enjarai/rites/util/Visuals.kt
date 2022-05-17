package nl.enjarai.rites.util

import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleType
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.cos
import kotlin.math.sin

object Visuals {
    private const val HUM_INTERVAL = 24

    private fun getSoundPitch(world: ServerWorld): Float {
        return 0.6f + world.getRandom().nextFloat() * 0.4f
    }

    fun drawParticleCircleArm(
        world: ServerWorld, pos: Vec3d, cycleTicks: Int,
        cycleOffset: Double, radius: Double, particle: ParticleType<*>,
        armAngle: Double, armSpeed: Double
    ) {
        val currentTick = world.server.ticks
        val animationPos = ((currentTick % cycleTicks / cycleTicks.toDouble()) + cycleOffset) * (2 * Math.PI)
        val animationPosTo = ((currentTick % cycleTicks / cycleTicks.toDouble()) + cycleOffset + armAngle) * (2 * Math.PI)
        val particlePos = pos.add(radius * cos(animationPos), 0.0, radius * sin(animationPos))
        val particlePosTo = pos.add(radius / 2 * cos(animationPosTo), 0.0, radius / 2 * sin(animationPosTo))
        val movementVector = particlePosTo.subtract(particlePos).multiply(armSpeed)
        world.spawnParticles(
            particle as ParticleEffect,
            particlePos.getX(), particlePos.getY(), particlePos.getZ(), 0,
            movementVector.getX(), movementVector.getY(), movementVector.getZ(), 1.0
        )
        world.spawnParticles(
            particle as ParticleEffect,
            particlePos.getX(), particlePos.getY(), particlePos.getZ(), 0,
            0.0, 0.0, 0.0, 1.0
        )
    }

    fun failParticles(world: ServerWorld, pos: Vec3d) {
        world.playSound(
            null, pos.getX(), pos.getY(), pos.getZ(),
            SoundEvents.BLOCK_NETHER_SPROUTS_BREAK, SoundCategory.BLOCKS,
            1f, getSoundPitch(world)
        )
        world.spawnParticles(
            ParticleTypes.SMOKE,
            pos.getX(), pos.getY(), pos.getZ(),
            20, 0.2, 0.0, 0.2, 0.02
        )
    }

    fun hum(world: ServerWorld, pos: BlockPos, volume: Float = 0.3f) {
        if (world.server.ticks % HUM_INTERVAL == 0) {
            val soundPos = Vec3d.ofBottomCenter(pos)
            world.playSound(
                null, soundPos.getX(), soundPos.getY(), soundPos.getZ(),
                SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.BLOCKS,
                volume, getSoundPitch(world) + 0.8f
            )
        }
    }
}