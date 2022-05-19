package nl.enjarai.rites.type.ritual_effect

import com.mojang.serialization.Lifecycle
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.util.registry.SimpleRegistry
import nl.enjarai.rites.RitesMod
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.util.RitualContext
import java.util.*

abstract class RitualEffect(values: Map<String, Any>) {
    val uuid: UUID = UUID.randomUUID()

    abstract fun activate(ritual: Ritual, ctx: RitualContext): Boolean

    open fun isTicking(): Boolean {
        return getTickCooldown() != 0
    }

    open fun getTickCooldown(): Int {
        return 0
    }

    companion object {
        val REGISTRY = SimpleRegistry<(Map<String, Any>) -> RitualEffect>(
            RegistryKey.ofRegistry(RitesMod.id("ritual_effects")),
            Lifecycle.experimental(),
            null
        )

        fun registerAll() {
            Registry.register(REGISTRY, RitesMod.id("ticking")) { TickingEffect(it) }
            Registry.register(REGISTRY, RitesMod.id("false")) { FalseEffect(it) }
            Registry.register(REGISTRY, RitesMod.id("play_sound")) { PlaySoundEffect(it) }
            Registry.register(REGISTRY, RitesMod.id("spawn_particles")) { SpawnParticlesEffect(it) }
            Registry.register(REGISTRY, RitesMod.id("spawn_moving_particles")) { SpawnMovingParticlesEffect(it) }
            Registry.register(REGISTRY, RitesMod.id("drop_item")) { DropItemEffect(it) }
            Registry.register(REGISTRY, RitesMod.id("summon_entity")) { SummonEntityEffect(it) }
            Registry.register(REGISTRY, RitesMod.id("give_potion")) { GivePotionEffect(it) }
            Registry.register(REGISTRY, RitesMod.id("extract_nbt")) { ExtractItemNbtEffect(it) }
        }

        fun fromMap(values: Map<String, Any>): RitualEffect {
            return REGISTRY.get(getIdNullSafe(getValue(values, "type")))?.invoke(values)
                ?: throw IllegalArgumentException("Invalid ritual type: ${values["type"]}")
        }

        fun getIdNullSafe(string: String?): Identifier? {
            return if (string == null) null else Identifier.tryParse(string)
        }

        inline fun <reified T> getValue(values: Map<String, Any>, key: String, default: T? = null): T {
            return values[key] as? T ?: default ?: throw IllegalArgumentException("Invalid $key/no $key given")
        }
    }
}