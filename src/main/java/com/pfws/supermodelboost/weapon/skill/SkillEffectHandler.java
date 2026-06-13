package com.pfws.supermodelboost.weapon.skill;

import com.pfws.supermodelboost.SupermodelBoostMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

/**
 * 主动技能效果处理器
 * 
 * @author PFWs
 */
public final class SkillEffectHandler {

    private SkillEffectHandler() {}

    public static void executeHeartExplosion(ServerPlayer player, ActiveSkillInstance skill, ItemStack weapon) {
        ActiveSkillData data = ActiveSkillRegistry.getById(skill.skillId()).orElse(null);
        if (data == null) return;
        int lv = skill.getLevel();

        int duration = data.getDuration(lv);
        int effectLv = data.getEffectLevel(lv);
        int selfDmg = data.getSelfDamage(lv);

        player.addEffect(new MobEffectInstance(
                MobEffects.STRENGTH, duration, effectLv - 1, false, true, true));

        if (selfDmg > 0) {
            player.setHealth(Math.max(1, player.getHealth() - selfDmg));
        }

        SupermodelBoostMod.LOGGER.info("心爆 Lv.{}: {}血量, 力量{} {}s", lv, selfDmg, effectLv, duration / 20);
    }

    public static void executeMadness(ServerPlayer player, ActiveSkillInstance skill, ItemStack weapon) {
        ActiveSkillData data = ActiveSkillRegistry.getById(skill.skillId()).orElse(null);
        if (data == null) return;
        int lv = skill.getLevel();
        int duration = data.getDuration(lv);

        SkillStateTracker.startMadness(player, skill, duration);

        SupermodelBoostMod.LOGGER.info("失智 Lv.{}: 持续{}s", lv, duration / 20);
    }

    public static void executeAvoidBattle(ServerPlayer player, ActiveSkillInstance skill, ItemStack weapon) {
        ActiveSkillData data = ActiveSkillRegistry.getById(skill.skillId()).orElse(null);
        if (data == null) return;
        int lv = skill.getLevel();

        int invisDuration = data.getInvisDuration(lv);
        int regenDuration = data.getDuration(lv);
        int regenLevel = data.getEffectLevel(lv);
        int speedLevel = data.getSpeedLevel(lv);
        int speedDuration = data.getHealthBoostLevel(lv);

        player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, invisDuration, 0, false, true, true));
        player.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION, regenDuration, regenLevel - 1, false, true, true));
        player.addEffect(new MobEffectInstance(
                MobEffects.SPEED, speedDuration, speedLevel - 1, false, true, true));

        SupermodelBoostMod.LOGGER.info("避战 Lv.{}: 隐身{}s, 生命恢复{} {}s, 速度{} {}s",
                lv, invisDuration / 20, regenLevel, regenDuration / 20, speedLevel, speedDuration / 20);
    }

    public static void executeSelfRescue(ServerPlayer player, ActiveSkillInstance skill, ItemStack weapon) {
        ActiveSkillData data = ActiveSkillRegistry.getById(skill.skillId()).orElse(null);
        if (data == null) return;
        int lv = skill.getLevel();
        int duration = data.getDuration(lv);

        SkillStateTracker.startSelfRescue(player, skill, duration);

        SupermodelBoostMod.LOGGER.info("自救 Lv.{}: 持续{}s", lv, duration / 20);
    }

    public static void executeSelfStrengthen(ServerPlayer player, ActiveSkillInstance skill, ItemStack weapon) {
        ActiveSkillData data = ActiveSkillRegistry.getById(skill.skillId()).orElse(null);
        if (data == null) return;
        int lv = skill.getLevel();

        int selfDmg = data.getSelfDamage(lv);
        int strLevel = data.getEffectLevel(lv);
        int strDuration = data.getSpeedLevel(lv);
        int duration = data.getDuration(lv);
        int hpBoostLevel = data.getHealthBoostLevel(lv);
        int hpBoostDuration = data.getHealPercent(lv);
        int regenLevel = data.getRegenLevel(lv);
        int regenDuration = data.getPoisonThreshold(lv);
        int delayTicks = data.getStrengthMaxLevel(lv);

        if (selfDmg > 0) {
            player.setHealth(Math.max(1, player.getHealth() - selfDmg));
        }

        player.addEffect(new MobEffectInstance(
                MobEffects.STRENGTH, strDuration, strLevel - 1, false, true, true));

        SkillStateTracker.startSelfStrengthen(player, skill, duration, delayTicks,
                hpBoostLevel, hpBoostDuration, regenLevel, regenDuration);

        SupermodelBoostMod.LOGGER.info("自强 Lv.{}: 扣{}血, 力量{} {}s, 延迟{}s后生命提升{}+恢复{}",
                lv, selfDmg, strLevel, strDuration / 20, delayTicks / 20, hpBoostLevel, regenLevel);
    }

    public static void executeAtavism(ServerPlayer player, ActiveSkillInstance skill, ItemStack weapon) {
        ActiveSkillData data = ActiveSkillRegistry.getById(skill.skillId()).orElse(null);
        if (data == null) return;
        int lv = skill.getLevel();

        float scale = data.getSizeScale(lv);
        int slowLevel = data.getEffectLevel(lv);
        int slowDuration = data.getSelfDamage(lv);
        int strLevel = data.getHealthBoostLevel(lv);
        int strDuration = data.getSpeedLevel(lv);
        int duration = data.getDuration(lv);

        SkillStateTracker.startAtavism(player, skill, duration, scale,
                slowLevel, slowDuration, strLevel, strDuration);

        SupermodelBoostMod.LOGGER.info("返祖 Lv.{}: 体型x{}, 缓慢{} {}s, 力量{} {}s",
                lv, scale, slowLevel, slowDuration / 20, strLevel, strDuration / 20);
    }

    public static void executeSkill(ServerPlayer player, ActiveSkillInstance skill, ItemStack weapon) {
        switch (skill.skillId()) {
            case ActiveSkillRegistry.HEART_EXPLOSION -> executeHeartExplosion(player, skill, weapon);
            case ActiveSkillRegistry.MADNESS -> executeMadness(player, skill, weapon);
            case ActiveSkillRegistry.AVOID_BATTLE -> executeAvoidBattle(player, skill, weapon);
            case ActiveSkillRegistry.SELF_RESCUE -> executeSelfRescue(player, skill, weapon);
            case ActiveSkillRegistry.SELF_STRENGTHEN -> executeSelfStrengthen(player, skill, weapon);
            case ActiveSkillRegistry.ATAVISM -> executeAtavism(player, skill, weapon);
            default -> SupermodelBoostMod.LOGGER.warn("未知技能: {}", skill.skillId());
        }
    }
}
