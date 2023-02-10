package nl.enjarai.rites.type.ritual_effect.item

import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.nbt.StringNbtReader
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class MergeItemNbtEffect : RitualEffect() {
    @FromJson
    private lateinit var ref: String
    @FromJson
    private lateinit var nbt: String

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val item = ctx.addressableItems[ref] ?: return false
        val nbt = try {
            StringNbtReader.parse(nbt)
        } catch (e: CommandSyntaxException) {
            return false
        }
        item.orCreateNbt.copyFrom(nbt)
        return true
    }
}