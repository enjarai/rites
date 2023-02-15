package nl.enjarai.rites.item

import eu.pb4.polymer.api.item.PolymerBlockItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.*
import net.minecraft.util.ActionResult
import net.minecraft.util.registry.Registry
import nl.enjarai.rites.RitesMod
import nl.enjarai.rites.block.ModBlocks

object ModItems {
    val RITE_CENTER: Item = object : PolymerBlockItem(ModBlocks.RITE_CENTER, FabricItemSettings().group(ItemGroup.MISC), Items.SUNFLOWER) {
        override fun place(context: ItemPlacementContext): ActionResult {
            val result = super.place(context)
            if (result != ActionResult.FAIL)
                context.player?.swingHand(context.hand, true)
            return result
        }
    }
    val RITE_FOCUS: Item = object : PolymerBlockItem(ModBlocks.RITE_FOCUS, FabricItemSettings().group(ItemGroup.MISC), Items.CONDUIT) {
        override fun hasGlint(stack: ItemStack): Boolean {
            return stack.nbt?.contains("riteData") ?: false
        }
    }
    val WAYSTONE: Item = WayStoneItem()

    fun register() {
        Registry.register(Registry.ITEM, RitesMod.id("rite_center"), RITE_CENTER)
        Registry.register(Registry.ITEM, RitesMod.id("rite_focus"), RITE_FOCUS)
        Registry.register(Registry.ITEM, RitesMod.id("waystone"), WAYSTONE)
    }
}