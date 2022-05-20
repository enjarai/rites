package nl.enjarai.rites.item

import eu.pb4.polymer.api.item.PolymerBlockItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.Items
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.registry.Registry
import nl.enjarai.rites.RitesMod
import nl.enjarai.rites.block.ModBlocks

object ModItems {
    val RITE_CENTER = object : PolymerBlockItem(ModBlocks.RITE_CENTER, FabricItemSettings().group(ItemGroup.MISC), Items.SUNFLOWER) {
        override fun place(context: ItemPlacementContext): ActionResult {
            val result = super.place(context)
            if (result != ActionResult.FAIL)
                context.player?.swingHand(context.hand, true)
            return result
        }
    }
    val WAYSTONE = WayStoneItem()

    fun register() {
        Registry.register(Registry.ITEM, RitesMod.id("rite_center"), RITE_CENTER)
        Registry.register(Registry.ITEM, RitesMod.id("waystone"), WAYSTONE)
    }
}