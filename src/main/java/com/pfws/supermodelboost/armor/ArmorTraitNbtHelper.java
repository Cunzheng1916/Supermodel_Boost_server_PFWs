package com.pfws.supermodelboost.armor;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

/**
 * 护甲特性NBT辅助类 - 使用Data Components API
 * 
 * NBT结构:
 * { "armor_traits": [{ "id": "iron_wall", "level": 3 }] }
 * { "total_reinforce_level": 2 }
 * 
 * @author PFWs
 */
public final class ArmorTraitNbtHelper {
    private static final String TRAIT_LIST_KEY = "armor_traits";
    private static final String TOTAL_REINFORCE_KEY = "total_reinforce_level";

    private ArmorTraitNbtHelper() {}

    /**
     * 从护甲物品读取特性列表
     */
    public static List<ArmorTraitInstance> getTraits(ItemStack stack) {
        if (stack.isEmpty()) return new ArrayList<>();
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (data.isEmpty()) return new ArrayList<>();
        CompoundTag nbt = data.copyTag();
        if (!nbt.contains(TRAIT_LIST_KEY)) return new ArrayList<>();

        ListTag list = nbt.getList(TRAIT_LIST_KEY).orElse(null);
        if (list == null) return new ArrayList<>();

        List<ArmorTraitInstance> traits = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i).orElse(null);
            if (t == null) continue;
            traits.add(new ArmorTraitInstance(
                    t.getStringOr("id", ""),
                    t.getIntOr("level", 0)));
        }
        return traits;
    }

    /**
     * 将特性列表写入护甲物品
     */
    public static void setTraits(ItemStack stack, List<ArmorTraitInstance> traits) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, nbt -> {
            nbt.remove(TRAIT_LIST_KEY);
            if (traits.isEmpty()) return;
            ListTag list = new ListTag();
            for (ArmorTraitInstance t : traits) {
                CompoundTag tc = new CompoundTag();
                tc.putString("id", t.getId());
                tc.putInt("level", t.getLevel());
                list.add(tc);
            }
            nbt.put(TRAIT_LIST_KEY, list);
        });
    }

    /**
     * 获取总强化等级
     */
    public static int getTotalReinforceLevel(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (data.isEmpty()) return 0;
        CompoundTag nbt = data.copyTag();
        return nbt.getIntOr(TOTAL_REINFORCE_KEY, 0);
    }

    /**
     * 设置总强化等级
     */
    public static void setTotalReinforceLevel(ItemStack stack, int level) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, nbt -> {
            nbt.putInt(TOTAL_REINFORCE_KEY, level);
        });
    }

    /**
     * 获取某特性的等级
     */
    public static int getTraitLevel(ItemStack stack, String traitId) {
        for (ArmorTraitInstance t : getTraits(stack)) {
            if (t.getId().equals(traitId)) return t.getLevel();
        }
        return 0;
    }

    /**
     * 添加一条特性（存在则更新等级）
     */
    public static void addTrait(ItemStack stack, ArmorTraitInstance trait) {
        List<ArmorTraitInstance> traits = new ArrayList<>(getTraits(stack));
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
        List<ArmorTraitInstance> traits = new ArrayList<>(getTraits(stack));
        boolean removed = traits.removeIf(t -> t.getId().equals(traitId));
        if (removed) {
            setTraits(stack, traits);
        }
        return removed;
    }

    /**
     * 设置某特性的等级（不存在则添加）
     */
    public static void setTraitLevel(ItemStack stack, String traitId, int level) {
        if (level <= 0) {
            removeTrait(stack, traitId);
            return;
        }
        addTrait(stack, new ArmorTraitInstance(traitId, level));
    }
}
