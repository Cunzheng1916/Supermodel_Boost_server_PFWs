package com.pfws.supermodelboost.armor;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 护甲特性配置文件数据结构
 * 每个特性单独配置，放在护甲文件夹内
 * 
 * @author PFWs
 */
public class ArmorTraitConfig {
    @SerializedName("enable_armor_forge")
    private boolean enableArmorForge = true;

    @SerializedName("max_traits")
    private int maxTraits = 3;

    @SerializedName("min_traits")
    private int minTraits = 1;

    @SerializedName("exclude_conflicts")
    private boolean excludeConflicts = true;

    @SerializedName("global_max_damage_reduction")
    private double globalMaxDamageReduction = 0.90;

    @SerializedName("trait_roll_weights")
    private Map<String, Integer> traitRollWeights = new HashMap<>();

    @SerializedName("level_chances")
    private Map<String, Double> levelChances = new HashMap<>();

    @SerializedName("applicable_items")
    private List<String> applicableItems = new ArrayList<>();

    // ========== 锻造材料配置 ==========

    @SerializedName("reset_material")
    private String resetMaterial = "minecraft:lapis_lazuli";

    @SerializedName("upgrade_material")
    private String upgradeMaterial = "minecraft:diamond";

    @SerializedName("upgrade_corner_material")
    private String upgradeCornerMaterial = "minecraft:lapis_lazuli";

    @SerializedName("recast_material")
    private String recastMaterial = "minecraft:netherite_ingot";

    @SerializedName("recast_corner_material")
    private String recastCornerMaterial = "minecraft:diamond";

    // ========== 各特性效果配置 ==========

    @SerializedName("iron_wall")
    private IronWallConfig ironWall = new IronWallConfig();

    @SerializedName("vampire")
    private VampireConfig vampire = new VampireConfig();

    @SerializedName("thorns")
    private ThornsConfig thorns = new ThornsConfig();

    @SerializedName("revive")
    private ReviveConfig revive = new ReviveConfig();

    @SerializedName("bulletproof")
    private BulletproofConfig bulletproof = new BulletproofConfig();

    @SerializedName("magic_ward")
    private MagicWardConfig magicWard = new MagicWardConfig();

    @SerializedName("sturdy")
    private SturdyConfig sturdy = new SturdyConfig();

    @SerializedName("lightweight")
    private LightweightConfig lightweight = new LightweightConfig();

    // ========== 特性效果子配置类 ==========

    public static class IronWallConfig {
        @SerializedName("max_level")
        public int maxLevel = 5;

        @SerializedName("damage_reduction_per_level")
        public double damageReductionPerLevel = 0.03;

        @SerializedName("description")
        public String description = "减少所有类型受到的伤害";
    }

    public static class VampireConfig {
        @SerializedName("max_level")
        public int maxLevel = 5;

        @SerializedName("heal_percent_per_level")
        public double healPercentPerLevel = 0.05;

        @SerializedName("description")
        public String description = "攻击时恢复造成伤害一定比例的生命";
    }

    public static class ThornsConfig {
        @SerializedName("max_level")
        public int maxLevel = 5;

        @SerializedName("reflect_percent_per_level")
        public double reflectPercentPerLevel = 0.08;

        @SerializedName("min_reflect_damage")
        public double minReflectDamage = 1.0;

        @SerializedName("description")
        public String description = "反弹部分近战伤害给攻击者";
    }

    public static class ReviveConfig {
        @SerializedName("max_level")
        public int maxLevel = 3;

        @SerializedName("base_interval_ticks")
        public int baseIntervalTicks = 1200;

        @SerializedName("heal_amount")
        public double healAmount = 1.0;

        @SerializedName("description")
        public String description = "每隔一段时间自动恢复生命";
    }

    public static class BulletproofConfig {
        @SerializedName("max_level")
        public int maxLevel = 4;

        @SerializedName("projectile_reduction_per_level")
        public double projectileReductionPerLevel = 0.10;

        @SerializedName("description")
        public String description = "减少来自弹射物的伤害";
    }

    public static class MagicWardConfig {
        @SerializedName("max_level")
        public int maxLevel = 4;

        @SerializedName("magic_reduction_per_level")
        public double magicReductionPerLevel = 0.12;

        @SerializedName("description")
        public String description = "减少魔法伤害";
    }

    public static class SturdyConfig {
        @SerializedName("max_level")
        public int maxLevel = 3;

        @SerializedName("repair_amount_per_level")
        public int repairAmountPerLevel = 1;

        @SerializedName("description")
        public String description = "护甲耐久损耗降低";
    }

    public static class LightweightConfig {
        @SerializedName("max_level")
        public int maxLevel = 5;

        @SerializedName("speed_percent_per_level")
        public double speedPercentPerLevel = 0.03;

        @SerializedName("description")
        public String description = "穿戴时增加移动速度";
    }

    // ========== Getters ==========

    public boolean isEnableArmorForge() { return enableArmorForge; }
    public int getMaxTraits() { return maxTraits; }
    public int getMinTraits() { return minTraits; }
    public boolean isExcludeConflicts() { return excludeConflicts; }
    public double getGlobalMaxDamageReduction() { return globalMaxDamageReduction; }
    public Map<String, Integer> getTraitRollWeights() { return traitRollWeights; }
    public Map<String, Double> getLevelChances() { return levelChances; }
    public List<String> getApplicableItems() { return applicableItems; }

    public String getResetMaterial() { return resetMaterial; }
    public String getUpgradeMaterial() { return upgradeMaterial; }
    public String getUpgradeCornerMaterial() { return upgradeCornerMaterial; }
    public String getRecastMaterial() { return recastMaterial; }
    public String getRecastCornerMaterial() { return recastCornerMaterial; }

    public IronWallConfig getIronWall() { return ironWall; }
    public VampireConfig getVampire() { return vampire; }
    public ThornsConfig getThorns() { return thorns; }
    public ReviveConfig getRevive() { return revive; }
    public BulletproofConfig getBulletproof() { return bulletproof; }
    public MagicWardConfig getMagicWard() { return magicWard; }
    public SturdyConfig getSturdy() { return sturdy; }
    public LightweightConfig getLightweight() { return lightweight; }
}
