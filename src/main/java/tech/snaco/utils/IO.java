package tech.snaco.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;

import tech.snaco.SplitWorld.SplitWorld;
import tech.snaco.utils.string.s;

public class IO {
  public static final String CONFIG_PATH = "config/splitworld/";

  public static String getDir(ServerPlayerEntity player) {
    return s.f("splitworld/%s_inv", player.getUuidAsString());
  }

  public static ActionResult nukeSavedSurvivalInventory(ServerPlayerEntity player) {
    if (nukeSavedInventory(player, GameMode.SURVIVAL)) {
      return ActionResult.PASS;
    }
    return ActionResult.FAIL;
  }

  public static boolean nukeSavedInventory(ServerPlayerEntity player, GameMode gameMode) {
    var dir = new File(getDir(player));
    var files = dir.listFiles();
    if (files == null) {
      return false;
    }
    for (var file : dir.listFiles()) {
      if (file.getName().contains(gameMode.toString())) {
        file.delete();
      }
    }
    return true;
  }

  public static ActionResult verifyPlayerDir(ServerPlayerEntity player) {
    var dirName = getDir(player);
    if (!IO.verifyDir(dirName)) {
      return ActionResult.FAIL;
    }
    return ActionResult.PASS;
  }

  public static void loadInventory(ServerPlayerEntity player, GameMode gameMode) {
    try {
      NbtList nbtList = new NbtList();
      var dir = new File(getDir(player));
      var files = dir.listFiles();
      if (files == null) {
        player.getInventory().clear();
      } else {
        for (var file : dir.listFiles()) {
          if (file.getName().contains(gameMode.toString())) {
            var nbt = NbtIo.read(file);
            if (nbt != null) {
              nbtList.add(nbt);
            }
          }
        }
      }
      player.getInventory().clear();
      player.getInventory().readNbt(nbtList);
    } catch (FileNotFoundException ex) {
      player.getInventory().dropAll();
      SplitWorld.LOGGER.error("Error reading inventory file!", ex);
    } catch (IOException ex) {
      player.getInventory().dropAll();
      SplitWorld.LOGGER.error("Error reading inventory file!", ex);
    }
  }

  public static void saveInventory(ServerPlayerEntity player, GameMode gameMode) {
    IO.nukeSavedInventory(player, gameMode);
    try {
      var dir = getDir(player);
      var nbtList = new NbtList();
      nbtList = player.getInventory().writeNbt(nbtList);
      for (int i = 0; i < nbtList.size(); i++) {
        var nbt = nbtList.get(i);
        var file = new File(s.f("%s/%s_%d.nbt", dir, gameMode.toString(), i));
        NbtIo.write((NbtCompound) nbt, file);
      }
    } catch (Exception ex) {
      SplitWorld.LOGGER.error(s.f("Error saving inventory file for %s!", player.getName().getString()));
    }
  }

  public static boolean verifyDir(String dirName) {
    var dir = Paths.get(dirName);
    try {
      var dirPath = Files.createDirectory(dir);
      if (Files.notExists(dirPath) && !Files.exists(dirPath)) {
        new File(dirName).mkdir();
        SplitWorld.LOGGER.info(s.f("Created directory: &s", dirName));
        return true;
      }
    } catch (FileAlreadyExistsException ex) {
      SplitWorld.LOGGER.debug(s.f("Verified directory: &s", dirName));
      return true;
    } catch (IOException ex) {
      SplitWorld.LOGGER.error(s.f("Error verifying directory: '%s'"), ex);
      return false;
    }
    return true;
  }
}
