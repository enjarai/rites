package nl.enjarai.rites.type.ritual_effect.item

import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.entity.ItemEntity
import net.minecraft.item.Items
import net.minecraft.nbt.StringNbtReader
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class DropItemEffect : RitualEffect() {
    @FromJson
    private lateinit var item: Identifier
    @FromJson
    private val count: InterpretedNumber = ConstantNumber(1)
    @FromJson
    private val nbt: String = "{}"

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val spawnPos = Vec3d.ofBottomCenter(pos)
        val item = Registry.ITEM.get(item)
        val itemStack = if (item != Items.AIR) item.defaultStack else return false

        itemStack.count = count.interpretAsInt(ctx)
        if (nbt != "{}") {
            itemStack.nbt = try {
                StringNbtReader.parse(nbt)
            } catch (e: CommandSyntaxException) {
                return false
            }
        }
        return ctx.world.spawnEntity(ItemEntity(ctx.world, spawnPos.x, spawnPos.y, spawnPos.z, itemStack))
    }
}