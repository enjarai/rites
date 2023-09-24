package nl.enjarai.rites.item

import eu.pb4.polymer.core.api.item.PolymerItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Rarity

class IngredientItem(
    private val polymerItem: Item,
    rarity: Rarity = Rarity.COMMON,
    private val enchanted: Boolean = false,
    maxCount: Int = 64
) : Item(FabricItemSettings().rarity(rarity).maxCount(maxCount)), PolymerItem {
    override fun getPolymerItem(itemStack: ItemStack, player: ServerPlayerEntity?): Item {
        return polymerItem
    }

    override fun hasGlint(stack: ItemStack): Boolean {
        return enchanted
    }
}