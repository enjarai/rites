package nl.enjarai.rites.item

import eu.pb4.polymer.core.api.item.PolymerItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import nl.enjarai.rites.resource.GuideBooks
import nl.enjarai.rites.type.book.GuideBook

class GuideBookItem : Item(FabricItemSettings().maxCount(1)), PolymerItem {
    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if (!world.isClient()) {
            val stack = user.getStackInHand(hand)
            val book = getBook(stack)
            if (book != null) {
                book.open(user as ServerPlayerEntity, stack)
                return TypedActionResult.success(stack)
            }
        }
        return super.use(world, user, hand)
    }

    private fun getBook(stack: ItemStack): GuideBook? {
        return GuideBooks.get(Identifier(stack.orCreateNbt.getString("BookId")))
    }

    override fun hasGlint(stack: ItemStack): Boolean {
        return false
    }

    override fun getPolymerItem(itemStack: ItemStack, player: ServerPlayerEntity?): Item {
        return Items.BOOK // TODO
    }
}