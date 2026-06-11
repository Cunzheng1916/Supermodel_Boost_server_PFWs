package com.pfws.supermodelboost.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 武器特性NBT辅助类 - 使用Data Components API读写武器特性数据
 * 
 * NBT结构:
 * { "weapon_traits": [{ "id": "lifesteal", "level": 2 }] }
 * { "weapon_enhance_level": 3 }
 * 
 * @author PFWs
 */
public final class WeaponTraitNbtHelper {
    public static final String TRAIT_KEY = "weapon_traits";
    public static final String ENHANCE_KEY = "weapon_enhance_level";
    public static final int MAX_ENHANCE_LEVEL = 5;

    private WeaponTraitNbtHelper() {}

    // ========== 特性读写 ==========

    /**
     * 从物品读取特性列表
     */
    public static List<WeaponTraitInstance> getTraits(ItemStack stack) {
        if (stack.isEmpty()) return Collections.emptyList();
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (data.isEmpty()) return Collections.emptyList();
        CompoundTag nbt = data.copyTag();
        if (!nbt.contains(TRAIT_KEY)) return Collections.emptyList();

        ListTag list = nbt.getList(TRAIT_KEY).orElse(null);
        if (list == null) return Collections.emptyList();

        List<WeaponTraitInstance> traits = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i).orElse(null);
            if (tag == null) continue;
            String id = tag.getStringOr("id", "");
            int level = tag.getIntOr("level", 0);
            if (!id.isEmpty() && level > 0) {
                traits.add(new WeaponTraitInstance(id, level));
            }
        }
        return traits;
    }

    /**
     * 将特性列表写入物品
     */
    public static void setTraits(ItemStack stack, List<WeaponTraitInstance> traits) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, compoundTag -> {
            compoundTag.remove(TRAIT_KEY);
            if (traits.isEmpty()) return;
            ListTag list = new ListTag();
            for (WeaponTraitInstance t : traits) {
                CompoundTag tc = new CompoundTag();
                tc.putString("id", t.getId());
                tc.putInt("level", t.getLevel());
                list.add(tc);
            }
            compoundTag.put(TRAIT_KEY, list);
        });
        updateLore(stack);
    }

    /**
     * 获取某特性的等级
     */
    public static int getTraitLevel(ItemStack stack, String traitId) {
        for (WeaponTraitInstance t : getTraits(stack)) {
            if (t.getId().equals(traitId)) return t.getLevel();
        }
        return 0;
    }

    /**
     * 检查物品是否有某特性
     */
    public static boolean hasTrait(ItemStack stack, String traitId) {
        return getTraitLevel(stack, traitId) > 0;
    }

    /**
     * 添加一条特性（存在则更新等级）
     */
    public static void addTrait(ItemStack stack, WeaponTraitInstance trait) {
        List<WeaponTraitInstance> traits = new ArrayList<>(getTraits(stack));
        boolean found = false;
        for (int i = 0; i < traits.size(); i++) {
            if (traits.get(i).getId().equals(trait.getId())) {
                traits.set(i, trait);
                found = true;
                break;
            }
        }
        if (!found) {
            traits.add(trait);
        }
        setTraits(stack, traits);
    }

    /**
     * 删除一条特性
     */
    public static boolean removeTrait(ItemStack stack, String traitId) {
        List<WeaponTraitInstance> traits = new ArrayList<>(getTraits(stack));
        boolean removed = traits.removeIf(t -> t.getId().equals(traitId));
        if (removed) {
            setTraits(stack, traits);
        }
        return removed;
    }

    /**
     * 清空所有特性
     */
    public static void clearTraits(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, compoundTag -> {
            compoundTag.remove(TRAIT_KEY);
        });
        updateLore(stack);
    }

    /**
     * 设置某特性的等级
     */
    public static void setTraitLevel(ItemStack stack, String traitId, int level) {
        if (level <= 0) {
            removeTrait(stack, traitId);
            return;
        }
        addTrait(stack, new WeaponTraitInstance(traitId, level));
    }

    /**
     * 获取已有特性ID列表
     */
    public static List<String> getTraitIds(ItemStack stack) {
        return getTraits(stack).stream().map(WeaponTraitInstance::getId).toList();
    }

    /**
     * 是否含有任何特性
     */
    public static boolean hasAnyTrait(ItemStack stack) {
        return !getTraits(stack).isEmpty();
    }

    // ========== 强化等级 ==========

    /**
     * 获取强化等级 (0=未强化, 1-5=一转~五转)
     */
    public static int getEnhanceLevel(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (data.isEmpty()) return 0;
        CompoundTag nbt = data.copyTag();
        return nbt.getIntOr(ENHANCE_KEY, 0);
    }

    /**
     * 设置强化等级
     */
    public static void setEnhanceLevel(ItemStack stack, int level) {
        int clamped = Math.clamp(level, 0, MAX_ENHANCE_LEVEL);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, compoundTag -> {
            if (clamped <= 0) {
                compoundTag.remove(ENHANCE_KEY);
            } else {
                compoundTag.putInt(ENHANCE_KEY, clamped);
            }
        });
        updateLore(stack);
    }

    /**
     * 强化等级+1
     */
    public static int incrementEnhanceLevel(ItemStack stack) {
        int current = getEnhanceLevel(stack);
        if (current >= MAX_ENHANCE_LEVEL) return -1;
        int next = current + 1;
        setEnhanceLevel(stack, next);
        return next;
    }

    /**
     * 获取强化等级显示名称
     */
    public static String getEnhanceLevelDisplayName(int level) {
        return switch (level) {
            case 0 -> "未强化";
            case 1 -> "一转";
            case 2 -> "二转";
            case 3 -> "三转";
            case 4 -> "四转";
            case 5 -> "五转";
            default -> "未知";
        };
    }

    /**
     * 获取强化等级颜色
     */
    public static ChatFormatting getEnhanceLevelColor(int level) {
        return switch (level) {
            case 1 -> ChatFormatting.WHITE;
            case 2 -> ChatFormatting.GREEN;
            case 3 -> ChatFormatting.BLUE;
            case 4 -> ChatFormatting.LIGHT_PURPLE;
            case 5 -> ChatFormatting.GOLD;
            default -> ChatFormatting.GRAY;
        };
    }

    // ========== Lore更新 ==========

    /**
     * 刷新物品Lore显示 - 显示武器特性信息
     */
    public static void updateLore(ItemStack stack) {
        ItemLore currentLore = stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
        List<Component> lines = new ArrayList<>(currentLore.lines());

        // 移除旧的武器特性区域
        while (!lines.isEmpty()) {
            Component last = lines.get(lines.size() - 1);
            String text = last.getString();
            if (text.contains("⚔ 武器特性") || text.contains("✦ 强化等级")
                    || text.startsWith("  ◆ ") || text.startsWith("     ↳ ") || text.isEmpty()) {
                lines.remove(lines.size() - 1);
            } else {
                break;
            }
        }

        List<WeaponTraitInstance> traits = getTraits(stack);
        int enhanceLevel = getEnhanceLevel(stack);

        if (traits.isEmpty() && enhanceLevel <= 0) {
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

        // 空行分隔
        if (!lines.isEmpty() && !lines.get(lines.size() - 1).getString().isEmpty()) {
            lines.add(Component.empty());
        }

        // 强化等级
        if (enhanceLevel > 0) {
            ChatFormatting enhanceColor = getEnhanceLevelColor(enhanceLevel);
            String enhanceName = getEnhanceLevelDisplayName(enhanceLevel);
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

        stack.set(DataComponents.LORE, new ItemLore(lines));
    }
}
