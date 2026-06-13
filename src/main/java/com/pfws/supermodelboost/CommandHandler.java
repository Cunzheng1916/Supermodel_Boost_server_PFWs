package com.pfws.supermodelboost;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pfws.supermodelboost.armor.*;
import com.pfws.supermodelboost.config.ConfigManager;
import com.pfws.supermodelboost.weapon.*;
import com.pfws.supermodelboost.weapon.skill.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 统一 /sboost 命令处理器
 * 
 * /sboost weapon info - 查看手持武器特性
 * /sboost weapon add <target> <trait> [level] - 给武器添加特性
 * /sboost weapon remove <target> <trait> - 移除武器特性
 * /sboost weapon clear <target> - 清除所有特性
 * /sboost weapon setlevel <target> <trait> <level> - 设置特性等级
 * /sboost weapon setenhance <target> <level> - 设置强化等级
 * /sboost weapon skill info - 查看手持武器技能详情
 * /sboost weapon skill settendency <target> <t> - 设置倾向
 * /sboost weapon skill setlevel <target> <level> - 设置技能等级
 * /sboost weapon skill setxp <target> <xp> - 设置技能经验值
 * /sboost weapon skill clear <target> - 清除所有技能数据
 * /sboost weapon skill setskill <target> <skillId> [level] - 设置技能
 * /sboost weapon skill clearcooldown <target> - 清除技能冷却
 * /sboost armor info - 查看身上护甲特性
 * /sboost armor add <target> <slot> <trait> [level] - 给护甲添加特性
 * /sboost armor remove <target> <slot> <trait> - 移除护甲特性
 * /sboost armor clear <target> - 清除目标所有护甲特性
 * /sboost armor setlevel <target> <slot> <trait> <level> - 设置护甲特性等级
 * /sboost reload - 重载配置
 * 
 * @author PFWs
 */
public final class CommandHandler {

