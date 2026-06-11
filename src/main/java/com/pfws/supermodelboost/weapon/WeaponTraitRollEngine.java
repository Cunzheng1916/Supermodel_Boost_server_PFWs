package com.pfws.supermodelboost.weapon;

import net.minecraft.util.Mth;
import java.util.*;

/**
 * 武器特性抽取引擎 - 随机判定特性数量和等级
 * 
 * @author PFWs
 */
public final class WeaponTraitRollEngine {
    private final Random random = new Random();

    /**
     * 随机生成武器特性列表
     * 
     * @param existingIds 已有特性ID列表（避免重复）
     * @param probabilities 每条特性的概率数组
     * @param excludeConflict 是否排除互斥特性
     * @return 生成的特性实例列表
     */
    public List<WeaponTraitInstance> rollTraits(List<String> existingIds,
                                                  double[] probabilities,
                                                  boolean excludeConflict) {
        List<WeaponTraitInstance> result = new ArrayList<>();
        Set<String> usedIds = new HashSet<>(existingIds);
        List<String> availablePool = new ArrayList<>(WeaponTraitRegistry.getAllIds());

        // 移除已有特性
        availablePool.removeAll(usedIds);

        // 排除互斥特性
        if (excludeConflict) {
            Set<String> conflictIds = WeaponTraitRegistry.getConflictIds(usedIds);
            availablePool.removeAll(conflictIds);
        }

        // 按概率逐条判定
        for (int i = 0; i < probabilities.length; i++) {
            if (availablePool.isEmpty()) break;

            if (random.nextDouble() >= probabilities[i]) {
                continue; // 未通过概率判定
            }

            // 随机选一条
            String pickedId = availablePool.get(random.nextInt(availablePool.size()));
            int level = rollLevel();
            int maxLv = WeaponTraitRegistry.getMaxLevel(pickedId);
            level = Mth.clamp(level, 1, maxLv);

            WeaponTraitInstance trait = new WeaponTraitInstance(pickedId, level);

            // 再次检查互斥（与本次已选的特性互斥检查）
            if (excludeConflict && WeaponTraitRegistry.isConflict(pickedId, usedIds)) {
                continue;
            }

            result.add(trait);
            usedIds.add(pickedId);
            availablePool.remove(pickedId);

            // 移除与新特性互斥的其他特性
            if (excludeConflict) {
                Set<String> newConflicts = WeaponTraitRegistry.getConflictIds(Set.of(pickedId));
                availablePool.removeAll(newConflicts);
            }
        }

        return result;
    }

    /**
     * 随机等级: Lv1=60%, Lv2=30%, Lv3=10%
     */
    public int rollLevel() {
        int rand = random.nextInt(100) + 1; // [1, 100]
        if (rand <= 10) return 3;
        if (rand <= 40) return 2;
        return 1;
    }

    /**
     * 自定义等级概率
     */
    public int rollLevel(double level3Chance, double level2Chance) {
        double rand = random.nextDouble();
        if (rand < level3Chance) return 3;
        if (rand < level3Chance + level2Chance) return 2;
        return 1;
    }

    /**
     * 随机选择一条可升级的特性进行升级
     * @return 被升级的特性新实例，或empty表示无特性可升级
     */
    public Optional<WeaponTraitInstance> rollUpgrade(List<WeaponTraitInstance> currentTraits) {
        List<WeaponTraitInstance> candidates = new ArrayList<>();
        for (WeaponTraitInstance t : currentTraits) {
            int maxLv = WeaponTraitRegistry.getMaxLevel(t.getId());
            if (t.getLevel() < maxLv) {
                candidates.add(t);
            }
        }
        if (candidates.isEmpty()) return Optional.empty();
        WeaponTraitInstance picked = candidates.get(random.nextInt(candidates.size()));
        return Optional.of(new WeaponTraitInstance(picked.getId(), picked.getLevel() + 1));
    }
}
