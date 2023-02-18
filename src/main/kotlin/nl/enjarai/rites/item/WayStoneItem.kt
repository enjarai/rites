package nl.enjarai.rites.item

import eu.pb4.polymer.core.api.item.PolymerItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity

class WayStoneItem : Item(FabricItemSettings()), PolymerItem {
    companion object {
        const val LINKED_KEY = "linked"
        const val LINKED_POS_KEY = "linkedPos"
        const val LINKED_DIM_KEY = "linkedDim"
    }

    override fun getPolymerItem(itemStack: ItemStack, player: ServerPlayerEntity?): Item {
        return Items.FIREWORK_STAR
    }

    override fun getPolymerItemStack(itemStack: ItemStack, context: TooltipContext, player: ServerPlayerEntity?): ItemStack {
        val stack = super.getPolymerItemStack(itemStack, context, player)
        if (isLinked(itemStack)) {
            val nbt = stack.getOrCreateSubNbt("Explosion")
            nbt.putIntArray("Colors", listOf(0xffffff))
        }
        return stack
    }

    private fun isLinked(stack: ItemStack): Boolean {
        return stack.nbt?.getBoolean(LINKED_KEY) == true
    }
}