package com.pfws.supermodelboost.weapon;

import java.util.*;

/**
 * 武器特性注册表 - 定义所有武器特性及其属性
 * 每个特性单独定义，互斥规则、最大等级等全部在此管理
 * 
 * @author PFWs
 */
public final class WeaponTraitRegistry {
    // 特性ID常量
    public static final String LIFESTEAL = "lifesteal";
    public static final String RETALIATION = "retaliation";
    public static final String BERSERKER = "berserker";
    public static final String TOUGH = "tough";
    public static final String FLEXIBLE = "flexible";
    public static final String MULTI_TOOL = "multi_tool";
    public static final String EXHILARATION = "exhilaration";

    private static final Map<String, WeaponTraitData> TRAITS = new LinkedHashMap<>();
    private static boolean registered = false;

    private WeaponTraitRegistry() {}

    /**
     * 注册所有武器特性
     */
    public static void registerAll() {
        if (registered) return;
        registered = true;

        // 吸血: 攻击恢复生命值
        addTrait(new WeaponTraitData(
            LIFESTEAL,
            "吸血",
            3,
            null,
            "攻击时恢复造成伤害一定比例的生命值",
            level -> String.format("攻击恢复造成伤害 %d%% 的生命", 10 * level)
        ));

        // 反击: 储存伤害并反弹
        addTrait(new WeaponTraitData(
            RETALIATION,
            "反击",
            2,
            null,
            "受到伤害时储存部分伤害，下次攻击时额外释放",
            level -> String.format("反弹 %d%% 储存伤害", 30 + 20 * level)
        ));

        // 战狂: 连续攻击加速度
        addTrait(new WeaponTraitData(
            BERSERKER,
            "战狂",
            3,
            null,
            "攻击时获得短暂力量效果，连续攻击时叠加力量层数",
            level -> String.format("攻击获得 %d 秒力量 %d", 1 + level, level)
        ));

        // 硬朗: 减少耐久消耗 (与柔韧互斥)
        addTrait(new WeaponTraitData(
            TOUGH,
            "硬朗",
            2,
            "durability",
            "大幅减少工具耐久消耗",
            level -> String.format("耐久损耗降低 %d%%", (20 + 15 * level))
        ));

        // 柔韧: 增加最大耐久 (与硬朗互斥)
        addTrait(new WeaponTraitData(
            FLEXIBLE,
            "韧性",
            2,
            "durability",
            "增加工具最大耐久度",
            level -> String.format("最大耐久增加 %d%%", (10 + 5 * level))
        ));

        // 千手: 3x3挖掘/急迫
        addTrait(new WeaponTraitData(
            MULTI_TOOL,
            "千手",
            3,
            null,
            "工具可挖掘相邻方块（镐3x3、铲急迫）",
            level -> String.format("镐3x3挖掘 / 铲急迫 Lv.%d", level)
        ));

        // 兴奋: 击杀后移动加速
        addTrait(new WeaponTraitData(
            EXHILARATION,
            "兴奋",
            3,
            null,
            "造成伤害后获得短暂移动速度加成",
            level -> String.format("造成伤害后获得 %d 秒速度 %d", 1 + level, level)
        ));
    }

    private static void addTrait(WeaponTraitData trait) {
        TRAITS.put(trait.getId(), trait);
    }

    // ========== 查询方法 ==========

    public static Optional<WeaponTraitData> getById(String id) {
        return Optional.ofNullable(TRAITS.get(id));
    }

    public static Collection<WeaponTraitData> getAll() {
        return TRAITS.values();
    }

    public static List<String> getAllIds() {
        return new ArrayList<>(TRAITS.keySet());
    }

    public static int getMaxLevel(String traitId) {
        return getById(traitId).map(WeaponTraitData::getMaxLevel).orElse(1);
    }

    public static String getDisplayName(String traitId) {
        return getById(traitId).map(WeaponTraitData::getDisplayName).orElse(traitId);
    }

    public static String getDescription(String traitId) {
        return getById(traitId).map(WeaponTraitData::getDescription).orElse("未知特性");
    }

    /**
     * 获取与当前已有特性列表中互斥的特性ID集合
     */
    public static Set<String> getConflictIds(Collection<String> existingIds) {
        Set<String> conflicts = new HashSet<>();
        for (String id : existingIds) {
            WeaponTraitData trait = TRAITS.get(id);
            if (trait != null && trait.getConflictGroup() != null && !trait.getConflictGroup().isEmpty()) {
                // 找出所有同组的其他特性ID
                for (WeaponTraitData other : TRAITS.values()) {
                    if (!other.getId().equals(id) && trait.getConflictGroup().equals(other.getConflictGroup())) {
                        conflicts.add(other.getId());
                    }
                }
            }
        }
        return conflicts;
    }

    /**
     * 检查新特性ID是否与已有特性冲突
     */
    public static boolean isConflict(String newId, Collection<String> existingIds) {
        return getConflictIds(existingIds).contains(newId);
    }

    /**
     * 判断是否为工具类物品
     */
    public static boolean isTool(String itemId) {
        return itemId.contains("pickaxe") || itemId.contains("_axe") || itemId.contains("shovel");
    }

    /**
     * 判断是否为武器类物品
     */
    public static boolean isWeapon(String itemId) {
        return itemId.contains("sword") || itemId.contains("_axe") || itemId.contains("trident") || itemId.contains("mace");
    }

    /**
     * 判断是否为镐
     */
    public static boolean isPickaxe(String itemId) {
        return itemId.contains("pickaxe");
    }

    /**
     * 判断是否为铲
     */
    public static boolean isShovel(String itemId) {
        return itemId.contains("shovel");
    }

    /**
     * 判断是否为斧
     */
    public static boolean isAxe(String itemId) {
        return itemId.contains("_axe");
    }
}
