package com.pfws.supermodelboost.armor;

import com.pfws.supermodelboost.config.ConfigManager;

import java.util.*;

/**
 * 护甲特性抽取引擎
 * 
 * @author PFWs
 */
public final class ArmorTraitRollEngine {
    private static final Random RANDOM = new Random();

    private ArmorTraitRollEngine() {}

    /**
     * 抽取特性组
     * @param minTraits 最少特性数
     * @param maxTraits 最多特性数
     * @return 抽取的特性列表
     */
    public static List<ArmorTraitInstance> rollTraitSet(int minTraits, int maxTraits) {
        int count = RANDOM.nextInt(maxTraits - minTraits + 1) + minTraits;
        return rollTraits(count, new ArrayList<>());
    }

    /**
     * 抽取指定数量的特性
     */
    public static List<ArmorTraitInstance> rollTraits(int count, List<ArmorTraitInstance> existing) {
        List<ArmorTraitInstance> chosen = new ArrayList<>(existing);
        Map<String, Integer> weights = ConfigManager.get().armor.traitRollWeights;
        if (weights == null || weights.isEmpty()) return chosen;

        for (int i = chosen.size(); i < count; i++) {
            ArmorTraitInstance next = rollNewTrait(chosen, weights);
            if (next == null) break;
            chosen.add(next);
        }
        return chosen;
    }

    /**
     * 抽取一条新特性（考虑互斥）
     */
    private static ArmorTraitInstance rollNewTrait(List<ArmorTraitInstance> currentTraits, Map<String, Integer> weights) {
        List<String> candidates = new ArrayList<>();
        int totalWeight = 0;

        boolean excludeConflicts = ConfigManager.get().armor.excludeConflicts;

        for (Map.Entry<String, Integer> entry : weights.entrySet()) {
            String id = entry.getKey();
            int weight = entry.getValue();
            if (weight <= 0) continue;

            ArmorTraitData trait = ArmorTraitRegistry.getById(id).orElse(null);
            if (trait == null) continue;

            if (excludeConflicts && ArmorTraitRegistry.conflictsWithExisting(trait, currentTraits)) continue;

            candidates.add(id);
            totalWeight += weight;
        }

        if (candidates.isEmpty()) return null;

        int roll = RANDOM.nextInt(totalWeight);
        int cumulative = 0;
        String chosen = null;
        for (String id : candidates) {
            cumulative += weights.get(id);
            if (roll < cumulative) {
                chosen = id;
                break;
            }
        }
        if (chosen == null) return null;

        ArmorTraitData trait = ArmorTraitRegistry.getById(chosen).orElse(null);
        if (trait == null) return null;
        int level = rollLevel();
        return new ArmorTraitInstance(chosen, Math.min(level, trait.getMaxLevel()));
    }

    /**
     * 随机抽取一个要升级的特性
     */
    public static Optional<ArmorTraitInstance> rollUpgradeTrait(List<ArmorTraitInstance> currentTraits) {
        List<ArmorTraitInstance> candidates = new ArrayList<>();
        for (ArmorTraitInstance t : currentTraits) {
            ArmorTraitData data = ArmorTraitRegistry.getById(t.getId()).orElse(null);
            if (data != null && t.getLevel() < data.getMaxLevel()) {
                candidates.add(t);
            }
        }
        if (candidates.isEmpty()) return Optional.empty();
        return Optional.of(candidates.get(RANDOM.nextInt(candidates.size())));
    }

    /**
     * 随机等级
     */
    public static int rollLevel() {
        Map<String, Double> chances = ConfigManager.get().armor.levelChances;
        if (chances == null || chances.isEmpty()) return 1;
        double roll = RANDOM.nextDouble();
        double cumulative = 0;
        for (int level = 1; level <= 5; level++) {
            cumulative += chances.getOrDefault("level" + level, 0.0);
            if (roll <= cumulative) return level;
        }
        return 1;
    }
}
