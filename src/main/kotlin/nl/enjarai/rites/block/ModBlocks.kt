package nl.enjarai.rites.block

import eu.pb4.polymer.api.block.PolymerBlockUtils
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.registry.Registry
import nl.enjarai.rites.RitesMod

object ModBlocks {
    val RITE_CENTER = RiteCenterBlock(FabricBlockSettings.of(Material.DECORATION).noCollision().breakInstantly())
    val RITE_CENTER_ENTITY: BlockEntityType<RiteCenterBlockEntity> =
        FabricBlockEntityTypeBuilder.create(::RiteCenterBlockEntity, RITE_CENTER).build()

    fun register() {
        Registry.register(Registry.BLOCK, RitesMod.id("rite_center"), RITE_CENTER)
        Registry.register(Registry.BLOCK_ENTITY_TYPE, RitesMod.id("rite_center"), RITE_CENTER_ENTITY)
        PolymerBlockUtils.registerBlockEntity(RITE_CENTER_ENTITY)
    }
}