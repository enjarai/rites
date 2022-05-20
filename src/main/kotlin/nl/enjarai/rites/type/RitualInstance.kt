package nl.enjarai.rites.type

import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import nl.enjarai.rites.resource.Rituals

class RitualInstance(val ritual: Ritual) {
    var active = false

    fun tick(ctx: RitualContext): RitualResult {
        if (active) {
            return RitualResult.successFromBool(ritual.tick(ctx))
        }
        return RitualResult.PASS
    }

    /**
     * Activate the ritual and start possibly ticking.
     */
    fun activate(ctx: RitualContext): Boolean {
        val success = ritual.activate(ctx)
        if (success) {
            active = true
        }
        return success
    }

    fun toNbt(): NbtCompound {
        val nbt = NbtCompound()
        nbt.putString("ritual", ritual.id.toString())
        nbt.putBoolean("active", active)
        return nbt
    }

    companion object {
        fun fromNbt(nbt: NbtCompound): RitualInstance? {
            val instance = Rituals.values[Identifier.tryParse(nbt.getString("ritual"))]?.let {
                RitualInstance(it)
            } ?: return null

            instance.active = nbt.getBoolean("active")

            return instance
        }
    }
}