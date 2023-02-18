package nl.enjarai.rites.block

import eu.pb4.polymer.core.api.block.PolymerBlockUtils
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import nl.enjarai.rites.RitesMod
import nl.enjarai.rites.item.ModItems

object ModBlocks {
    val RITE_CENTER = RiteCenterBlock(FabricBlockSettings.of(Material.DECORATION).noCollision().breakInstantly())
    val RITE_FOCUS = RiteFocusBlock(FabricBlockSettings
        .of(Material.DECORATION).strength(3.0f).luminance { 15 }.nonOpaque()) { ModItems.RITE_FOCUS.defaultStack }
    val RITE_CENTER_ENTITY: BlockEntityType<RiteCenterBlockEntity> =
        FabricBlockEntityTypeBuilder.create(::RiteCenterBlockEntity, RITE_CENTER).build()
    val RITE_FOCUS_ENTITY: BlockEntityType<RiteFocusBlockEntity> =
        FabricBlockEntityTypeBuilder.create(::RiteFocusBlockEntity, RITE_FOCUS).build()

    fun register() {
        Registry.register(Registries.BLOCK, RitesMod.id("rite_center"), RITE_CENTER)
        Registry.register(Registries.BLOCK_ENTITY_TYPE, RitesMod.id("rite_center"), RITE_CENTER_ENTITY)
        Registry.register(Registries.BLOCK, RitesMod.id("rite_focus"), RITE_FOCUS)
        Registry.register(Registries.BLOCK_ENTITY_TYPE, RitesMod.id("rite_focus"), RITE_FOCUS_ENTITY)
        PolymerBlockUtils.registerBlockEntity(RITE_CENTER_ENTITY)
        PolymerBlockUtils.registerBlockEntity(RITE_FOCUS_ENTITY)
    }
}