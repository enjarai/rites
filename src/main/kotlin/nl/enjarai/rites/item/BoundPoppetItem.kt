package nl.enjarai.rites.item

import com.mojang.authlib.GameProfile
import eu.pb4.polymer.core.api.item.PolymerItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.entity.SkullBlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.Util
import net.minecraft.world.World

class BoundPoppetItem: Item(FabricItemSettings().maxCount(1).maxDamageIfAbsent(10)), PolymerItem {
    override fun getName(stack: ItemStack): Text? {
        if (stack.hasNbt()) {
            var username: String? = null
            val nbt = stack.nbt
            if (nbt!!.contains("Owner", NbtElement.STRING_TYPE.toInt())) {
                username = nbt.getString("Owner")
            } else if (nbt.contains("Owner", NbtElement.COMPOUND_TYPE.toInt())) {
                val gameProfileTag = nbt.getCompound("Owner")
                if (gameProfileTag.contains("Name", NbtElement.STRING_TYPE.toInt())) {
                    username = gameProfileTag.getString("Name")
                }
            }
            if (username != null) {
                return Text.translatable("$translationKey.bound", username)
            }
        }
        return super.getName(stack)
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        val maxDamage = stack.maxDamage
        tooltip.add(Text.translatable("$translationKey.durability_tooltip", maxDamage - stack.damage, maxDamage)
            .formatted(Formatting.GRAY))

        super.appendTooltip(stack, world, tooltip, context)
    }

    override fun postProcessNbt(nbt: NbtCompound) {
        super.postProcessNbt(nbt)
        if (nbt.contains("Owner", NbtElement.STRING_TYPE.toInt())) {
            val username = nbt.getString("Owner");
            if (!Util.isBlank(username)) {
                val gameProfile = GameProfile(null, username)
                SkullBlockEntity.loadProperties(gameProfile) {
                    nbt.put("Owner", NbtHelper.writeGameProfile(NbtCompound(), it))
                }
            }
        }
    }

    override fun getPolymerItem(itemStack: ItemStack, player: ServerPlayerEntity?): Item {
        return Items.PLAYER_HEAD
    }

    // TEMP
    override fun use(world: World?, user: PlayerEntity?, hand: Hand?): TypedActionResult<ItemStack> {
        user?.getStackInHand(hand)?.damage(1, user) { it?.sendToolBreakStatus(hand) }
        return TypedActionResult.success(user?.getStackInHand(hand))
    }

    override fun getPolymerItemStack(itemStack: ItemStack, context: TooltipContext, player: ServerPlayerEntity?): ItemStack {
        val stack = super.getPolymerItemStack(itemStack, context, player)
        if (itemStack.hasNbt() && (itemStack.nbt!!.contains("Owner", NbtElement.COMPOUND_TYPE.toInt()) || itemStack.nbt!!.contains("Owner", NbtElement.STRING_TYPE.toInt()))) {
            stack.orCreateNbt.put("SkullOwner", itemStack.nbt!!.get("Owner"))
        }
        return stack
    }
}