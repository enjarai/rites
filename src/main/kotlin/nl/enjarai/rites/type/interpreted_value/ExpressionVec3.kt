package nl.enjarai.rites.type.interpreted_value

import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.type.RitualContext

open class ExpressionVec3(val x: InterpretedNumber, val y: InterpretedNumber, val z: InterpretedNumber) : InterpretedVec3 {
    override fun interpret(ctx: RitualContext): Vec3d {
        return Vec3d(x.interpret(ctx), y.interpret(ctx), z.interpret(ctx))
    }
}