package com.pfws.supermodelboost.weapon.skill;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.*;

/**
 * 技能状态追踪器 - 管理持续型技能状态
 * 
 * @author PFWs
 */
public final class SkillStateTracker {

    private static final Map<UUID, MadnessState> MADNESS_STATES = new HashMap<>();
    private static final Map<UUID, SelfRescueState> SELF_RESCUE_STATES = new HashMap<>();
    private static final Map<UUID, BloodHealState> BLOOD_HEAL_STATES = new HashMap<>();
    private static final Map<UUID, SkillEndState> SKILL_END_STATES = new HashMap<>();
    private static final Map<UUID, SelfStrengthenState> SELF_STRENGTHEN_STATES = new HashMap<>();
    private static final Map<UUID, AtavismState> ATAVISM_STATES = new HashMap<>();

    private static final Identifier ATAVISM_SCALE_ID = Identifier.tryParse("supermodel_boost_server_pfws:atavism_scale");

    private SkillStateTracker() {}

    // ========== 失智 ==========

    public static void startMadness(ServerPlayer player, ActiveSkillInstance skill, int duration) {
        MADNESS_STATES.put(player.getUUID(), new MadnessState(skill, duration, player.level().getGameTime()));
    }

    public static MadnessState getMadnessState(UUID uuid) {
        return MADNESS_STATES.get(uuid);
    }

    public static void recordMadnessHit(ServerPlayer player) {
        MadnessState state = MADNESS_STATES.get(player.getUUID());
        if (state != null) {
            state.recordHit(player.level().getGameTime());
        }
    }

    // ========== 自救 ==========

    public static void startSelfRescue(ServerPlayer player, ActiveSkillInstance skill, int duration) {
        SELF_RESCUE_STATES.put(player.getUUID(), new SelfRescueState(skill, duration, player.level().getGameTime()));
    }

    public static SelfRescueState getSelfRescueState(UUID uuid) {
        return SELF_RESCUE_STATES.get(uuid);
    }

    // ========== 血使回血 ==========

    public static void startBloodHeal(ServerPlayer player, ActiveSkillInstance skill, int duration) {
        BLOOD_HEAL_STATES.put(player.getUUID(), new BloodHealState(skill, duration, player.level().getGameTime()));
    }

    public static BloodHealState getBloodHealState(UUID uuid) {
        return BLOOD_HEAL_STATES.get(uuid);
    }

    // ========== 自强 ==========

    public static void startSelfStrengthen(ServerPlayer player, ActiveSkillInstance skill,
            int duration, int delayTicks, int hpBoostLevel, int hpBoostDuration,
            int regenLevel, int regenDuration) {
        long gameTime = player.level().getGameTime();
        SELF_STRENGTHEN_STATES.put(player.getUUID(), new SelfStrengthenState(
                skill, duration, gameTime, delayTicks, hpBoostLevel, hpBoostDuration, regenLevel, regenDuration));
        BLOOD_HEAL_STATES.put(player.getUUID(), new BloodHealState(skill, duration, gameTime));
    }

    public static SelfStrengthenState getSelfStrengthenState(UUID uuid) {
        return SELF_STRENGTHEN_STATES.get(uuid);
    }

