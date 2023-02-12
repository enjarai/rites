package nl.enjarai.rites.type.ritual_effect

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.mojang.serialization.Lifecycle
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.util.registry.SimpleRegistry
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
import nl.enjarai.rites.type.ritual_effect.visual.PlaySoundEffect
import nl.enjarai.rites.type.ritual_effect.visual.SpawnMovingParticlesEffect
import nl.enjarai.rites.type.ritual_effect.visual.SpawnParticlesEffect
import nl.enjarai.rites.type.ritual_effect.waystone.BindWaystoneEffect
import nl.enjarai.rites.type.ritual_effect.waystone.UseWaystoneEffect
import java.lang.reflect.ParameterizedType
import java.util.*

abstract class RitualEffect {
    val uuid: UUID = UUID.randomUUID()

    val id: Identifier? get() {
        return REGISTRY.getId(this.javaClass)
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
        val REGISTRY = SimpleRegistry<Class<out RitualEffect>>(
            RegistryKey.ofRegistry(RitesMod.id("ritual_effects")),
            Lifecycle.stable(),
            null
        )

        fun registerAll() {
            // control flow effects
            Registry.register(REGISTRY, RitesMod.id("tick"), TickingEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("for_i"), ForIEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("for_area"), ForAreaEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("if"), IfEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("true"), TrueEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("false"), FalseEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("and"), AndEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("or"), OrEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("not"), NotEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("variable"), VariableEffect::class.java)

            // effects that actually do stuff
            Registry.register(REGISTRY, RitesMod.id("bind_waystone"), BindWaystoneEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("use_waystone"), UseWaystoneEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("play_sound"), PlaySoundEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("spawn_particles"), SpawnParticlesEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("spawn_moving_particles"), SpawnMovingParticlesEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("drop_item"), DropItemEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("drop_item_ref"), DropItemRefEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("merge_item_nbt"), MergeItemNbtEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("summon_entity"), SummonEntityEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("give_potion"), GivePotionEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("match_block"), MatchBlockEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("set_block"), SetBlockEffect::class.java)
            Registry.register(REGISTRY, RitesMod.id("run_function"), RunFunctionEffect::class.java)
        }

        fun deserialize(id: Identifier, json: JsonObject, context: JsonDeserializationContext): RitualEffect {
            val clazz = REGISTRY.get(id) ?: throw JsonParseException("Unknown ritual effect $id")
            val instance = clazz.getConstructor().newInstance()

            // fill in fields from the json object
            for (field in clazz.declaredFields) {
                if (field.isAnnotationPresent(FromJson::class.java)) {
                    val annotation = field.getAnnotation(FromJson::class.java)
                    val name = field.name

                    field.isAccessible = true
                    try {
                        val jsonValue = json.get(name)

                        // manual array handling to make sure everything gets deserialized correctly
                        if (jsonValue?.isJsonArray == true) {
                            val jsonArray = jsonValue.asJsonArray
                            val arrayType = field.genericType as ParameterizedType
                            val array = jsonArray.map { context.deserialize<Any>(it, arrayType.actualTypeArguments[0]) }
                            field.set(instance, array)
                            continue
                        }

                        context.deserialize<Any>(jsonValue, field.type)?.let { field.set(instance, it) }
                    } catch (e: Exception) {
                        throw JsonParseException("Error deserializing field $name in $clazz", e)
                    }

                    if (field.get(instance) == null) {
                        throw JsonParseException("Missing required property $name for ritual effect $id")
                    }
                }
            }

            return instance
        }
    }
}