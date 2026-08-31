package com.fogmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class FogMod implements ModInitializer {

    @Override
    public void onInitialize() {
        // Komutları Fabric dinleyicisine kaydet
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ModCommands.register(dispatcher);
        });
    }
}

// Komutların yönetildiği sınıf
class ModCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        
        // 1. /fogspawn
        dispatcher.register(
            CommandManager.literal("fogspawn")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> spawnEntity(context.getSource()))
        );

        // 2. /fogman spawn
        dispatcher.register(
            CommandManager.literal("fogman")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("spawn")
                    .executes(context -> spawnEntity(context.getSource()))
                )
        );

        // 3. /manfromthefog spawn
        dispatcher.register(
            CommandManager.literal("manfromthefog")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("spawn")
                    .executes(context -> spawnEntity(context.getSource()))
                )
        );
    }

    private static int spawnEntity(ServerCommandSource source) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            ServerWorld world = player.getServerWorld();

            var entity = EntityRegistry.FOG_MAN.create(world);
            if (entity != null) {
                entity.refreshPositionAndAngles(
                    player.getX(), 
                    player.getY(), 
                    player.getZ(), 
                    player.getYaw(), 
                    player.getPitch()
                );
                world.spawnEntity(entity);
                source.sendFeedback(() -> Text.literal("§c[Man From The Fog] Sislerin içinden çağrıldı!"), false);
                return 1;
            }
        } catch (Exception e) {
            source.sendError(Text.literal("§c[Hata] Yaratık çağrılamadı!"));
        }
        return 0;
    }
}
