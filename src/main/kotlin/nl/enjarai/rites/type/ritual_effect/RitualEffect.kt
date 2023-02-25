package nl.enjarai.rites.type.ritual_effect

import com.mojang.serialization.Codec
import com.mojang.serialization.Lifecycle
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.SimpleRegistry
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.RitesMod
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.entity.SummonEntityEffect
import nl.enjarai.rites.type.ritual_effect.flow.*
import nl.enjarai.rites.type.ritual_effect.flow.logic.*
import nl.enjarai.rites.type.ritual_effect.flow.loop.ForAreaEffect
import nl.enjarai.rites.type.ritual_effect.flow.loop.ForIEffect
import nl.enjarai.rites.type.ritual_effect.item.DropItemEffect
import nl.enjarai.rites.type.ritual_effect.item.DropItemRefEffect
import nl.enjarai.rites.type.ritual_effect.item.MergeItemNbtEffect
import nl.enjarai.rites.type.ritual_effect.special.focus.InternalizeFocusEffect
import nl.enjarai.rites.type.ritual_effect.special.waystone.BindWaystoneEffect
import nl.enjarai.rites.type.ritual_effect.special.waystone.UseWaystoneEffect
import nl.enjarai.rites.type.ritual_effect.visual.PlaySoundEffect
import nl.enjarai.rites.type.ritual_effect.visual.SpawnMovingParticlesEffect
import nl.enjarai.rites.type.ritual_effect.visual.SpawnParticlesEffect
import java.util.*

abstract class RitualEffect(val codec: Codec<out RitualEffect>) {
    val uuid: UUID = UUID.randomUUID()

    val id: Identifier? get() {
        return REGISTRY.getId(codec)
    }

    abstract fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean

    open fun isTicking(): Boolean {
        return false
    }

    open fun getTickCooldown(ctx: RitualContext): Int {
        return 0
    }

    open fun shouldKeepRitualRunning(): Boolean {
        return isTicking()
    }

    @Target(AnnotationTarget.FIELD)
    annotation class FromJson

    companion object {
        val REGISTRY = SimpleRegistry<Codec<out RitualEffect>>(
            RegistryKey.ofRegistry(RitesMod.id("ritual_effects")),
            Lifecycle.stable()
        )
        val CODEC: Codec<RitualEffect> = REGISTRY.codec.dispatch(
            "type",
            { it.codec },
            { it }
        )

        fun registerAll() {
            // control flow effects
            Registry.register(REGISTRY, RitesMod.id("tick"), TickingEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("for_i"), ForIEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("for_area"), ForAreaEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("if"), IfEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("true"), TrueEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("false"), FalseEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("and"), AndEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("or"), OrEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("not"), NotEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("variable"), VariableEffect.CODEC)

            // effects that actually do stuff
            Registry.register(REGISTRY, RitesMod.id("bind_waystone"), BindWaystoneEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("use_waystone"), UseWaystoneEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("internalize_focus"), InternalizeFocusEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("play_sound"), PlaySoundEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("spawn_particles"), SpawnParticlesEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("spawn_moving_particles"), SpawnMovingParticlesEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("drop_item"), DropItemEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("drop_item_ref"), DropItemRefEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("merge_item_nbt"), MergeItemNbtEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("summon_entity"), SummonEntityEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("give_potion"), GivePotionEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("match_block"), MatchBlockEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("set_block"), SetBlockEffect.CODEC)
            Registry.register(REGISTRY, RitesMod.id("run_function"), RunFunctionEffect.CODEC)
        }
    }
}