package com.pfws.supermodelboost.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一配置管理器 - 管理武器和护甲双系统的所有配置
 * 
 * 配置文件位置: config/supermodel_boost_server_pfws/config.json
 * 
 * @author PFWs
 */
public final class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("supermodel_boost_server_pfws");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_DIR = "supermodel_boost_server_pfws";
    private static UnifiedConfig config;

    private ConfigManager() {}

    public static void init() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_DIR);
        Path configFile = configDir.resolve("config.json");

        try {
            if (Files.notExists(configFile)) {
                Files.createDirectories(configDir);
                // 从jar中复制默认配置
                try (InputStream stream = ConfigManager.class.getResourceAsStream("/supermodel_boost/config.json")) {
                    if (stream != null) {
                        Files.copy(stream, configFile);
                    } else {
                        // fallback: 直接写入默认配置
                        config = new UnifiedConfig();
                        String json = GSON.toJson(config);
                        Files.writeString(configFile, json);
                        return;
                    }
                }
            }
            try (Reader reader = Files.newBufferedReader(configFile)) {
                config = GSON.fromJson(reader, UnifiedConfig.class);
                if (config == null) {
                    config = new UnifiedConfig();
                }
            }
        } catch (IOException e) {
            LOGGER.error("配置加载失败，使用默认配置", e);
            config = new UnifiedConfig();
        }
    }

    public static void reload() {
        init();
    }

    public static UnifiedConfig get() {
        if (config == null) {
            init();
        }
        return config;
    }

    // ========== 统一配置数据结构 ==========

    public static class UnifiedConfig {
        // 全局设置
        @SerializedName("enable_weapon_forge")
        public boolean enableWeaponForge = true;

        @SerializedName("enable_armor_forge")
        public boolean enableArmorForge = true;

        @SerializedName("remove_level_cap_for_ops")
        public boolean removeLevelCapForOps = false;

        @SerializedName("log_forge_actions")
        public boolean logForgeActions = true;

        @SerializedName("debug_mode")
        public boolean debugMode = false;

        // 武器系统配置
        @SerializedName("weapon")
        public WeaponConfigSection weapon = new WeaponConfigSection();

        // 护甲系统配置
        @SerializedName("armor")
        public ArmorConfigSection armor = new ArmorConfigSection();
    }

    /**
     * 武器系统配置段
     */
    public static class WeaponConfigSection {
        @SerializedName("max_traits")
        public int maxTraits = 4;

        @SerializedName("min_traits")
        public int minTraits = 0;

        @SerializedName("exclude_traits_conflict")
        public boolean excludeTraitsConflict = true;

        @SerializedName("max_enhance_level")
        public int maxEnhanceLevel = 5;

        // 每条特性的概率 (对应第1条到第N条)
        @SerializedName("trait_probabilities")
        public double[] traitProbabilities = {0.90, 0.60, 0.20, 0.05};

        // 等级概率
        @SerializedName("level1_chance")
        public double level1Chance = 0.60;

        @SerializedName("level2_chance")
        public double level2Chance = 0.30;

        @SerializedName("level3_chance")
        public double level3Chance = 0.10;

        // 适用物品列表
        @SerializedName("applicable_items")
        public List<String> applicableItems = new ArrayList<>();

        // 重置配方材料
        @SerializedName("reset_material")
        public String resetMaterial = "minecraft:lapis_lazuli";

        // 强化配方材料
        @SerializedName("enhance_main_material")
        public String enhanceMainMaterial = "minecraft:diamond";

        @SerializedName("enhance_corner_material")
        public String enhanceCornerMaterial = "minecraft:lapis_lazuli";

        // 特性效果参数
        @SerializedName("trait_effects")
        public WeaponEffectParams traitEffects = new WeaponEffectParams();
    }

    /**
     * 武器特性效果参数
     */
    public static class WeaponEffectParams {
        // 吸血
        @SerializedName("lifesteal_heal_percent_per_level")
        public double lifestealHealPercentPerLevel = 0.10;

        // 反击
        @SerializedName("retaliation_store_percent")
        public double retaliationStorePercent = 1.0;

        @SerializedName("retaliation_release_percent_base")
        public double retaliationReleasePercentBase = 0.30;

        @SerializedName("retaliation_release_percent_per_level")
        public double retaliationReleasePercentPerLevel = 0.20;

        // 战狂
        @SerializedName("berserker_strength_duration_per_level")
        public int berserkerStrengthDurationPerLevel = 20;

        // 硬朗
        @SerializedName("tough_durability_reduce_base")
        public double toughDurabilityReduceBase = 0.20;

        @SerializedName("tough_durability_reduce_per_level")
        public double toughDurabilityReducePerLevel = 0.15;

        // 柔韧
        @SerializedName("flexible_max_durability_inc_base")
        public double flexibleMaxDurabilityIncBase = 0.10;

        @SerializedName("flexible_max_durability_inc_per_level")
        public double flexibleMaxDurabilityIncPerLevel = 0.05;

        // 兴奋
        @SerializedName("exhilaration_speed_duration_per_level")
        public int exhilarationSpeedDurationPerLevel = 20;
    }

    /**
     * 护甲系统配置段
     */
    public static class ArmorConfigSection {
        @SerializedName("max_traits")
        public int maxTraits = 3;

        @SerializedName("min_traits")
        public int minTraits = 1;

        @SerializedName("exclude_conflicts")
        public boolean excludeConflicts = true;

        // 特性抽取权重
        @SerializedName("trait_roll_weights")
        public Map<String, Integer> traitRollWeights = new HashMap<>();

        // 等级概率
        @SerializedName("level_chances")
        public Map<String, Double> levelChances = new HashMap<>();

        // 适用物品列表 (支持通配符 *)
        @SerializedName("applicable_items")
        public List<String> applicableItems = new ArrayList<>();

        // 锻造配方材料
        @SerializedName("reset_material")
        public String resetMaterial = "minecraft:lapis_block";

        @SerializedName("upgrade_material")
        public String upgradeMaterial = "minecraft:diamond_block";

        @SerializedName("upgrade_corner_material")
        public String upgradeCornerMaterial = "minecraft:lapis_block";

        @SerializedName("recast_material")
        public String recastMaterial = "minecraft:netherite_ingot";

        @SerializedName("recast_corner_material")
        public String recastCornerMaterial = "minecraft:diamond_block";

        // 全局效果限制
        @SerializedName("global_max_damage_reduction")
        public double globalMaxDamageReduction = 0.90;

        // 特性效果参数
        @SerializedName("trait_effects")
        public ArmorEffectParams traitEffects = new ArmorEffectParams();
    }

    /**
     * 护甲特性效果参数
     */
    public static class ArmorEffectParams {
        // 铁壁
        @SerializedName("iron_wall_reduction_per_level")
        public double ironWallReductionPerLevel = 0.03;

        // 吸血
        @SerializedName("vampire_heal_percent_per_level")
        public double vampireHealPercentPerLevel = 0.05;

        // 荆棘
        @SerializedName("thorns_reflect_percent_per_level")
        public double thornsReflectPercentPerLevel = 0.08;

        @SerializedName("thorns_min_reflect_damage")
        public double thornsMinReflectDamage = 1.0;

        // 复苏
        @SerializedName("revive_base_interval_ticks")
        public int reviveBaseIntervalTicks = 1200;

        @SerializedName("revive_heal_amount")
        public double reviveHealAmount = 1.0;

        // 箭矢防护
        @SerializedName("bulletproof_reduction_per_level")
        public double bulletproofReductionPerLevel = 0.10;

        // 魔御
        @SerializedName("magic_ward_reduction_per_level")
        public double magicWardReductionPerLevel = 0.12;

        // 坚韧
        @SerializedName("sturdy_repair_amount_per_level")
        public int sturdyRepairAmountPerLevel = 1;

        // 轻盈
        @SerializedName("lightweight_speed_percent_per_level")
        public double lightweightSpeedPercentPerLevel = 0.03;
    }

    /**
     * 判断物品ID是否在列表中（支持通配符 *）
     */
    public static boolean matchesItemPattern(String itemId, List<String> patterns) {
        for (String pattern : patterns) {
            if (pattern.contains("*")) {
                String regex = java.util.regex.Pattern.quote(pattern).replace("\\*", ".*");
                if (java.util.regex.Pattern.matches(regex, itemId)) return true;
            } else if (pattern.equals(itemId)) {
                return true;
            }
        }
        return false;
    }
}
