package nl.enjarai.rites.type.ritual_effect.special.waystone

import com.mojang.serialization.Codec
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtOps
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import nl.enjarai.rites.RitesMod.LOGGER
import nl.enjarai.rites.item.ModItems
import nl.enjarai.rites.item.WayStoneItem
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class BindWaystoneEffect : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<BindWaystoneEffect> = Codec.unit(::BindWaystoneEffect)
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val waystoneStack = ctx.storedItems.firstOrNull {
            it.isOf(ModItems.WAYSTONE)
        } ?: return false

        val waystone = waystoneStack.split(1)
        val nbt = waystone.orCreateNbt

        nbt.putBoolean(WayStoneItem.LINKED_KEY, true)
        nbt.put(WayStoneItem.LINKED_POS_KEY, NbtHelper.fromBlockPos(pos))
        World.CODEC.encodeStart(NbtOps.INSTANCE, ctx.world.registryKey).resultOrPartial(LOGGER::error)
            .ifPresent { nbtElement: NbtElement ->
                nbt.put(
                    WayStoneItem.LINKED_DIM_KEY,
                    nbtElement
                )
            }
        ctx.returnableItems += waystone

        return true
    }
}