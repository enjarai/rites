package nl.enjarai.rites.type.ritual_effect

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.StringNbtReader
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import nl.enjarai.rites.type.Ritual

class SummonEntityEffect(values: HashMap<String, Any>) : RitualEffect(values) {
    private val entity = Registry.ENTITY_TYPE.getOrEmpty(getIdNullSafe(values["entity"] as? String)).or {
        throw IllegalArgumentException("Invalid entity/no entity given")
    }.get()
    private val nbt = run {
        val string = values["nbt"] as? String ?: return@run NbtCompound()
        StringNbtReader.parse(string)!!
    }

    override fun activate(world: World, pos: BlockPos, ritual: Ritual): Boolean {
        val entityPos = Vec3d.ofBottomCenter(pos).add(.0, .01, .0)
        val entityNbt = nbt.copy()
        entityNbt.putString("id", EntityType.getId(entity).toString())

        val entityObj = EntityType.loadEntityWithPassengers(
            entityNbt, world
        ) { entity: Entity ->
            entity.refreshPositionAndAngles(
                entityPos.getX(), entityPos.getY(), entityPos.getZ(),
                entity.yaw, entity.pitch
            )
            entity
        } ?: return false
        entityObj.setPos(entityPos.getX(), entityPos.getY(), entityPos.getZ())

        return (world as ServerWorld).spawnNewEntityAndPassengers(entityObj)
    }
}