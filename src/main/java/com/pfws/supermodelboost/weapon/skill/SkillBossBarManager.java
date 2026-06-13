package com.pfws.supermodelboost.weapon.skill;

import com.pfws.supermodelboost.SupermodelBoostMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 技能BossBar管理器
 * 
 * @author PFWs
 */
public final class SkillBossBarManager {
    private static final Map<UUID, ServerBossEvent> BOSS_BARS = new HashMap<>();

    private SkillBossBarManager() {}

    public static void updateBossBar(ServerPlayer player, ItemStack mainHand) {
        UUID uuid = player.getUUID();

        ActiveSkillInstance skill = ActiveSkillNbtHelper.getSkill(mainHand);
        if (skill == null || skill.getLevel() <= 0 || ActiveSkillRegistry.getById(skill.skillId()).isEmpty()) {
            removeBossBar(player);
            return;
        }

        ActiveSkillData skillData = ActiveSkillRegistry.getById(skill.skillId()).get();
        int cooldownTicks = skillData.getCooldown(skill.getLevel());
        long cooldownEnd = ActiveSkillNbtHelper.getCooldownEnd(mainHand);
        long now = player.level().getGameTime();

        String skillName = skillData.getDisplayName() + " " + getLevelName(skill.getLevel());

        ServerBossEvent bar = BOSS_BARS.computeIfAbsent(uuid, id -> {
            ServerBossEvent newBar = new ServerBossEvent(
                    UUID.randomUUID(),
                    Component.literal(skillName),
                    BossEvent.BossBarColor.GREEN,
                    BossEvent.BossBarOverlay.NOTCHED_20
            );
            newBar.addPlayer(player);
            newBar.setVisible(true);
            return newBar;
        });

        bar.setName(Component.literal(skillName));

        if (now >= cooldownEnd) {
            bar.setColor(BossEvent.BossBarColor.GREEN);
            bar.setProgress(1.0f);
        } else {
            bar.setColor(BossEvent.BossBarColor.RED);
            long remaining = cooldownEnd - now;
            float progress = Math.clamp((float) remaining / cooldownTicks, 0.0f, 1.0f);
            bar.setProgress(1.0f - progress);
        }
    }

    public static void removeBossBar(ServerPlayer player) {
        UUID uuid = player.getUUID();
        ServerBossEvent bar = BOSS_BARS.remove(uuid);
        if (bar != null) {
            bar.removeAllPlayers();
            bar.setVisible(false);
        }
    }

    public static void showSkillActivated(ServerPlayer player, String skillName) {
        player.sendSystemMessage(
                Component.literal("技能-" + skillName + " 已激活").withStyle(net.minecraft.ChatFormatting.GREEN),
                false
        );
        SupermodelBoostMod.LOGGER.debug("Player {} activated skill: {}", player.getName().getString(), skillName);
    }

    public static void showSkillEnded(ServerPlayer player, String skillName) {
        player.sendSystemMessage(
                Component.literal("技能-" + skillName + " 已结束").withStyle(net.minecraft.ChatFormatting.RED),
                false
        );
        SupermodelBoostMod.LOGGER.debug("Player {} skill ended: {}", player.getName().getString(), skillName);
    }

    public static void cleanup() {
        for (ServerBossEvent bar : BOSS_BARS.values()) {
            bar.removeAllPlayers();
            bar.setVisible(false);
        }
        BOSS_BARS.clear();
    }

    private static String getLevelName(int level) {
        return switch (level) {
            case 1 -> "一转"; case 2 -> "二转"; case 3 -> "三转";
            case 4 -> "四转"; case 5 -> "五转"; default -> "?转";
        };
    }
}
