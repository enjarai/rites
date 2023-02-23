package nl.enjarai.rites.item

import eu.pb4.polymer.core.api.item.PolymerBlockItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.*
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.ActionResult
import net.minecraft.util.Rarity
import nl.enjarai.rites.RitesMod
import nl.enjarai.rites.block.ModBlocks

object ModItems {
    val RITE_CENTER: BlockItem = register("rite_center", object : PolymerBlockItem(
        ModBlocks.RITE_CENTER,
        FabricItemSettings(), Items.SUNFLOWER
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
        FabricItemSettings(), Items.CONDUIT
    ) {
        override fun hasGlint(stack: ItemStack): Boolean {
            return stack.nbt?.contains("riteData") ?: false
        }
    })
    val WAYSTONE: Item = register("waystone", WayStoneItem())

    // Custom ingredients
    val DISRUPTIVE_OINTMENT = register("disruptive_ointment", PotionLikeIngredientItem(0x993b3c))
    val EMULSION_OF_CONJOINMENT = register("emulsion_of_conjoinment", PotionLikeIngredientItem(0xe0a243))
    val EVER_CHANGING_EXTRACT = register("ever_changing_extract", PotionLikeIngredientItem(0x3c7b99))
    val ESSENCE_OF_PASSIONS = register("essence_of_passions", PotionLikeIngredientItem(0xac64ae))
    val DEATHLY_PANACEA = register("deathly_panacea", PotionLikeIngredientItem(0x185c16, Rarity.RARE))
    val TENEBROUS_MARROW = register("tenebrous_marrow", PotionLikeIngredientItem(0x1c1c1c, Rarity.RARE))
    val KERNEL_OF_INDUSTRY = register("kernel_of_industry", PotionLikeIngredientItem(0x567641, Rarity.RARE))

    fun init() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register {
            it.add(ItemStack(RITE_CENTER))
            it.add(ItemStack(RITE_FOCUS))
            it.add(ItemStack(WAYSTONE))
        }
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register {
            it.add(ItemStack(DISRUPTIVE_OINTMENT))
            it.add(ItemStack(EMULSION_OF_CONJOINMENT))
            it.add(ItemStack(EVER_CHANGING_EXTRACT))
            it.add(ItemStack(ESSENCE_OF_PASSIONS))
            it.add(ItemStack(DEATHLY_PANACEA))
            it.add(ItemStack(TENEBROUS_MARROW))
            it.add(ItemStack(KERNEL_OF_INDUSTRY))
        }
    }

    private fun <T : Item> register(id: String, item: T): T {
        return Registry.register(Registries.ITEM, RitesMod.id(id), item)
    }
}