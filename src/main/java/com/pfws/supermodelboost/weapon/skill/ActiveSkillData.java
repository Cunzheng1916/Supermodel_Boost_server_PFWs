package com.pfws.supermodelboost.weapon.skill;

/**
 * 主动技能数据定义 - 包含所有等级的Buff参数
 * 
 * 字段说明（部分字段因不同技能含义不同，会复用）：
 * - durations: 技能持续/主要Buff持续
 * - cooldowns: CD
 * - effectLevels: 主效果等级（心爆=力量，失智=中毒，避战=生命恢复，自救=生命提升持续，返祖=缓慢）
 * - selfDamages: 扣血量 / 自救=生命恢复持续 / 失智=力量每层tick / 返祖=缓慢持续
 * - healAmounts: 每击回血量
 * - healthBoostLevels: 生命提升等级 / 避战=速度持续 / 自救=生命提升等级 / 返祖=力量等级
 * - regenLevels: 生命恢复等级
 * - speedLevels: 速度等级 / 自救=抗火持续 / 自强=力量持续 / 返祖=力量持续
 * - healPercents: 恢复血量百分比 / 自强=生命提升持续
 * - poisonThresholds: 未伤害阈值tick / 自强=生命恢复持续
 * - strengthMaxLevels: 力量最高层数 / 自强=延迟tick
 * - invisDurations: 隐身持续
 * - sizeScales: 体型缩放
 * 
 * @author PFWs
 */
public final class ActiveSkillData {
    private final String id;
    private final String family;
    private final String subName;
    private final String displayName;
    private final int maxLevel;
    private final int[] durations;
    private final int[] cooldowns;
    private final int[] effectLevels;
    private final int[] selfDamages;
    private final int[] healAmounts;
    private final int[] healthBoostLevels;
    private final int[] regenLevels;
    private final int[] speedLevels;
    private final int[] healPercents;
    private final int[] poisonThresholds;
    private final int[] strengthMaxLevels;
    private final int[] invisDurations;
    private final float[] sizeScales;

    private ActiveSkillData(Builder builder) {
        this.id = builder.id;
        this.family = builder.family;
        this.subName = builder.subName;
        this.displayName = builder.displayName;
        this.maxLevel = builder.maxLevel;
        this.durations = builder.durations.clone();
        this.cooldowns = builder.cooldowns.clone();
        this.effectLevels = builder.effectLevels.clone();
        this.selfDamages = builder.selfDamages.clone();
        this.healAmounts = builder.healAmounts.clone();
        this.healthBoostLevels = builder.healthBoostLevels.clone();
        this.regenLevels = builder.regenLevels.clone();
        this.speedLevels = builder.speedLevels.clone();
        this.healPercents = builder.healPercents.clone();
        this.poisonThresholds = builder.poisonThresholds.clone();
        this.strengthMaxLevels = builder.strengthMaxLevels.clone();
        this.invisDurations = builder.invisDurations.clone();
        this.sizeScales = builder.sizeScales.clone();
    }

    public String getId() { return id; }
    public String getFamily() { return family; }
    public String getSubName() { return subName; }
    public String getDisplayName() { return displayName; }
    public int getMaxLevel() { return maxLevel; }

    public int getDuration(int level) { return safeGet(durations, level); }
    public int getCooldown(int level) { return safeGet(cooldowns, level); }
    public int getEffectLevel(int level) { return safeGet(effectLevels, level); }
    public int getSelfDamage(int level) { return safeGet(selfDamages, level); }
    public int getHealAmount(int level) { return safeGet(healAmounts, level); }
    public int getHealthBoostLevel(int level) { return safeGet(healthBoostLevels, level); }
    public int getRegenLevel(int level) { return safeGet(regenLevels, level); }
    public int getSpeedLevel(int level) { return safeGet(speedLevels, level); }
    public int getHealPercent(int level) { return safeGet(healPercents, level); }
    public int getPoisonThreshold(int level) { return safeGet(poisonThresholds, level); }
    public int getStrengthMaxLevel(int level) { return safeGet(strengthMaxLevels, level); }
    public int getInvisDuration(int level) { return safeGet(invisDurations, level); }
    public float getSizeScale(int level) { return safeGetFloat(sizeScales, level); }

    private int safeGet(int[] arr, int level) {
        int idx = Math.clamp(level - 1, 0, arr.length - 1);
        return arr[idx];
    }

    private float safeGetFloat(float[] arr, int level) {
        int idx = Math.clamp(level - 1, 0, arr.length - 1);
        return arr[idx];
    }

    public int getCooldownSeconds(int level) {
        return getCooldown(level) / 20;
    }

    /**
     * 获取指定等级的Lore描述（每个技能格式不同，严格遵循策划案）
     */
    public String describe(int level) {
        int lv = Math.clamp(level, 1, maxLevel);
        return switch (id) {
            case ActiveSkillRegistry.HEART_EXPLOSION -> describeHeartExplosion(lv);
            case ActiveSkillRegistry.MADNESS -> describeMadness(lv);
            case ActiveSkillRegistry.AVOID_BATTLE -> describeAvoidBattle(lv);
            case ActiveSkillRegistry.SELF_RESCUE -> describeSelfRescue(lv);
            case ActiveSkillRegistry.SELF_STRENGTHEN -> describeSelfStrengthen(lv);
            case ActiveSkillRegistry.ATAVISM -> describeAtavism(lv);
            default -> describeDefault(lv);
        };
    }

    private String describeHeartExplosion(int lv) {
        return String.format("获得%d秒力量%s，扣除%d滴血，CD %d秒",
                getDuration(lv) / 20, toChineseNum(getEffectLevel(lv)),
                getSelfDamage(lv), getCooldownSeconds(lv));
    }

