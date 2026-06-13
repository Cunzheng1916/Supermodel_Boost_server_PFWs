package com.pfws.supermodelboost;

import com.pfws.supermodelboost.weapon.WeaponTraitData;
import com.pfws.supermodelboost.weapon.WeaponTraitInstance;
import com.pfws.supermodelboost.weapon.WeaponTraitNbtHelper;
import com.pfws.supermodelboost.weapon.WeaponTraitRegistry;
import com.pfws.supermodelboost.weapon.skill.ActiveSkillData;
import com.pfws.supermodelboost.weapon.skill.ActiveSkillInstance;
import com.pfws.supermodelboost.weapon.skill.ActiveSkillNbtHelper;
import com.pfws.supermodelboost.weapon.skill.ActiveSkillRegistry;
import com.pfws.supermodelboost.weapon.skill.SkillRollEngine;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一Lore更新工具
 * 
 * @author PFWs
 */
public final class LoreUpdateHelper {

    private LoreUpdateHelper() {}

    public static void updateAllLore(ItemStack stack) {
        ItemLore currentLore = stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
        List<Component> lines = new ArrayList<>(currentLore.lines());

        clearAllModLore(lines);

        List<WeaponTraitInstance> traits = WeaponTraitNbtHelper.getTraits(stack);
        int enhanceLevel = WeaponTraitNbtHelper.getEnhanceLevel(stack);
        boolean hasTraits = !traits.isEmpty() || enhanceLevel > 0;

        ActiveSkillInstance skill = ActiveSkillNbtHelper.getSkill(stack);
        int skillXp = ActiveSkillNbtHelper.getSkillXp(stack);
        String tendency = ActiveSkillNbtHelper.getTendency(stack);
        boolean isApplicableWeapon = SkillRollEngine.isApplicableWeapon(stack);
        boolean hasSkillInfo = skill != null || skillXp > 0 || tendency != null
                || (enhanceLevel >= 5 && isApplicableWeapon);

        if (!hasTraits && !hasSkillInfo) {
            while (!lines.isEmpty() && lines.get(lines.size() - 1).getString().isEmpty()) {
                lines.remove(lines.size() - 1);
            }
            if (lines.isEmpty()) {
                stack.remove(DataComponents.LORE);
            } else {
                stack.set(DataComponents.LORE, new ItemLore(lines));
            }
            return;
        }

        if (!lines.isEmpty() && !lines.get(lines.size() - 1).getString().isEmpty()) {
            lines.add(Component.empty());
        }

        if (enhanceLevel > 0) {
            ChatFormatting enhanceColor = WeaponTraitNbtHelper.getEnhanceLevelColor(enhanceLevel);
            String enhanceName = WeaponTraitNbtHelper.getEnhanceLevelDisplayName(enhanceLevel);
            lines.add(Component.empty()
                    .append(Component.literal("✦ 强化等级 ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(enhanceName).withStyle(enhanceColor, ChatFormatting.BOLD)));
        }

        if (!traits.isEmpty()) {
            lines.add(Component.empty());
            lines.add(Component.literal("⚔ 武器特性 ⚔").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            for (WeaponTraitInstance trait : traits) {
                WeaponTraitData data = WeaponTraitRegistry.getById(trait.getId()).orElse(null);
                if (data == null) continue;
                lines.add(Component.empty()
                        .append(Component.literal("  ◆ ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(data.getDisplayName()).withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(" Lv." + trait.getLevel()).withStyle(ChatFormatting.AQUA)));
                lines.add(Component.empty()
                        .append(Component.literal("     ↳ ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(data.describe(trait.getLevel())).withStyle(ChatFormatting.GRAY)));
            }
        }

        if (hasSkillInfo) {
            lines.add(Component.empty());
            lines.add(Component.literal("⚡ 主动技能系统 ⚡").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));

            if (skill != null) {
                ActiveSkillData skillData = ActiveSkillRegistry.getById(skill.skillId()).orElse(null);
                String skillName = skillData != null ? skillData.getDisplayName() : skill.skillId();
                int lv = skill.getLevel();
                lines.add(Component.empty()
                        .append(Component.literal("  技能: ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(skillName).withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(" " + ActiveSkillNbtHelper.getLevelDisplayNamePublic(lv))
                                .withStyle(ActiveSkillNbtHelper.getLevelColorPublic(lv))));

                if (skillData != null) {
                    lines.add(Component.empty()
                            .append(Component.literal("     " + skillData.describe(lv)).withStyle(ChatFormatting.GRAY)));
                }
            } else if (enhanceLevel >= 5) {
                lines.add(Component.empty()
                        .append(Component.literal("  状态: ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("奇器零转 - 击杀怪物觉醒").withStyle(ChatFormatting.DARK_GREEN)));
            }

            if (tendency != null && !tendency.isEmpty()) {
                String tendencyName = getTendencyDisplayName(tendency);
                lines.add(Component.empty()
                        .append(Component.literal("  倾向: ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(tendencyName).withStyle(ChatFormatting.LIGHT_PURPLE)));
            }

            int displayLevel = (skill != null) ? skill.getLevel() : 0;
            if (displayLevel >= 5) {
                lines.add(Component.empty()
                        .append(Component.literal("  经验: ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(skillXp + " (已满级)").withStyle(ChatFormatting.DARK_GREEN)));
            } else {
                int nextLv = displayLevel + 1;
                int needed = ActiveSkillNbtHelper.getXpForLevel(nextLv);
                String xpLabel = (skill == null) ? "  觉醒进度: " : "  经验: ";
                lines.add(Component.empty()
                        .append(Component.literal(xpLabel).withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(skillXp + "/" + needed).withStyle(ChatFormatting.DARK_GREEN)));
            }
        }

        stack.set(DataComponents.LORE, new ItemLore(lines));
    }

    private static void clearAllModLore(List<Component> lines) {
        for (int i = lines.size() - 1; i >= 0; i--) {
            Component comp = lines.get(i);
            String text = comp.getString();
            if (text.contains("✦ 强化等级")
                    || text.contains("⚔ 武器特性")
                    || text.contains("⚡ 主动技能系统")
                    || text.startsWith("  ◆ ")
                    || text.startsWith("     ↳ ")
                    || text.startsWith("  技能: ")
                    || text.startsWith("  状态: ")
                    || text.startsWith("  倾向: ")
                    || text.startsWith("  经验: ")
                    || text.startsWith("  觉醒进度: ")
                    || isSkillDescriptionLine(text)
                    || text.isEmpty()) {
                lines.remove(i);
            }
        }
    }

    private static boolean isSkillDescriptionLine(String text) {
        if (!text.startsWith("     ")) return false;
        if (text.startsWith("     ↳ ")) return false;
        return true;
    }

    private static String getTendencyDisplayName(String tendency) {
        return switch (tendency) {
            case ActiveSkillRegistry.VIOLENT -> "§4暴戾";
            case ActiveSkillRegistry.ABUNDANCE -> "§2丰饶";
            case ActiveSkillRegistry.BLOOD -> "§c血使";
            default -> tendency;
        };
    }
}