    public static void tickSelfStrengthen(ServerPlayer player, long gameTime) {
        SelfStrengthenState state = SELF_STRENGTHEN_STATES.get(player.getUUID());
        if (state == null) return;
        if (state.isExpired(gameTime)) {
            SELF_STRENGTHEN_STATES.remove(player.getUUID());
            return;
        }
        if (!state.delayedApplied) {
            long elapsed = gameTime - state.startTime;
            if (elapsed >= state.delayTicks) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.HEALTH_BOOST, state.hpBoostDuration, state.hpBoostLevel - 1, false, true, true));
                player.addEffect(new MobEffectInstance(
                        MobEffects.REGENERATION, state.regenDuration, state.regenLevel - 1, false, true, true));
                state.delayedApplied = true;
            }
        }
    }

    // ========== 返祖 ==========

    public static void startAtavism(ServerPlayer player, ActiveSkillInstance skill,
            int duration, float scale, int slowLevel, int slowDuration,
            int strLevel, int strDuration) {
        long gameTime = player.level().getGameTime();
        ATAVISM_STATES.put(player.getUUID(), new AtavismState(
                skill, duration, gameTime, scale, slowLevel, slowDuration, strLevel, strDuration));
        BLOOD_HEAL_STATES.put(player.getUUID(), new BloodHealState(skill, duration, gameTime));

        var scaleAttr = player.getAttribute(Attributes.SCALE);
        if (scaleAttr != null) {
            scaleAttr.removeModifier(ATAVISM_SCALE_ID);
            scaleAttr.addTransientModifier(new AttributeModifier(
                    ATAVISM_SCALE_ID, scale - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }

        player.addEffect(new MobEffectInstance(
                MobEffects.SLOWNESS, slowDuration, slowLevel - 1, false, true, true));
        player.addEffect(new MobEffectInstance(
                MobEffects.STRENGTH, strDuration, strLevel - 1, false, true, true));
    }

    public static AtavismState getAtavismState(UUID uuid) {
        return ATAVISM_STATES.get(uuid);
    }

    public static void tickAtavism(ServerPlayer player, long gameTime) {
        AtavismState state = ATAVISM_STATES.get(player.getUUID());
        if (state == null) return;
        if (state.isExpired(gameTime)) {
            ATAVISM_STATES.remove(player.getUUID());
            var scaleAttr = player.getAttribute(Attributes.SCALE);
            if (scaleAttr != null) {
                scaleAttr.removeModifier(ATAVISM_SCALE_ID);
            }
        }
    }

    // ========== 技能结束提示 ==========

    public static void notifySkillEnd(ServerPlayer player, String skillId, int level) {
        ActiveSkillData data = ActiveSkillRegistry.getById(skillId).orElse(null);
        if (data == null) return;
        String name = data.getSubName();
        SKILL_END_STATES.put(player.getUUID(), new SkillEndState(name));
    }

    // ========== Tick更新 ==========

    public static void onServerTick(long gameTime) {
        MADNESS_STATES.entrySet().removeIf(e -> e.getValue().isExpired(gameTime));
        SELF_RESCUE_STATES.entrySet().removeIf(e -> e.getValue().isExpired(gameTime));
        BLOOD_HEAL_STATES.entrySet().removeIf(e -> e.getValue().isExpired(gameTime));
    }

    public static void tickMadnessPoison(ServerPlayer player, long gameTime) {
        MadnessState state = MADNESS_STATES.get(player.getUUID());
        if (state == null || state.isExpired(gameTime)) return;

        ActiveSkillData data = ActiveSkillRegistry.getById(state.skill.skillId()).orElse(null);
        if (data == null) return;
        int lv = state.skill.getLevel();
        int threshold = data.getPoisonThreshold(lv);
        int poisonLevel = data.getEffectLevel(lv);
        int strengthTick = data.getSelfDamage(lv);
        int maxStrength = data.getStrengthMaxLevel(lv);

        long timeSinceLastHit = gameTime - state.lastHitTime;
        if (timeSinceLastHit >= threshold) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.POISON, 40, poisonLevel - 1, false, true, true));
        }

        int currentStrengthLevel = state.currentStrengthLevel;
        if (currentStrengthLevel > 0) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.STRENGTH, strengthTick, Math.min(currentStrengthLevel - 1, maxStrength - 1),
                    false, true, true));
        }
    }

    public static void tickSelfRescue(ServerPlayer player, long gameTime) {
        SelfRescueState state = SELF_RESCUE_STATES.get(player.getUUID());
        if (state == null || state.isExpired(gameTime) || state.triggered) return;
        if (player.isDeadOrDying()) return;
        float healthPercent = player.getHealth() / player.getMaxHealth();
        if (healthPercent <= 0.2f && player.getHealth() > 0) {
            triggerSelfRescue(player, state);
        }
    }

    private static void triggerSelfRescue(ServerPlayer player, SelfRescueState state) {
        ActiveSkillData data = ActiveSkillRegistry.getById(state.skill.skillId()).orElse(null);
        if (data == null) return;
        int lv = state.skill.getLevel();

        int healPercent = data.getHealPercent(lv);
        int healthBoostLevel = data.getHealthBoostLevel(lv);
        int healthBoostDuration = data.getEffectLevel(lv);
        int regenLevel = data.getRegenLevel(lv);
        int regenDuration = data.getSelfDamage(lv);
        int fireResistDuration = data.getSpeedLevel(lv);

        float newHealth = player.getMaxHealth() * (healPercent / 100.0f);
        player.setHealth(Math.min(newHealth, player.getMaxHealth()));

        player.addEffect(new MobEffectInstance(
                MobEffects.HEALTH_BOOST, healthBoostDuration, healthBoostLevel - 1, false, true, true));
        player.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION, regenDuration, regenLevel - 1, false, true, true));
        player.addEffect(new MobEffectInstance(
                MobEffects.FIRE_RESISTANCE, fireResistDuration, 0, false, true, true));

        state.triggered = true;
    }

    public static void triggerSelfRescueNow(ServerPlayer player, SelfRescueState state) {
        triggerSelfRescue(player, state);
    }

    public static void handleBloodHealOnDamage(ServerPlayer player) {
        BloodHealState state = BLOOD_HEAL_STATES.get(player.getUUID());
        if (state == null || state.isExpired(player.level().getGameTime())) return;

        ActiveSkillData data = ActiveSkillRegistry.getById(state.skill.skillId()).orElse(null);
        if (data == null) return;
        int lv = state.skill.getLevel();
        int healAmount = data.getHealAmount(lv);

        if (healAmount > 0) {
            float newHealth = Math.min(player.getHealth() + healAmount, player.getMaxHealth());
            player.setHealth(newHealth);
        }
    }

    public static void showPendingEndMessages(ServerPlayer player) {
        SkillEndState endState = SKILL_END_STATES.remove(player.getUUID());
        if (endState != null) {
            SkillBossBarManager.showSkillEnded(player, endState.skillName);
        }
    }

    public static void cleanup() {
        MADNESS_STATES.clear();
        SELF_RESCUE_STATES.clear();
        BLOOD_HEAL_STATES.clear();
        SKILL_END_STATES.clear();
        SELF_STRENGTHEN_STATES.clear();
        ATAVISM_STATES.clear();
    }

    // ========== 状态数据类 ==========

    public static class MadnessState {
        public final ActiveSkillInstance skill;
        public final int totalDuration;
        public final long startTime;
        public long lastHitTime;
        public int currentStrengthLevel;

        public MadnessState(ActiveSkillInstance skill, int duration, long gameTime) {
            this.skill = skill;
            this.totalDuration = duration;
            this.startTime = gameTime;
            this.lastHitTime = gameTime;
            this.currentStrengthLevel = 0;
        }

        public boolean isExpired(long gameTime) {
            return gameTime - startTime > totalDuration;
        }

        public void recordHit(long gameTime) {
            this.lastHitTime = gameTime;
            ActiveSkillData data = ActiveSkillRegistry.getById(skill.skillId()).orElse(null);
            if (data != null) {
                int maxStrength = data.getStrengthMaxLevel(skill.getLevel());
                this.currentStrengthLevel = Math.min(this.currentStrengthLevel + 1, maxStrength);
            }
        }
    }

    public static class SelfRescueState {
        public final ActiveSkillInstance skill;
        public final int totalDuration;
        public final long startTime;
        public boolean triggered;

        public SelfRescueState(ActiveSkillInstance skill, int duration, long gameTime) {
            this.skill = skill;
            this.totalDuration = duration;
            this.startTime = gameTime;
            this.triggered = false;
        }

        public boolean isExpired(long gameTime) {
            return gameTime - startTime > totalDuration;
        }
    }

    public static class BloodHealState {
        public final ActiveSkillInstance skill;
        public final int totalDuration;
        public final long startTime;

        public BloodHealState(ActiveSkillInstance skill, int duration, long gameTime) {
            this.skill = skill;
            this.totalDuration = duration;
            this.startTime = gameTime;
        }

        public boolean isExpired(long gameTime) {
            return gameTime - startTime > totalDuration;
        }
    }

    public static class SkillEndState {
        public final String skillName;

        public SkillEndState(String skillName) {
            this.skillName = skillName;
        }
    }

    public static class SelfStrengthenState {
        public final ActiveSkillInstance skill;
        public final int totalDuration;
        public final long startTime;
        public final int delayTicks;
        public final int hpBoostLevel;
        public final int hpBoostDuration;
        public final int regenLevel;
        public final int regenDuration;
        public boolean delayedApplied;

        public SelfStrengthenState(ActiveSkillInstance skill, int duration, long gameTime,
                int delayTicks, int hpBoostLevel, int hpBoostDuration, int regenLevel, int regenDuration) {
            this.skill = skill;
            this.totalDuration = duration;
            this.startTime = gameTime;
            this.delayTicks = delayTicks;
            this.hpBoostLevel = hpBoostLevel;
            this.hpBoostDuration = hpBoostDuration;
            this.regenLevel = regenLevel;
            this.regenDuration = regenDuration;
            this.delayedApplied = false;
        }

        public boolean isExpired(long gameTime) {
            return gameTime - startTime > totalDuration;
        }
    }

    public static class AtavismState {
        public final ActiveSkillInstance skill;
        public final int totalDuration;
        public final long startTime;
        public final float scale;
        public final int slowLevel;
        public final int slowDuration;
        public final int strLevel;
        public final int strDuration;

        public AtavismState(ActiveSkillInstance skill, int duration, long gameTime,
                float scale, int slowLevel, int slowDuration, int strLevel, int strDuration) {
            this.skill = skill;
            this.totalDuration = duration;
            this.startTime = gameTime;
            this.scale = scale;
            this.slowLevel = slowLevel;
            this.slowDuration = slowDuration;
            this.strLevel = strLevel;
            this.strDuration = strDuration;
        }

        public boolean isExpired(long gameTime) {
            return gameTime - startTime > totalDuration;
        }
    }
}
