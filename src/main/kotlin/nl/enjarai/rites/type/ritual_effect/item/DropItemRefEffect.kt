package nl.enjarai.rites.type.ritual_effect.item

import net.minecraft.entity.ItemEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class DropItemRefEffect : RitualEffect() {
    @FromJson
    private lateinit var ref: String

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val item = ctx.addressableItems[ref] ?: return false
        val spawnPos = Vec3d.ofBottomCenter(pos)
        return ctx.world.spawnEntity(ItemEntity(ctx.world, spawnPos.x, spawnPos.y, spawnPos.z, item))
    }
}