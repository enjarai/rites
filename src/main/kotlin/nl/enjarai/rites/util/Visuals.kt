package nl.enjarai.rites.util

import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleType
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.resource.CircleTypes
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
        particleSettings: CircleTypes.ParticleSettings
    ) {
        val directionModifier = if (particleSettings.reverse_rotation) -1 else 1
        val currentTick = world.server.ticks
        val animationPos = ((currentTick % cycleTicks / cycleTicks.toDouble()) + cycleOffset) *
                (2 * Math.PI) * directionModifier
        val animationPosTo = ((currentTick % cycleTicks / cycleTicks.toDouble()) + cycleOffset + particleSettings.arm_angle) *
                (2 * Math.PI) * directionModifier
        val particlePos = pos.add(radius * cos(animationPos), 0.0, radius * sin(animationPos))
        val particlePosTo = pos.add(radius / 2 * cos(animationPosTo), 0.0, radius / 2 * sin(animationPosTo))
        val movementVector = particlePosTo.subtract(particlePos).multiply(particleSettings.arm_speed)
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

    fun outwardsCircle(world: ServerWorld, pos: Vec3d, radius: Double) {
        var i = .0
        while (i <= 1) {
            i += 0.02
            val angle = i * (2 * Math.PI)
            val particlePosTo = pos.add(radius / 2 * cos(angle), 0.0, radius / 2 * sin(angle))
            val movementVector = particlePosTo.subtract(pos).multiply(0.4)
            world.spawnParticles(
                ParticleTypes.FIREWORK,
                pos.getX(), pos.getY(), pos.getZ(), 0,
                movementVector.getX(), movementVector.getY(), movementVector.getZ(), 1.0
            )
        }
    }

    fun inwardsCircle(world: ServerWorld, pos: Vec3d, radius: Double) {
        var i = .0
        while (i <= 1) {
            i += 0.02
            val angle = i * (2 * Math.PI)
            val particlePosTo = pos.add(radius / 2 * cos(angle), 0.0, radius / 2 * sin(angle))
            val movementVector = pos.subtract(particlePosTo).multiply(0.4)
            world.spawnParticles(
                ParticleTypes.FIREWORK,
                particlePosTo.getX(), particlePosTo.getY(), particlePosTo.getZ(), 0,
                movementVector.getX(), movementVector.getY(), movementVector.getZ(), 1.0
            )
        }
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

    fun movingParticleCloud(
        world: ServerWorld,
        particle: ParticleEffect,
        pos: Vec3d,
        delta: Vec3d,
        movementVector: Vec3d,
        count: Int
    ) {
        val random = world.getRandom()

        for (i in 0 until count) {
            val g: Double = random.nextGaussian() * delta.getX()
            val h: Double = random.nextGaussian() * delta.getY()
            val j: Double = random.nextGaussian() * delta.getZ()

            world.spawnParticles(
                particle,
                pos.getX() + g,
                pos.getY() + h,
                pos.getZ() + j,
                0,
                movementVector.getX(),
                movementVector.getY(),
                movementVector.getZ(),
                1.0
            )
            continue
        }
    }

    fun absorb(world: ServerWorld, pos: Vec3d) {
        inwardsCircle(world, pos.add(0.0, 0.1, 0.0), 0.5)
        world.playSound(
            null, pos.getX(), pos.getY(), pos.getZ(),
            SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS,
            0.4f, 0.0f
        )
    }

    fun activate(world: ServerWorld, pos: BlockPos) {
        val sPos = Vec3d.ofBottomCenter(pos)
        world.playSound(
            null, sPos.getX(), sPos.getY(), sPos.getZ(),
            SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS,
            0.4f, 0.8f
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

    fun runningFocus(world: ServerWorld, pos: BlockPos) {
        val sPos = Vec3d.ofBottomCenter(pos)
        world.spawnParticles(
            ParticleTypes.SCRAPE,
            sPos.getX(), sPos.getY(), sPos.getZ(),
            2, 0.2, 0.2, 0.2, 0.2
        )
    }
}