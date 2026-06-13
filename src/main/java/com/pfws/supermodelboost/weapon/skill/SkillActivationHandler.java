package com.pfws.supermodelboost.weapon.skill;

import com.pfws.supermodelboost.SupermodelBoostMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 技能激活处理器 - 处理快速潜行两下激活技能的核心逻辑
 * 
 * @author PFWs
 */
public final class SkillActivationHandler {

    private static final Map<UUID, Long> LAST_SNEAK_TICK = new HashMap<>();
    private static final Map<UUID, Boolean> WAS_SNEAKING = new HashMap<>();
    private static final long DOUBLE_TAP_WINDOW = 6;

    private SkillActivationHandler() {}

    public static void onPlayerTick(ServerPlayer player) {
        boolean isSneaking = player.isShiftKeyDown();
        UUID uuid = player.getUUID();
        boolean wasSneaking = WAS_SNEAKING.getOrDefault(uuid, false);

        if (isSneaking && !wasSneaking) {
            long now = player.level().getGameTime();
            Long lastSneak = LAST_SNEAK_TICK.get(uuid);

            if (lastSneak != null && (now - lastSneak) <= DOUBLE_TAP_WINDOW) {
                handleDoubleTap(player);
                LAST_SNEAK_TICK.remove(uuid);
            } else {
                LAST_SNEAK_TICK.put(uuid, now);
            }
        }

        WAS_SNEAKING.put(uuid, isSneaking);

        ItemStack mainHand = player.getMainHandItem();
        if (ActiveSkillNbtHelper.hasSkill(mainHand)) {
            SkillBossBarManager.updateBossBar(player, mainHand);
        } else {
            SkillBossBarManager.removeBossBar(player);
        }

        SkillStateTracker.showPendingEndMessages(player);
    }

    private static void handleDoubleTap(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) return;

        ActiveSkillInstance skill = ActiveSkillNbtHelper.getSkill(mainHand);
        if (skill == null || skill.getLevel() <= 0) return;

        ActiveSkillData skillData = ActiveSkillRegistry.getById(skill.skillId()).orElse(null);
        if (skillData == null) return;

        long now = player.level().getGameTime();
        long cooldownEnd = ActiveSkillNbtHelper.getCooldownEnd(mainHand);
        if (now < cooldownEnd) {
            long remainingTicks = cooldownEnd - now;
            int remainingSeconds = (int)(remainingTicks / 20);
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("技能冷却中，剩余 " + remainingSeconds + " 秒")
                            .withStyle(net.minecraft.ChatFormatting.RED),
                    false
            );
            return;
        }

        int cooldownTicks = skillData.getCooldown(skill.getLevel());
        long newCooldownEnd = now + cooldownTicks;
        ActiveSkillNbtHelper.setCooldownEnd(mainHand, newCooldownEnd);

        SkillEffectHandler.executeSkill(player, skill, mainHand);

        SkillBossBarManager.showSkillActivated(player, skillData.getSubName());

        ActiveSkillNbtHelper.addSkillXp(mainHand, 20);

        int newLevel = ActiveSkillNbtHelper.tryLevelUp(mainHand);
        if (newLevel > skill.getLevel()) {
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("\u00a7a\u2726 技能升级！\u00a7b" + skillData.getDisplayName()
                            + " \u00a7e" + ActiveSkillNbtHelper.getLevelDisplayNamePublic(newLevel))
                            .withStyle(net.minecraft.ChatFormatting.GOLD),
                    false
            );
        }

        // 先清除旧Lore，再重建（避免Lore栏错乱）
        mainHand.remove(net.minecraft.core.component.DataComponents.LORE);
        com.pfws.supermodelboost.LoreUpdateHelper.updateAllLore(mainHand);
        SkillBossBarManager.updateBossBar(player, mainHand);
    }

    public static void cleanup() {
        LAST_SNEAK_TICK.clear();
        WAS_SNEAKING.clear();
    }
}
