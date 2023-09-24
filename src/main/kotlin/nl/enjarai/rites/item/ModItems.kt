package nl.enjarai.rites.item

import eu.pb4.polymer.core.api.item.PolymerBlockItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.enchantment.Enchantment
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
        FabricItemSettings(), Items.LIGHT_WEIGHTED_PRESSURE_PLATE
    ) {
        override fun place(context: ItemPlacementContext): ActionResult {
            val result = super.place(context)
            if (result != ActionResult.FAIL)
                context.player?.swingHand(context.hand, true)
            return result
        }
    })
    val RITE_SUBCENTER: BlockItem = register("rite_subcenter", object : PolymerBlockItem(
        ModBlocks.RITE_SUBCENTER,
        FabricItemSettings(), Items.HEAVY_WEIGHTED_PRESSURE_PLATE
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
    val GUIDE_BOOK: Item = register("guide_book", GuideBookItem())
    val WAYSTONE: Item = register("waystone", WayStoneItem())
    val POPPET: Item = register("poppet", IngredientItem(Items.PLAYER_HEAD, maxCount = 16))
    val BOUND_POPPET: Item = register("bound_poppet", BoundPoppetItem())

    // Custom ingredients
    // Low grade ingredients
    val DISRUPTIVE_OINTMENT = register("disruptive_ointment", PotionLikeIngredientItem(0x993b3c))
    val EMULSION_OF_CONJOINMENT = register("emulsion_of_conjoinment", PotionLikeIngredientItem(0xe0a243))
    val EVER_CHANGING_EXTRACT = register("ever_changing_extract", PotionLikeIngredientItem(0x3c7b99))
    val ESSENCE_OF_PASSIONS = register("essence_of_passions", PotionLikeIngredientItem(0xac64ae))
    val SUPPLE_DEW = register("supple_dew", PotionLikeIngredientItem(0x3c9940))
    val OIL_OF_VITRIOL = register("oil_of_vitriol", PotionLikeIngredientItem(0x979914))
    val MOTIVE_CATALYST = register("motive_catalyst", PotionLikeIngredientItem(0x646464))

    // Mid grade ingredients
    val DEATHLY_PANACEA = register("deathly_panacea", PotionLikeIngredientItem(0x185c16, Rarity.RARE))
    val BLOOD_OF_THE_EARTH = register("blood_of_the_earth", PotionLikeIngredientItem(0x671222, Rarity.RARE))

    // High grade ingredients
    val TENEBROUS_MARROW = register("tenebrous_marrow", PotionLikeIngredientItem(0x1c1c1c, Rarity.EPIC))
//    val KERNEL_OF_INDUSTRY = register("kernel_of_industry", PotionLikeIngredientItem(0x567641, Rarity.RARE))

    val ASH = register("ash", IngredientItem(Items.GUNPOWDER))
    val SCORCHED_ASH = register("scorched_ash", IngredientItem(Items.REDSTONE, Rarity.RARE))
    val LEVITATING_ASH = register("levitating_ash", IngredientItem(Items.SUGAR, Rarity.EPIC, true))

    // Enchantments
    val SOUL_BINDING = register("soul_binding", SoulBindingEnchantment());

    fun init() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register {
            it.add(ItemStack(RITE_CENTER))
            it.add(ItemStack(RITE_SUBCENTER))
            it.add(ItemStack(RITE_FOCUS))
            it.add(ItemStack(GUIDE_BOOK))
            it.add(ItemStack(WAYSTONE))
            it.add(ItemStack(POPPET))
            it.add(ItemStack(BOUND_POPPET))
        }
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register {
            it.add(ItemStack(ASH))
            it.add(ItemStack(SCORCHED_ASH))
            it.add(ItemStack(LEVITATING_ASH))

            it.add(ItemStack(DISRUPTIVE_OINTMENT))
            it.add(ItemStack(EMULSION_OF_CONJOINMENT))
            it.add(ItemStack(EVER_CHANGING_EXTRACT))
            it.add(ItemStack(ESSENCE_OF_PASSIONS))
            it.add(ItemStack(SUPPLE_DEW))
            it.add(ItemStack(OIL_OF_VITRIOL))
            it.add(ItemStack(MOTIVE_CATALYST))

            it.add(ItemStack(DEATHLY_PANACEA))
            it.add(ItemStack(BLOOD_OF_THE_EARTH))

            it.add(ItemStack(TENEBROUS_MARROW))
//            it.add(ItemStack(KERNEL_OF_INDUSTRY))
        }
    }

    private fun <T : Item> register(id: String, item: T): T {
        return Registry.register(Registries.ITEM, RitesMod.id(id), item)
    }

    private fun <T : Enchantment> register(id: String, enchantment: T): T {
        return Registry.register(Registries.ENCHANTMENT, RitesMod.id(id), enchantment)
    }
}