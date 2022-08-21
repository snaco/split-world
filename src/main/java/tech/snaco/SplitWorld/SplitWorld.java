package tech.snaco.SplitWorld;

import java.io.*;
import java.util.List;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import tech.snaco.SplitWorld.mixin.SplitPlayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class SplitWorld implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("modid");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
        ServerTickEvents.START_WORLD_TICK.register((world) -> {
            var players = world.getPlayers();
            for (ServerPlayerEntity serverPlayer : players) {
                var player = (SplitPlayer)serverPlayer;
                var pos = player.getPos();
                if (pos.x < 0) {
                    setGameMode(player, GameMode.CREATIVE);
                }
                if (pos.x > 0)  {
                    setGameMode(player, GameMode.SURVIVAL);
                }
            }
        });
	}

    private boolean setGameMode(SplitPlayer player, GameMode targetGameMode) {
        // var savedCreative = getInventoryFile(player, GameMode.CREATIVE);
        // var savedSurvival = getInventoryFile(player, GameMode.SURVIVAL);
        // if (player.isCreative() && targetGameMode == GameMode.SURVIVAL) {
        //     // save creative from player
        //     saveInventoryFile(player.getInventory(), savedCreative);
        //     // load survival
        //     if (savedSurvival.exists()) {
        //         player.getInventory().clone(loadInventoryFile(player, targetGameMode));
        //     } else {
        //         player.getInventory().clear();
        //     }
        // } else if (!player.isCreative() && targetGameMode == GameMode.CREATIVE) {
        //     // save survival inventory
        //     saveInventoryFile(player.getInventory(), savedSurvival);
        //     // load creative inventory
        //     if (savedCreative.exists()) {
        //         player.getInventory().clone(loadInventoryFile(player, targetGameMode));
        //     } else {
        //         player.getInventory().clear();
        //     }
        // }
        var backupInv = player.backupInventory;
        player.backupInventory.clone(player.getInventory());
        player.getInventory().clone(backupInv);
        player.changeGameMode(targetGameMode);
        return true;
    }

    private File getInventoryFile(SplitPlayer player, GameMode gameMode) {
        var fileName = String.format("%s_%s.inv", player.getUuidAsString(), gameMode.toString());
        var file = new File(fileName);
        return file;
    }

    private PlayerInventory loadInventoryFile(SplitPlayer player, GameMode gameMode) {
        var file = getInventoryFile(player, gameMode);
        var gson = new Gson();
        try {
            var in = new FileInputStream(file);
            var br = new BufferedReader(new InputStreamReader(in));
            var sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            br.close();
            var contents = sb.toString();
            return gson.fromJson(contents, PlayerInventory.class);
        } catch (Exception ex) {
            System.err.println("Error reading inventory file!");
            return null;
        }
    }

    private void saveInventoryFile(PlayerInventory inventory, File file) {
        var gson = new Gson();
        try {
            var out = new FileOutputStream(file);
            var json = gson.toJson(inventory);
            out.write(json.getBytes());
            out.close();
        } catch (Exception ex) {
            System.err.println("Error setting inventory file contents!");
        }
    }

}
