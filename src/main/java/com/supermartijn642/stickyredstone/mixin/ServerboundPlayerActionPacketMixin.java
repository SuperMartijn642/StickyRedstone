package com.supermartijn642.stickyredstone.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.stickyredstone.content.wire.StickyRedstoneDust;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Created 04/08/2026 by SuperMartijn642
 */
@Mixin(ServerboundPlayerActionPacket.class)
public class ServerboundPlayerActionPacketMixin {

    @ModifyVariable(
        method = "<init>(Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;I)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Object;<init>()V",
            shift = At.Shift.AFTER
        )
    )
    private Direction init(Direction original, @Local ServerboundPlayerActionPacket.Action action){
        if(action != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK && action != ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK)
            return original;
        Direction direction = StickyRedstoneDust.DESTROY_FACE_OVERRIDE.get();
        return direction == null ? original : direction;
    }
}
