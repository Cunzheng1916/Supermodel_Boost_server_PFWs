package com.pfws.supermodelboost;

import com.pfws.supermodelboost.armor.ArmorLoreRefreshHelper;
import com.pfws.supermodelboost.armor.ArmorTraitData;
import com.pfws.supermodelboost.armor.ArmorTraitInstance;
import com.pfws.supermodelboost.armor.ArmorTraitNbtHelper;
import com.pfws.supermodelboost.armor.ArmorTraitRegistry;
import com.pfws.supermodelboost.armor.ArmorTraitRollEngine;
import com.pfws.supermodelboost.config.ConfigManager;
import com.pfws.supermodelboost.weapon.WeaponTraitInstance;
import com.pfws.supermodelboost.weapon.WeaponTraitNbtHelper;
import com.pfws.supermodelboost.weapon.WeaponTraitRegistry;
import com.pfws.supermodelboost.weapon.WeaponTraitRollEngine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import java.util.*;

/**
 * 结构处理器 - 检测两种强化台结构并处理强化/重置
 * 
 * 结构一（武器强化台）：两个附魔台夹着发射器，发射器上方有石头/木质按钮
 * 结构二（护甲强化台）：两个铁砧夹着发射器，发射器上方有石头/木质按钮
 * 
 * @author PFWs
 */
public final class StructureHandler {
    private static final int CENTER_SLOT = 4;
    private static final int[] CARDINAL_SLOTS = {1, 3, 5, 7};
    private static final int[] CORNER_SLOTS = {0, 2, 6, 8};

    private StructureHandler() {}

    /**
     * 按钮点击事件 - 入口
     */
    public static InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (world.isClientSide()) return InteractionResult.PASS;

        BlockPos clickedPos = hitResult.getBlockPos();
        BlockState clickedState = world.getBlockState(clickedPos);

        // 必须是按钮
        if (!(clickedState.getBlock() instanceof ButtonBlock)) {
            return InteractionResult.PASS;
        }

        // 查找发射器位置
        BlockPos dispenserPos = findAttachedDispenser(world, clickedPos, clickedState);
        if (dispenserPos == null) return InteractionResult.PASS;

        BlockState dispenserState = world.getBlockState(dispenserPos);
        if (!(dispenserState.getBlock() instanceof DispenserBlock)) return InteractionResult.PASS;
        if (dispenserState.getValue(DispenserBlock.FACING) != Direction.UP) return InteractionResult.PASS;

        // 判断结构类型
        StructureType type = detectStructureType(world, dispenserPos);

        if (type == StructureType.WEAPON) {
            if (!ConfigManager.get().enableWeaponForge) return InteractionResult.PASS;
            BlockEntity be = world.getBlockEntity(dispenserPos);
            if (!(be instanceof DispenserBlockEntity dispenser)) return InteractionResult.PASS;
            processWeaponForge(player, dispenser, world, dispenserPos);
            return InteractionResult.SUCCESS;
        }

