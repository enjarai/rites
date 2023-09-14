package nl.enjarai.rites.item

import eu.pb4.polymer.core.api.item.PolymerItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Rarity
import net.minecraft.world.World

class PotionLikeIngredientItem(private val color: Int, rarity: Rarity = Rarity.COMMON) : Item(FabricItemSettings().rarity(rarity)), PolymerItem {
    override fun getPolymerItem(itemStack: ItemStack, player: ServerPlayerEntity?): Item {
        return Items.POTION
    }

    override fun getPolymerItemStack(itemStack: ItemStack, context: TooltipContext, player: ServerPlayerEntity?): ItemStack {
        return super.getPolymerItemStack(itemStack, context, player).apply {
            orCreateNbt.putInt("CustomPotionColor", color)
        }
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        tooltip.add(Text.translatable(getTranslationKey(stack) + ".tooltip").formatted(Formatting.GRAY))
        super.appendTooltip(stack, world, tooltip, context)
    }
}