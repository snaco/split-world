package tech.snaco.SplitWorld;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.snaco.SplitWorld.callbacks.PlayerCallback;
import tech.snaco.SplitWorld.types.DimensionConfig;
import tech.snaco.SplitWorld.types.Config;
import tech.snaco.utils.IO;
import tech.snaco.utils.mc;
import tech.snaco.utils.exceptions.SplitWorldConfigException;
import tech.snaco.utils.string.string;

import java.util.ArrayList;

public class SplitWorld implements ModInitializer {
  public static final Logger LOGGER = LoggerFactory.getLogger("splitworld");
  public Config config;


  @Override
  public void onInitialize() {
    IO.verifyDir("splitworld");
    this.config = new ConfigLoader().load();
    PlayerCallback.PLAYER_TICK.register(IO::verifyPlayerDir);
    PlayerCallback.PLAYER_TICK.register(this::trackPlayerPosition);
    PlayerCallback.BEFORE_DEATH.register(this::nukeInventoryIfSurvival);
    PlayerCallback.WORLD_CHANGE.register(this::worldChange);
  }

  private ActionResult worldChange(ServerPlayerEntity player) {
    var validGameModes = new ArrayList<String>();
    validGameModes.add("survival");
    validGameModes.add("creative");
    validGameModes.add("adventure");
    validGameModes.add("spectator");
    var dimension = getPlayerCurrentDimension(player);
    var cfg = config.dimensionConfigs.stream().filter(dc -> dc.dimensionName.equals(dimension)).findFirst();
    if (cfg.isPresent()) {
      var config = cfg.get();
      if (config.gameModeOverride != null) {
        if (validGameModes.contains(config.gameModeOverride)) {
          changeGameMode(player, mc.getPlayerGameMode(player), mc.getGameModeFromString(config.gameModeOverride));
        }
        else {
          LOGGER.error(string.error("Dimension %s has an invalid game mode override of %s. Please set it to 'survival', 'creative', 'adventure' or 'spectator'"), config.dimensionName, config.gameModeOverride);
        }
      }
      else {
        return trackPlayerPosition(player);
      }
    }
    return ActionResult.PASS;
  }

