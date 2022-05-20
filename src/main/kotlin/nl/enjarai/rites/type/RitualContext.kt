package nl.enjarai.rites.type

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import nl.enjarai.rites.resource.CircleTypes
import nl.enjarai.rites.resource.Rituals
import nl.enjarai.rites.type.ritual_effect.RitualEffect
import nl.enjarai.rites.util.PlaceholderFillerInner
import java.util.*

class RitualContext(val worldGetter: () -> World, val pos: BlockPos, val ritual: Ritual) {
    val world: World get() = worldGetter()
    var storedItems = arrayOf<ItemStack>()
    var returnableItems = arrayOf<ItemStack>()
    val variables = hashMapOf<String, String>()
    val tickCooldown = hashMapOf<UUID, Int>()
    var selectedRitual = 0
    var rituals = arrayOf<Ritual>()
    var circles = arrayOf<CircleType>()

    constructor(worldGetter: () -> World, pos: BlockPos, ritual: Ritual, nbtCompound: NbtCompound) : this(worldGetter, pos, ritual) {
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
        for ((i, string) in nbtCompound.getList("rituals", NbtList.STRING_TYPE.toInt()).withIndex()) {
            rituals[i] = Rituals.values [Identifier.tryParse(string.asString())] ?: continue
        }
        for ((i, string) in nbtCompound.getList("circles", NbtList.STRING_TYPE.toInt()).withIndex()) {
            circles[i] = CircleTypes.values [Identifier.tryParse(string.asString())] ?: continue
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
            rituals.add(NbtString.of(ritual.id.toString()))
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

    fun canMaintain(world: World, pos: BlockPos): Boolean {
        for (circle in circles) {
            if (!circle.isSelfValid(world, pos)) return false
        }
        return true
    }

    fun canAddCircle(size: Int): Boolean {
        for (circle in circles) {
            if (circle.size == size) return false
        }
        return true
    }

    fun drawParticleEffects(world: World, pos: BlockPos) {
        val serverWorld = world as? ServerWorld ?: return
        circles.forEach {
            it.drawParticleCircle(serverWorld, pos)
        }
    }

    fun activateAll(): Boolean {
        rituals.forEach {
            if (!it.activate(this)) return false
        }
        return true
    }

    fun tickAll(): Boolean {
        rituals.forEach {
            if (!it.tick(this)) return false
        }
        return true
    }

    fun getActiveRitual(): Ritual? {
        return rituals.getOrNull(selectedRitual)
    }

    fun appendRitual(ritual: Ritual) {
        rituals += (ritual)
    }

    fun cycleRituals() {
        selectedRitual += 1
        if (selectedRitual > rituals.size) selectedRitual = 0
    }
}