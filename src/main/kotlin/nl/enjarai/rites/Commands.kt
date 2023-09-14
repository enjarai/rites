package nl.enjarai.rites

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.IdentifierArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.resource.CircleTypes

object Commands {
    fun register() {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _, _ ->
            dispatcher.register(literal("rites")
                    .then(literal("circletype")
                            .then(argument("name", IdentifierArgumentType.identifier())
                                    .suggests { _: CommandContext<ServerCommandSource>, builder: SuggestionsBuilder? ->
                                        CommandSource.suggestMatching(
                                            CircleTypes.values.keys.map { it.toString() }, builder
                                        )
                                    }
                                    .executes(this::checkCircleType)
                            )
                    )
            )
        })
    }

    private fun checkCircleType(ctx: CommandContext<ServerCommandSource>): Int {
        val id = ctx.getArgument("name", Identifier::class.java)
        val pos = ctx.source.position
        val valid = CircleTypes.values[id]?.isValid(ctx.source.world, BlockPos(pos.x.toInt(), pos.y.toInt(), pos.z.toInt()), null)
        ctx.source.sendFeedback({ Text.literal("valid: $valid") }, false)
        return 1
    }
}