  private ActionResult trackPlayerPosition(ServerPlayerEntity player) {
    var dimension = getPlayerCurrentDimension(player);
    var cfg = config.dimensionConfigs.stream().filter(dc -> dc.dimensionName.equals(dimension)).findFirst();
    if (cfg.isPresent()) {
      var config = cfg.get();
      if (!config.enabled) {
        return ActionResult.PASS;
      }
      var playerPosition = getRelevantPlayerPosition(player, config);
      if (config.borderWidth < 0) {
        LOGGER.error(string.error("Error in split world config file!"),
                new SplitWorldConfigException("Split World border width cannot be a negative value!"));
        return ActionResult.FAIL;
      } else if (config.borderWidth == 0) {
        setGameMode(player, playerPosition > config.borderLocation, true, config);
      } else {
        var positiveBorder = config.borderLocation + (config.borderWidth / 2);
        var negativeBorder = config.borderLocation - (config.borderWidth / 2);
        if (playerPosition > positiveBorder) {
          setGameMode(player, true, false, config);
        } else if (playerPosition <= positiveBorder && playerPosition >= negativeBorder) {
          bufferZone(player);
          if (config.replaceBorderBlocks) {
            convertBorderBlocksAtFeet(player, config);
            keepPlayerAboveGround(player);
          }
        } else if (playerPosition < negativeBorder) {
          setGameMode(player, false, false, config);
        } else {
          LOGGER.info(string.info("Cannot make heads or tails of player position. Ignoring."));
        }
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
      mc.setPlayerGameMode(player, GameMode.SPECTATOR);
      player.world.playSound(null, new BlockPos(player.getPos()), SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL,
          SoundCategory.BLOCKS, 0.25f, 1f);
      LOGGER.info(string.info("%s is now adventuring.", mc.playerName(player)));
    }
    player.getInventory().clear();
  }

  private void setGameMode(ServerPlayerEntity player, boolean onPositiveSide, boolean preNukeInventory, DimensionConfig config) {
    var playerGameMode = mc.getPlayerGameMode(player);
    if (shouldSetCreative(onPositiveSide, config) && playerGameMode != GameMode.CREATIVE) {
      if (preNukeInventory) {
        IO.nukeSavedInventory(player, GameMode.SURVIVAL);
        IO.saveInventory(player, GameMode.SURVIVAL);
      }
      mc.setPlayerGameMode(player, GameMode.CREATIVE);
      IO.loadInventory(player, GameMode.CREATIVE);
      player.world.playSound(null, new BlockPos(player.getPos()), SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL,
          SoundCategory.BLOCKS, 0.25f, 1f);
      LOGGER.info(string.info("%s is now creative.", mc.playerName(player)));
    } else if (shouldSetSurvival(onPositiveSide, config) && playerGameMode != GameMode.SURVIVAL) {
      if (preNukeInventory) {
        IO.nukeSavedInventory(player, GameMode.CREATIVE);
        IO.saveInventory(player, GameMode.CREATIVE);
      }
      mc.setPlayerGameMode(player, GameMode.SURVIVAL);
      IO.loadInventory(player, GameMode.SURVIVAL);
      player.world.playSound(null, new BlockPos(player.getPos()), SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL,
          SoundCategory.BLOCKS, 0.25f, 1f);
      if (config.creativeSide.equals("negative")) {
        player.teleport(player.getX(), findGroundLevel(player) + 1, player.getZ());
      }
      LOGGER.info(string.info("%s is now surviving.", mc.playerName(player)));
    }
  }

  private boolean shouldSetSurvival(boolean onPositiveSide, DimensionConfig config) {
    return !shouldSetCreative(onPositiveSide, config);
  }

  private boolean shouldSetCreative(boolean onPositiveSide, DimensionConfig config) {
    if (onPositiveSide && config.creativeSide.equals("positive")) {
      return true;
    }
    return !onPositiveSide && config.creativeSide.equals("negative");
  }

  private double getRelevantPlayerPosition(ServerPlayerEntity player, DimensionConfig config) {
    config.borderAxis = config.borderAxis.toUpperCase();
    if (config.borderAxis.equals("Y")) {
      return player.getPos().y;
    }
    if (config.borderAxis.equals("Z")) {
      return player.getPos().z;
    }
    return player.getPos().x;
  }

  private String getPlayerCurrentDimension(ServerPlayerEntity player) {
    return player.getEntityWorld().getRegistryKey().getValue().toString();
  }

  private double getRelevantBlockPos(BlockPos blockPos, DimensionConfig config) {
    config.borderAxis = config.borderAxis.toUpperCase();
    if (config.borderAxis.equals("Z")) {
      return blockPos.getZ();
    }
    return blockPos.getX();
  }

  private void convertBorderBlocksAtFeet(ServerPlayerEntity player, DimensionConfig config) {
    for (int k = -400; k < 5; k++) {
      for (int i = -5; i < 5; i++) {
        for (int j = -config.borderWidth - 1; j < config.borderWidth; j++) {
          var feet = new BlockPos(
              new Vec3i(player.getBlockPos().getX(), player.getBlockPos().getY() - 1, player.getPos().getZ()));
          if (config.borderAxis.equals("X")) {
            feet = feet.add(j, k, i);
          }
          if (config.borderAxis.equals("Z")) {
            feet = feet.add(i, k, j);
          }
          var blockAtFeet = player.world.getBlockState(feet);
          if (blockAtFeet != Blocks.AIR.getDefaultState() &&
              blockAtFeet != Blocks.BEDROCK.getDefaultState() &&
              blockAtFeet != Blocks.END_PORTAL.getDefaultState() &&
              blockAtFeet != Blocks.END_PORTAL_FRAME.getDefaultState() &&
              getRelevantBlockPos(feet, config) >= config.borderLocation - (config.borderWidth / 2.0) &&
              getRelevantBlockPos(feet, config) < config.borderLocation + (config.borderWidth / 2.0)) {
            var head = new BlockPos(new Vec3i(player.getX(), player.getY() + 1, player.getZ()));
            var body = new BlockPos(new Vec3i(player.getX(), player.getY(), player.getZ()));
            // TODO: Add custom block that uses END_GATE texture and is unbreakable to be the border
            player.world.setBlockState(feet, Blocks.BEDROCK.getDefaultState());
          }
        }
      }
    }
  }

  private void keepPlayerAboveGround(ServerPlayerEntity player) {
    var ground = findGroundLevel(player);
    if (player.getY() < ground) {
      player.teleport(player.getX(), ground + 1, player.getZ());
    }
  }

  private int findGroundLevel(ServerPlayerEntity player) {
    for (int y = 319; y > -64; y--) {
      var block = player.world.getBlockState(new BlockPos(new Vec3i(player.getBlockPos().getX(), y, player.getPos().getZ())));
      if (block != Blocks.AIR.getDefaultState() && block != Blocks.VOID_AIR.getDefaultState()) {
        return y;
      }
    }
    return 0;
  }

  private void changeGameMode(ServerPlayerEntity player, GameMode currentGameMode, GameMode newGameMode) {
    IO.nukeSavedInventory(player, currentGameMode);
    IO.saveInventory(player, currentGameMode);
    IO.loadInventory(player, newGameMode);
    mc.setPlayerGameMode(player, newGameMode);
  }
}
