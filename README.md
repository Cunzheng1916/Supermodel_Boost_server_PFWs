# Supermodel Boost - 武器&护甲强化系统

> **PFWs 出品** | 纯服务端 Fabric 模组 | MC 26.1.1+
>
> 通过搭建强化台结构，使用发射器配方为武器和护甲随机附加特性，打造独一无二的装备！

---

## 📦 模组信息

| 项目 | 详情 |
|------|------|
| **模组名称** | Supermodel Boost - 武器&护甲强化系统 |
| **模组ID** | `supermodel_boost_server_pfws` |
| **版本** | 1.0.0 |
| **作者** | PFWs |
| **运行环境** | 纯服务端 (Server-Side) |
| **许可证** |  Apache-2.0 |
| **Minecraft** | 26.1.1+ |
| **Fabric Loader** | >= 0.19.3 |
| **Java** | >= 25 |

---

## ✨ 功能特性

### 🏗️ 两种强化台结构

在游戏中搭建特定方块结构，右击发射器上的按钮即可触发强化：

| 强化台 | 结构组成 | 用途 |
|--------|----------|------|
| **武器强化台** | 2个附魔台中间夹发射器 + 发射器上方放石头/木质按钮 | 强化武器 |
| **护甲强化台** | 2个铁砧中间夹发射器 + 发射器上方放石头/木质按钮 | 强化护甲 |

### ⚔️ 武器特性 (7种)

| 特性ID | 名称 | 最大等级 | 效果描述 |
|--------|------|:------:|----------|
| `lifesteal` | 吸血 | 3 | 攻击时恢复造成伤害一定比例的生命值 |
| `retaliation` | 反击 | 2 | 受到伤害时储存部分伤害，下次攻击额外释放 |
| `berserker` | 战狂 | 3 | 连续攻击获得短暂力量效果并叠加 |
| `tough` | 硬朗 | 2 | 大幅减少工具耐久消耗 |
| `flexible` | 柔韧 | 2 | 增加工具最大耐久度 |
| `multi_tool` | 千手观音 | 3 | 镐3x3挖掘 / 铲急迫效果 |
| `exhilaration` | 兴奋 | 3 | 击杀生物后获得移动速度加成 |

> **互斥组**: `硬朗` 与 `柔韧` 属于同一互斥组，无法共存。

### 🛡️ 护甲特性 (8种)

| 特性ID | 名称 | 最大等级 | 效果描述 |
|--------|------|:------:|----------|
| `iron_wall` | 铁壁 | 5 | 减少受到的所有伤害 |
| `vampire` | 吸血 | 5 | 攻击恢复造成伤害比例的生命值 |
| `thorns` | 荆棘 | 5 | 反弹近战伤害给攻击者 |
| `revive` | 复苏 | 3 | 每间隔一段时间自动恢复生命值 |
| `bulletproof` | 箭矢防护 | 4 | 减少弹射物造成的伤害 |
| `magic_ward` | 魔御 | 4 | 减少魔法伤害 |
| `sturdy` | 坚韧 | 3 | 护甲耐久损耗降低 |
| `lightweight` | 轻盈 | 5 | 增加移动速度 |

> 护甲特性同样支持互斥组，同组特性无法共存。

---

## 🎮 使用方法

### 基本指令

所有功能统一通过 `/sboost` 指令管理：

```
/sboost help          - 查看帮助
/sboost info          - 查看手中物品特性
/sboost list          - 列出所有可用特性
/sboost reset         - 重置手中物品特性
/sboost enhance       - 升级手中物品已有特性
/sboost roll <id>     - 为手中物品随机添加指定特性
```

### 强化流程

1. 搭建对应的强化台结构（武器/护甲）
2. 将要强化的物品放入发射器
3. 右击发射器上方的按钮
4. 系统随机为物品附加特性

### 适用物品

- **武器强化台**: 所有木/石/铁/金/钻/下界合金/铜质的剑、斧、镐、铲、锄、三叉戟、重锤
- **护甲强化台**: 头盔、胸甲、护腿、靴子（所有材质）

---

## ⚙️ 配置说明

配置文件位置: `config/supermodel_boost_server_pfws/config.json`

### 主要配置项

