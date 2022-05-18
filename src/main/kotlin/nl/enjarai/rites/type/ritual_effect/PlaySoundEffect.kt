package nl.enjarai.rites.type.ritual_effect

import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.util.RitualContext

class PlaySoundEffect(values: Map<String, Any>) : RitualEffect(values) {
    private val sound: String = getValue(values, "sound")
    private val pitch: Float = getValue(values, "pitch", 10.0).toFloat()
    private val volume: Float = getValue(values, "volume", 1.0).toFloat()

    override fun activate(ritual: Ritual, ctx: RitualContext): Boolean {
        val sPos = Vec3d.ofBottomCenter(ctx.pos)
        ctx.world.playSound(
            null, sPos.x, sPos.y, sPos.z,
            Registry.SOUND_EVENT.get(Identifier.tryParse(ctx.parseVariables(sound))) ?: return false,
            SoundCategory.BLOCKS,
            volume, pitch - 0.2f + ctx.world.getRandom().nextFloat() * 0.4f
        )
        return true
    }
}