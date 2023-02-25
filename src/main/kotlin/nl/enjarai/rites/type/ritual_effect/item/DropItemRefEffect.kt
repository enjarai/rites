package nl.enjarai.rites.type.ritual_effect.item

import com.mojang.serialization.Codec
import net.minecraft.entity.ItemEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class DropItemRefEffect(val ref: String) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<DropItemRefEffect> = Codec.STRING
            .xmap(::DropItemRefEffect, DropItemRefEffect::ref).fieldOf("ref").codec()
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val item = ctx.addressableItems[ref] ?: return false
        val spawnPos = Vec3d.ofBottomCenter(pos)
        return ctx.world.spawnEntity(ItemEntity(ctx.world, spawnPos.x, spawnPos.y, spawnPos.z, item))
    }
}