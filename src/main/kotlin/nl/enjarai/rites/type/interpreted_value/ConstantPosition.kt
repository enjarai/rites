package nl.enjarai.rites.type.interpreted_value

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class ConstantPosition(x: Double, y: Double, z: Double) :
    ExpressionPosition(ConstantNumber(x), ConstantNumber(y), ConstantNumber(z)) {

    constructor(vec3d: Vec3d) : this(vec3d.x, vec3d.y, vec3d.z)

    constructor(blockPos: BlockPos) : this(Vec3d.of(blockPos))
}