package nl.enjarai.rites.type

import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import nl.enjarai.rites.resource.CircleTypes
import nl.enjarai.rites.type.ritual_effect.RitualEffect
import nl.enjarai.rites.util.PlaceholderFillerInner
import nl.enjarai.rites.util.Visuals
import java.util.*

class RitualContext(val worldGetter: () -> World, val realPos: BlockPos) {
    val world: World get() = worldGetter()
    var pos: BlockPos = realPos.mutableCopy()
    var storedItems = arrayOf<ItemStack>()
    var returnableItems = arrayOf<ItemStack>()
    val variables = hashMapOf<String, String>()
    val tickCooldown = hashMapOf<UUID, Int>()
    private var selectedRitual = 0
    private var rituals = arrayOf<RitualInstance>()
    var circles = arrayOf<CircleType>()

    val hasTickingEffects: Boolean get() {
        rituals.forEach {
            if (it.ritual.shouldKeepRunning) return true
        }
        return false
    }
    val range: Int get() = circles.maxOf { it.size }

    constructor(worldGetter: () -> World, pos: BlockPos, nbtCompound: NbtCompound) : this(worldGetter, pos) {
        storedItems = nbtCompound.getList("storedItems", NbtList.COMPOUND_TYPE.toInt()).map {
            ItemStack.fromNbt(it as NbtCompound)
        }.toTypedArray()
        returnableItems = nbtCompound.getList("returnableItems", NbtList.COMPOUND_TYPE.toInt()).map {
            ItemStack.fromNbt(it as NbtCompound)
        }.toTypedArray()
        nbtCompound.getCompound("variables").keys.forEach {
            variables[it] = nbtCompound.get(it)?.asString() ?: return@forEach
        }
        val cooldowns = nbtCompound.getCompound("tickCooldown")
        cooldowns.keys.forEach {
            tickCooldown[UUID.fromString(it)] = cooldowns.getInt(it)
        }
        selectedRitual = nbtCompound.getInt("selectedRitual")
        for (ritual in nbtCompound.getList("rituals", NbtList.COMPOUND_TYPE.toInt())) {
            rituals += RitualInstance.fromNbt(ritual as NbtCompound) ?: continue
        }
        for (string in nbtCompound.getList("circles", NbtList.STRING_TYPE.toInt())) {
            circles += CircleTypes.values [Identifier.tryParse(string.asString())] ?: continue
        }
    }

    fun toNbt(): NbtCompound {
        val nbt = NbtCompound()

        val items = NbtList()
        for (stack in storedItems) {
            val entry = NbtCompound()
            items.add(stack.writeNbt(entry))
        }
        nbt.put("storedItems", items)

        val items2 = NbtList()
        for (stack in returnableItems) {
            val entry = NbtCompound()
            items2.add(stack.writeNbt(entry))
        }
        nbt.put("returnableItems", items2)

        val varsNbt = NbtCompound()
        variables.forEach {
            nbt.put(it.key, NbtString.of(it.value))
        }
        nbt.put("variables", varsNbt)

        val cooldowns = NbtCompound()
        for (i in tickCooldown.entries) {
            cooldowns.putInt(i.key.toString(), i.value)
        }
        nbt.put("tickCooldown", cooldowns)

        nbt.putInt("selectedRitual", selectedRitual)

        val rituals = NbtList()
        for (ritual in this.rituals) {
            rituals.add(ritual.toNbt())
        }
        nbt.put("rituals", rituals)

        val circles = NbtList()
        for (circle in this.circles) {
            circles.add(NbtString.of(circle.id.toString()))
        }
        nbt.put("circles", circles)

        return nbt
    }

    fun parseVariables(string: String): String {
        return PlaceholderFillerInner.fillInPlaceholders(variables, string)
    }

    fun checkCooldown(ritualEffect: RitualEffect): Boolean {
        val result = (tickCooldown[ritualEffect.uuid] ?: 0) < 1
        if (result) tickCooldown[ritualEffect.uuid] = ritualEffect.getTickCooldown()
        tickCooldown[ritualEffect.uuid]?.minus(1)?.let { tickCooldown.put(ritualEffect.uuid, it) }
        return result
    }

    fun canMaintain(): Boolean {
        for (circle in circles) {
            if (!circle.isSelfValid(world, realPos, null)) return false
        }
        return true
    }

    fun canAddCircle(size: Int): Boolean {
        for (circle in circles) {
            if (circle.size == size) return false
        }
        return true
    }

    fun drawParticleEffects() {
        val serverWorld = world as? ServerWorld ?: return
        circles.forEach {
            it.drawParticleCircle(serverWorld, realPos)
        }
    }

    /**
     * Activate the selected ritual
     */
    fun activateRitual(storedItems: Array<ItemStack>): RitualResult {
        val ritual = getSelectedRitual()
        if (ritual?.active == false) {
            this.storedItems = storedItems
            val success = ritual.activate(this)
            if (success) {
                Visuals.activate(world as ServerWorld, realPos)
            }
            return RitualResult.successFromBool(success)
        }
        return RitualResult.PASS
    }

    /**
     * Safely stop the specified ritual.
     * The success parameter specifies whether the ritual was considered a success or not.
     */
    fun stopRitual(ritual: RitualInstance, success: Boolean) {
        val ritualId = rituals.indexOf(ritual)

        if (!success) {
            Visuals.failParticles(world as ServerWorld, Vec3d.ofBottomCenter(realPos))
        }

        ritual.active = false
        rituals = rituals.drop(ritualId).toTypedArray()
    }

    fun stopAllRituals(success: Boolean): Array<ItemStack> {
        rituals.forEach {
            stopRitual(it, success)
        }
        return returnableItems
    }

    fun tickAll(): RitualResult {
        return RitualResult.merge(rituals.map {
            it.tick(this)
        })
    }

    fun getSelectedRitual(): RitualInstance? {
        return rituals.getOrNull(selectedRitual)
    }

    /**
     * Instances a ritual, adds it to the end of the stack and
     * selects it.
     */
    fun appendRitual(ritual: Ritual) {
        rituals += RitualInstance(ritual)
        selectedRitual = rituals.size - 1
    }

    fun appendCircles(circles: List<CircleType>) {
        this.circles += circles
    }

    /**
     * Safely cycles through rituals in the stack.
     * Does nothing if a ritual is currently activating.
     */
    fun cycleRituals() {
        if (!hasActivating()) {
            selectedRitual += 1
            if (selectedRitual > rituals.size) selectedRitual = 0
        }
    }

    fun hasActivating(): Boolean {
        rituals.forEach {
            if (!it.active) return true
        }
        return false
    }

    fun hasRituals(): Boolean {
        return rituals.isNotEmpty()
    }


    fun <T : Entity> getEntitiesInRangeByClass(clazz: Class<T>, verticalRange: Double = 0.0): List<T> {
        return getEntitiesInRangeByClass(world, pos, circles.toList(), clazz, verticalRange)
    }

    /**
     * Find all item entities within the ritual at a specific position
     */
    fun getItemsInRange(): List<ItemEntity> {
        return getItemsInRange(world, realPos, circles.toList())
    }

    companion object {
        fun <T : Entity> getEntitiesInRangeByClass(world: World, pos: BlockPos, circles: List<CircleType>, clazz: Class<T>, verticalRange: Double = 0.0): List<T> {
            val range = circles.maxOf { it.size }.toDouble()
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
        fun getItemsInRange(world: World, pos: BlockPos, circles: List<CircleType>): List<ItemEntity> {
            return getEntitiesInRangeByClass(world, pos, circles, ItemEntity::class.java)
        }
    }
}