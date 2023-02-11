package nl.enjarai.rites.type.interpreted_value

import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.StringNbtReader
import nl.enjarai.rites.type.RitualContext

interface InterpretedString : InterpretedValue<String> {
    fun interpretAsNbt(ctx: RitualContext): NbtCompound? {
        return try {
            StringNbtReader.parse(interpret(ctx))
        } catch (e: CommandSyntaxException) {
            null
        }
    }
}
