package tech.snaco.utils;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public class mc {
  public static String playerName(ServerPlayerEntity player) {
    return player.getName().getString();
  }

  public static GameMode Not(GameMode gameMode) {
    switch (gameMode) {
      case SURVIVAL:
        return GameMode.CREATIVE;
      case CREATIVE:
        return GameMode.SURVIVAL;
      default:
        return gameMode;
    }
  }

  public static boolean isCreativeOrSurvival(GameMode gameMode) {
    return gameMode == GameMode.SURVIVAL || gameMode == GameMode.CREATIVE;
  }

  public static boolean playerInGameMode(ServerPlayerEntity player, GameMode gameMode) {
    if (player.isCreative() && gameMode == GameMode.CREATIVE)
      return true;
    if (!player.isCreative() && !player.isSpectator() && gameMode == GameMode.SURVIVAL)
      return true;
    if (player.isSpectator() && gameMode == GameMode.SPECTATOR)
      return true;
    return false;
  }
}
