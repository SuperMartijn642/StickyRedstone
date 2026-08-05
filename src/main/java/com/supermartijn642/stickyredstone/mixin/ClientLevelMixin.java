package com.supermartijn642.stickyredstone.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.supermartijn642.stickyredstone.content.wire.StickyRedstoneDust;
import com.supermartijn642.stickyredstone.content.wire.StickyRedstoneWireEvaluator;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Created 04/08/2026 by SuperMartijn642
 */
@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @WrapOperation(
        method = "addDestroyBlockEffect",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"
        )
    )
    private VoxelShape addDestroyBlockEffect(BlockState state, BlockGetter level, BlockPos pos, Operation<VoxelShape> operation) {
        if(!StickyRedstoneWireEvaluator.isStickyRedstoneWire(state.getBlock()))
            return operation.call(state, level, pos);
        Direction face = StickyRedstoneDust.DESTROY_FACE_OVERRIDE.get();
        if(face == null)
            return operation.call(state, level, pos);
        return ((StickyRedstoneDust)state.getBlock()).getHighlightShape(state, level, pos, CollisionContext.empty(), face);
    }
}
