package tech.snaco.SplitWorld;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.snaco.SplitWorld.callbacks.PlayerCallback;
import tech.snaco.utils.IO;

public class SplitWorld implements ModInitializer {
  public static final Logger LOGGER = LoggerFactory.getLogger("splitworld");

  @Override
  public void onInitialize() {
    IO.verifyDir("splitworld");
    PlayerCallback.PLAYER_TICK.register(IO::verifyPlayerDir);
    PlayerCallback.PLAYER_TICK.register(this::trackPlayerPosition);
    PlayerCallback.BEFORE_DEATH.register(IO::nukeSavedSurvivalInventory);
  }

  private ActionResult trackPlayerPosition(ServerPlayerEntity player) {
    if (player.getPos().x < 0) {
      setGameMode(player, GameMode.CREATIVE);
    }
    if (player.getPos().x > 0) {
      setGameMode(player, GameMode.SURVIVAL);
    }
    return ActionResult.PASS;
  }

  private void setGameMode(ServerPlayerEntity player, GameMode targetGameMode) {
    if (player.isCreative() && targetGameMode == GameMode.SURVIVAL) {
      IO.saveInventory(player, GameMode.CREATIVE);
      IO.loadInventory(player, GameMode.SURVIVAL);
    } else if (!player.isCreative() && targetGameMode == GameMode.CREATIVE) {
      IO.saveInventory(player, GameMode.SURVIVAL);
      IO.loadInventory(player, GameMode.CREATIVE);
    }
    player.changeGameMode(targetGameMode);
  }

}
