package com.pfws.supermodelboost.weapon;

import java.util.function.Function;

/**
 * 武器单条特性数据定义
 * 
 * @param id 特性ID
 * @param displayName 显示名称（中文）
 * @param maxLevel 最大等级
 * @param conflictGroup 互斥组（null表示不互斥）
 * @param description 特性效果描述
 * @param levelDescriber 等级描述函数
 * 
 * @author PFWs
 */
public record WeaponTraitData(
    String id,
    String displayName,
    int maxLevel,
    String conflictGroup,
    String description,
    Function<Integer, String> levelDescriber
) {
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getMaxLevel() { return maxLevel; }
    public String getConflictGroup() { return conflictGroup; }
    public String getDescription() { return description; }

    /**
     * 获取指定等级的效果描述
     */
    public String describe(int level) {
        return levelDescriber.apply(level);
    }
}
