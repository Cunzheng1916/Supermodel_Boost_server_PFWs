package com.pfws.supermodelboost.storage;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反击伤害存储 - 用于武器反击特性
 * 
 * @author PFWs
 */
public final class RetaliationStorage {
    private static final Map<UUID, Float> STORED_DAMAGE = new ConcurrentHashMap<>();

    private RetaliationStorage() {}

    public static void storeDamage(ServerPlayer player, float damage) {
        STORED_DAMAGE.merge(player.getUUID(), damage, Float::sum);
    }

    public static float consumeDamage(ServerPlayer player) {
        Float stored = STORED_DAMAGE.remove(player.getUUID());
        return stored != null ? stored : 0f;
    }

    public static float getStoredDamage(ServerPlayer player) {
        Float stored = STORED_DAMAGE.get(player.getUUID());
        return stored != null ? stored : 0f;
    }

    public static void clearDamage(ServerPlayer player) {
        STORED_DAMAGE.remove(player.getUUID());
    }

    public static void clearAll() {
        STORED_DAMAGE.clear();
    }
}
