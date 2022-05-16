package nl.enjarai.rites.type.ritual_effect

import com.mojang.serialization.Lifecycle
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.util.registry.SimpleRegistry
import net.minecraft.world.World
import nl.enjarai.rites.RitesMod
import nl.enjarai.rites.type.Ritual

abstract class RitualEffect(values: HashMap<String, Any>) {
    abstract fun activate(world: World, pos: BlockPos, ritual: Ritual): Boolean

    open fun tick(world: World, pos: BlockPos, ritual: Ritual): Boolean {
        return true
    }

    open fun isContinuous(): Boolean {
        return false
    }

    companion object {
        val REGISTRY = SimpleRegistry<(HashMap<String, Any>) -> RitualEffect>(
            RegistryKey.ofRegistry(RitesMod.id("ritual_effects")),
            Lifecycle.experimental(),
            null
        )

        fun registerAll() {
            Registry.register(REGISTRY, RitesMod.id("return_item")) { ReturnItemEffect(it) }
        }

        fun fromHashMap(values: HashMap<String, Any>): RitualEffect? {
            val type = values["type"] as? String ?: return null

            return REGISTRY.get(Identifier.tryParse(type))?.invoke(values)
        }

        fun getIdNullSafe(string: String?): Identifier? {
            return if (string == null) null else Identifier.tryParse(string)
        }
    }
}