package tech.snaco.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;

import tech.snaco.SplitWorld.SplitWorld;
import tech.snaco.utils.string.string;

public class IO {

    public static String getInventoryDir(ServerPlayerEntity player) {
        return string.f("splitworld/%s_inv", player.getUuidAsString());
    }

    public static String getEnderChestDir(ServerPlayerEntity player) {
        return string.f("splitworld/%s_enderchest", player.getUuidAsString());
    }

    public static ActionResult nukeSavedEnderChest(ServerPlayerEntity player) {
        if (nukeSavedEnderChest(player, GameMode.SURVIVAL)) {
            return ActionResult.PASS;
        }
        return ActionResult.FAIL;
    }

    public static ActionResult nukeSavedSurvivalInventory(ServerPlayerEntity player) {
        if (nukeSavedInventory(player, GameMode.SURVIVAL)) {
            return ActionResult.PASS;
        }
        return ActionResult.FAIL;
    }

    public static boolean nukeSavedEnderChest(ServerPlayerEntity player, GameMode gameMode) {
        var dir = new File(getEnderChestDir(player));
        var files = dir.listFiles();
        if (files == null) {
            return false;
        }
        for (var file : Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().contains(gameMode.toString())) {
                file.delete();
            }
        }
        return true;
    }

    public static boolean nukeSavedInventory(ServerPlayerEntity player, GameMode gameMode) {
        var dir = new File(getInventoryDir(player));
        var files = dir.listFiles();
        if (files == null) {
            return false;
        }
        for (var file : Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().contains(gameMode.toString())) {
                file.delete();
            }
        }
        return true;
    }

    public static ActionResult verifyPlayerInventoryDir(ServerPlayerEntity player) {
        var dirName = getInventoryDir(player);
        if (!IO.verifyDir(dirName)) {
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    public static ActionResult verifyPlayerEnderChestDir(ServerPlayerEntity player) {
        var dirName = getEnderChestDir(player);
        if (!IO.verifyDir(dirName)) {
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    public static void loadInventory(ServerPlayerEntity player, GameMode gameMode) {
        try {
            NbtList nbtList = new NbtList();
            var dir = new File(getInventoryDir(player));
            var files = dir.listFiles();
            if (files == null) {
                player.getInventory().clear();
            } else {
                for (var file : Objects.requireNonNull(dir.listFiles())) {
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
        } catch (IOException ex) {
            player.getInventory().dropAll();
            SplitWorld.LOGGER.error(string.error("Error reading inventory file!"), ex);
        }
    }

    public static void loadEnderChest(ServerPlayerEntity player, GameMode gameMode) {
        try {
            NbtList nbtList = new NbtList();
            var dir = new File(getEnderChestDir(player));
            var files = dir.listFiles();
            if (files == null) {
                player.getEnderChestInventory().clear();
            } else {
                for (var file : Objects.requireNonNull(dir.listFiles())) {
                    if (file.getName().contains(gameMode.toString())) {
                        var nbt = NbtIo.read(file);
                        if (nbt != null) {
                            nbtList.add(nbt);
                        }
                    }
                }
            }
            player.getEnderChestInventory().clear();
            player.getEnderChestInventory().readNbtList(nbtList);
        } catch (IOException ex) {
            player.getEnderChestInventory().clear();
            SplitWorld.LOGGER.error(string.error("Error reading ender chest file!"), ex);
        }
    }

    public static void saveInventory(ServerPlayerEntity player, GameMode gameMode) {
        try {
            var dir = getInventoryDir(player);
            var nbtList = new NbtList();
            nbtList = player.getInventory().writeNbt(nbtList);
            for (int i = 0; i < nbtList.size(); i++) {
                var nbt = nbtList.get(i);
                var file = new File(string.f("%s/%s_%d.nbt", dir, gameMode.toString(), i));
                NbtIo.write((NbtCompound) nbt, file);
            }
        } catch (Exception ex) {
            SplitWorld.LOGGER.error(string.error("Error saving inventory file for %s!", player.getName().getString()));
        }
    }

    public static void saveEnderChest(ServerPlayerEntity player, GameMode gameMode) {
        try {
            var nbtList = player.getEnderChestInventory().toNbtList();
            var dir = getEnderChestDir(player);
            for (int i = 0; i < nbtList.size(); i++) {
                var nbt = nbtList.get(i);
                var file = new File(string.f("%s/%s_%d.nbt", dir, gameMode.toString(), i));
                NbtIo.write((NbtCompound) nbt, file);
            }
        } catch (IOException e) {
            SplitWorld.LOGGER.error(string.error("Error saving ender chest file for %s!", player.getName().getString()));
        }
    }

    public static boolean verifyDir(String dirName) {
        var dir = Paths.get(dirName);
        try {
            var dirPath = Files.createDirectory(dir);
            if (Files.notExists(dirPath) && !Files.exists(dirPath)) {
                new File(dirName).mkdir();
                SplitWorld.LOGGER.info(string.info("Created directory: %s", dirName));
                return true;
            }
        } catch (FileAlreadyExistsException ex) {
            SplitWorld.LOGGER.debug(string.debug("Verified directory: %s", dirName));
            return true;
        } catch (IOException ex) {
            SplitWorld.LOGGER.error(string.error("Error verifying directory: %s", dirName), ex);
            return false;
        }
        return true;
    }
}
