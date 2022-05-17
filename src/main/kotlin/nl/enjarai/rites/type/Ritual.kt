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
import nl.enjarai.rites.util.RitualContext

class Ritual(val circleTypes: List<CircleType>, val ingredients: Map<Item, Int>, val effects: List<RitualEffect>) {
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

    fun isValid(world: World, pos: BlockPos): Boolean {
        for (circle in circleTypes) {
            if (!circle.isValid(world, pos)) return false
        }
        return true
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

    fun activate(ctx: RitualContext): Boolean {
        for (effect in effects) {
            if (!effect.activate(this, ctx)) return false
        }
        return true
    }

    fun tick(ctx: RitualContext): Boolean {
        for ((i, effect) in tickingEffects.withIndex()) {
            if (ctx.tickCooldown[i] < 1) {
                ctx.tickCooldown[i] = effect.getTickCooldown()
                if (!effect.tick(this, ctx)) return false
            }
            ctx.tickCooldown[i] -= 1
        }
        return true
    }

    fun drawParticleEffects(world: World, pos: BlockPos) {
        val serverWorld = world as? ServerWorld ?: return
        circleTypes.forEach {
            it.drawParticleCircle(serverWorld, pos)
        }
    }
}