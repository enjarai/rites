package nl.enjarai.rites.util

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import nl.enjarai.rites.type.Ritual

class RitualContext(val world: World, val pos: BlockPos, val ritual: Ritual) {
    var storedItems = arrayOf<ItemStack>()
    var returnableItems = arrayOf<ItemStack>()
    val variables = hashMapOf<String, String>()
    var tickCooldown = Array(ritual.effects.size) { ritual.effects[it].getTickCooldown() }

    constructor(world: World, pos: BlockPos, ritual: Ritual, nbtCompound: NbtCompound) : this(world, pos, ritual) {
        storedItems = nbtCompound.getList("storedItems", NbtList.COMPOUND_TYPE.toInt()).map {
            ItemStack.fromNbt(it as NbtCompound)
        }.toTypedArray()
        returnableItems = nbtCompound.getList("returnableItems", NbtList.COMPOUND_TYPE.toInt()).map {
            ItemStack.fromNbt(it as NbtCompound)
        }.toTypedArray()
        nbtCompound.getCompound("variables").keys.forEach {
            variables[it] = nbtCompound.get(it)?.asString() ?: return@forEach
        }
        tickCooldown = nbtCompound.getList("tickCooldown", NbtList.INT_TYPE.toInt()).map {
            (it as NbtInt).intValue()
        }.toTypedArray()
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

        val cooldowns = NbtList()
        for (i in tickCooldown) {
            cooldowns.add(NbtInt.of(i))
        }
        nbt.put("tickCooldown", cooldowns)

        return nbt
    }

    fun parseVariables(string: String): String {
        return PlaceholderFillerInner.fillInPlaceholders(variables, string)
    }
}