        if (type == StructureType.ARMOR) {
            if (!ConfigManager.get().enableArmorForge) return InteractionResult.PASS;
            BlockEntity be = world.getBlockEntity(dispenserPos);
            if (!(be instanceof DispenserBlockEntity dispenser)) return InteractionResult.PASS;
            processArmorForge(player, dispenser, world, dispenserPos);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    /**
     * 根据按钮附着面找到发射器位置
     */
    private static BlockPos findAttachedDispenser(Level world, BlockPos buttonPos, BlockState buttonState) {
        Direction facing = buttonState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        AttachFace attachFace = buttonState.getValue(BlockStateProperties.ATTACH_FACE);

        BlockPos dispenserPos = null;
        if (attachFace == AttachFace.FLOOR) {
            dispenserPos = buttonPos.below();
        } else if (attachFace == AttachFace.CEILING) {
            dispenserPos = buttonPos.above();
        } else {
            dispenserPos = buttonPos.relative(facing.getOpposite());
        }
        return dispenserPos;
    }

    /**
     * 检测结构类型
     */
    private static StructureType detectStructureType(Level world, BlockPos dispenserPos) {
        // 结构一检查：两侧附魔台
        if (checkHorizontalPair(world, dispenserPos, Blocks.ENCHANTING_TABLE)) {
            return StructureType.WEAPON;
        }

        // 结构二检查：两侧铁砧（含开裂和损坏的铁砧）
        if (checkHorizontalPairAnvil(world, dispenserPos)) {
            return StructureType.ARMOR;
        }

        return StructureType.NONE;
    }

    /**
     * 检查水平方向是否有两个指定的方块
     */
    private static boolean checkHorizontalPair(Level world, BlockPos pos, Block target) {
        boolean north = world.getBlockState(pos.north()).is(target);
        boolean south = world.getBlockState(pos.south()).is(target);
        if (north && south) return true;

        boolean east = world.getBlockState(pos.east()).is(target);
        boolean west = world.getBlockState(pos.west()).is(target);
        return east && west;
    }

    /**
     * 检查水平方向是否有两个铁砧（任意类型）
     */
    private static boolean checkHorizontalPairAnvil(Level world, BlockPos pos) {
        boolean north = world.getBlockState(pos.north()).getBlock() instanceof AnvilBlock;
        boolean south = world.getBlockState(pos.south()).getBlock() instanceof AnvilBlock;
        if (north && south) return true;

        boolean east = world.getBlockState(pos.east()).getBlock() instanceof AnvilBlock;
        boolean west = world.getBlockState(pos.west()).getBlock() instanceof AnvilBlock;
        return east && west;
    }

    /**
     * 判断是否为武器强化台合法结构
     */
    public static boolean isValidWeaponStructure(Level world, BlockPos dispenserPos) {
        BlockState state = world.getBlockState(dispenserPos);
        if (!state.is(Blocks.DISPENSER)) return false;
        if (state.getValue(DispenserBlock.FACING) != Direction.UP) return false;
        return detectStructureType(world, dispenserPos) == StructureType.WEAPON;
    }

    /**
     * 判断是否为护甲强化台合法结构
     */
    public static boolean isValidArmorStructure(Level world, BlockPos dispenserPos) {
        BlockState state = world.getBlockState(dispenserPos);
        if (!state.is(Blocks.DISPENSER)) return false;
        if (state.getValue(DispenserBlock.FACING) != Direction.UP) return false;
        return detectStructureType(world, dispenserPos) == StructureType.ARMOR;
    }

    // ========== 武器强化处理 ==========

    private static void processWeaponForge(Player player, DispenserBlockEntity dispenser, Level world, BlockPos pos) {
        ItemStack centerItem = dispenser.getItem(CENTER_SLOT);
        if (centerItem.isEmpty()) {
            sendMessage(player, "§c✘ 发射器中心没有物品！");
            return;
        }

        String itemId = BuiltInRegistries.ITEM.getKey(centerItem.getItem()).toString();
        if (!ConfigManager.matchesItemPattern(itemId, ConfigManager.get().weapon.applicableItems)) {
            sendMessage(player, "§c✘ 中心物品不适用武器特性系统！");
            return;
        }

        // 判断配方：重置（青金石）还是强化（钻石+青金石）
        String resetMat = ConfigManager.get().weapon.resetMaterial;
        String enhanceMain = ConfigManager.get().weapon.enhanceMainMaterial;
        String enhanceCorner = ConfigManager.get().weapon.enhanceCornerMaterial;

        if (matchesWeaponReset(dispenser, resetMat)) {
            doWeaponReset(player, dispenser, centerItem);
            return;
        }

        if (matchesWeaponEnhance(dispenser, enhanceMain, enhanceCorner)) {
            doWeaponEnhance(player, dispenser, centerItem);
            return;
        }

        sendMessage(player, "§c✘ 配方不正确！重置需要上下左右放" + resetMat + "，强化需要上下左右放" + enhanceMain + "、四角放" + enhanceCorner);
    }

    private static boolean matchesWeaponReset(DispenserBlockEntity dispenser, String materialId) {
        for (int slot : CARDINAL_SLOTS) {
            if (!isItem(dispenser.getItem(slot), materialId)) return false;
        }
        for (int slot : CORNER_SLOTS) {
            if (!dispenser.getItem(slot).isEmpty()) return false;
        }
        return true;
    }

    private static boolean matchesWeaponEnhance(DispenserBlockEntity dispenser, String mainMaterial, String cornerMaterial) {
        for (int slot : CARDINAL_SLOTS) {
            if (!isItem(dispenser.getItem(slot), mainMaterial)) return false;
        }
        for (int slot : CORNER_SLOTS) {
            if (!isItem(dispenser.getItem(slot), cornerMaterial)) return false;
        }
        return true;
    }

    private static void doWeaponReset(Player player, DispenserBlockEntity dispenser, ItemStack weapon) {
        WeaponTraitNbtHelper.clearTraits(weapon);
        WeaponTraitNbtHelper.setEnhanceLevel(weapon, 0);

        ConfigManager.UnifiedConfig config = ConfigManager.get();
        WeaponTraitRollEngine engine = new WeaponTraitRollEngine();
        List<WeaponTraitInstance> newTraits = engine.rollTraits(
                Collections.emptyList(),
                config.weapon.traitProbabilities,
                config.weapon.excludeTraitsConflict
        );

        ItemStack result = weapon.copy();
        if (!newTraits.isEmpty()) {
            WeaponTraitNbtHelper.setTraits(result, newTraits);
        }

        dispenser.setItem(CENTER_SLOT, result);
        for (int slot : CARDINAL_SLOTS) dispenser.getItem(slot).shrink(1);
        dispenser.setChanged();

        StringBuilder sb = new StringBuilder("§a✔ 武器重置成功！特性：");
        for (WeaponTraitInstance t : newTraits) {
            sb.append(" §e").append(WeaponTraitRegistry.getDisplayName(t.getId()))
              .append(" Lv.").append(t.getLevel());
        }
        sendMessage(player, sb.toString());
        SupermodelBoostMod.LOGGER.info("Player {} 重置武器特性: {}", player.getName().getString(), sb);
    }

    private static void doWeaponEnhance(Player player, DispenserBlockEntity dispenser, ItemStack weapon) {
        int currentLevel = WeaponTraitNbtHelper.getEnhanceLevel(weapon);
        int maxLevel = ConfigManager.get().weapon.maxEnhanceLevel;
        if (currentLevel >= maxLevel) {
            sendMessage(player, "§c✘ 已达最高强化等级（" + WeaponTraitNbtHelper.getEnhanceLevelDisplayName(maxLevel) + "）！");
            return;
        }

        List<WeaponTraitInstance> traits = WeaponTraitNbtHelper.getTraits(weapon);

        ItemStack result = weapon.copy();

        // 先尝试升级已有特性
        WeaponTraitRollEngine engine = new WeaponTraitRollEngine();
        Optional<WeaponTraitInstance> upgraded = engine.rollUpgrade(traits);
        if (upgraded.isPresent()) {
            WeaponTraitInstance up = upgraded.get();
            List<WeaponTraitInstance> newTraits = new ArrayList<>();
            for (WeaponTraitInstance t : traits) {
                if (t.getId().equals(up.getId())) {
                    newTraits.add(up);
                } else {
                    newTraits.add(t);
                }
            }
            WeaponTraitNbtHelper.setTraits(result, newTraits);
            WeaponTraitNbtHelper.incrementEnhanceLevel(result);

            consumeEnhanceMaterials(dispenser, result);
            sendMessage(player, "§a✔ 强化成功！§6" + WeaponTraitRegistry.getDisplayName(up.getId())
                    + " §7升级至 §bLv." + up.getLevel()
                    + " §7强化等级: " + WeaponTraitNbtHelper.getEnhanceLevelColor(WeaponTraitNbtHelper.getEnhanceLevel(result))
                    + WeaponTraitNbtHelper.getEnhanceLevelDisplayName(WeaponTraitNbtHelper.getEnhanceLevel(result)));
            return;
        }

        // 所有特性已满级，尝试新增特性
        int maxTraits = ConfigManager.get().weapon.maxTraits;
        if (traits.size() >= maxTraits) {
            sendMessage(player, "§c✘ 特性数已达上限（" + maxTraits + "），且所有特性已满级，无法强化！");
            return;
        }

        List<String> existingIds = WeaponTraitNbtHelper.getTraitIds(weapon);
        List<WeaponTraitInstance> newRoll = engine.rollTraits(existingIds, new double[]{1.0},
                ConfigManager.get().weapon.excludeTraitsConflict);
        if (!newRoll.isEmpty()) {
            WeaponTraitInstance newTrait = newRoll.get(0);
            WeaponTraitNbtHelper.addTrait(result, newTrait);
            WeaponTraitNbtHelper.incrementEnhanceLevel(result);

            consumeEnhanceMaterials(dispenser, result);
            sendMessage(player, "§a✔ 强化成功！获得新特性 §6" + WeaponTraitRegistry.getDisplayName(newTrait.getId())
                    + " §bLv." + newTrait.getLevel()
                    + " §7强化等级: " + WeaponTraitNbtHelper.getEnhanceLevelColor(WeaponTraitNbtHelper.getEnhanceLevel(result))
                    + WeaponTraitNbtHelper.getEnhanceLevelDisplayName(WeaponTraitNbtHelper.getEnhanceLevel(result)));
            return;
        }

        sendMessage(player, "§c✘ 无法强化！所有特性已满级，且没有可新增的特性");
    }

    private static void consumeWeaponEnhanceMaterials(DispenserBlockEntity dispenser, ItemStack result) {
        for (int slot : CARDINAL_SLOTS) dispenser.getItem(slot).shrink(1);
        for (int slot : CORNER_SLOTS) dispenser.getItem(slot).shrink(1);
        dispenser.setItem(CENTER_SLOT, result);
        dispenser.setChanged();
    }

    // ========== 护甲强化处理 ==========

    private static void processArmorForge(Player player, DispenserBlockEntity dispenser, Level world, BlockPos pos) {
        ItemStack centerItem = dispenser.getItem(CENTER_SLOT);
        if (centerItem.isEmpty()) {
            sendMessage(player, "§c✘ 发射器中心没有物品！请在中心放入护甲");
            return;
        }

        String itemId = BuiltInRegistries.ITEM.getKey(centerItem.getItem()).toString();
        if (!ConfigManager.matchesItemPattern(itemId, ConfigManager.get().armor.applicableItems)) {
            sendMessage(player, "§c✘ 中心物品不是可强化的护甲！");
            return;
        }

        ConfigManager.ArmorConfigSection cfg = ConfigManager.get().armor;

        // 判断配方：重置、升级、重铸
        if (matchesArmorReset(dispenser, cfg.resetMaterial)) {
            doArmorReset(player, dispenser, centerItem);
            return;
        }
        if (matchesArmorUpgrade(dispenser, cfg.upgradeMaterial, cfg.upgradeCornerMaterial)) {
            doArmorUpgrade(player, dispenser, centerItem);
            return;
        }
        if (matchesArmorRecast(dispenser, cfg.recastMaterial, cfg.recastCornerMaterial)) {
            doArmorRecast(player, dispenser, centerItem);
            return;
        }

        sendMessage(player, "§c✘ 配方不正确！");
    }

    private static boolean matchesArmorReset(DispenserBlockEntity dispenser, String material) {
        for (int slot : CARDINAL_SLOTS) {
            if (!isItem(dispenser.getItem(slot), material)) return false;
        }
        for (int slot : CORNER_SLOTS) {
            if (!dispenser.getItem(slot).isEmpty()) return false;
        }
        return true;
    }

    private static boolean matchesArmorUpgrade(DispenserBlockEntity dispenser, String surround, String corners) {
        for (int slot : CARDINAL_SLOTS) {
            if (!isItem(dispenser.getItem(slot), surround)) return false;
        }
        for (int slot : CORNER_SLOTS) {
            if (!isItem(dispenser.getItem(slot), corners)) return false;
        }
        return true;
    }

    private static boolean matchesArmorRecast(DispenserBlockEntity dispenser, String surround, String corners) {
        return matchesArmorUpgrade(dispenser, surround, corners);
    }

    private static void doArmorReset(Player player, DispenserBlockEntity dispenser, ItemStack armor) {
        ItemStack result = armor.copy();
        ArmorTraitNbtHelper.setTraits(result, new ArrayList<>());
        ArmorTraitNbtHelper.setTotalReinforceLevel(result, 0);

        ConfigManager.ArmorConfigSection cfg = ConfigManager.get().armor;
        List<ArmorTraitInstance> newTraits = ArmorTraitRollEngine.rollTraitSet(cfg.minTraits, cfg.maxTraits);
        ArmorTraitNbtHelper.setTraits(result, newTraits);

        ArmorLoreRefreshHelper.refreshLore(result);

        dispenser.setItem(CENTER_SLOT, result);
        for (int slot : CARDINAL_SLOTS) dispenser.getItem(slot).shrink(1);
        dispenser.setChanged();

        StringBuilder sb = new StringBuilder("§a✔ 护甲重置成功！特性：");
        for (ArmorTraitInstance ti : newTraits) {
            ArmorTraitData t = ArmorTraitRegistry.getById(ti.getId()).orElse(null);
            if (t != null) sb.append(" §e").append(t.getDisplayName()).append(" Lv.").append(ti.getLevel());
        }
        sendMessage(player, sb.toString());
        SupermodelBoostMod.LOGGER.info("Player {} 重置护甲特性: {}", player.getName().getString(), sb);
    }

    private static void doArmorUpgrade(Player player, DispenserBlockEntity dispenser, ItemStack armor) {
        List<ArmorTraitInstance> currentTraits = ArmorTraitNbtHelper.getTraits(armor);
        if (currentTraits.isEmpty()) {
            sendMessage(player, "§c✘ 此护甲没有特性，无法强化！请先进行重置");
            return;
        }

        ItemStack result = armor.copy();

        // 先尝试升级已有特性
        Optional<ArmorTraitInstance> toUpgrade = ArmorTraitRollEngine.rollUpgradeTrait(currentTraits);

        if (toUpgrade.isPresent()) {
            ArmorTraitInstance target = toUpgrade.get();
            List<ArmorTraitInstance> newTraits = new ArrayList<>();
            for (ArmorTraitInstance ti : currentTraits) {
                if (ti.getId().equals(target.getId())) {
                    newTraits.add(new ArmorTraitInstance(target.getId(), ti.getLevel() + 1));
                } else {
                    newTraits.add(ti);
                }
            }
            ArmorTraitNbtHelper.setTraits(result, newTraits);
            int totalLv = ArmorTraitNbtHelper.getTotalReinforceLevel(armor);
            ArmorTraitNbtHelper.setTotalReinforceLevel(result, totalLv + 1);
            ArmorLoreRefreshHelper.refreshLore(result);

            consumeArmorUpgradeMaterials(dispenser, result);

            ArmorTraitData td = ArmorTraitRegistry.getById(target.getId()).orElse(null);
            sendMessage(player, "§a✔ 护甲强化成功！§6" + (td != null ? td.getDisplayName() : target.getId())
                    + " §7升级至 §bLv." + (target.getLevel() + 1));
            return;
        }

        // 所有特性已满级，尝试新增特性
        int maxTraits = ConfigManager.get().armor.maxTraits;
        if (currentTraits.size() >= maxTraits) {
            sendMessage(player, "§c✘ 特性数已达上限（" + maxTraits + "），且所有特性已满级，无法强化！");
            return;
        }

        List<ArmorTraitInstance> newTraits = ArmorTraitRollEngine.rollTraits(1, currentTraits);
        if (newTraits.size() > currentTraits.size()) {
            ArmorTraitNbtHelper.setTraits(result, newTraits);
            int totalLv = ArmorTraitNbtHelper.getTotalReinforceLevel(armor);
            ArmorTraitNbtHelper.setTotalReinforceLevel(result, totalLv + 1);
            ArmorLoreRefreshHelper.refreshLore(result);

            consumeArmorUpgradeMaterials(dispenser, result);

            ArmorTraitInstance added = newTraits.get(newTraits.size() - 1);
            ArmorTraitData td = ArmorTraitRegistry.getById(added.getId()).orElse(null);
            sendMessage(player, "§a✔ 护甲强化成功！获得新特性 §6" + (td != null ? td.getDisplayName() : added.getId())
                    + " §bLv." + added.getLevel());
            return;
        }

        sendMessage(player, "§c✘ 所有特性已满级，且没有可新增的特性，无法强化！");
    }

    private static void doArmorRecast(Player player, DispenserBlockEntity dispenser, ItemStack armor) {
        // 重铸：保留已有特性，再随机追加新特性
        ItemStack result = armor.copy();
        List<ArmorTraitInstance> currentTraits = ArmorTraitNbtHelper.getTraits(armor);
        int maxTraits = ConfigManager.get().armor.maxTraits;

        if (currentTraits.size() >= maxTraits) {
            sendMessage(player, "§c✘ 特性数已达上限（" + maxTraits + "），无法重铸！");
            return;
        }

        List<ArmorTraitInstance> newTraits = ArmorTraitRollEngine.rollTraits(1, currentTraits);
        if (newTraits.size() > currentTraits.size()) {
            ArmorTraitNbtHelper.setTraits(result, newTraits);
            int totalLv = ArmorTraitNbtHelper.getTotalReinforceLevel(armor);
            ArmorTraitNbtHelper.setTotalReinforceLevel(result, totalLv + 1);
            ArmorLoreRefreshHelper.refreshLore(result);

            consumeArmorRecastMaterials(dispenser, result);

            // 找出新增的特性
            ArmorTraitInstance added = newTraits.get(newTraits.size() - 1);
            ArmorTraitData td = ArmorTraitRegistry.getById(added.getId()).orElse(null);
            sendMessage(player, "§a✔ 重铸成功！新增 §6" + (td != null ? td.getDisplayName() : added.getId())
                    + " §bLv." + added.getLevel());
            return;
        }

        sendMessage(player, "§c✘ 重铸失败！没有可用的新特性");
    }

    // ========== 材料消耗 ==========

    private static void consumeArmorUpgradeMaterials(DispenserBlockEntity dispenser, ItemStack result) {
        for (int slot : CARDINAL_SLOTS) dispenser.getItem(slot).shrink(1);
        for (int slot : CORNER_SLOTS) dispenser.getItem(slot).shrink(1);
        dispenser.setItem(CENTER_SLOT, result);
        dispenser.setChanged();
    }

    private static void consumeArmorRecastMaterials(DispenserBlockEntity dispenser, ItemStack result) {
        consumeArmorUpgradeMaterials(dispenser, result);
    }

    private static void consumeEnhanceMaterials(DispenserBlockEntity dispenser, ItemStack result) {
        for (int slot : CARDINAL_SLOTS) dispenser.getItem(slot).shrink(1);
        for (int slot : CORNER_SLOTS) dispenser.getItem(slot).shrink(1);
        dispenser.setItem(CENTER_SLOT, result);
        dispenser.setChanged();
    }

    // ========== 工具方法 ==========

    private static boolean isItem(ItemStack stack, String itemId) {
        if (stack.isEmpty()) return false;
        String actualId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return actualId.equals(itemId);
    }

    private static void sendMessage(Player player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }

    /**
     * 结构类型枚举
     */
    enum StructureType {
        NONE,
        WEAPON,  // 附魔台+发射器+附魔台 + 按钮
        ARMOR    // 铁砧+发射器+铁砧 + 按钮
    }
}
