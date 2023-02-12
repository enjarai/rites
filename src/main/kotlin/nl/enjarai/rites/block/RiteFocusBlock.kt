package nl.enjarai.rites.block

import eu.pb4.polymer.api.block.PolymerBlock
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.tag.FluidTags
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import nl.enjarai.rites.util.Visuals
import java.util.*

class RiteFocusBlock(settings: Settings) : BlockWithEntity(settings), Waterloggable, PolymerBlock {
    companion object {
        val WATERLOGGED: BooleanProperty = Properties.WATERLOGGED
        val ACTIVE: BooleanProperty = Properties.ENABLED

        val SHAPE: VoxelShape = createCuboidShape(5.0, 5.0, 5.0, 11.0, 11.0, 11.0)

        fun setActive(world: World, pos: BlockPos, active: Boolean) {
            world.setBlockState(pos, world.getBlockState(pos).with(ACTIVE, active))
        }
    }

    init {
        defaultState = defaultState
            .with(WATERLOGGED, false)
            .with(ACTIVE, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(WATERLOGGED, ACTIVE)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return RiteFocusBlockEntity(pos, state)
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        val nbt = itemStack.orCreateNbt
        val blockEntity = world.getBlockEntity(pos) as? RiteFocusBlockEntity
        if (blockEntity != null && nbt.contains("riteData")) {
            val riteData = nbt.getCompound("riteData")

            blockEntity.storedItems = riteData.getList("storedItems", NbtList.COMPOUND_TYPE.toInt()).map {
                ItemStack.fromNbt(it as NbtCompound)
            }.toTypedArray()

            blockEntity.rituals = riteData.getList("rituals", NbtList.STRING_TYPE.toInt()).map {
                Identifier(it.asString())
            }.toList()
        }
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        val blockEntity = world.getBlockEntity(pos) as? RiteFocusBlockEntity
        if (blockEntity != null) {
            if (player.isSneaking && state.get(ACTIVE)) {
                blockEntity.endAllRituals(true)
                return ActionResult.success(true)
            } else if (!player.isSneaking && !state.get(ACTIVE)) {
                blockEntity.tryStartRituals()
                return ActionResult.success(true)
            }
        }
        return super.onUse(state, world, pos, player, hand, hit)
    }

    override fun hasRandomTicks(state: BlockState): Boolean {
        return true
    }

    override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        Visuals.runningFocus(world, pos)
    }

    override fun getPolymerBlock(state: BlockState): Block {
        return Blocks.CONDUIT
    }

    override fun getPolymerBlockState(state: BlockState): BlockState {
        return Blocks.CONDUIT.defaultState
            .with(ConduitBlock.WATERLOGGED, state.get(WATERLOGGED))
    }

    override fun <T : BlockEntity> getTicker(world: World, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T> {
        return BlockEntityTicker<T> { _, _, _, blockEntity ->
            (blockEntity as? RiteFocusBlockEntity)?.tick()
        }
    }

    override fun getFluidState(state: BlockState): FluidState {
        return if (state.get(WATERLOGGED)) {
            Fluids.WATER.getStill(false)
        } else super.getFluidState(state)
    }

    override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState {
        if (state.get(WATERLOGGED)) {
            world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        val fluidState = ctx.world.getFluidState(ctx.blockPos)
        return defaultState.with(
            ConduitBlock.WATERLOGGED,
            fluidState.isIn(FluidTags.WATER) && fluidState.level == 8
        )
    }

    override fun canPathfindThrough(state: BlockState, world: BlockView, pos: BlockPos, type: NavigationType): Boolean {
        return false
    }

    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        return SHAPE
    }
}