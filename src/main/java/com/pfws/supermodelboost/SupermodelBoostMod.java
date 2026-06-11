package com.pfws.supermodelboost;

import com.pfws.supermodelboost.armor.ArmorTraitRegistry;
import com.pfws.supermodelboost.config.ConfigManager;
import com.pfws.supermodelboost.weapon.WeaponTraitRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supermodel Boost - 超模强化系统 (仅服务端)
 * 
 * 功能：
 * - 武器强化台（结构一）：两个附魔台夹着发射器，发射器上方有石头/木质按钮
 * - 护甲强化台（结构二）：两个铁砧夹着发射器，发射器上方有石头/木质按钮
 * - 武器特性：吸血、反击、战狂、硬朗、柔韧、千手观音、兴奋
 * - 护甲特性：铁壁、吸血、荆棘、复苏、箭矢防护、魔御、坚韧、轻盈
 * 
 * 统一指令：/sboost
 * 
 * @author PFWs
 * @version 1.0.0
 * @since MC 26.1.1
 */
public class SupermodelBoostMod implements ModInitializer {
    public static final String MOD_ID = "supermodel_boost_server_pfws";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("============================================");
        LOGGER.info("  Supermodel Boost v{} initializing...", "1.0.0");
        LOGGER.info("  Author: PFWs");
        LOGGER.info("  Target: Minecraft 26.1.1+ (Fabric Server)");
        LOGGER.info("============================================");

        // 1. 加载配置
        ConfigManager.init();
        LOGGER.info("[Config] 配置文件加载完成");

        // 2. 注册武器特性
        WeaponTraitRegistry.registerAll();
        LOGGER.info("[Weapon] 武器特性注册完成 ({} 种特性)", WeaponTraitRegistry.getAll().size());

        // 3. 注册护甲特性
        ArmorTraitRegistry.registerAll();
        LOGGER.info("[Armor] 护甲特性注册完成 ({} 种特性)", ArmorTraitRegistry.getAll().size());

        // 4. 注册事件监听
        EventHandler.init();
        LOGGER.info("[Events] 事件监听器注册完成");

        // 5. 注册按钮交互（强化台触发）
        UseBlockCallback.EVENT.register(StructureHandler::onUseBlock);
        LOGGER.info("[Structure] 强化台结构检测器注册完成");

        // 6. 注册统一指令 /sboost
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandHandler.register(dispatcher);
            LOGGER.info("[Command] /sboost 指令注册完成");
        });

        // 7. 服务器关闭时清理
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            EventHandler.cleanup();
            LOGGER.info("[Cleanup] 服务器关闭，已清理临时数据");
        });

        LOGGER.info("============================================");
        LOGGER.info("  Supermodel Boost 初始化完成!");
        LOGGER.info("  指令: /sboost help");
        LOGGER.info("============================================");
    }
}
