package tech.snaco.SplitWorld.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public interface PlayerCallback {
  Event<PlayerCallback> PLAYER_TICK = EventFactory.createArrayBacked(PlayerCallback.class, (listeners) -> (player) -> {
    for (PlayerCallback listener : listeners) {
      ActionResult result = listener.interact(player);
      if (result != ActionResult.PASS) {
        return result;
      }
    }
    return ActionResult.PASS;
  });

  Event<PlayerCallback> BEFORE_DEATH = EventFactory.createArrayBacked(PlayerCallback.class, (listeners) -> (player) -> {
    for (PlayerCallback listener : listeners) {
      ActionResult result = listener.interact(player);
      if (result != ActionResult.PASS) {
        return result;
      }
    }
    return ActionResult.PASS;
  });

  ActionResult interact(ServerPlayerEntity player);
}
