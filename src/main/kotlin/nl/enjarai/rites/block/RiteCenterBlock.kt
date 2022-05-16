package nl.enjarai.rites.block

import eu.pb4.polymer.api.block.PolymerBlock
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.enums.WireConnection
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World

class RiteCenterBlock(settings: Settings) : BlockWithEntity(settings), PolymerBlock {
    companion object {
        val POWER = Properties.POWER
        val SHAPE = createCuboidShape(3.0, 0.0, 3.0, 13.0, 1.0, 13.0)
    }

    fun setPower(world: World, pos: BlockPos, state: BlockState, value: Int) {
        world.setBlockState(pos, state.with(POWER, value))
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder.add(POWER))
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return RiteCenterBlockEntity(pos, state)
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        if (hand == Hand.MAIN_HAND) {
            (world.getBlockEntity(pos) as? RiteCenterBlockEntity)?.onUse(player)
        }
        return ActionResult.success(true)
    }

    override fun <T : BlockEntity> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T> {
        return BlockEntityTicker<T> { _, _, _, blockEntity -> (blockEntity as? RiteCenterBlockEntity)?.tick() }
    }

    override fun getOutlineShape(
        state: BlockState, world: BlockView,
        pos: BlockPos, context: ShapeContext
    ): VoxelShape {
        return SHAPE
    }

    override fun getPolymerBlock(state: BlockState): Block {
        return Blocks.REDSTONE_WIRE
    }

    override fun getPolymerBlockState(state: BlockState): BlockState {
        return Blocks.REDSTONE_WIRE.defaultState
            .with(RedstoneWireBlock.WIRE_CONNECTION_NORTH, WireConnection.NONE)
            .with(RedstoneWireBlock.WIRE_CONNECTION_EAST, WireConnection.NONE)
            .with(RedstoneWireBlock.WIRE_CONNECTION_SOUTH, WireConnection.NONE)
            .with(RedstoneWireBlock.WIRE_CONNECTION_WEST, WireConnection.NONE)
            .with(RedstoneWireBlock.POWER, state.get(POWER))
    }
}