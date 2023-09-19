package nl.enjarai.rites.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nl.enjarai.rites.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(
            method = "copyFrom",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;enchantmentTableSeed:I",
                    ordinal = 0
            )
    )
    private void copySoulBindingItems(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if (!this.getInventory().isEmpty()) return;
        var enchantment = ModItems.INSTANCE.getSOUL_BINDING();
        for (int i = 0; i < this.getInventory().size(); ++i) {
            var stack = oldPlayer.getInventory().getStack(i);
            if (EnchantmentHelper.getLevel(enchantment, stack) == 0) continue;
            rites$downgradeEnchantment(stack, enchantment);
            this.getInventory().setStack(i, stack);
        }
    }

    @Unique
    private static void rites$downgradeEnchantment(ItemStack stack, Enchantment enchantment) {
        var id = EnchantmentHelper.getEnchantmentId(enchantment);
        var nbtEnchantments = stack.getEnchantments();
        NbtElement nbtEnchantmentToRemove = null;
        for (var nbtEnchantment : nbtEnchantments) {
            if (!(nbtEnchantment instanceof NbtCompound enchantmentCompound)) continue;
            var compoundId = EnchantmentHelper.getIdFromNbt(enchantmentCompound);
            if (!Objects.equals(id, compoundId)) continue;
            var level = EnchantmentHelper.getLevelFromNbt(enchantmentCompound);
            if (level == 1) {
                nbtEnchantmentToRemove = nbtEnchantment;
            } else {
                EnchantmentHelper.writeLevelToNbt(enchantmentCompound, level - 1);
            }
            break;
        }
        if (nbtEnchantmentToRemove == null) return;
        nbtEnchantments.remove(nbtEnchantmentToRemove);
    }

}
