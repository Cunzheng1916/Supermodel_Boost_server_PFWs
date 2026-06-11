package com.pfws.supermodelboost.armor;

import java.util.function.Function;

/**
 * 护甲特性数据定义
 * 
 * @param id 特性ID
 * @param displayName 显示名称（中文）
 * @param maxLevel 最大等级
 * @param conflictGroup 互斥组（null表示不互斥）
 * @param levelDescriber 等级描述函数
 * 
 * @author PFWs
 */
public record ArmorTraitData(
    String id,
    String displayName,
    int maxLevel,
    String conflictGroup,
    Function<Integer, String> levelDescriber
) {
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getMaxLevel() { return maxLevel; }
    public String getConflictGroup() { return conflictGroup; }

    /**
     * 获取指定等级的效果描述
     */
    public String describe(int level) {
        return levelDescriber.apply(level);
    }
}
