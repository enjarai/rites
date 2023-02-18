package nl.enjarai.rites.item

import eu.pb4.polymer.api.item.PolymerItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity

class PotionLikeIngredientItem(private val color: Int) : Item(FabricItemSettings().group(ItemGroup.MISC)), PolymerItem {
    override fun getPolymerItem(itemStack: ItemStack, player: ServerPlayerEntity?): Item {
        return Items.POTION
    }

    override fun getPolymerItemStack(itemStack: ItemStack, player: ServerPlayerEntity?): ItemStack {
        return super.getPolymerItemStack(itemStack, player).apply {
            orCreateNbt.putInt("CustomPotionColor", color)
        }
    }
}