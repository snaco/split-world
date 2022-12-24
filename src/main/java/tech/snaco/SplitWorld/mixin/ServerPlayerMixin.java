package tech.snaco.SplitWorld.mixin;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import tech.snaco.SplitWorld.callbacks.PlayerCallback;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerMixin {
  @Inject(at = @At("HEAD"), method = "Lnet/minecraft/server/network/ServerPlayerEntity;onDeath(Lnet/minecraft/entity/damage/DamageSource;)V", cancellable = true)
  private void onDeath(final DamageSource damageSource, final CallbackInfo info) {
    ActionResult result = PlayerCallback.BEFORE_DEATH.invoker().interact((ServerPlayerEntity) (Object) (this));
    if (result == ActionResult.FAIL) {
      info.cancel();
    }
  }

  @Inject(at = @At("HEAD"), method = "Lnet/minecraft/server/network/ServerPlayerEntity;tick()V", cancellable = true)
  private void playerTick(final CallbackInfo info) {
    ActionResult result = PlayerCallback.PLAYER_TICK.invoker().interact((ServerPlayerEntity) (Object) (this));
    if (result == ActionResult.FAIL) {
      info.cancel();
    }
  }

  @Inject(at=@At("HEAD"), method = "Lnet/minecraft/server/network/ServerPlayerEntity;worldChanged(Lnet/minecraft/server/world/ServerWorld;)V", cancellable = true)
  private void worldChange(final ServerWorld world, final CallbackInfo info) {
    ActionResult result = PlayerCallback.WORLD_CHANGE.invoker().interact((ServerPlayerEntity) (Object) (this));
    if (result == ActionResult.FAIL) {
      info.cancel();
    }
  }
}
