package com.pfws.supermodelboost.weapon;

import com.pfws.supermodelboost.SupermodelBoostMod;
import com.pfws.supermodelboost.config.ConfigManager;
import com.pfws.supermodelboost.storage.RetaliationStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 武器特性效果处理器
 * 
 * @author PFWs
 */
public final class WeaponTraitEffectHandler {

    private WeaponTraitEffectHandler() {}

    /**
     * 吸血 - 攻击时恢复造成伤害一定比例的生命
     */
    public static void handleLifesteal(ServerPlayer attacker, float damageAmount, int level) {
        ConfigManager.WeaponEffectParams cfg = ConfigManager.get().weapon.traitEffects;
        int healAmount = (int) Math.ceil(damageAmount * cfg.lifestealHealPercentPerLevel * level);
        if (healAmount > 0) {
            attacker.heal(healAmount);
            SupermodelBoostMod.LOGGER.debug("吸血 Lv.{}: 恢复 {} HP", level, healAmount);
        }
    }

    /**
     * 反击 - 存储受到的伤害
     */
    public static void handleRetaliationStore(ServerPlayer victim, float damageAmount, int level) {
        RetaliationStorage.storeDamage(victim, damageAmount);
        SupermodelBoostMod.LOGGER.debug("反击 Lv.{}: 储存伤害 {} (总计: {})",
                level, damageAmount, RetaliationStorage.getStoredDamage(victim));
    }

    /**
     * 反击 - 释放储存的伤害
     * @return 额外伤害值
     */
    public static float handleRetaliationRelease(ServerPlayer attacker, int level) {
        float stored = RetaliationStorage.consumeDamage(attacker);
        if (stored <= 0f) return 0f;
        ConfigManager.WeaponEffectParams cfg = ConfigManager.get().weapon.traitEffects;
        float bonusDamage = stored * (float)(cfg.retaliationReleasePercentBase + cfg.retaliationReleasePercentPerLevel * level);
        SupermodelBoostMod.LOGGER.debug("反击 Lv.{}: 释放额外伤害 {} (储存: {})", level, bonusDamage, stored);
        return bonusDamage;
    }

    /**
     * 战狂 - 攻击时获得力量效果
     */
    public static void handleBerserker(ServerPlayer attacker, int level) {
        ConfigManager.WeaponEffectParams cfg = ConfigManager.get().weapon.traitEffects;
        int duration = cfg.berserkerStrengthDurationPerLevel * (1 + level);
        attacker.addEffect(new MobEffectInstance(MobEffects.STRENGTH, duration, level - 1,
                false, true, true));
    }

    /**
     * 硬朗 - 减少耐久消耗
     */
    public static int handleToughDurability(int originalDamage, int level) {
        ConfigManager.WeaponEffectParams cfg = ConfigManager.get().weapon.traitEffects;
        double reduction = cfg.toughDurabilityReduceBase + cfg.toughDurabilityReducePerLevel * level;
        return (int) Math.max(1, originalDamage * (1.0 - reduction));
    }

    /**
     * 硬朗 - 给工具使用者急迫效果
     */
    public static void handleToughHaste(ServerPlayer player, int level) {
        MobEffectInstance current = player.getEffect(MobEffects.HASTE);
        if (current == null || current.getDuration() < 40) {
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, level - 1,
                    false, false, true));
        }
    }

    /**
     * 兴奋 - 击杀后加速
     */
    public static void handleExhilaration(ServerPlayer attacker, int level) {
        ConfigManager.WeaponEffectParams cfg = ConfigManager.get().weapon.traitEffects;
        int duration = cfg.exhilarationSpeedDurationPerLevel * (1 + level);
        attacker.addEffect(new MobEffectInstance(MobEffects.SPEED, duration, level - 1,
                false, true, true));
    }

    /**
     * 千手观音 - 镐3x3挖掘
     */
    public static void handleMultiToolPickaxe(ServerPlayer player, BlockPos pos, Direction facing, int level) {
        Level world = player.level();
        BlockPos.MutableBlockPos mutablePos = pos.mutable();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;

                int x, y, z;
                switch (facing) {
                    case UP, DOWN -> { x = pos.getX() + dx; y = pos.getY(); z = pos.getZ() + dy; }
                    case NORTH, SOUTH -> { x = pos.getX() + dx; y = pos.getY() + dy; z = pos.getZ(); }
                    default -> { x = pos.getX(); y = pos.getY() + dy; z = pos.getZ() + dx; }
                }

                mutablePos.set(x, y, z);
                if (!world.isEmptyBlock(mutablePos) && player.hasCorrectToolForDrops(world.getBlockState(mutablePos))) {
                    world.destroyBlock(mutablePos, true, player);
                }
            }
        }
    }

    /**
     * 千手观音 - 铲获得急迫
     */
    public static void handleMultiToolShovel(ServerPlayer player, int level) {
        player.addEffect(new MobEffectInstance(MobEffects.HASTE, 40, level - 1,
                false, false, true));
    }

    /**
     * 检查主手物品是否有某特性
     */
    public static boolean hasTraitOnMainHand(ServerPlayer player, String traitId) {
        return WeaponTraitNbtHelper.hasTrait(player.getMainHandItem(), traitId);
    }

    /**
     * 获取主手物品某特性等级
     */
    public static int getTraitLevelOnMainHand(ServerPlayer player, String traitId) {
        return WeaponTraitNbtHelper.getTraitLevel(player.getMainHandItem(), traitId);
    }
}
