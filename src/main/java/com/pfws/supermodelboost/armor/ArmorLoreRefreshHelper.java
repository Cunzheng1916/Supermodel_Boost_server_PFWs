package com.pfws.supermodelboost.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * 护甲物品Lore刷新辅助类
 * 
 * @author PFWs
 */
public final class ArmorLoreRefreshHelper {

    private ArmorLoreRefreshHelper() {}

    /**
     * 刷新护甲的Lore显示
     */
    public static void refreshLore(ItemStack stack) {
        if (stack.isEmpty()) return;
        List<ArmorTraitInstance> traits = ArmorTraitNbtHelper.getTraits(stack);

        ItemLore currentLore = stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
        List<Component> lines = new ArrayList<>();

        lines.add(Component.literal("🛡 护甲特性 🛡").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        if (!traits.isEmpty()) {
            for (ArmorTraitInstance trait : traits) {
                ArmorTraitData armorTrait = ArmorTraitRegistry.getById(trait.getId()).orElse(null);
                if (armorTrait == null) continue;
                lines.add(Component.empty()
                        .append(Component.literal("◆ ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(armorTrait.getDisplayName() + " Lv." + trait.getLevel())
                                .withStyle(ChatFormatting.GOLD)));
                lines.add(Component.empty()
                        .append(Component.literal("  ↳ ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(armorTrait.describe(trait.getLevel()))
                                .withStyle(ChatFormatting.GRAY)));
            }
        } else {
            lines.add(Component.literal("无特性").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        int reinforceLevel = ArmorTraitNbtHelper.getTotalReinforceLevel(stack);
        if (reinforceLevel > 0) {
            String romanLevel = toRoman(reinforceLevel);
            lines.add(Component.literal("⭐ 强化等级: " + romanLevel).withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        stack.set(DataComponents.LORE, new ItemLore(lines));
    }

    /**
     * 数字转罗马数字
     */
    private static String toRoman(int n) {
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (n >= values[i]) {
                sb.append(symbols[i]);
                n -= values[i];
            }
        }
        return sb.toString();
    }
}