```json
{
  "enable_weapon_forge": true,
  "enable_armor_forge": true,
  "remove_level_cap_for_ops": false,
  "log_forge_actions": true,
  "debug_mode": false,
  "weapon": {
    "max_traits": 4,
    "min_traits": 0,
    "exclude_traits_conflict": true,
    "max_enhance_level": 5,
    "trait_probabilities": [0.90, 0.60, 0.20, 0.05],
    "level1_chance": 0.60,
    "level2_chance": 0.30,
    "level3_chance": 0.10,
    "applicable_items": [...],
    "reset_material": "minecraft:lapis_lazuli",
    "enhance_main_material": "minecraft:diamond",
    "enhance_corner_material": "minecraft:lapis_lazuli"
  },
  "armor": {
    "max_traits": 3,
    "min_traits": 1,
    "exclude_conflicts": true,
    ...
  }
}
```

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `enable_weapon_forge` | 是否启用武器强化台 | `true` |
| `enable_armor_forge` | 是否启用护甲强化台 | `true` |
| `debug_mode` | 调试模式（输出额外日志） | `false` |
| `weapon.max_traits` | 武器最多特性数 | 4 |
| `armor.max_traits` | 护甲最多特性数 | 3 |
| `weapon.trait_probabilities` | 武器获得 1/2/3/4 个特性的概率 | 见上表 |
| `weapon.level1_chance` | 特性为1级的概率 | 60% |
| `weapon.level2_chance` | 特性为2级的概率 | 30% |
| `weapon.level3_chance` | 特性为3级的概率 | 10% |
| `reset_material` | 重置物品所需材料 | 青金石 |
| `enhance_main_material` | 升级特性所需主材料 | 钻石 |
| `enhance_corner_material` | 升级特性所需副材料 | 青金石 |

---

## 🔨 开发构建

### 环境要求

- JDK 25+
- Gradle (通过 wrapper 自动下载)

### 构建命令

**Windows:**
```bash
gradlew build
```

**Linux/macOS:**
```bash
./gradlew build
```

构建产物输出到: `build/libs/supermodel_boost_server_pfws-1.0.0.jar`

### 项目结构

```
src/main/java/com/pfws/supermodelboost/
├── SupermodelBoostMod.java          # 模组主入口
├── EventHandler.java                # 事件监听处理
├── StructureHandler.java            # 强化台结构检测
├── CommandHandler.java              # /sboost 指令处理
├── armor/                           # 护甲特性模块
│   ├── ArmorTraitRegistry.java      #   护甲特性注册表
│   ├── ArmorTraitData.java          #   特性数据定义
│   ├── ArmorTraitInstance.java      #   特性实例（含等级）
│   ├── ArmorTraitRollEngine.java    #   特性随机抽选引擎
│   ├── ArmorTraitNbtHelper.java     #   NBT读写工具
│   ├── ArmorTraitConfig.java        #   护甲配置
│   └── ArmorLoreRefreshHelper.java  #   Lore刷新工具
├── weapon/                          # 武器特性模块
│   ├── WeaponTraitRegistry.java     #   武器特性注册表
│   ├── WeaponTraitData.java         #   特性数据定义
│   ├── WeaponTraitInstance.java     #   特性实例（含等级）
│   ├── WeaponTraitRollEngine.java   #   特性随机抽选引擎
│   ├── WeaponTraitEffectHandler.java#   特性效果处理器
│   ├── WeaponTraitNbtHelper.java    #   NBT读写工具
│   └── WeaponTraitConfig.java       #   武器配置
├── config/                          # 配置管理
│   └── ConfigManager.java           #   统一配置管理器
├── mixin/                           # Mixin注入
│   ├── ItemStackMixin.java          #   物品堆叠修改
│   └── DispenserBlockMixin.java     #   发射器修改
└── storage/                         # 持久化存储
    └── RetaliationStorage.java      #   反击伤害存储
```

---

## 📌 技术细节

- **特性存储**: 特性数据通过 NBT 标签持久化在物品的 `ItemStack` 上
- **特性抽取**: 使用加权随机算法，等级1/2/3各有不同概率
- **互斥管理**: 同组特性（如硬朗/柔韧）无法共存，冲突检测在抽取时自动进行
- **效果处理**: 武器特性通过攻击事件触发，护甲特性通过受伤/定时事件触发
- **纯服务端**: 模组仅在服务端运行，客户端无需安装

---

## 📝 更新日志

### v1.0.0 (2026-06)

- 🎉 首个正式版本发布
- ⚔️ 实现7种武器特性
- 🛡️ 实现8种护甲特性
- 🏗️ 武器强化台 + 护甲强化台两种结构
- 📋 统一 `/sboost` 指令系统
- ⚙️ 完整的 JSON 配置文件支持
- 🔧 NBT 持久化存储

---

## 📄 许可证

本项目基于 **Apache-2.0** 发布。详见LICENSE文件。

---

> 💡 **提示**: 此模组为纯服务端模组，只需安装在服务器端即可生效。客户端无需安装即可加入服务器享受完整特性！（若为本地游玩可无视本提示）