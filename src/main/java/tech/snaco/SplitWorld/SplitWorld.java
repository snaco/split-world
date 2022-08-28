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
import tech.snaco.utils.exceptions.SplitWorldConfigException;
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
    if (config.borderWidth == 0) {
      if (this.playerOnCreativeSide(player)) {
        guardedSetGameMode(player, GameMode.CREATIVE);
      }
      else {
        guardedSetGameMode(player, GameMode.SURVIVAL);
      }
    } else if (config.borderWidth < 0) {
      LOGGER.error("Error in split world config file!", new SplitWorldConfigException("Split World border width cannot be a negative value!"));
      return ActionResult.FAIL;
    } else {
      var positiveBorder = config.borderLocation + (config.borderWidth / 2);
      var negativeBorder = config.borderLocation - (config.borderWidth / 2);
      var playerPosition = getRelevantPlayerPosition(player);
      var playerGameMode = mc.getPlayerGameMode(player);
      if (playerPosition > positiveBorder) {
        if (shouldSetCreative(true) && playerGameMode != GameMode.CREATIVE) {
          mc.setPlayerGameMode(player, GameMode.CREATIVE);
          LOGGER.info(s.f("%s is now creative.", mc.playerName(player)));
        } else if (!shouldSetCreative(true) && playerGameMode != GameMode.SURVIVAL) {
          mc.setPlayerGameMode(player, GameMode.SURVIVAL);
          LOGGER.info(s.f("%s is now surviving.", mc.playerName(player)));
        }
      } else if (playerPosition <= positiveBorder) {
        if (playerGameMode != GameMode.ADVENTURE) {
          mc.setPlayerGameMode(player, GameMode.ADVENTURE);
          LOGGER.info(s.f("%s is now adventuring.", mc.playerName(player)));
        }
      } else if (playerPosition == config.borderLocation) {
        LOGGER.info("Crossed the border.");
      } else if (playerPosition >= negativeBorder) {
        if (playerGameMode != GameMode.ADVENTURE) {
          mc.setPlayerGameMode(player, GameMode.ADVENTURE);
          LOGGER.info(s.f("%s is now adventuring.", mc.playerName(player)));
        }
      } else if (playerPosition < negativeBorder) {
        if (shouldSetCreative(false) && playerGameMode != GameMode.CREATIVE) {
          mc.setPlayerGameMode(player, GameMode.CREATIVE);
          LOGGER.info(s.f("%s is now creative.", mc.playerName(player)));
        } else if (!shouldSetCreative(false) && playerGameMode != GameMode.SURVIVAL) {
          mc.setPlayerGameMode(player, GameMode.SURVIVAL);
          LOGGER.info(s.f("%s is now surviving.", mc.playerName(player)));
        }
      } else {
        LOGGER.info("Cannot make heads or tails of player position. Ignoring.");
      }
    }
    return ActionResult.PASS;
  }

  private boolean shouldSetCreative(boolean onPositiveSide) {
    if (onPositiveSide && config.creativeSide.equals("positive")) {
      return true;
    }
    if (!onPositiveSide && config.creativeSide.equals("negative")) {
      return true;
    }
    return false;
  }

  private void guardedSetGameMode(ServerPlayerEntity player, GameMode targetGameMode) {
    var playerGameMode = mc.getPlayerGameMode(player);
    if (playerGameMode == GameMode.SURVIVAL && targetGameMode == GameMode.CREATIVE ||
        playerGameMode == GameMode.CREATIVE && targetGameMode == GameMode.SURVIVAL) {
      IO.nukeSavedInventory(player, mc.Not(targetGameMode));
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
