package nl.enjarai.rites.type

import net.minecraft.item.Item
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import nl.enjarai.rites.RitesMod.LOGGER
import nl.enjarai.rites.resource.Rituals
import nl.enjarai.rites.type.ritual_effect.RitualEffect
import nl.enjarai.rites.util.Visuals

class Ritual(val circleTypes: List<CircleType>, val ingredients: Map<Item, Int>, val effects: List<RitualEffect>) {
    private val unTickingEffects = effects.filter { ritualEffect -> !ritualEffect.isTicking() }
    private val tickingEffects = effects.filter { ritualEffect -> ritualEffect.isTicking() }
    val shouldKeepRunning = effects.filter { ritualEffect -> ritualEffect.shouldKeepRitualRunning() }.isNotEmpty()

    val id: Identifier? get() {
        Rituals.values.entries.forEach {
            if (it.value === this) {
                return it.key
            }
        }
        return null
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
        return ingredients.mapValues { entry ->
            var requiredAmount = entry.value
            inCircle.forEach {
                val stack = it.stack
                if (stack.isOf(entry.key)) {
                    requiredAmount -= stack.count
                }
            }
            requiredAmount
        }.filter { entry -> entry.value > 0 }.isEmpty()
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
                LOGGER.error("Error occurred while activating effect ${effect.uuid} on ritual $id:", e)
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
                LOGGER.error("Error occurred while ticking effect ${effect.uuid} on ritual $id:", e)
                return false
            }
        }
        return true
    }
}