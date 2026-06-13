package com.pfws.supermodelboost.weapon.skill;

import java.util.*;

/**
 * 主动技能注册表 - 定义所有武器主动技能及其参数
 * 
 * 所有技能仅剑(SWORD)和斧(AXE)可获得
 * 
 * @author PFWs
 */
public final class ActiveSkillRegistry {

    public static final String VIOLENT = "violent";
    public static final String ABUNDANCE = "abundance";
    public static final String BLOOD = "blood";

    public static final String HEART_EXPLOSION = "heart_explosion";
    public static final String MADNESS = "madness";
    public static final String AVOID_BATTLE = "avoid_battle";
    public static final String SELF_RESCUE = "self_rescue";
    public static final String SELF_STRENGTHEN = "self_strengthen";
    public static final String ATAVISM = "atavism";

    private static final Map<String, ActiveSkillData> SKILLS = new LinkedHashMap<>();
    private static final Map<String, List<String>> FAMILY_MAP = new HashMap<>();
    private static boolean registered = false;

    private ActiveSkillRegistry() {}

    public static void registerAll() {
        if (registered) return;
        registered = true;

        addSkill(ActiveSkillData.builder(HEART_EXPLOSION, VIOLENT, "心爆")
                .displayName("暴戾·心爆")
                .durations(200, 220, 220, 260, 300)
                .cooldowns(900, 800, 700, 600, 600)
                .effectLevels(1, 1, 2, 2, 3)
                .selfDamages(6, 4, 4, 2, 2)
                .build());

        addSkill(ActiveSkillData.builder(MADNESS, VIOLENT, "失智")
                .displayName("暴戾·失智")
                .durations(800, 900, 1000, 1100, 1200)
                .cooldowns(3600, 3500, 3400, 3300, 3200)
                .effectLevels(2, 2, 1, 1, 1)
                .poisonThresholds(80, 100, 120, 160, 200)
                .strengthMaxLevels(2, 2, 3, 3, 4)
                .selfDamages(40, 40, 60, 80, 100)
                .build());

        addSkill(ActiveSkillData.builder(AVOID_BATTLE, ABUNDANCE, "避战")
                .displayName("丰饶·避战")
                .invisDurations(80, 120, 120, 160, 200)
                .durations(200, 240, 240, 280, 320)
                .effectLevels(1, 1, 2, 2, 3)
                .speedLevels(2, 2, 2, 3, 3)
                .cooldowns(1600, 1600, 1400, 1300, 1300)
                .healthBoostLevels(40, 60, 60, 60, 60)
                .build());

        addSkill(ActiveSkillData.builder(SELF_RESCUE, ABUNDANCE, "自救")
                .displayName("丰饶·自救")
                .durations(600, 700, 800, 1000, 1100)
                .cooldowns(5600, 5600, 5600, 5600, 5600)
                .healPercents(30, 35, 40, 45, 50)
                .healthBoostLevels(2, 3, 4, 5, 6)
                .effectLevels(800, 900, 900, 1000, 1200)
                .regenLevels(2, 2, 3, 3, 3)
                .selfDamages(160, 160, 240, 240, 160)
                .speedLevels(1400, 1400, 1400, 1400, 1400)
                .build());

        addSkill(ActiveSkillData.builder(SELF_STRENGTHEN, BLOOD, "自强")
                .displayName("血使·自强")
                .durations(400, 420, 440, 460, 480)
                .cooldowns(1600, 1560, 1520, 1480, 1440)
                .effectLevels(1, 1, 1, 2, 2)
                .selfDamages(8, 7, 6, 5, 4)
                .healAmounts(1, 1, 3, 3, 5)
                .healthBoostLevels(10, 10, 10, 10, 10)
                .regenLevels(1, 2, 2, 2, 3)
                .speedLevels(400, 400, 400, 400, 400)
                .healPercents(320, 320, 320, 320, 320)
                .poisonThresholds(40, 40, 40, 60, 60)
                .strengthMaxLevels(80, 80, 80, 80, 80)
                .build());

        addSkill(ActiveSkillData.builder(ATAVISM, BLOOD, "返祖")
                .displayName("血使·返祖")
                .durations(800, 800, 800, 800, 800)
                .cooldowns(1800, 1800, 1800, 1800, 1800)
                .sizeScales(1.4f, 1.4f, 1.3f, 1.3f, 1.25f)
                .effectLevels(2, 2, 2, 2, 2)
                .selfDamages(900, 900, 900, 900, 800)
                .healthBoostLevels(2, 2, 2, 3, 3)
                .speedLevels(760, 800, 800, 800, 800)
                .healAmounts(1, 2, 3, 4, 4)
                .build());
    }

    private static void addSkill(ActiveSkillData skill) {
        SKILLS.put(skill.getId(), skill);
        FAMILY_MAP.computeIfAbsent(skill.getFamily(), k -> new ArrayList<>()).add(skill.getId());
    }

    public static Optional<ActiveSkillData> getById(String id) {
        return Optional.ofNullable(SKILLS.get(id));
    }

    public static Collection<ActiveSkillData> getAll() {
        return SKILLS.values();
    }

    public static List<String> getAllIds() {
        return new ArrayList<>(SKILLS.keySet());
    }

    public static List<String> getSkillsByFamily(String family) {
        return FAMILY_MAP.getOrDefault(family, Collections.emptyList());
    }

    public static String getDisplayName(String id) {
        return getById(id).map(ActiveSkillData::getDisplayName).orElse(id);
    }
}
