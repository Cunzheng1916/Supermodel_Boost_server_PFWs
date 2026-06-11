package com.pfws.supermodelboost.mixin;

import com.pfws.supermodelboost.config.ConfigManager;
import com.pfws.supermodelboost.weapon.WeaponTraitNbtHelper;
import com.pfws.supermodelboost.weapon.WeaponTraitRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ItemStack Mixin - 修改耐久度相关行为
 * 
 * @author PFWs
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    /**
     * 柔韧特性 - 增加最大耐久度
     */
    @Inject(method = "getMaxDamage", at = @At("RETURN"), cancellable = true)
    private void modifyMaxDamage(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.isEmpty()) return;

        int flexLv = WeaponTraitNbtHelper.getTraitLevel(self, WeaponTraitRegistry.FLEXIBLE);
        if (flexLv <= 0) return;

        String itemId = BuiltInRegistries.ITEM.getKey(self.getItem()).toString();
        if (!ConfigManager.matchesItemPattern(itemId, ConfigManager.get().weapon.applicableItems)) {
            return;
        }

        int base = cir.getReturnValue();
        ConfigManager.WeaponEffectParams fx = ConfigManager.get().weapon.traitEffects;
        double inc = fx.flexibleMaxDurabilityIncBase + fx.flexibleMaxDurabilityIncPerLevel * flexLv;
        cir.setReturnValue((int) Math.ceil(base * (1.0 + inc)));
    }

    /**
     * 硬朗特性 - 减少耐久消耗
     */
    @ModifyVariable(
            method = "hurtAndBreak",
            at = @At("HEAD"),
            argsOnly = true
    )
    private int modifyDurabilityLoss(int originalDamage) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.isEmpty()) return originalDamage;

        int toughLv = WeaponTraitNbtHelper.getTraitLevel(self, WeaponTraitRegistry.TOUGH);
        if (toughLv <= 0) return originalDamage;

        String itemId = BuiltInRegistries.ITEM.getKey(self.getItem()).toString();
        if (!ConfigManager.matchesItemPattern(itemId, ConfigManager.get().weapon.applicableItems)) {
            return originalDamage;
        }

        ConfigManager.WeaponEffectParams fx = ConfigManager.get().weapon.traitEffects;
        float reduction = (float)(fx.toughDurabilityReduceBase + fx.toughDurabilityReducePerLevel * (toughLv - 1));
        reduction = Math.min(reduction, 0.90f);
        int newDamage = Math.max(1, (int) (originalDamage * (1.0f - reduction)));
        return newDamage;
    }
}