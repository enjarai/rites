package nl.enjarai.rites.type

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.particle.ParticleType
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.Registries
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import nl.enjarai.rites.resource.CircleTypes
import nl.enjarai.rites.type.predicate.BlockStatePredicate
import nl.enjarai.rites.util.Visuals

class CircleType(
    private val rawLayout: List<List<String>>,
    private val keys: Map<String, LayoutKey>,
    val particle: ParticleType<*>,
    val particleSettings: CircleTypes.ParticleSettings
) {
    val layout: List<List<LayoutKey?>> = rawLayout.map { row ->
        row.map mapRow@{ block ->
            if (block.isBlank()) return@mapRow null
            keys[block] ?: throw IllegalArgumentException("Unknown block: $block")
        }
    }
    var alternatives = listOf<CircleType>()
    val size get() = layout.size / 2
    val dependentBlocks: List<BlockPos> = layout.flatMapIndexed { y, row ->
        row.mapIndexedNotNull { x, block ->
            if (block == null) return@mapIndexedNotNull null
            BlockPos(x - size, 0, y - size)
        }
    }

    companion object {
        val CODEC: Codec<CircleType> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.listOf().listOf().fieldOf("layout").forGetter { it.rawLayout },
                Codec.unboundedMap(Codec.STRING, LayoutKey.CODEC).fieldOf("keys").forGetter { it.keys },
                Registries.PARTICLE_TYPE.codec.optionalFieldOf("particle", ParticleTypes.SOUL_FIRE_FLAME)
                    .forGetter { it.particle },
                CircleTypes.ParticleSettings.CODEC
                    .optionalFieldOf("particle_settings", CircleTypes.ParticleSettings())
                    .forGetter { it.particleSettings }
            ).apply(instance, ::CircleType)
        }
    }

    val id: Identifier?
        get() {
            CircleTypes.values.entries.forEach {
                if (it.value === this) {
                    return it.key
                }
            }
            return null
        }

    fun finalize() {
        keys.values.forEach { it.predicate.finalize() }
    }

    fun isValid(world: World, pos: BlockPos, ctx: RitualContext?): CircleType? {
        if (isSelfValid(world, pos, ctx)) return this

        for (circle in alternatives) {
            val result = circle.isValid(world, pos, ctx)
            if (result != null) return result
        }

        return null
    }

    /**
     * Checks the validity of this circle only.
     * If the circle might be added, ctx should be set, **otherwise it should always be null**.
     */
    fun isSelfValid(world: World, pos: BlockPos, ctx: RitualContext?): Boolean {
        if (layout.isEmpty() || (ctx != null && !ctx.canAddCircle(size))) return false

        val offset = size
        val offsetPos = pos.add(-offset, 0, -offset)
        for ((x, row) in layout.withIndex()) {
            for ((z, key) in row.withIndex()) {
                if (key != null && !key.predicate.test(world.getBlockState(offsetPos.add(x, 0, z)))) {
                    return false
                }
            }
        }

        return true
    }

    fun drawParticleCircle(world: ServerWorld, pos: BlockPos) {
        val cycle = size * 30
        for (i in 1..particleSettings.cycles) {
            Visuals.drawParticleCircle(
                world,
                Vec3d.ofBottomCenter(pos).add(0.0, 0.2, 0.0),
                cycle,
                ((1.0 / particleSettings.cycles) * i),
                size.toDouble(),
                particle,
                particleSettings
            )
        }
    }

    class LayoutKey(val predicate: BlockStatePredicate, val color: Int) {
        companion object {
            @Suppress("RemoveExplicitTypeArguments")
            val CODEC: Codec<LayoutKey> = Codec.either(
                RecordCodecBuilder.create<LayoutKey> { instance ->
                    instance.group(
                        BlockStatePredicate.CODEC.fieldOf("predicate").forGetter { it.predicate },
                        Codec.STRING.xmap({ Integer.valueOf(it, 16) }, { Integer.toHexString(it) })
                            .fieldOf("color").forGetter { it.color }
                    ).apply(instance, ::LayoutKey)
                },
                BlockStatePredicate.CODEC.xmap({ LayoutKey(it, 0) }, { it.predicate })
            ).xmap(
                { it.left().orElseGet { it.right().get() } },
                { if (it.color == 0) Either.right(it) else Either.left(it) }
            )
        }
    }
}