package tech.snaco.SplitWorld;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import net.minecraft.item.ItemStack;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SplitWorld implements ModInitializer {
    Type ITEM_STACK_LIST_TYPE = new TypeToken<ArrayList<ItemStack>>() {}.getType();
    Gson gson = new Gson();

	@Override
	public void onInitialize() {
        ServerTickEvents.START_WORLD_TICK.register((world) -> {
            var players = world.getPlayers();
            for (var player : players) {
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

    private boolean setGameMode(ServerPlayerEntity player, GameMode targetGameMode) {
        var savedCreative = getInventoryFile(player, GameMode.CREATIVE);
        var savedSurvival = getInventoryFile(player, GameMode.SURVIVAL);
        if (player.isCreative() && targetGameMode == GameMode.SURVIVAL) {
            // save creative from player
            saveInventoryFile(player.getInventory(), savedCreative);
            // load survival
            if (savedSurvival.exists()) {
                player.getInventory().clone(loadInventoryFile(player, targetGameMode));
            } else {
                player.getInventory().clear();
            }
        } else if (!player.isCreative() && targetGameMode == GameMode.CREATIVE) {
            // save survival inventory
            saveInventoryFile(player.getInventory(), savedSurvival);
            // load creative inventory
            if (savedCreative.exists()) {
                player.getInventory().clone(loadInventoryFile(player, targetGameMode));
            } else {
                player.getInventory().clear();
            }
        }
        player.changeGameMode(targetGameMode);
        return true;
    }

    private File getInventoryFile(ServerPlayerEntity player, GameMode gameMode) {
        var fileName = String.format("%s_%s.inv", player.getUuidAsString(), gameMode.toString());
        var file = new File(fileName);
        return file;
    }

    private PlayerInventory loadInventoryFile(ServerPlayerEntity player, GameMode gameMode) {
        var file = getInventoryFile(player, gameMode);
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
            ArrayList<ItemStack> stacks = gson.fromJson(contents, ITEM_STACK_LIST_TYPE);
            var temp_inv = new PlayerInventory(null);
            for (int i = 0; i < temp_inv.size() && i < stacks.size(); i++) {
                temp_inv.insertStack(i, stacks.get(i));
            }
            return temp_inv;
        } catch (Exception ex) {
            System.err.println("Error reading inventory file!");
            return null;
        }
    }

    private void saveInventoryFile(PlayerInventory inventory, File file) {
        try {
            var stacks = extractStacks(inventory);
            var out = new FileOutputStream(file);
            var json = gson.toJson(stacks);
            out.write(json.getBytes());
            out.close();
        } catch (Exception ex) {
            System.err.println("Error setting inventory file contents!");
        }
    }

    private ArrayList<ItemStack> extractStacks(PlayerInventory inventory) {
        var output = new ArrayList<ItemStack>();
        for (int i = 0; i < inventory.size(); i++) {
            output.add(inventory.getStack(i));
        }
        return output;
    }
}
