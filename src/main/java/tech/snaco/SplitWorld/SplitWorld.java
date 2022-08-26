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
  public Config config = ConfigLoader.load();

  @Override
  public void onInitialize() {
    IO.verifyDir("splitworld");
    PlayerCallback.PLAYER_TICK.register(IO::verifyPlayerDir);
    PlayerCallback.PLAYER_TICK.register(this::trackPlayerPosition);
    PlayerCallback.BEFORE_DEATH.register(IO::nukeSavedSurvivalInventory);
  }

  private ActionResult trackPlayerPosition(ServerPlayerEntity player) {
    if (this.playerOnCreativeSide(player)) {
      setGameMode(player, GameMode.CREATIVE);
    }
    else {
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

  public boolean playerOnCreativeSide(ServerPlayerEntity player) {
    var playerPosition = getRelevantPlayerPosition(player);
    if (this.config.creativeSide.equals("positive")) {
      return playerPosition > this.config.borderLocation;
    }
    return playerPosition < this.config.borderLocation;
  }
  
  private double getRelevantPlayerPosition(ServerPlayerEntity player) {
    this.config.borderAxis = this.config.borderAxis.toUpperCase();
    if (this.config.borderAxis.equals("Y")) {
      return player.getPos().y;
    }
    if (this.config.borderAxis.equals("Z")) {
      return player.getPos().z;
    }
    return player.getPos().x;
  }
}
