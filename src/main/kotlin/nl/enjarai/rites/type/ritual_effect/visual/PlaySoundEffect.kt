package nl.enjarai.rites.type.ritual_effect.visual

import net.minecraft.registry.Registries
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class PlaySoundEffect : RitualEffect() {
    @FromJson
    private lateinit var sound: Identifier
    @FromJson
    private val pitch: InterpretedNumber = ConstantNumber(10.0)
    @FromJson
    private val volume: InterpretedNumber = ConstantNumber(1.0)

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val sPos = Vec3d.ofBottomCenter(pos)
        ctx.world.playSound(
            null, sPos.x, sPos.y, sPos.z,
            Registries.SOUND_EVENT.get(sound) ?: return false,
            SoundCategory.BLOCKS,
            volume.interpret(ctx).toFloat(), pitch.interpret(ctx).toFloat() - 0.2f + ctx.world.getRandom().nextFloat() * 0.4f
        )
        return true
    }
}