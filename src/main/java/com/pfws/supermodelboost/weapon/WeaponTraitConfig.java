package com.pfws.supermodelboost.weapon;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 武器特性模组配置文件数据结构
 * 每个特性单独配置，放在武器文件夹内
 * 
 * @author PFWs
 */
public class WeaponTraitConfig {
    @SerializedName("enable_weapon_trait_system")
    private boolean enableWeaponTraitSystem = true;

    @SerializedName("max_traits")
    private int maxTraits = 4;

    @SerializedName("exclude_traits_conflict")
    private boolean excludeTraitsConflict = true;

    @SerializedName("max_enhance_level")
    private int maxEnhanceLevel = 5;

    @SerializedName("trait_probabilities")
    private double[] traitProbabilities = {0.90, 0.60, 0.20, 0.05};

    @SerializedName("level3_chance")
    private double level3Chance = 0.10;

    @SerializedName("level2_chance")
    private double level2Chance = 0.30;

    @SerializedName("level1_chance")
    private double level1Chance = 0.60;

    @SerializedName("applicable_items")
    private List<String> applicableItems = List.of(
        // 剑
        "minecraft:wooden_sword", "minecraft:stone_sword", "minecraft:iron_sword",
        "minecraft:golden_sword", "minecraft:diamond_sword", "minecraft:netherite_sword",
        // 斧
        "minecraft:wooden_axe", "minecraft:stone_axe", "minecraft:iron_axe",
        "minecraft:golden_axe", "minecraft:diamond_axe", "minecraft:netherite_axe",
        // 镐
        "minecraft:wooden_pickaxe", "minecraft:stone_pickaxe", "minecraft:iron_pickaxe",
        "minecraft:golden_pickaxe", "minecraft:diamond_pickaxe", "minecraft:netherite_pickaxe",
        // 铲
        "minecraft:wooden_shovel", "minecraft:stone_shovel", "minecraft:iron_shovel",
        "minecraft:golden_shovel", "minecraft:diamond_shovel", "minecraft:netherite_shovel",
        // 锄
        "minecraft:wooden_hoe", "minecraft:stone_hoe", "minecraft:iron_hoe",
        "minecraft:golden_hoe", "minecraft:diamond_hoe", "minecraft:netherite_hoe",
        // 三叉戟
        "minecraft:trident",
        // 狼牙棒 (Mace)
        "minecraft:mace"
    );

    // ========== 特性效果详细配置 ==========

    @SerializedName("lifesteal")
    private LifestealConfig lifesteal = new LifestealConfig();

    @SerializedName("retaliation")
    private RetaliationConfig retaliation = new RetaliationConfig();

    @SerializedName("berserker")
    private BerserkerConfig berserker = new BerserkerConfig();

    @SerializedName("tough")
    private ToughConfig tough = new ToughConfig();

    @SerializedName("flexible")
    private FlexibleConfig flexible = new FlexibleConfig();

    @SerializedName("multi_tool")
    private MultiToolConfig multiTool = new MultiToolConfig();

    @SerializedName("exhilaration")
    private ExhilarationConfig exhilaration = new ExhilarationConfig();

    // ========== 特性效果子配置类 ==========

    public static class LifestealConfig {
        @SerializedName("max_level")
        public int maxLevel = 3;

        @SerializedName("heal_percent_per_level")
        public double healPercentPerLevel = 0.10;

        @SerializedName("description")
        public String description = "攻击时恢复造成伤害一定比例的生命值";
    }

    public static class RetaliationConfig {
        @SerializedName("max_level")
        public int maxLevel = 2;

        @SerializedName("store_percent")
        public double storePercent = 1.0;

        @SerializedName("release_percent_base")
        public double releasePercentBase = 0.30;

        @SerializedName("release_percent_per_level")
        public double releasePercentPerLevel = 0.20;

        @SerializedName("description")
        public String description = "受到伤害时储存部分伤害，下次攻击时额外释放";
    }

    public static class BerserkerConfig {
        @SerializedName("max_level")
        public int maxLevel = 3;

        @SerializedName("strength_duration_per_level")
        public int strengthDurationPerLevel = 20;

        @SerializedName("description")
        public String description = "攻击时获得短暂力量效果，连击时叠加";
    }

    public static class ToughConfig {
        @SerializedName("max_level")
        public int maxLevel = 2;

        @SerializedName("durability_reduce_base")
        public double durabilityReduceBase = 0.20;

        @SerializedName("durability_reduce_per_level")
        public double durabilityReducePerLevel = 0.15;

        @SerializedName("description")
        public String description = "大幅减少工具耐久消耗";
    }

    public static class FlexibleConfig {
        @SerializedName("max_level")
        public int maxLevel = 2;

        @SerializedName("max_durability_inc_base")
        public double maxDurabilityIncBase = 0.10;

        @SerializedName("max_durability_inc_per_level")
        public double maxDurabilityIncPerLevel = 0.05;

        @SerializedName("description")
        public String description = "增加工具最大耐久度";
    }

    public static class MultiToolConfig {
        @SerializedName("max_level")
        public int maxLevel = 3;

        @SerializedName("description")
        public String description = "工具可挖掘相邻方块（镐3x3、铲急迫）";
    }

    public static class ExhilarationConfig {
        @SerializedName("max_level")
        public int maxLevel = 3;

        @SerializedName("speed_duration_per_level")
        public int speedDurationPerLevel = 20;

        @SerializedName("description")
        public String description = "击杀生物后获得短暂移动速度加成";
    }

    // ========== Getters ==========

    public boolean isEnableWeaponTraitSystem() { return enableWeaponTraitSystem; }
    public void setEnableWeaponTraitSystem(boolean v) { this.enableWeaponTraitSystem = v; }

    public int getMaxTraits() { return maxTraits; }
    public void setMaxTraits(int v) { this.maxTraits = v; }

    public boolean isExcludeTraitsConflict() { return excludeTraitsConflict; }
    public void setExcludeTraitsConflict(boolean v) { this.excludeTraitsConflict = v; }

    public int getMaxEnhanceLevel() { return maxEnhanceLevel; }
    public void setMaxEnhanceLevel(int v) { this.maxEnhanceLevel = v; }

    public double[] getTraitProbabilities() { return traitProbabilities; }
    public void setTraitProbabilities(double[] v) { this.traitProbabilities = v; }

    public double getLevel3Chance() { return level3Chance; }
    public void setLevel3Chance(double v) { this.level3Chance = v; }

    public double getLevel2Chance() { return level2Chance; }
    public void setLevel2Chance(double v) { this.level2Chance = v; }

    public double getLevel1Chance() { return level1Chance; }
    public void setLevel1Chance(double v) { this.level1Chance = v; }

    public List<String> getApplicableItems() { return applicableItems; }
    public void setApplicableItems(List<String> v) { this.applicableItems = v; }

    public LifestealConfig getLifestealConfig() { return lifesteal; }
    public RetaliationConfig getRetaliationConfig() { return retaliation; }
    public BerserkerConfig getBerserkerConfig() { return berserker; }
    public ToughConfig getToughConfig() { return tough; }
    public FlexibleConfig getFlexibleConfig() { return flexible; }
    public MultiToolConfig getMultiToolConfig() { return multiTool; }
    public ExhilarationConfig getExhilarationConfig() { return exhilaration; }
}
