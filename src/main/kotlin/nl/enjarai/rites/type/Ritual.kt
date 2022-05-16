package nl.enjarai.rites.type

import net.minecraft.entity.ItemEntity
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import nl.enjarai.rites.resource.Rituals
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class Ritual(val circleTypes: List<CircleType>, val ingredients: Map<Item, Int>, val effects: List<RitualEffect>) {
    private val tickingEffects = effects.filter { ritualEffect -> ritualEffect.isContinuous() }
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

    fun isValid(world: World, pos: BlockPos): Boolean {
        for (circle in circleTypes) {
            if (!circle.isValid(world, pos)) return false
        }
        return true
    }

    /**
     * Find all item entities within the ritual at a specific position
     */
    fun getItemsInRange(world: World, pos: BlockPos): List<ItemEntity> {
        val range = pickupRange.toDouble()
        val center = Vec3d.ofBottomCenter(pos)

        return world.getEntitiesByClass(
            ItemEntity::class.java,
            Box.of(center, range * 2, 1.0, range * 2)
        ) { it.squaredDistanceTo(center) < range * range }
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

    fun activate(world: World, pos: BlockPos): Boolean {
        for (effect in effects) {
            if (!effect.activate(world, pos, this)) return false
        }
        return true
    }

    fun tick(world: World, pos: BlockPos): Boolean {
        if (hasTickingEffects) {
            for (effect in tickingEffects) {
                if (!effect.tick(world, pos, this)) return false
            }
        }
        return true
    }
}