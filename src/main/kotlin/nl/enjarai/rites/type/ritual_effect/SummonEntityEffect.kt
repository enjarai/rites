package nl.enjarai.rites.type.ritual_effect

import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.nbt.StringNbtReader
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.util.RitualContext

class SummonEntityEffect(values: Map<String, Any>) : RitualEffect(values) {
    private val entity: String = getValue(values, "entity")
    private val nbt: String = getValue(values, "nbt", "{}")

    override fun activate(ritual: Ritual, ctx: RitualContext): Boolean {
        val entityPos = Vec3d.ofBottomCenter(ctx.pos).add(.0, .01, .0)
        val entityNbt = try {
            StringNbtReader.parse(ctx.parseVariables(nbt))
        } catch (e: CommandSyntaxException) {
            return false
        }
        entityNbt.putString("id", ctx.parseVariables(entity))

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