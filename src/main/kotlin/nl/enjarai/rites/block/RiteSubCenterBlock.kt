package nl.enjarai.rites.block

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos

class RiteSubCenterBlock(settings: Settings) : RiteCenterBlock(settings) {
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return RiteSubCenterBlockEntity(pos, state)
    }

    override fun getPolymerBlock(state: BlockState): Block {
        return Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE
    }
}