    private CommandHandler() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("sboost")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                // ---- 武器子命令 ----
                .then(Commands.literal("weapon")
                    .then(Commands.literal("info")
                        .executes(CommandHandler::weaponInfo))
                    .then(Commands.literal("add")
                        .then(Commands.argument("target", EntityArgument.player())
                            .then(Commands.argument("trait", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (String id : WeaponTraitRegistry.getAllIds()) {
                                        builder.suggest(id);
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 5))
                                    .executes(CommandHandler::weaponAddLevel)
                                )
                                .executes(CommandHandler::weaponAdd)
                            )))
                    .then(Commands.literal("remove")
                        .then(Commands.argument("target", EntityArgument.player())
                            .then(Commands.argument("trait", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (String id : WeaponTraitRegistry.getAllIds()) {
                                        builder.suggest(id);
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(CommandHandler::weaponRemove)
                            )))
                    .then(Commands.literal("clear")
                        .then(Commands.argument("target", EntityArgument.player())
                            .executes(CommandHandler::weaponClear)))
                    .then(Commands.literal("setlevel")
                        .then(Commands.argument("target", EntityArgument.player())
                            .then(Commands.argument("trait", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (String id : WeaponTraitRegistry.getAllIds()) {
                                        builder.suggest(id);
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 5))
                                    .executes(CommandHandler::weaponSetLevel)
                                ))))
                    .then(Commands.literal("setenhance")
                        .then(Commands.argument("target", EntityArgument.player())
                            .then(Commands.argument("level", IntegerArgumentType.integer(0, 5))
                                .executes(CommandHandler::weaponSetEnhance)
                            )))
                    // ---- 技能子命令 ----
                    .then(Commands.literal("skill")
                        .then(Commands.literal("info")
                            .executes(CommandHandler::skillInfo))
                        .then(Commands.literal("settendency")
                            .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("tendency", StringArgumentType.word())
                                    .suggests((ctx, builder) -> {
                                        builder.suggest("violent");
                                        builder.suggest("abundance");
                                        builder.suggest("blood");
                                        builder.suggest("none");
                                        return builder.buildFuture();
                                    })
                                    .executes(CommandHandler::skillSetTendency)
                                )))
                        .then(Commands.literal("setlevel")
                            .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 5))
                                    .executes(CommandHandler::skillSetLevel)
                                )))
                        .then(Commands.literal("setxp")
                            .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("xp", IntegerArgumentType.integer(0))
                                    .executes(CommandHandler::skillSetXp)
                                )))
                        .then(Commands.literal("clear")
                            .then(Commands.argument("target", EntityArgument.player())
                                .executes(CommandHandler::skillClear)
                            ))
                        .then(Commands.literal("setskill")
                            .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("skillId", StringArgumentType.word())
                                    .suggests((ctx, builder) -> {
                                        for (String id : ActiveSkillRegistry.getAllIds()) {
                                            builder.suggest(id);
                                        }
                                        return builder.buildFuture();
                                    })
                                    .then(Commands.argument("level", IntegerArgumentType.integer(1, 5))
                                        .executes(CommandHandler::skillSetSkillLevel)
                                    )
                                    .executes(CommandHandler::skillSetSkill)
                                )))
                        .then(Commands.literal("clearcooldown")
                            .then(Commands.argument("target", EntityArgument.player())
                                .executes(CommandHandler::skillClearCooldown)
                            ))
                    )
                )
                // ---- 护甲子命令 ----
                .then(Commands.literal("armor")
                    .then(Commands.literal("info")
                        .executes(CommandHandler::armorInfo))
                    .then(Commands.literal("add")
                        .then(Commands.argument("target", EntityArgument.player())
                            .then(Commands.argument("slot", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("head");
                                    builder.suggest("chest");
                                    builder.suggest("legs");
                                    builder.suggest("feet");
                                    builder.suggest("mainhand");
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("trait", StringArgumentType.word())
                                    .suggests((ctx, builder) -> {
                                        for (String id : ArmorTraitRegistry.getAllIds()) {
                                            builder.suggest(id);
                                        }
                                        return builder.buildFuture();
                                    })
                                    .then(Commands.argument("level", IntegerArgumentType.integer(1, 5))
                                        .executes(CommandHandler::armorAddLevel)
                                    )
                                    .executes(CommandHandler::armorAdd)
                                ))))
                    .then(Commands.literal("remove")
                        .then(Commands.argument("target", EntityArgument.player())
                            .then(Commands.argument("slot", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("head");
                                    builder.suggest("chest");
                                    builder.suggest("legs");
                                    builder.suggest("feet");
                                    builder.suggest("mainhand");
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("trait", StringArgumentType.word())
                                    .suggests((ctx, builder) -> {
                                        for (String id : ArmorTraitRegistry.getAllIds()) {
                                            builder.suggest(id);
                                        }
                                        return builder.buildFuture();
                                    })
                                    .executes(CommandHandler::armorRemove)
                                ))))
                    .then(Commands.literal("clear")
                        .then(Commands.argument("target", EntityArgument.player())
                            .executes(CommandHandler::armorClear)))
                    .then(Commands.literal("setlevel")
                        .then(Commands.argument("target", EntityArgument.player())
                            .then(Commands.argument("slot", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("head");
                                    builder.suggest("chest");
                                    builder.suggest("legs");
                                    builder.suggest("feet");
                                    builder.suggest("mainhand");
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("trait", StringArgumentType.word())
                                    .suggests((ctx, builder) -> {
                                        for (String id : ArmorTraitRegistry.getAllIds()) {
                                            builder.suggest(id);
                                        }
                                        return builder.buildFuture();
                                    })
                                    .then(Commands.argument("level", IntegerArgumentType.integer(1, 5))
                                        .executes(CommandHandler::armorSetLevel)
                                    )))))
                )
                // ---- 全局命令 ----
                .then(Commands.literal("reload")
                    .executes(CommandHandler::reloadConfig))
        );
    }

    // ========== 武器命令实现 ==========

    private static int weaponInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) {
            send(ctx, "§c你手上没有物品！");
            return 0;
        }

        List<WeaponTraitInstance> traits = WeaponTraitNbtHelper.getTraits(mainHand);
        int enh = WeaponTraitNbtHelper.getEnhanceLevel(mainHand);

        StringBuilder sb = new StringBuilder("§6===== 武器特性信息 =====\n");
        sb.append("§7物品: §f").append(mainHand.getDisplayName().getString()).append("\n");
        sb.append("§7强化等级: ").append(WeaponTraitNbtHelper.getEnhanceLevelColor(enh))
                .append(WeaponTraitNbtHelper.getEnhanceLevelDisplayName(enh)).append("\n");

        if (traits.isEmpty()) {
            sb.append("§7无特性");
        } else {
            for (WeaponTraitInstance t : traits) {
                WeaponTraitData data = WeaponTraitRegistry.getById(t.getId()).orElse(null);
                if (data != null) {
                    sb.append("§e  ◆ ").append(data.getDisplayName())
                            .append(" §bLv.").append(t.getLevel())
                            .append(" §7- ").append(data.describe(t.getLevel())).append("\n");
                }
            }
        }

        send(ctx, sb.toString());
        return 1;
    }

    private static int weaponAdd(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return weaponAddWithLevel(ctx, 1);
    }

    private static int weaponAddLevel(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        int level = IntegerArgumentType.getInteger(ctx, "level");
        return weaponAddWithLevel(ctx, level);
    }

    private static int weaponAddWithLevel(CommandContext<CommandSourceStack> ctx, int level) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        String traitId = StringArgumentType.getString(ctx, "trait");

        Optional<WeaponTraitData> data = WeaponTraitRegistry.getById(traitId);
        if (data.isEmpty()) {
            send(ctx, "§c未知特性: " + traitId);
            return 0;
        }

        int maxLevel = data.get().getMaxLevel();
        if (level > maxLevel) level = maxLevel;

        ItemStack mainHand = target.getMainHandItem();
        if (mainHand.isEmpty()) {
            send(ctx, "§c目标手上没有物品！");
            return 0;
        }

        String itemId = BuiltInRegistries.ITEM.getKey(mainHand.getItem()).toString();
        if (!ConfigManager.matchesItemPattern(itemId, ConfigManager.get().weapon.applicableItems)) {
            send(ctx, "§c该物品不适用武器特性系统！");
            return 0;
        }

        // 检查冲突
        String conflict = WeaponTraitRegistry.getById(traitId)
                .map(WeaponTraitData::getConflictGroup).orElse(null);
        if (conflict != null) {
            for (WeaponTraitInstance existing : WeaponTraitNbtHelper.getTraits(mainHand)) {
                String existingConflict = WeaponTraitRegistry.getById(existing.getId())
                        .map(WeaponTraitData::getConflictGroup).orElse(null);
                if (conflict.equals(existingConflict)) {
                    send(ctx, "§c特性冲突！该武器已有同组特性: " + existing.getId());
                    return 0;
                }
            }
        }

        WeaponTraitNbtHelper.addTrait(mainHand, new WeaponTraitInstance(traitId, level));
        LoreUpdateHelper.updateAllLore(mainHand);
        send(ctx, "§a✔ 已给 " + target.getName().getString() + " 的武器添加 "
                + data.get().getDisplayName() + " Lv." + level);
        return 1;
    }

    private static int weaponRemove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        String traitId = StringArgumentType.getString(ctx, "trait");

        ItemStack mainHand = target.getMainHandItem();
        WeaponTraitNbtHelper.removeTrait(mainHand, traitId);
        LoreUpdateHelper.updateAllLore(mainHand);

        send(ctx, "§a✔ 已移除特性: " + traitId);
        return 1;
    }

    private static int weaponClear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        ItemStack mainHand = target.getMainHandItem();
        WeaponTraitNbtHelper.clearTraits(mainHand);
        WeaponTraitNbtHelper.setEnhanceLevel(mainHand, 0);
        LoreUpdateHelper.updateAllLore(mainHand);

        send(ctx, "§a✔ 已清除所有武器特性");
        return 1;
    }

    private static int weaponSetLevel(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        String traitId = StringArgumentType.getString(ctx, "trait");
        int level = IntegerArgumentType.getInteger(ctx, "level");

        ItemStack mainHand = target.getMainHandItem();
        WeaponTraitNbtHelper.setTraitLevel(mainHand, traitId, level);
        LoreUpdateHelper.updateAllLore(mainHand);

        send(ctx, "§a✔ 已设置 " + traitId + " 等级为 " + level);
        return 1;
    }

    private static int weaponSetEnhance(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        int level = IntegerArgumentType.getInteger(ctx, "level");

        ItemStack mainHand = target.getMainHandItem();
        WeaponTraitNbtHelper.setEnhanceLevel(mainHand, level);
        LoreUpdateHelper.updateAllLore(mainHand);

        String displayName = WeaponTraitNbtHelper.getEnhanceLevelDisplayName(level);
        send(ctx, "§a✔ 已设置强化等级为 " + displayName);
        return 1;
    }

    // ========== 护甲命令实现 ==========

    private static int armorInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        StringBuilder sb = new StringBuilder("§6===== 护甲特性信息 =====\n");

        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            if (slot.getType() != net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            ItemStack armor = player.getItemBySlot(slot);
            if (armor.isEmpty()) continue;

            sb.append("§7[").append(slot.getName()).append("] §f")
                    .append(armor.getDisplayName().getString()).append("\n");

            List<ArmorTraitInstance> traits = ArmorTraitNbtHelper.getTraits(armor);
            if (traits.isEmpty()) {
                sb.append("  §7无特性\n");
            } else {
                for (ArmorTraitInstance ti : traits) {
                    ArmorTraitData data = ArmorTraitRegistry.getById(ti.getId()).orElse(null);
                    if (data != null) {
                        sb.append("  §e◆ ").append(data.getDisplayName())
                                .append(" §bLv.").append(ti.getLevel())
                                .append(" §7- ").append(data.describe(ti.getLevel())).append("\n");
                    }
                }
            }
            int totalLv = ArmorTraitNbtHelper.getTotalReinforceLevel(armor);
            if (totalLv > 0) {
                sb.append("  §d⭐ 强化等级: ").append(totalLv).append("\n");
            }
        }

        send(ctx, sb.toString());
        return 1;
    }

    private static int armorAdd(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return armorAddWithLevel(ctx, 1);
    }

    private static int armorAddLevel(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        int level = IntegerArgumentType.getInteger(ctx, "level");
        return armorAddWithLevel(ctx, level);
    }

    private static int armorAddWithLevel(CommandContext<CommandSourceStack> ctx, int level) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        String slotName = StringArgumentType.getString(ctx, "slot");
        String traitId = StringArgumentType.getString(ctx, "trait");

        Optional<ArmorTraitData> data = ArmorTraitRegistry.getById(traitId);
        if (data.isEmpty()) {
            send(ctx, "§c未知特性: " + traitId);
            return 0;
        }

        int maxLevel = data.get().getMaxLevel();
        if (level > maxLevel) level = maxLevel;

        ItemStack armor = getArmorBySlotName(target, slotName);
        if (armor == null || armor.isEmpty()) {
            send(ctx, "§c目标该槽位没有护甲！");
            return 0;
        }

        // 检查冲突
        String conflict = data.get().getConflictGroup();
        if (conflict != null) {
            for (ArmorTraitInstance existing : ArmorTraitNbtHelper.getTraits(armor)) {
                ArmorTraitData ed = ArmorTraitRegistry.getById(existing.getId()).orElse(null);
                if (ed != null && conflict.equals(ed.getConflictGroup())) {
                    send(ctx, "§c特性冲突！该护甲已有同组特性: " + existing.getId());
                    return 0;
                }
            }
        }

        ArmorTraitNbtHelper.addTrait(armor, new ArmorTraitInstance(traitId, level));
        ArmorLoreRefreshHelper.refreshLore(armor);

        send(ctx, "§a✔ 已给 " + target.getName().getString() + " 的 " + slotName + " 添加 "
                + data.get().getDisplayName() + " Lv." + level);
        return 1;
    }

    private static int armorRemove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        String slotName = StringArgumentType.getString(ctx, "slot");
        String traitId = StringArgumentType.getString(ctx, "trait");

        ItemStack armor = getArmorBySlotName(target, slotName);
        if (armor == null || armor.isEmpty()) {
            send(ctx, "§c目标该槽位没有护甲！");
            return 0;
        }

        ArmorTraitNbtHelper.removeTrait(armor, traitId);
        ArmorLoreRefreshHelper.refreshLore(armor);

        send(ctx, "§a✔ 已移除特性: " + traitId);
        return 1;
    }

    private static int armorClear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            if (slot.getType() != net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            ItemStack armor = target.getItemBySlot(slot);
            if (!armor.isEmpty()) {
                ArmorTraitNbtHelper.setTraits(armor, new ArrayList<>());
                ArmorTraitNbtHelper.setTotalReinforceLevel(armor, 0);
                ArmorLoreRefreshHelper.refreshLore(armor);
            }
        }

        send(ctx, "§a✔ 已清除目标所有护甲特性");
        return 1;
    }

    private static int armorSetLevel(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        String slotName = StringArgumentType.getString(ctx, "slot");
        String traitId = StringArgumentType.getString(ctx, "trait");
        int level = IntegerArgumentType.getInteger(ctx, "level");

        ItemStack armor = getArmorBySlotName(target, slotName);
        if (armor == null || armor.isEmpty()) {
            send(ctx, "§c目标该槽位没有护甲！");
            return 0;
        }

        ArmorTraitNbtHelper.setTraitLevel(armor, traitId, level);
        ArmorLoreRefreshHelper.refreshLore(armor);

        send(ctx, "§a✔ 已设置 " + traitId + " 等级为 " + level);
        return 1;
    }

    // ========== 全局命令 ==========

    private static int reloadConfig(CommandContext<CommandSourceStack> ctx) {
        ConfigManager.reload();
        send(ctx, "§a✔ 配置已重载！");
        SupermodelBoostMod.LOGGER.info("配置通过 /sboost reload 重载");
        return 1;
    }

    // ========== 技能命令实现 ==========

    private static int skillInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) {
            send(ctx, "§c你手上没有物品！");
            return 0;
        }

        if (!ActiveSkillNbtHelper.hasSkill(mainHand)) {
            send(ctx, "§7该武器没有技能");
            int xp = ActiveSkillNbtHelper.getSkillXp(mainHand);
            int enhance = WeaponTraitNbtHelper.getEnhanceLevel(mainHand);
            if (enhance >= 5 && SkillRollEngine.isApplicableWeapon(mainHand)) {
                int needed = ActiveSkillNbtHelper.getXpForLevel(1);
                send(ctx, "§e技能XP: " + xp + " / " + needed + " (强化等级≥5，击杀可觉醒)");
            }
            return 1;
        }

        ActiveSkillInstance skill = ActiveSkillNbtHelper.getSkill(mainHand);
        if (skill == null) { send(ctx, "§c读取技能失败"); return 0; }

        ActiveSkillData data = ActiveSkillRegistry.getById(skill.skillId()).orElse(null);
        if (data == null) { send(ctx, "§c技能数据不存在"); return 0; }

        StringBuilder sb = new StringBuilder("§6===== 技能信息 =====\n");
        sb.append("§7武器: §f").append(mainHand.getDisplayName().getString()).append("\n");
        sb.append("§7技能: §b").append(data.getDisplayName())
                .append(" §e").append(ActiveSkillNbtHelper.getLevelDisplayNamePublic(skill.getLevel())).append("\n");
        sb.append("§7描述: §a").append(data.describe(skill.getLevel())).append("\n");
        sb.append("§7XP: §e").append(ActiveSkillNbtHelper.getSkillXp(mainHand));
        if (skill.getLevel() >= 5) {
            sb.append(" §a(已满级)\n");
        } else {
            sb.append(" / ").append(ActiveSkillNbtHelper.getXpForLevel(skill.getLevel() + 1)).append("\n");
        }
        long cdEnd = ActiveSkillNbtHelper.getCooldownEnd(mainHand);
        long now = player.level().getGameTime();
        if (cdEnd > now) {
            sb.append("§7CD: §c").append((cdEnd - now) / 20).append("秒后可用\n");
        } else {
            sb.append("§7CD: §a可用\n");
        }
        String tendency = ActiveSkillNbtHelper.getTendency(mainHand);
        if (tendency != null && !tendency.isEmpty()) {
            String tName = switch (tendency) {
                case ActiveSkillRegistry.VIOLENT -> "暴戾";
                case ActiveSkillRegistry.ABUNDANCE -> "丰饶";
                case ActiveSkillRegistry.BLOOD -> "血使";
                default -> tendency;
            };
            sb.append("§7倾向: §d").append(tName).append("\n");
        }

        send(ctx, sb.toString());
        return 1;
    }

    private static int skillSetTendency(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        String tendency = StringArgumentType.getString(ctx, "tendency");

        ItemStack mainHand = target.getMainHandItem();
        if (mainHand.isEmpty()) {
            send(ctx, "§c目标手上没有物品！");
            return 0;
        }

        if (!SkillRollEngine.isApplicableWeapon(mainHand)) {
            send(ctx, "§c该物品不适用技能系统！");
            return 0;
        }

        if ("none".equals(tendency)) {
            ActiveSkillNbtHelper.setTendency(mainHand, null);
            LoreUpdateHelper.updateAllLore(mainHand);
            send(ctx, "§a✔ 已清除倾向");
        } else if (ActiveSkillRegistry.VIOLENT.equals(tendency)
                || ActiveSkillRegistry.ABUNDANCE.equals(tendency)
                || ActiveSkillRegistry.BLOOD.equals(tendency)) {
            ActiveSkillNbtHelper.setTendency(mainHand, tendency);
            LoreUpdateHelper.updateAllLore(mainHand);
            String tName = switch (tendency) {
                case ActiveSkillRegistry.VIOLENT -> "暴戾";
                case ActiveSkillRegistry.ABUNDANCE -> "丰饶";
                case ActiveSkillRegistry.BLOOD -> "血使";
                default -> tendency;
            };
            send(ctx, "§a✔ 已设置倾向为 " + tName);
        } else {
            send(ctx, "§c未知倾向: " + tendency + " (可选: violent/abundance/blood/none)");
        }
        return 1;
    }

    private static int skillSetLevel(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        int level = IntegerArgumentType.getInteger(ctx, "level");

        ItemStack mainHand = target.getMainHandItem();
        if (!ActiveSkillNbtHelper.hasSkill(mainHand)) {
            send(ctx, "§c该武器没有技能！");
            return 0;
        }

        ActiveSkillInstance skill = ActiveSkillNbtHelper.getSkill(mainHand);
        if (skill == null) { send(ctx, "§c读取技能失败"); return 0; }

        ActiveSkillNbtHelper.setSkill(mainHand, new ActiveSkillInstance(skill.skillId(), level));
        LoreUpdateHelper.updateAllLore(mainHand);
        send(ctx, "§a✔ 已设置技能等级为 " + level);
        return 1;
    }

    private static int skillSetXp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        int xp = IntegerArgumentType.getInteger(ctx, "xp");

        ItemStack mainHand = target.getMainHandItem();
        if (!SkillRollEngine.isApplicableWeapon(mainHand)) {
            send(ctx, "§c该物品不适用技能系统！");
            return 0;
        }

        ActiveSkillNbtHelper.setSkillXp(mainHand, xp);
        ActiveSkillNbtHelper.tryLevelUp(mainHand);
        LoreUpdateHelper.updateAllLore(mainHand);
        send(ctx, "§a✔ 已设置技能XP为 " + xp);
        return 1;
    }

    private static int skillClear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

        ItemStack mainHand = target.getMainHandItem();
        ActiveSkillNbtHelper.clearSkill(mainHand);
        LoreUpdateHelper.updateAllLore(mainHand);
        send(ctx, "§a✔ 已清除所有技能数据");
        return 1;
    }

    private static int skillSetSkill(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return skillSetSkillWithLevel(ctx, 1);
    }

    private static int skillSetSkillLevel(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        int level = IntegerArgumentType.getInteger(ctx, "level");
        return skillSetSkillWithLevel(ctx, level);
    }

    private static int skillSetSkillWithLevel(CommandContext<CommandSourceStack> ctx, int level) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        String skillId = StringArgumentType.getString(ctx, "skillId");

        ItemStack mainHand = target.getMainHandItem();
        if (mainHand.isEmpty()) {
            send(ctx, "§c目标手上没有物品！");
            return 0;
        }

        if (!SkillRollEngine.isApplicableWeapon(mainHand)) {
            send(ctx, "§c该物品不适用技能系统！");
            return 0;
        }

        ActiveSkillData data = ActiveSkillRegistry.getById(skillId).orElse(null);
        if (data == null) {
            send(ctx, "§c未知技能ID: " + skillId);
            return 0;
        }

        ActiveSkillNbtHelper.setSkill(mainHand, new ActiveSkillInstance(skillId, level));
        LoreUpdateHelper.updateAllLore(mainHand);
        send(ctx, "§a✔ 已设置技能为 " + data.getDisplayName()
                + " §e" + ActiveSkillNbtHelper.getLevelDisplayNamePublic(level));
        return 1;
    }

    private static int skillClearCooldown(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

        ItemStack mainHand = target.getMainHandItem();
        if (!ActiveSkillNbtHelper.hasSkill(mainHand)) {
            send(ctx, "§c该武器没有技能！");
            return 0;
        }

        ActiveSkillNbtHelper.setCooldownEnd(mainHand, 0L);
        send(ctx, "§a✔ 已清除技能冷却");
        return 1;
    }

    // ========== 辅助方法 ==========

    private static void send(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().sendSystemMessage(Component.literal(message));
    }

    private static ItemStack getArmorBySlotName(ServerPlayer player, String slotName) {
        return switch (slotName) {
            case "head" -> player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
            case "chest" -> player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
            case "legs" -> player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
            case "feet" -> player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);
            case "mainhand" -> player.getMainHandItem();
            default -> null;
        };
    }
}