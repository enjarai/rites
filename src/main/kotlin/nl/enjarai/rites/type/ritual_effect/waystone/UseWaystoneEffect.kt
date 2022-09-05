package nl.enjarai.rites.type.ritual_effect.waystone

import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtOps
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import nl.enjarai.rites.item.ModItems
import nl.enjarai.rites.item.WayStoneItem
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class UseWaystoneEffect : RitualEffect() {
    override fun shouldKeepRitualRunning(): Boolean {
        return true
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val waystoneStack = ctx.storedItems.firstOrNull {
            it.isOf(ModItems.WAYSTONE)
        } ?: return false

        val waystone = waystoneStack.split(1)
        val nbt = waystone.orCreateNbt

        ctx.returnableItems += waystone
        if (!nbt.getBoolean(WayStoneItem.LINKED_KEY)) return false

        val newPos = NbtHelper.toBlockPos(nbt.getCompound(WayStoneItem.LINKED_POS_KEY))
        val dim = World.CODEC.parse(NbtOps.INSTANCE, nbt[WayStoneItem.LINKED_DIM_KEY]).result().orElse(null)
            ?: return false

        if (dim != ctx.world.registryKey) return false
        ctx.pos = newPos

        return true
    }
}