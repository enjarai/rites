package nl.enjarai.rites.block

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.type.CircleType
import nl.enjarai.rites.type.Ritual

class RiteSubCenterBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : RiteCenterBlockEntity(type, pos, state) {
    constructor(pos: BlockPos, state: BlockState) : this(ModBlocks.RITE_SUBCENTER_ENTITY, pos, state)

    var linkedCenter: BlockPos? = null

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)

        if (nbt.contains("linkedCenter")) linkedCenter = NbtHelper.toBlockPos(nbt.getCompound("linkedCenter"))
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)

        if (linkedCenter != null) nbt.put("linkedCenter", NbtHelper.fromBlockPos(linkedCenter))
    }

    override fun tick() {
        super.tick()

        if (getWorld()?.isClient() == false && linkedCenter != null) {
            val spacing = 1
            val fromPos = Vec3d.ofBottomCenter(getPos()).add(0.0, 0.2, 0.0)
            val toPos = Vec3d.ofBottomCenter(linkedCenter!!).add(0.0, 0.2, 0.0)
            val deltaVec = toPos.subtract(fromPos)
            val deltaLength = deltaVec.length();
            val iterations = (deltaLength / spacing).toInt()
            val offset = getWorld()?.time?.div(8.0) ?: 0.0
//            for (i in 0..iterations) {
                var delta = (offset) * spacing / deltaLength
                delta %= 1.0
                val pos = fromPos.add(deltaVec.multiply(delta))
                val particleVelocity = deltaVec.multiply(0.0) // deltaVec.multiply((deltaLength - i * spacing) / deltaLength)
                (getWorld() as ServerWorld).spawnParticles(
                    ParticleTypes.WAX_OFF,
                    pos.getX(), pos.getY(), pos.getZ(), 0,
                    particleVelocity.getX(), particleVelocity.getY(), particleVelocity.getZ(), 1.0
                )
//            }
//            (getWorld() as ServerWorld).spawnParticles(
//                ParticleTypes.WAX_OFF,
//                fromPos.getX(), fromPos.getY(), fromPos.getZ(), 0,
//                deltaVec.getX(), deltaVec.getY(), deltaVec.getZ(), 1.0
//            )
        }
    }

    override fun slowTick(world: ServerWorld) {
        super.slowTick(world)

        // If the linked center is gone, unlink
        if (linkedCenter != null) {
            val blockEntity = world.getBlockEntity(linkedCenter)
            if (ritualContext == null || blockEntity !is RiteCenterBlockEntity || blockEntity.ritualContext == null) {
                linkedCenter = null
                endAllRituals(!(ritualContext?.hasActivating() ?: false))
            }
        }
    }

    override fun canRunRitual(ritual: Ritual, circles: List<CircleType>): Boolean {
        return ritual.shouldKeepRunning
    }

    override fun startRitual(ritual: Ritual, circles: List<CircleType>) {
        super.startRitual(ritual, circles)

        // TODO signal to linked center?
    }
}