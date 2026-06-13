package com.pfws.supermodelboost.weapon.skill;

/**
 * 主动技能实例 - 存储技能ID和等级
 * 
 * @author PFWs
 */
public final class ActiveSkillInstance {
    private final String skillId;
    private final int level;

    public ActiveSkillInstance(String skillId, int level) {
        this.skillId = skillId;
        this.level = Math.max(1, Math.min(level, 5));
    }

    public String skillId() { return skillId; }
    public String getSkillId() { return skillId; }
    public int level() { return level; }
    public int getLevel() { return level; }

    @Override
    public String toString() {
        return "ActiveSkillInstance{skillId='" + skillId + "', level=" + level + "}";
    }
}
