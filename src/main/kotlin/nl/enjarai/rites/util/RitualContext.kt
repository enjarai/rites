package nl.enjarai.rites.util

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.ritual_effect.RitualEffect
import java.util.*

class RitualContext(val worldGetter: () -> World, val pos: BlockPos, val ritual: Ritual) {
    val world: World get() = worldGetter()
    var storedItems = arrayOf<ItemStack>()
    var returnableItems = arrayOf<ItemStack>()
    val variables = hashMapOf<String, String>()
    val tickCooldown = hashMapOf<UUID, Int>()

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
}