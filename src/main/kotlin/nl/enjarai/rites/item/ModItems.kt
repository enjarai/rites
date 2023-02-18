package nl.enjarai.rites.item

import eu.pb4.polymer.api.item.PolymerBlockItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.*
import net.minecraft.util.ActionResult
import net.minecraft.util.registry.Registry
import nl.enjarai.rites.RitesMod
import nl.enjarai.rites.block.ModBlocks

object ModItems {
    val RITE_CENTER: BlockItem = register("rite_center", object : PolymerBlockItem(
        ModBlocks.RITE_CENTER,
        FabricItemSettings().group(ItemGroup.MISC), Items.SUNFLOWER
    ) {
        override fun place(context: ItemPlacementContext): ActionResult {
            val result = super.place(context)
            if (result != ActionResult.FAIL)
                context.player?.swingHand(context.hand, true)
            return result
        }
    })
    val RITE_FOCUS: BlockItem = register("rite_focus", object : PolymerBlockItem(
        ModBlocks.RITE_FOCUS,
        FabricItemSettings().group(ItemGroup.MISC), Items.CONDUIT
    ) {
        override fun hasGlint(stack: ItemStack): Boolean {
            return stack.nbt?.contains("riteData") ?: false
        }
    })
    val WAYSTONE: Item = register("waystone", WayStoneItem())

    // Custom ingredients
    val DISRUPTIVE_OINTMENT = register("disruptive_ointment", PotionLikeIngredientItem(0x993b3c))
    val EMULSION_OF_CONJOINMENT = register("emulsion_of_conjoinment", PotionLikeIngredientItem(0xe0a243))
    val EVER_CHANGING_EXTRACT = register("ever_changing_extract", PotionLikeIngredientItem(0x2c2b79))
    val ESSENCE_OF_PASSIONS = register("essence_of_passions", PotionLikeIngredientItem(0xac64ae))
    val DEATHLY_PANACEA = register("deathly_panacea", PotionLikeIngredientItem(0x182c16))
    val TENEBROUS_MARROW = register("tenebrous_marrow", PotionLikeIngredientItem(0x1c1c1c))
    val KERNEL_OF_INDUSTRY = register("kernel_of_industry", PotionLikeIngredientItem(0x276486))

    fun init() {
        // Initialize this class
    }

    private fun <T : Item> register(id: String, item: T): T {
        return Registry.register(Registry.ITEM, RitesMod.id(id), item)
    }
}