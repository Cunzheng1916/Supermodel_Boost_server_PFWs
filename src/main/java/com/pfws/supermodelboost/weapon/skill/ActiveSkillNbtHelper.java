package com.pfws.supermodelboost.weapon.skill;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 主动技能NBT辅助类 - 管理技能相关的CustomData
 * 
 * @author PFWs
 */
public final class ActiveSkillNbtHelper {

    private static final String KEY_SKILL_ID = "supermodel_skill_id";
    private static final String KEY_SKILL_LEVEL = "supermodel_skill_level";
    private static final String KEY_SKILL_XP = "supermodel_skill_xp";
    private static final String KEY_COOLDOWN_END = "supermodel_cooldown_end";
    private static final String KEY_TENDENCY = "supermodel_tendency";

    private static final int[] XP_THRESHOLDS = {1000, 2000, 4000, 8000, 20000};

    private ActiveSkillNbtHelper() {}

    private static CompoundTag getOrCreateTag(ItemStack stack) {
        CustomData cd = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = cd.copyTag();
        if (tag == null) tag = new CompoundTag();
        return tag;
    }

    private static void saveTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static ActiveSkillInstance getSkill(ItemStack stack) {
        CompoundTag tag = getOrCreateTag(stack);
        if (!tag.contains(KEY_SKILL_ID) || !tag.contains(KEY_SKILL_LEVEL)) {
            return null;
        }
        String id = tag.getStringOr(KEY_SKILL_ID, "");
        int level = tag.getIntOr(KEY_SKILL_LEVEL, 0);
        if (id.isEmpty() || level <= 0) return null;
        return new ActiveSkillInstance(id, level);
    }

    public static void setSkill(ItemStack stack, ActiveSkillInstance skill) {
        CompoundTag tag = getOrCreateTag(stack);
        if (skill == null) {
            tag.remove(KEY_SKILL_ID);
            tag.remove(KEY_SKILL_LEVEL);
        } else {
            tag.putString(KEY_SKILL_ID, skill.skillId());
            tag.putInt(KEY_SKILL_LEVEL, skill.getLevel());
        }
        saveTag(stack, tag);
    }

    public static boolean hasSkill(ItemStack stack) {
        return getSkill(stack) != null;
    }

    public static int getSkillXp(ItemStack stack) {
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getIntOr(KEY_SKILL_XP, 0);
    }

    public static void setSkillXp(ItemStack stack, int xp) {
        CompoundTag tag = getOrCreateTag(stack);
        tag.putInt(KEY_SKILL_XP, Math.max(0, xp));
        saveTag(stack, tag);
    }

    public static void addSkillXp(ItemStack stack, int amount) {
        setSkillXp(stack, getSkillXp(stack) + amount);
    }

    public static long getCooldownEnd(ItemStack stack) {
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getLongOr(KEY_COOLDOWN_END, 0L);
    }

    public static void setCooldownEnd(ItemStack stack, long tick) {
        CompoundTag tag = getOrCreateTag(stack);
        tag.putLong(KEY_COOLDOWN_END, tick);
        saveTag(stack, tag);
    }

    public static String getTendency(ItemStack stack) {
        CompoundTag tag = getOrCreateTag(stack);
        if (!tag.contains(KEY_TENDENCY)) return null;
        String t = tag.getStringOr(KEY_TENDENCY, "");
        return t.isEmpty() ? null : t;
    }

    public static void setTendency(ItemStack stack, String tendency) {
        CompoundTag tag = getOrCreateTag(stack);
        if (tendency == null || tendency.isEmpty()) {
            tag.remove(KEY_TENDENCY);
        } else {
            tag.putString(KEY_TENDENCY, tendency);
        }
        saveTag(stack, tag);
    }

    public static int getXpForLevel(int level) {
        if (level <= 0) return 0;
        if (level > XP_THRESHOLDS.length) return Integer.MAX_VALUE;
        return XP_THRESHOLDS[level - 1];
    }

    public static int tryLevelUp(ItemStack stack) {
        ActiveSkillInstance skill = getSkill(stack);
        if (skill == null) return 0;
        int currentLevel = skill.getLevel();
        if (currentLevel >= 5) return currentLevel;
        int needed = getXpForLevel(currentLevel + 1);
        int xp = getSkillXp(stack);
        if (xp < needed) return currentLevel;

        setSkillXp(stack, xp - needed);
        setSkill(stack, new ActiveSkillInstance(skill.skillId(), currentLevel + 1));
        return currentLevel + 1;
    }

    public static String getLevelDisplayNamePublic(int level) {
        return switch (level) {
            case 1 -> "奇器·一转"; case 2 -> "奇器·二转"; case 3 -> "奇器·三转";
            case 4 -> "奇器·四转"; case 5 -> "奇器·五转"; default -> "奇器·?转";
        };
    }

    public static net.minecraft.ChatFormatting getLevelColorPublic(int level) {
        return switch (level) {
            case 1 -> net.minecraft.ChatFormatting.GREEN;
            case 2 -> net.minecraft.ChatFormatting.AQUA;
            case 3 -> net.minecraft.ChatFormatting.LIGHT_PURPLE;
            case 4 -> net.minecraft.ChatFormatting.GOLD;
            case 5 -> net.minecraft.ChatFormatting.RED;
            default -> net.minecraft.ChatFormatting.GRAY;
        };
    }

    public static void clearSkill(ItemStack stack) {
        CompoundTag tag = getOrCreateTag(stack);
        tag.remove(KEY_SKILL_ID);
        tag.remove(KEY_SKILL_LEVEL);
        tag.remove(KEY_SKILL_XP);
        tag.remove(KEY_COOLDOWN_END);
        tag.remove(KEY_TENDENCY);
        saveTag(stack, tag);
    }

    /**
     * @deprecated Use LoreUpdateHelper.updateAllLore() instead.
     */
    @Deprecated
    public static void updateSkillLore(ItemStack stack) {
        com.pfws.supermodelboost.LoreUpdateHelper.updateAllLore(stack);
    }
}
