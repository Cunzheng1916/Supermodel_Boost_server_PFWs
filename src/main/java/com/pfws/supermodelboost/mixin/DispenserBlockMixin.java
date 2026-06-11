package com.pfws.supermodelboost.mixin;

import com.pfws.supermodelboost.StructureHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 取消发射器在检测到强化台结构时的物品发射行为
 * 
 * @author PFWs
 */
@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin {

    /**
     * 在 neighborChanged 执行前检查是否为合法强化台结构，
     * 如果是则取消事件（阻止发射器发射物品，因为结构处理器已处理此交互）
     */
    @Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true)
    private void onNeighborChanged(BlockState state, Level level, BlockPos pos,
                                    net.minecraft.world.level.block.Block neighborBlock,
                                    Orientation orientation, boolean movedByPiston, CallbackInfo ci) {
        if (level.isClientSide()) return;

        // 仅在合法结构中且有红石信号时取消（阻止发射器发射物品，由UseBlockCallback处理）
        if (level.hasNeighborSignal(pos)
                && (StructureHandler.isValidWeaponStructure(level, pos)
                || StructureHandler.isValidArmorStructure(level, pos))) {
            ci.cancel();
        }
    }
}