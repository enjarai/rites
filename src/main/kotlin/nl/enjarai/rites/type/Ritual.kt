package nl.enjarai.rites.type

import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.item.Item
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import nl.enjarai.rites.resource.Rituals
import nl.enjarai.rites.type.ritual_effect.RitualEffect
import nl.enjarai.rites.util.Visuals

class Ritual(val circleTypes: List<CircleType>, val ingredients: Map<Item, Int>, val effects: List<RitualEffect>) {
    private val unTickingEffects = effects.filter { ritualEffect -> !ritualEffect.isTicking() }
    private val tickingEffects = effects.filter { ritualEffect -> ritualEffect.isTicking() }
    val hasTickingEffects = tickingEffects.isNotEmpty()

    val id: Identifier? get() {
        Rituals.values.entries.forEach {
            if (it.value === this) {
                return it.key
            }
        }
        return null
    }

    val pickupRange: Int get() {
        return circleTypes.maxOf { it.size }
    }

    /**
     * Constructs a list of circles that will be used for this ritual.
     * If list returns empty, no ritual should be started.
     */
    fun isValid(world: World, pos: BlockPos, ctx: RitualContext?): List<CircleType> {
        val result = mutableListOf<CircleType>()
        for (circle in circleTypes) {
            // ctx will not be null if this is a multi-rite setup,
            // so we should check if that circle size isn't already occupied
            if ((ctx != null) && !ctx.canAddCircle(circle.size)) return emptyList()

            result += circle.isValid(world, pos) ?: return emptyList()
        }
        return result
    }

    fun <T : Entity> getEntitiesInRangeByClass(world: World, pos: BlockPos, clazz: Class<T>, verticalRange: Double = 0.0): List<T> {
        val range = pickupRange.toDouble()
        val center = Vec3d.ofBottomCenter(pos)
        val box = Box.of(center, range * 2, range * 2, range * 2)

        return world.getEntitiesByClass(
            clazz,
            box.withMaxY(box.maxY + verticalRange)
        ) { it.squaredDistanceTo(Vec3d(
            center.getX(),
            it.y.coerceAtLeast(center.getY()).coerceAtMost(center.getY() + verticalRange),
            center.getZ()
        )) < range * range }
    }

    /**
     * Find all item entities within the ritual at a specific position
     */
    fun getItemsInRange(world: World, pos: BlockPos): List<ItemEntity> {
        return getEntitiesInRangeByClass(world, pos, ItemEntity::class.java)
    }

    /**
     * Check if all items required for the ritual are within the circle
     */
    fun requiredItemsNearby(world: World, pos: BlockPos): Boolean {
        val inCircle = getItemsInRange(world, pos)
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
            if (!effect.activate(this, ctx)) return false
        }
        for (circle in circleTypes) {
            Visuals.outwardsCircle(ctx.world as ServerWorld, Vec3d.ofCenter(ctx.pos, .1), circle.size.toDouble())
        }
        return true
    }

    /**
     * Ticks ticking rituals.
     * They handle tick counters themselves, so we should call this every tick once the ritual has started
     */
    fun tick(ctx: RitualContext): Boolean {
        for (effect in tickingEffects) {
            if (!effect.activate(this, ctx)) return false
        }
        return true
    }
}