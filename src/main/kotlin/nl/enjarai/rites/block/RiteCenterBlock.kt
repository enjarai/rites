package nl.enjarai.rites.block

import eu.pb4.polymer.api.block.PolymerBlock
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World

class RiteCenterBlock(settings: Settings) : BlockWithEntity(settings), PolymerBlock {
    companion object {
        val POWER = Properties.POWER
        val SHAPE = createCuboidShape(3.0, 0.0, 3.0, 13.0, 1.0, 13.0)
    }

    init {
        defaultState = defaultState.with(POWER, 0)
    }

    fun setPower(world: World, pos: BlockPos, state: BlockState, value: Int) {
        if ((value == 0) != (state.get(POWER) == 0)) {
            val soundPos = Vec3d.ofBottomCenter(pos)
            if (value == 0) world.playSound(
                null,
                soundPos.x, soundPos.y, soundPos.z,
                SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, SoundCategory.BLOCKS,
                0.3f, 0.75f
            )
            else world.playSound(
                null,
                soundPos.x, soundPos.y, soundPos.z,
                SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, SoundCategory.BLOCKS,
                0.3f, 0.9f
            )
            world.updateNeighbors(pos, this)
        }
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
            return ActionResult.success(true)
        }
        return ActionResult.success(false)
    }

    override fun <T : BlockEntity> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T> {
        return BlockEntityTicker<T> { world1, pos, state1, blockEntity ->
            val power = state1.get(POWER)
            if (power in 1..14) {
                setPower(world1, pos, state1, power - 1)
            }
            (blockEntity as? RiteCenterBlockEntity)?.tick()
        }
    }

    override fun getOutlineShape(
        state: BlockState, world: BlockView,
        pos: BlockPos, context: ShapeContext
    ): VoxelShape {
        return SHAPE
    }

    override fun getPolymerBlock(state: BlockState): Block {
        return Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE
    }

    override fun getPolymerBlockState(state: BlockState): BlockState {
        return Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.defaultState
            .with(WeightedPressurePlateBlock.POWER, state.get(POWER))
    }
}