package nl.enjarai.rites.item

import eu.pb4.polymer.core.api.other.PolymerEnchantment
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.EquipmentSlot

class SoulBindingEnchantment : Enchantment(Rarity.RARE, EnchantmentTarget.BREAKABLE, EquipmentSlot.values()), PolymerEnchantment {
    override fun getMaxLevel(): Int {
        return 5
    }

    override fun canAccept(other: Enchantment?): Boolean {
        return other != Enchantments.VANISHING_CURSE
    }

    override fun isAvailableForEnchantedBookOffer(): Boolean {
        return false
    }

    override fun isAvailableForRandomSelection(): Boolean {
        return false
    }

}