package tech.snaco.SplitWorld;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.snaco.SplitWorld.callbacks.PlayerCallback;
import tech.snaco.utils.IO;
import tech.snaco.utils.mc;
import tech.snaco.utils.string.s;

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
    if (player.isSpectator()) {
      return;
    }
    if ((mc.playerInGameMode(player, GameMode.SURVIVAL) && targetGameMode == GameMode.CREATIVE) ||
        (mc.playerInGameMode(player, GameMode.CREATIVE) && targetGameMode == GameMode.SURVIVAL)) {
      IO.saveInventory(player, mc.Not(targetGameMode));
      IO.loadInventory(player, targetGameMode);
      player.changeGameMode(targetGameMode);
      LOGGER.info(s.f("Switched %s game mode from %s to %s", mc.playerName(player), mc.Not(targetGameMode), targetGameMode));
    }
  }
}
