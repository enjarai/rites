package nl.enjarai.rites.type.ritual_effect

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.InterpretedString
import java.util.Optional

class RunCommandEffect(val command: InterpretedString, resultVariable: Optional<String>) : RitualEffect(CODEC) {
    val resultVariable: String? = resultVariable.orElse(null)

    companion object {
        val CODEC: Codec<RunCommandEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                InterpretedString.CODEC.fieldOf("command").forGetter { it.command },
                Codec.STRING.optionalFieldOf("result_variable").forGetter { Optional.ofNullable(it.resultVariable) }
            ).apply(instance, ::RunCommandEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val result = ctx.world.server?.commandManager?.executeWithPrefix(getCommandSource(ctx), command.interpret(ctx)) ?: return false
        if (resultVariable != null) {
            ctx.variables[resultVariable] = result.toDouble()
        }
        return true
    }
}