    private String describeMadness(int lv) {
        return String.format("%d秒内未造成伤害则中毒%s。每造成一次伤害获得一层%d秒力量（最高%d级），持续%d秒，CD %d秒",
                getPoisonThreshold(lv) / 20, toChineseNum(getEffectLevel(lv)),
                getSelfDamage(lv) / 20, getStrengthMaxLevel(lv),
                getDuration(lv) / 20, getCooldownSeconds(lv));
    }

    private String describeAvoidBattle(int lv) {
        return String.format("获得%d秒隐身，%d秒生命恢复%s，%d秒速度%s，CD %d秒",
                getInvisDuration(lv) / 20, getDuration(lv) / 20, toChineseNum(getEffectLevel(lv)),
                getHealthBoostLevel(lv) / 20, toChineseNum(getSpeedLevel(lv)), getCooldownSeconds(lv));
    }

    private String describeSelfRescue(int lv) {
        return String.format("持续%d秒。期间若濒死，恢复至%d%%血量，获%d秒生命提升%s、%d秒生命恢复%s、%d秒抗火，CD %d秒",
                getDuration(lv) / 20, getHealPercent(lv),
                getEffectLevel(lv) / 20, toChineseNum(getHealthBoostLevel(lv)),
                getSelfDamage(lv) / 20, toChineseNum(getRegenLevel(lv)),
                getSpeedLevel(lv) / 20, getCooldownSeconds(lv));
    }

    private String describeSelfStrengthen(int lv) {
        return String.format("扣除%d点生命值，获得%d秒力量%s，四秒后获得%d秒生命提升%d，获得%d秒生命恢复%s，每次造成伤害恢复%d点血量（持续%d秒）CD %d秒",
                getSelfDamage(lv), getSpeedLevel(lv) / 20, toChineseNum(getEffectLevel(lv)),
                getHealPercent(lv) / 20, getHealthBoostLevel(lv),
                getPoisonThreshold(lv) / 20, toChineseNum(getRegenLevel(lv)),
                getHealAmount(lv), getDuration(lv) / 20, getCooldownSeconds(lv));
    }

    private String describeAtavism(int lv) {
        int scalePercent = Math.round(getSizeScale(lv) * 100 - 100);
        return String.format("%d秒内体型变大%d%%，获得%d秒缓慢%s、%d秒力量%s，每次造成伤害获得%d点血量 CD %d秒",
                getDuration(lv) / 20, scalePercent,
                getSelfDamage(lv) / 20, toChineseNum(getEffectLevel(lv)),
                getSpeedLevel(lv) / 20, toChineseNum(getHealthBoostLevel(lv)),
                getHealAmount(lv), getCooldownSeconds(lv));
    }

    private String describeDefault(int lv) {
        StringBuilder sb = new StringBuilder();
        sb.append("持续 ").append(getDuration(lv) / 20).append("秒 | CD ").append(getCooldownSeconds(lv)).append("秒");
        if (getSelfDamage(lv) > 0) sb.append(" | 扣除 ").append(getSelfDamage(lv)).append("血");
        if (getHealAmount(lv) > 0) sb.append(" | 回复 ").append(getHealAmount(lv)).append("血/次");
        return sb.toString();
    }

    private static String toChineseNum(int n) {
        return switch (n) {
            case 1 -> "一"; case 2 -> "二"; case 3 -> "三";
            case 4 -> "四"; case 5 -> "五"; case 6 -> "六";
            case 7 -> "七"; case 8 -> "八"; case 9 -> "九"; case 10 -> "十";
            default -> String.valueOf(n);
        };
    }

    public static Builder builder(String id, String family, String subName) {
        return new Builder(id, family, subName);
    }

    public static class Builder {
        private final String id;
        private final String family;
        private final String subName;
        private String displayName = "";
        private int maxLevel = 5;
        private int[] durations = new int[5];
        private int[] cooldowns = new int[5];
        private int[] effectLevels = new int[5];
        private int[] selfDamages = new int[5];
        private int[] healAmounts = new int[5];
        private int[] healthBoostLevels = new int[5];
        private int[] regenLevels = new int[5];
        private int[] speedLevels = new int[5];
        private int[] healPercents = new int[5];
        private int[] poisonThresholds = new int[5];
        private int[] strengthMaxLevels = new int[5];
        private int[] invisDurations = new int[5];
        private float[] sizeScales = new float[5];

        public Builder(String id, String family, String subName) {
            this.id = id;
            this.family = family;
            this.subName = subName;
        }

        public Builder displayName(String s) { this.displayName = s; return this; }
        public Builder durations(int... v) { this.durations = v; return this; }
        public Builder cooldowns(int... v) { this.cooldowns = v; return this; }
        public Builder effectLevels(int... v) { this.effectLevels = v; return this; }
        public Builder selfDamages(int... v) { this.selfDamages = v; return this; }
        public Builder healAmounts(int... v) { this.healAmounts = v; return this; }
        public Builder healthBoostLevels(int... v) { this.healthBoostLevels = v; return this; }
        public Builder regenLevels(int... v) { this.regenLevels = v; return this; }
        public Builder speedLevels(int... v) { this.speedLevels = v; return this; }
        public Builder healPercents(int... v) { this.healPercents = v; return this; }
        public Builder poisonThresholds(int... v) { this.poisonThresholds = v; return this; }
        public Builder strengthMaxLevels(int... v) { this.strengthMaxLevels = v; return this; }
        public Builder invisDurations(int... v) { this.invisDurations = v; return this; }
        public Builder sizeScales(float... v) { this.sizeScales = v; return this; }

        public ActiveSkillData build() {
            return new ActiveSkillData(this);
        }
    }
}
