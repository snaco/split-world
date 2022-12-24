package tech.snaco.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import java.util.Locale;

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

  public static GameMode getPlayerGameMode(ServerPlayerEntity player) {
    var nbt = player.writeNbt(new NbtCompound());
    return GameMode.byId(nbt.getInt("playerGameType"));
  }

  public static GameMode getGameModeFromString(String gameMode) {
    gameMode = gameMode.toLowerCase();
    return switch (gameMode) {
      case "creative" -> GameMode.CREATIVE;
      case "survival" -> GameMode.SURVIVAL;
      case "adventure" -> GameMode.ADVENTURE;
      case "spectator" -> GameMode.SPECTATOR;
      default -> null;
    };
  }

  public static void getPlayerDimension(ServerPlayerEntity player) {
    var nbt = player.writeNbt((new NbtCompound()));

  }

  public static void setPlayerGameMode(ServerPlayerEntity player, GameMode gameMode) {
    player.changeGameMode(gameMode);
  }
}
