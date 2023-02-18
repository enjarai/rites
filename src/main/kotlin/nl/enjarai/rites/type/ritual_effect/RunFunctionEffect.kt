package nl.enjarai.rites.type.ritual_effect

import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext

class RunFunctionEffect : RitualEffect() {
    @FromJson
    private lateinit var function: Identifier

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val funManager = ctx.world.server!!.commandFunctionManager
        val functionObj = funManager.getFunction(function).orElse(null) ?: return false
        val source = ServerCommandSource(
            ctx.world.server, Vec3d.ofBottomCenter(pos), Vec2f.ZERO, ctx.world as ServerWorld,
            4, "Ritual", Text.literal("Ritual"), ctx.world.server, null
        )

        funManager.execute(functionObj, source)

        return true
    }
}