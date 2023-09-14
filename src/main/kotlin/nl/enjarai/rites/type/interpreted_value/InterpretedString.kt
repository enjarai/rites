package nl.enjarai.rites.type.interpreted_value

import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.StringNbtReader
import nl.enjarai.rites.type.RitualContext

interface InterpretedString : InterpretedValue<String> {
    companion object {
        val CODEC: Codec<InterpretedString> = Codec.STRING.flatXmap({ string ->
            try {
                DataResult.success(InterpolatedString(string))
            } catch (e: RuntimeException) {
                DataResult.error { "Invalid expression: '$string', ${e.message}" }
            }
        }, { interpretedString ->
            when (interpretedString) {
                is ConstantString -> {
                    DataResult.success(interpretedString.value.replace("$", "\\$"))
                }
                is InterpolatedString -> {
                    DataResult.success(interpretedString.inputString)
                }
                else -> {
                    DataResult.error { "Invalid interpreted string: $interpretedString" }
                }
            }
        })
    }

    fun interpretAsNbt(ctx: RitualContext): NbtCompound? {
        return try {
            StringNbtReader.parse(interpret(ctx))
        } catch (e: CommandSyntaxException) {
            null
        }
    }
}
