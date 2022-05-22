package nl.enjarai.rites.type.ritual_effect

import com.mojang.serialization.Lifecycle
import com.mthaler.aparser.arithmetic.Expression
import com.mthaler.aparser.arithmetic.tryEval
import com.mthaler.aparser.util.Try
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.util.registry.SimpleRegistry
import nl.enjarai.rites.RitesMod
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.flow.ExtractItemNbtEffect
import nl.enjarai.rites.type.ritual_effect.flow.FalseEffect
import nl.enjarai.rites.type.ritual_effect.flow.TickingEffect
import nl.enjarai.rites.type.ritual_effect.visual.PlaySoundEffect
import nl.enjarai.rites.type.ritual_effect.visual.SpawnMovingParticlesEffect
import nl.enjarai.rites.type.ritual_effect.visual.SpawnParticlesEffect
import nl.enjarai.rites.type.ritual_effect.waystone.BindWaystoneEffect
import nl.enjarai.rites.type.ritual_effect.waystone.UseWaystoneEffect
import java.util.*

abstract class RitualEffect(values: Map<String, Any>) {
    val uuid: UUID = UUID.randomUUID()

    abstract fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean

    open fun isTicking(): Boolean {
        return getTickCooldown() != 0
    }

    open fun getTickCooldown(): Int {
        return 0
    }

    open fun shouldKeepRitualRunning(): Boolean {
        return isTicking()
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
            Registry.register(REGISTRY, RitesMod.id("bind_waystone")) { BindWaystoneEffect(it) }
            Registry.register(REGISTRY, RitesMod.id("use_waystone")) { UseWaystoneEffect(it) }
            Registry.register(REGISTRY, RitesMod.id("play_sound")) { PlaySoundEffect(it) }
            Registry.register(REGISTRY, RitesMod.id("spawn_particles")) { SpawnParticlesEffect(it) }
            Registry.register(REGISTRY, RitesMod.id("spawn_moving_particles")) { SpawnMovingParticlesEffect(it) }
            Registry.register(REGISTRY, RitesMod.id("drop_item")) { DropItemEffect(it) }
            Registry.register(REGISTRY, RitesMod.id("summon_entity")) { SummonEntityEffect(it) }
            Registry.register(REGISTRY, RitesMod.id("give_potion")) { GivePotionEffect(it) }
            Registry.register(REGISTRY, RitesMod.id("extract_nbt")) { ExtractItemNbtEffect(it) }
        }

        fun fromMap(values: Map<String, Any>): RitualEffect {
            return REGISTRY.get(getIdNullSafe(getValue<String>(values, "type")(null)))?.invoke(values)
                ?: throw IllegalArgumentException("Invalid effect type: ${values["type"]}")
        }

        fun getIdNullSafe(string: String?): Identifier? {
            return if (string == null) null else Identifier.tryParse(string)
        }

        inline fun <reified T> getValue(values: Map<String, Any>, key: String, default: T? = null): (RitualContext?) -> T {
            // If the value as T is a String, thus making T a String, parse only the variables
            val asT = values[key] as? T
            if (asT != null && asT is String) return {
                it?.parseVariables(asT) as? T ?: asT
            }

            // If T is not a String, but the value can be cast to a String,
            // parse variables, then math expressions, and cast to T
            val parsable = values[key] as? String
            if (parsable != null) return {
                (Expression(it?.parseVariables(parsable) ?: parsable).tryEval() as Try.Success).value as T
            }
            val value = asT ?: default ?: throw IllegalArgumentException("Invalid $key/no $key given")
            return { value }
        }
    }
}