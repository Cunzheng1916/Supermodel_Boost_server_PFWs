package com.pfws.supermodelboost;

import com.pfws.supermodelboost.armor.ArmorLoreRefreshHelper;
import com.pfws.supermodelboost.armor.ArmorTraitInstance;
import com.pfws.supermodelboost.armor.ArmorTraitNbtHelper;
import com.pfws.supermodelboost.armor.ArmorTraitRegistry;
import com.pfws.supermodelboost.config.ConfigManager;
import com.pfws.supermodelboost.storage.RetaliationStorage;
import com.pfws.supermodelboost.weapon.WeaponTraitEffectHandler;
import com.pfws.supermodelboost.weapon.WeaponTraitNbtHelper;
import com.pfws.supermodelboost.weapon.WeaponTraitRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import java.util.*;

/**
 * 统一事件处理器 - 处理武器和护甲特性相关的所有事件
 * 
 * @author PFWs
 */
public final class EventHandler {
    private static final Map<UUID, Integer> REVIVE_TICK = new HashMap<>();
    private static final Set<UUID> REDO_SET = new HashSet<>();

    private EventHandler() {}

    public static void init() {
        registerWeaponEvents();
        registerArmorEvents();
        registerCommonEvents();
        SupermodelBoostMod.LOGGER.info("事件监听器全部注册完成");
    }

    // ========== 武器事件 ==========

