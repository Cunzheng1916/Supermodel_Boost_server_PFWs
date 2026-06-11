package com.pfws.supermodelboost.armor;

import com.pfws.supermodelboost.config.ConfigManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 护甲特性注册表
 * 注册所有8种护甲特性，提供伤害计算和效果处理
 * 
 * @author PFWs
 */
public final class ArmorTraitRegistry {
    private static final Map<String, ArmorTraitData> TRAITS = new LinkedHashMap<>();
    private static boolean registered = false;

    private ArmorTraitRegistry() {}

    // 特性ID常量
    public static final String IRON_WALL = "iron_wall";
    public static final String VAMPIRE = "vampire";
    public static final String THORNS = "thorns";
    public static final String REVIVE = "revive";
    public static final String BULLETPROOF = "bulletproof";
    public static final String MAGIC_WARD = "magic_ward";
    public static final String STURDY = "sturdy";
    public static final String LIGHTWEIGHT = "lightweight";

    public static void registerAll() {
        if (registered) return;
        registered = true;

        // 铁壁 - 减少受到的所有伤害
        addTrait(new ArmorTraitData(IRON_WALL, "铁壁", 5, "defense",
                level -> String.format("减少 %d%% 受到的伤害", 3 * level)));

        // 吸血 - 攻击恢复生命
        addTrait(new ArmorTraitData(VAMPIRE, "吸血", 5, "heal",
                level -> String.format("攻击恢复造成伤害 %d%% 的生命", 5 * level)));

        // 荆棘 - 反弹近战伤害
        addTrait(new ArmorTraitData(THORNS, "荆棘", 5, "counter",
                level -> String.format("反弹 %d%% 近战伤害给攻击者", 8 * level)));

        // 复苏 - 自动回血
        addTrait(new ArmorTraitData(REVIVE, "复苏", 3, "restore",
                level -> String.format("每 %d 秒恢复 1 点生命值", 60 / Math.max(level, 1))));

        // 箭矢防护 - 减少弹射物伤害
        addTrait(new ArmorTraitData(BULLETPROOF, "箭矢防护", 4, "defense",
                level -> String.format("减少来自弹射物的伤害 %d%%", 10 * level)));

        // 魔御 - 减少魔法伤害
        addTrait(new ArmorTraitData(MAGIC_WARD, "魔御", 4, "defense",
                level -> String.format("减少魔法伤害 %d%%", 12 * level)));

        // 坚韧 - 护甲耐久损耗降低
        addTrait(new ArmorTraitData(STURDY, "坚韧", 3, "durability",
                level -> String.format("护甲耐久损耗降低 %d%%", 15 * level)));

        // 轻盈 - 增加移动速度
        addTrait(new ArmorTraitData(LIGHTWEIGHT, "轻盈", 5, "mobility",
                level -> String.format("增加移动速度 %d%%", 3 * level)));
    }

    private static void addTrait(ArmorTraitData trait) {
        TRAITS.put(trait.getId(), trait);
    }

    // ========== 查询方法 ==========

    public static Optional<ArmorTraitData> getById(String id) {
        return Optional.ofNullable(TRAITS.get(id));
    }

    public static Collection<ArmorTraitData> getAll() {
        return TRAITS.values();
    }

    public static List<String> getAllIds() {
        return new ArrayList<>(TRAITS.keySet());
    }

    /**
     * 获取不与已有特性互斥的可用特性列表
     */
    public static List<ArmorTraitData> getCandidates(List<ArmorTraitInstance> existing) {
        Set<String> conflictGroups = new HashSet<>();
        for (ArmorTraitInstance ti : existing) {
            ArmorTraitData t = TRAITS.get(ti.getId());
            if (t != null && t.getConflictGroup() != null && !t.getConflictGroup().isEmpty()) {
                conflictGroups.add(t.getConflictGroup());
            }
        }
        List<ArmorTraitData> candidates = new ArrayList<>();
        for (ArmorTraitData t : TRAITS.values()) {
            if (t.getConflictGroup() == null || t.getConflictGroup().isEmpty()
                    || !conflictGroups.contains(t.getConflictGroup())) {
                candidates.add(t);
            }
        }
        return candidates;
    }

