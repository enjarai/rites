package nl.enjarai.rites.type.ritual_effect

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.ConstantString
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedString

class SummonEntityEffect : RitualEffect() {
    @FromJson
    private lateinit var entity: Identifier
    @FromJson
    private val nbt: InterpretedString = ConstantString("{}")
    @FromJson
    private val pos_offset: List<InterpretedNumber> = listOf(ConstantNumber(.0), ConstantNumber(.0), ConstantNumber(.0))

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val entityPos = Vec3d.ofBottomCenter(pos).add(.0, .01, .0).add(
            pos_offset[0].interpret(ctx),
            pos_offset[1].interpret(ctx),
            pos_offset[2].interpret(ctx)
        )
        val entityNbt = nbt.interpretAsNbt(ctx) ?: return false
        entityNbt.putString("id", entity.toString())

        val entityObj = EntityType.loadEntityWithPassengers(
            entityNbt, ctx.world
        ) { entity: Entity ->
            entity.refreshPositionAndAngles(
                entityPos.getX(), entityPos.getY(), entityPos.getZ(),
                entity.yaw, entity.pitch
            )
            entity
        } ?: return false
        entityObj.setPos(entityPos.getX(), entityPos.getY(), entityPos.getZ())

        return (ctx.world as ServerWorld).spawnNewEntityAndPassengers(entityObj)
    }
}