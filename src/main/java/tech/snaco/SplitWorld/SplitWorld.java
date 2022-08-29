package tech.snaco.SplitWorld;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

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
    PlayerCallback.BEFORE_DEATH.register(this::nukeInventoryIfSurvival);
  }

  private ActionResult trackPlayerPosition(ServerPlayerEntity player) {
    var playerPosition = getRelevantPlayerPosition(player);
    if (config.borderWidth < 0) {
      LOGGER.error("Error in split world config file!",
          new SplitWorldConfigException("Split World border width cannot be a negative value!"));
      return ActionResult.FAIL;
    } else if (config.borderWidth == 0) {
      if (playerPosition > config.borderLocation) {
        setGameMode(player, true, true);
      } else {
        setGameMode(player, false, true);
      }
    } else {
      var positiveBorder = config.borderLocation + (config.borderWidth / 2);
      var negativeBorder = config.borderLocation - (config.borderWidth / 2);
      if (playerPosition > positiveBorder) {
        setGameMode(player, true, false);
      } else if (playerPosition <= positiveBorder && playerPosition >= negativeBorder) {
        bufferZone(player);
        convertBorderBlocksAtFeet(player);
      } else if (playerPosition < negativeBorder) {
        setGameMode(player, false, false);
      } else {
        LOGGER.info("Cannot make heads or tails of player position. Ignoring.");
      }
    }
    return ActionResult.PASS;
  }

  private ActionResult nukeInventoryIfSurvival(ServerPlayerEntity player) {
    if (mc.getPlayerGameMode(player) == GameMode.SURVIVAL
        && !player.server.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
      IO.nukeSavedSurvivalInventory(player);
    }
    return ActionResult.PASS;
  }

  private void bufferZone(ServerPlayerEntity player) {
    var gameMode = mc.getPlayerGameMode(player);
    if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SURVIVAL) {
      IO.nukeSavedInventory(player, gameMode);
      IO.saveInventory(player, gameMode);
      player.getInventory().clear();
      mc.setPlayerGameMode(player, GameMode.ADVENTURE);
      player.world.playSound(null, new BlockPos(player.getPos()), SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL,
          SoundCategory.BLOCKS, 0.25f, 1f);
      LOGGER.info(s.f("%s is now adventuring.", mc.playerName(player)));
    }
    player.getInventory().clear();
  }

  private void setGameMode(ServerPlayerEntity player, boolean onPositiveSide, boolean preNukeInventory) {
    var playerGameMode = mc.getPlayerGameMode(player);
    if (shouldSetCreative(onPositiveSide) && playerGameMode != GameMode.CREATIVE) {
      if (preNukeInventory) {
        IO.nukeSavedInventory(player, GameMode.SURVIVAL);
        IO.saveInventory(player, GameMode.SURVIVAL);
      }
      mc.setPlayerGameMode(player, GameMode.CREATIVE);
      IO.loadInventory(player, GameMode.CREATIVE);
      player.world.playSound(null, new BlockPos(player.getPos()), SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL,
          SoundCategory.BLOCKS, 0.25f, 1f);
      LOGGER.info(s.f("%s is now creative.", mc.playerName(player)));
    } else if (shouldSetSurvival(onPositiveSide) && playerGameMode != GameMode.SURVIVAL) {
      if (preNukeInventory) {
        IO.nukeSavedInventory(player, GameMode.CREATIVE);
        IO.saveInventory(player, GameMode.CREATIVE);
      }
      mc.setPlayerGameMode(player, GameMode.SURVIVAL);
      IO.loadInventory(player, GameMode.SURVIVAL);
      player.world.playSound(null, new BlockPos(player.getPos()), SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL,
          SoundCategory.BLOCKS, 0.25f, 1f);
      LOGGER.info(s.f("%s is now surviving.", mc.playerName(player)));
    }
  }

  private boolean shouldSetSurvival(boolean onPositiveSide) {
    return !shouldSetCreative(onPositiveSide);
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

  private double getRelevantBlockPos(BlockPos blockPos) {
    this.config.borderAxis = this.config.borderAxis.toUpperCase();
    if (this.config.borderAxis.equals("Z")) {
      return blockPos.getZ();
    }
    return blockPos.getX();
  }

  private void convertBorderBlocksAtFeet(ServerPlayerEntity player) {
    for (int k = -5; k < 5; k++) {
      for (int i = -5; i < 5; i++) {
        for (int j = 0 - config.borderWidth - 1; j < config.borderWidth; j++) {
          var feet = new BlockPos(
              new Vec3i(player.getBlockPos().getX(), player.getBlockPos().getY() - 1, player.getPos().getZ()));
          if (config.borderAxis.equals("X")) {
            feet = feet.add(j, k, i);
          }
          if (config.borderAxis.equals("Z")) {
            feet = feet.add(i, k, j);
          }
          if (player.world.getBlockState(feet) != Blocks.AIR.getDefaultState() &&
              player.world.getBlockState(feet) != Blocks.BEDROCK.getDefaultState() &&
              getRelevantBlockPos(feet) >= config.borderLocation - (config.borderWidth / 2) &&
              getRelevantBlockPos(feet) < config.borderLocation + (config.borderWidth / 2)) {
            player.world.setBlockState(feet, Blocks.BEDROCK.getDefaultState());
          }
        }
      }
    }
  }
}
