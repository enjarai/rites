package nl.enjarai.rites.type.ritual_effect.visual

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.registry.Registries
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class PlaySoundEffect(
    val sound: SoundEvent,
    val pitch: InterpretedNumber,
    val volume: InterpretedNumber
) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<PlaySoundEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                Registries.SOUND_EVENT.codec.fieldOf("sound").forGetter { it.sound },
                InterpretedNumber.CODEC.optionalFieldOf("pitch", ConstantNumber(10.0)).forGetter { it.pitch },
                InterpretedNumber.CODEC.optionalFieldOf("volume", ConstantNumber(1.0)).forGetter { it.volume }
            ).apply(instance, ::PlaySoundEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val sPos = Vec3d.ofBottomCenter(pos)
        ctx.world.playSound(
            null, sPos.x, sPos.y, sPos.z,
            sound, SoundCategory.BLOCKS,
            volume.interpret(ctx).toFloat(), pitch.interpret(ctx).toFloat() - 0.2f + ctx.world.getRandom().nextFloat() * 0.4f
        )
        return true
    }
}