package tech.snaco.SplitWorld.mixin;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import org.spongepowered.asm.mixin.Mixin;

import com.mojang.authlib.GameProfile;

@Mixin(ServerPlayerEntity.class)
public class SplitPlayer extends ServerPlayerEntity {

    public SplitPlayer(MinecraftServer server, ServerWorld world, GameProfile profile, PlayerPublicKey publicKey) {
        super(server, world, profile, publicKey);
    }

    public PlayerInventory backupInventory;
}


