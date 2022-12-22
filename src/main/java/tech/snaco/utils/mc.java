package tech.snaco.utils;

import net.minecraft.nbt.NbtCompound;
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

  public static GameMode getPlayerGameMode(ServerPlayerEntity player) {
    var nbt = player.writeNbt(new NbtCompound());
    return GameMode.byId(nbt.getInt("playerGameType"));
  }

  public static void getPlayerDimension(ServerPlayerEntity player) {
    var nbt = player.writeNbt((new NbtCompound()));

  }

  public static void setPlayerGameMode(ServerPlayerEntity player, GameMode gameMode) {
    player.changeGameMode(gameMode);
  }
}
