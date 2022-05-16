package nl.enjarai.rites.type.ritual_effect

import net.minecraft.entity.ItemEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import nl.enjarai.rites.type.Ritual

class ReturnItemEffect(values: HashMap<String, Any>) : RitualEffect(values) {
    private val item = Registry.ITEM.get(getIdNullSafe(values["item"] as? String)) ?:
        throw IllegalArgumentException("Invalid return item/no item given")
    private val count = values["count"] as? Int ?: 1

    override fun activate(world: World, pos: BlockPos, ritual: Ritual): Boolean {
        val spawnPos = Vec3d.ofBottomCenter(pos)
        val itemStack = item.defaultStack
        itemStack.count = count
        world.spawnEntity(ItemEntity(world, spawnPos.x, spawnPos.y, spawnPos.z, itemStack))
        return true
    }
}