package tech.snaco.SplitWorld.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public interface PlayerTickCallback {
    Event<PlayerTickCallback> PLAYER_TICK = EventFactory.createArrayBacked(PlayerTickCallback.class, (listeners) -> (player) -> {
        for (PlayerTickCallback listener: listeners) {
            ActionResult result = listener.interact(player);
            if (result != ActionResult.PASS) {
                return result;
            }
        }
        return ActionResult.PASS;
    });
    
    ActionResult interact(ServerPlayerEntity player);
}
