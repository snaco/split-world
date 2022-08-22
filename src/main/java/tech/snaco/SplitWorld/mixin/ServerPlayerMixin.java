package tech.snaco.SplitWorld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import tech.snaco.SplitWorld.callbacks.PlayerDeathCallback;
import tech.snaco.SplitWorld.callbacks.PlayerTickCallback;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerMixin {
    @Inject(at = @At("HEAD"), method="Lnet/minecraft/server/network/ServerPlayerEntity;onDeath()V", cancellable = true)
    private void onDeath(final DamageSource damageSource, final CallbackInfo info) {
        ActionResult result = PlayerDeathCallback.BEFORE_DEATH.invoker().interact((ServerPlayerEntity) (Object) (this));
        if(result == ActionResult.FAIL) {
            info.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method="Lnet/minecraft/server/network/ServerPlayerEntity;tick()V", cancellable = true)
    private void playerTick(final CallbackInfo info) {
        ActionResult result = PlayerTickCallback.PLAYER_TICK.invoker().interact((ServerPlayerEntity)(Object)(this));
        if (result == ActionResult.FAIL) {
            info.cancel();
        }
    }
}
