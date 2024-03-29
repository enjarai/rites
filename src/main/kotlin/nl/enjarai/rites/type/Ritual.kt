package nl.enjarai.rites.type

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.ItemEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import nl.enjarai.rites.RitesMod.LOGGER
import nl.enjarai.rites.resource.CircleTypes
import nl.enjarai.rites.type.predicate.Ingredient
import nl.enjarai.rites.type.ritual_effect.RitualEffect
import nl.enjarai.rites.util.Visuals

class Ritual(val circleTypes: List<CircleType>,
             val ingredients: List<Ingredient>,
             val effects: List<RitualEffect>) {
    @Transient private val unTickingEffects = effects.filter { ritualEffect -> !ritualEffect.isTicking() }
    @Transient private val tickingEffects = effects.filter { ritualEffect -> ritualEffect.isTicking() }
    @Transient val shouldKeepRunning = effects.any { ritualEffect -> ritualEffect.shouldKeepRitualRunning() }
    @Transient lateinit var id: Identifier

    companion object {
        val CODEC: Codec<Ritual> = RecordCodecBuilder.create { instance ->
            instance.group(
                CircleTypes.CODEC.listOf().fieldOf("circles").forGetter { it.circleTypes },
                Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter { it.ingredients },
                RitualEffect.CODEC.listOf().fieldOf("effects").forGetter { it.effects }
            ).apply(instance, ::Ritual)
        }

        fun requiredItemsNearby(requiredItems: Map<Ingredient, Int>, inCircle: List<ItemEntity>): Map<Ingredient, ItemEntity>? {
            val usedItems = mutableSetOf<ItemEntity>()
            val matchingMap = mutableMapOf<Ingredient, ItemEntity>()
            val ingredients = requiredItems.keys.toList()

            fun backtrack(index: Int): Boolean {
                if (index == ingredients.size) {
                    return true // All criteria matched successfully
                }

                val criterion = ingredients[index]
                for (item in inCircle) {
                    if (criterion.test(item.stack) && item.stack.count >= requiredItems[criterion]!! && item !in usedItems) {
                        usedItems.add(item) // Mark the item as used
                        matchingMap[criterion] = item
                        if (backtrack(index + 1)) {
                            return true // Move to the next criterion
                        }
                        usedItems.remove(item) // Backtrack if unsuccessful
                        matchingMap.remove(criterion)
                    }
                }

                return false
            }

            return if (backtrack(0)) matchingMap else null
        }
    }

    fun getPickupRange(circles: List<CircleType>): Int {
        return circles.maxOf { it.size }
    }

    /**
     * Constructs a list of circles that will be used for this ritual.
     * If list returns empty, no ritual should be started.
     */
    fun isValid(world: World, pos: BlockPos, ctx: RitualContext?): List<CircleType> {
        val result = mutableListOf<CircleType>()
        for (circle in circleTypes) {
            result += circle.isValid(world, pos, ctx) ?: return emptyList()
        }
        return result
    }

    /**
     * Check if all items required for the ritual are within the circle
     */
    fun requiredItemsNearby(world: World, pos: BlockPos, circles: List<CircleType>): Boolean {
        val inCircle = RitualContext.getItemsInRange(world, pos, circles)
        return requiredItemsNearby(ingredients.associateWith { it.amount.min }, inCircle) != null
    }

    /**
     * Triggers effects that only run once.
     * This method should therefore only be activated once per initiated ritual.
     */
    fun activate(ctx: RitualContext): Boolean {
        for (effect in unTickingEffects) {
            try {
                if (!effect.activate(ctx.pos, this, ctx)) return false
            } catch (e: Exception) {
                LOGGER.error("Error occurred while activating effect ${effect.id}(${effect.uuid}) on ritual $id:", e)
                return false
            }
        }
        for (circle in circleTypes) {
            Visuals.outwardsCircle(ctx.world as ServerWorld, Vec3d.ofCenter(ctx.realPos, .1), circle.size.toDouble())
        }
        return true
    }

    /**
     * Ticks ticking rituals.
     * They handle tick counters themselves, so we should call this every tick once the ritual has started
     */
    fun tick(ctx: RitualContext): Boolean {
        for (effect in tickingEffects) {
            try {
                if (!effect.activate(ctx.pos, this, ctx)) return false
            } catch (e: Exception) {
                LOGGER.error("Error occurred while ticking effect ${effect.id}(${effect.uuid}) on ritual $id:", e)
                return false
            }
        }
        return true
    }
}