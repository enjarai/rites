package nl.enjarai.rites.type.ritual_effect

import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.entity.ItemEntity
import net.minecraft.item.Items
import net.minecraft.nbt.StringNbtReader
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext

class DropItemEffect(values: Map<String, Any>) : RitualEffect(values) {
    private val item: String = getValue(values, "item")
    private val count: Int = getValue(values, "count", 1.0).toInt()
    private val nbt: String = getValue(values, "nbt", "{}")

    override fun activate(ritual: Ritual, ctx: RitualContext): Boolean {
        val spawnPos = Vec3d.ofBottomCenter(ctx.pos)
        val item = Registry.ITEM.get(getIdNullSafe(ctx.parseVariables(item)))
        val itemStack = if (item != Items.AIR) item.defaultStack else return false

        itemStack.count = count
        if (nbt != "{}") {
            itemStack.nbt = try {
                StringNbtReader.parse(ctx.parseVariables(nbt))
            } catch (e: CommandSyntaxException) {
                return false
            }
        }
        return ctx.world.spawnEntity(ItemEntity(ctx.world, spawnPos.x, spawnPos.y, spawnPos.z, itemStack))
    }
}