    private static void registerWeaponEvents() {
        // 攻击事件 - 战狂、兴奋、反击释放
        AttackEntityCallback.EVENT.register((Player player, Level level, InteractionHand hand,
                                             Entity target, EntityHitResult hitResult) -> {
            if (level.isClientSide()) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;

            ItemStack mainHand = sp.getMainHandItem();

            // 战狂
            int berserkerLv = WeaponTraitNbtHelper.getTraitLevel(mainHand, WeaponTraitRegistry.BERSERKER);
            if (berserkerLv > 0) {
                WeaponTraitEffectHandler.handleBerserker(sp, berserkerLv);
            }

            // 兴奋
            int exhLv = WeaponTraitNbtHelper.getTraitLevel(mainHand, WeaponTraitRegistry.EXHILARATION);
            if (exhLv > 0) {
                WeaponTraitEffectHandler.handleExhilaration(sp, exhLv);
            }

            // 反击释放
            int retLv = WeaponTraitNbtHelper.getTraitLevel(mainHand, WeaponTraitRegistry.RETALIATION);
            if (retLv > 0 && target instanceof LivingEntity lt) {
                float bonus = WeaponTraitEffectHandler.handleRetaliationRelease(sp, retLv);
                if (bonus > 0) {
                    lt.hurtServer((ServerLevel) level, level.damageSources().playerAttack(sp), bonus);
                }
            }

            return InteractionResult.PASS;
        });

        // 受击事件 - 反击储存
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayer victim) {
                int retLv = WeaponTraitNbtHelper.getTraitLevel(victim.getMainHandItem(), WeaponTraitRegistry.RETALIATION);
                if (retLv > 0) {
                    WeaponTraitEffectHandler.handleRetaliationStore(victim, amount, retLv);
                }
            }
            return true;
        });

        // 伤害后处理 - 吸血
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, taken, blocked) -> {
            if (source.getEntity() instanceof ServerPlayer attacker) {
                if (taken <= 0) return;
                ItemStack mainHand = attacker.getMainHandItem();
                int ll = WeaponTraitNbtHelper.getTraitLevel(mainHand, WeaponTraitRegistry.LIFESTEAL);
                if (ll > 0) {
                    WeaponTraitEffectHandler.handleLifesteal(attacker, taken, ll);
                }
            }
        });

        // 实体卸载 - 清理反击数据
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayer player && (player.isDeadOrDying() || player.getHealth() <= 0)) {
                RetaliationStorage.clearDamage(player);
            }
        });

        // Tick事件 - 硬朗给工具急迫
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ItemStack mainHand = player.getMainHandItem();
                int tl = WeaponTraitNbtHelper.getTraitLevel(mainHand, WeaponTraitRegistry.TOUGH);
                if (tl <= 0) continue;
                String itemId = BuiltInRegistries.ITEM.getKey(mainHand.getItem()).toString();
                if (WeaponTraitRegistry.isTool(itemId)) {
                    WeaponTraitEffectHandler.handleToughHaste(player, tl);
                }
            }
        });

        // 方块破坏后 - 千手观音
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide()) return;
            if (!(player instanceof ServerPlayer sp)) return;
            ItemStack mainHand = sp.getMainHandItem();
            int ml = WeaponTraitNbtHelper.getTraitLevel(mainHand, WeaponTraitRegistry.MULTI_TOOL);
            if (ml <= 0) return;
            String itemId = BuiltInRegistries.ITEM.getKey(mainHand.getItem()).toString();
            if (WeaponTraitRegistry.isPickaxe(itemId)) {
                WeaponTraitEffectHandler.handleMultiToolPickaxe(sp, pos, sp.getDirection(), ml);
            }
            if (WeaponTraitRegistry.isShovel(itemId)) {
                WeaponTraitEffectHandler.handleMultiToolShovel(sp, ml);
            }
        });
    }

    // ========== 护甲事件 ==========

    private static void registerArmorEvents() {
        // 护甲伤害减免
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.level().isClientSide()) return true;

            UUID uuid = entity.getUUID();
            synchronized (REDO_SET) {
                if (REDO_SET.remove(uuid)) return true; // 重入跳过
            }

            float reduction = 0f;
            for (ItemStack stack : getArmorStacks(entity)) {
                reduction += ArmorTraitRegistry.computeDamageReduction(stack, source);
            }
            reduction = Math.min(reduction, (float) ConfigManager.get().armor.globalMaxDamageReduction);
            if (reduction <= 0f) return true;

            float newAmount = amount * (1f - reduction);
            if (newAmount <= 0f) return false;

            synchronized (REDO_SET) {
                REDO_SET.add(uuid);
            }
            entity.hurtServer((ServerLevel) entity.level(), source, newAmount);
            return false;
        });

        // 护甲伤害后效果
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, taken, blocked) -> {
            if (entity.level().isClientSide()) return;
            for (ItemStack stack : getArmorStacks(entity)) {
                ArmorTraitRegistry.applyAfterDamageEffects(stack, entity, source, taken);
            }
        });
    }

    // ========== 通用事件 ==========

    private static void registerCommonEvents() {
        // 玩家进服 - 补全Lore
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            server.execute(() -> refreshPlayerItems(player));
        });

        // Tick - 轻盈效果、复苏效果
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.level().isClientSide()) continue;

                // 轻盈
                ArmorTraitRegistry.refreshLightweightModifier(player, getArmorStacks(player));

                // 复苏
                tickRevive(player);

                // 刷新Lore
                refreshPlayerItemsSilent(player);
            }
        });
    }

    public static void cleanup() {
        RetaliationStorage.clearAll();
        REVIVE_TICK.clear();
    }

    // ========== 辅助方法 ==========

    /**
     * 获取实体的护甲物品列表
     */
    public static List<ItemStack> getArmorStacks(LivingEntity entity) {
        List<ItemStack> list = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                list.add(entity.getItemBySlot(slot));
            }
        }
        return list;
    }

    /**
     * 刷新玩家所有物品的Lore
     */
    private static void refreshPlayerItems(ServerPlayer player) {
        int fixed = 0;
        // 扫描背包
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            if (refreshItemLore(stack)) fixed++;
        }

        // 副手
        if (refreshItemLore(player.getOffhandItem())) fixed++;

        if (fixed > 0) {
            SupermodelBoostMod.LOGGER.debug("Player {}: 已补全 {} 个物品的Lore",
                    player.getName().getString(), fixed);
        }
    }

    /**
     * 静默刷新（不计数，仅刷新Lore）
     */
    private static void refreshPlayerItemsSilent(ServerPlayer player) {
        for (ItemStack stack : getArmorStacks(player)) {
            if (!stack.isEmpty() && matchesArmorPattern(stack)) {
                ArmorLoreRefreshHelper.refreshLore(stack);
            }
        }
    }

    /**
     * 刷新单个物品的Lore
     */
    private static boolean refreshItemLore(ItemStack stack) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        // 武器
        if (ConfigManager.matchesItemPattern(itemId, ConfigManager.get().weapon.applicableItems)) {
            if (WeaponTraitNbtHelper.hasAnyTrait(stack) || WeaponTraitNbtHelper.getEnhanceLevel(stack) > 0) {
                WeaponTraitNbtHelper.updateLore(stack);
                return true;
            }
        }

        // 护甲
        if (ConfigManager.matchesItemPattern(itemId, ConfigManager.get().armor.applicableItems)) {
            if (!ArmorTraitNbtHelper.getTraits(stack).isEmpty()) {
                ArmorLoreRefreshHelper.refreshLore(stack);
                return true;
            }
        }

        return false;
    }

    private static boolean matchesArmorPattern(ItemStack stack) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return ConfigManager.matchesItemPattern(itemId, ConfigManager.get().armor.applicableItems);
    }

    /**
     * 复苏tick - 自动回血
     */
    private static void tickRevive(ServerPlayer player) {
        UUID uuid = player.getUUID();
        int tick = REVIVE_TICK.getOrDefault(uuid, 0) + 1;
        int reviveInterval = getReviveInterval(player);
        if (reviveInterval > 0 && tick >= reviveInterval) {
            tick = 0;
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal((float) ConfigManager.get().armor.traitEffects.reviveHealAmount);
            }
        }
        REVIVE_TICK.put(uuid, tick);
    }

    private static int getReviveInterval(ServerPlayer player) {
        int minInterval = Integer.MAX_VALUE;
        ConfigManager.ArmorConfigSection cfg = ConfigManager.get().armor;
        for (ItemStack stack : getArmorStacks(player)) {
            for (ArmorTraitInstance ti : ArmorTraitNbtHelper.getTraits(stack)) {
                if (ArmorTraitRegistry.REVIVE.equals(ti.getId())) {
                    int interval = cfg.traitEffects.reviveBaseIntervalTicks / Math.max(ti.getLevel(), 1);
                    if (interval < minInterval) minInterval = interval;
                }
            }
        }
        return minInterval == Integer.MAX_VALUE ? 0 : minInterval;
    }
}