    /**
     * 检查特性是否与已有特性互斥
     */
    public static boolean conflictsWithExisting(ArmorTraitData trait, List<ArmorTraitInstance> existing) {
        if (trait.getConflictGroup() == null || trait.getConflictGroup().isEmpty()) return false;
        for (ArmorTraitInstance ti : existing) {
            ArmorTraitData t = TRAITS.get(ti.getId());
            if (t != null && trait.getConflictGroup().equals(t.getConflictGroup())) return true;
        }
        return false;
    }

    // ========== 伤害计算 ==========

    /**
     * 计算一件护甲的伤害减免
     */
    public static float computeDamageReduction(ItemStack stack, DamageSource source) {
        List<ArmorTraitInstance> traits = ArmorTraitNbtHelper.getTraits(stack);
        float reduction = 0f;
        ConfigManager.ArmorConfigSection cfg = ConfigManager.get().armor;

        for (ArmorTraitInstance ti : traits) {
            switch (ti.getId()) {
                case IRON_WALL:
                    reduction += cfg.traitEffects.ironWallReductionPerLevel * ti.getLevel();
                    break;
                case BULLETPROOF:
                    if (source.is(DamageTypeTags.IS_PROJECTILE)) {
                        reduction += cfg.traitEffects.bulletproofReductionPerLevel * ti.getLevel();
                    }
                    break;
                case MAGIC_WARD:
                    if (source.is(DamageTypeTags.BYPASSES_ARMOR) || source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                        reduction += cfg.traitEffects.magicWardReductionPerLevel * ti.getLevel();
                    }
                    break;
            }
        }
        return Math.min(reduction, (float) cfg.globalMaxDamageReduction);
    }

    /**
     * 处理受击后的效果（荆棘、吸血）
     */
    public static void applyAfterDamageEffects(ItemStack stack, LivingEntity entity, DamageSource source, float amount) {
        List<ArmorTraitInstance> traits = ArmorTraitNbtHelper.getTraits(stack);
        ConfigManager.ArmorConfigSection cfg = ConfigManager.get().armor;

        for (ArmorTraitInstance ti : traits) {
            switch (ti.getId()) {
                case THORNS:
                    applyThorns(entity, source, amount, ti.getLevel(), cfg);
                    break;
                case VAMPIRE:
                    applyVampire(entity, amount, ti.getLevel(), cfg);
                    break;
            }
        }
    }

    private static void applyThorns(LivingEntity entity, DamageSource source, float amount, int level,
                                     ConfigManager.ArmorConfigSection cfg) {
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity livingAttacker && attacker != entity) {
            float reflect = amount * (float) cfg.traitEffects.thornsReflectPercentPerLevel * level;
            reflect = Math.max(reflect, (float) cfg.traitEffects.thornsMinReflectDamage);
            livingAttacker.hurtServer((ServerLevel) entity.level(),
                    entity.level().damageSources().thorns(entity), reflect);
        }
    }

    private static void applyVampire(LivingEntity entity, float amount, int level,
                                      ConfigManager.ArmorConfigSection cfg) {
        float heal = amount * (float) cfg.traitEffects.vampireHealPercentPerLevel * level;
        if (heal > 0) {
            entity.heal(heal);
        }
    }

    // ========== 轻盈计算 ==========

    /**
     * 计算所有护甲的轻盈总加成
     */
    public static float computeTotalLightweight(Iterable<ItemStack> armorItems) {
        float total = 0f;
        ConfigManager.ArmorConfigSection cfg = ConfigManager.get().armor;
        for (ItemStack stack : armorItems) {
            List<ArmorTraitInstance> traits = ArmorTraitNbtHelper.getTraits(stack);
            for (ArmorTraitInstance ti : traits) {
                if (LIGHTWEIGHT.equals(ti.getId())) {
                    total += (float) cfg.traitEffects.lightweightSpeedPercentPerLevel * ti.getLevel();
                }
            }
        }
        return total;
    }

    /**
     * 刷新轻盈效果（通过给予速度药水效果实现）
     */
    public static void refreshLightweightModifier(Player player, Iterable<ItemStack> armorItems) {
        float total = computeTotalLightweight(armorItems);
        if (total > 0f) {
            int amplifier = Math.max(0, Math.min(9, Math.round(total * 10f) - 1));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, amplifier,
                    false, false, true));
        }
    }
}
