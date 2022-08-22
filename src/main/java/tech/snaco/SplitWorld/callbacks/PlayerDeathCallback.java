package tech.snaco.SplitWorld.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public interface PlayerDeathCallback {
    Event<PlayerDeathCallback> BEFORE_DEATH = EventFactory.createArrayBacked(PlayerDeathCallback.class, (listeners) -> (player) -> {
        for (PlayerDeathCallback listener: listeners) {
            ActionResult result = listener.interact(player);
            if (result != ActionResult.PASS) {
                return result;
            }
        }
        return ActionResult.PASS;
    });
    
    ActionResult interact(ServerPlayerEntity player);
}
