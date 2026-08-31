package com.fogmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class FogMod implements ModInitializer {

    @Override
    public void onInitialize() {
        // Komutları oyun başladığında kaydet
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            
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
        });
    }

    private static int spawnEntity(ServerCommandSource source) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            ServerWorld world = player.getServerWorld();

            // Modun eklediği yaratığı ID üzerinden arar
            EntityType<?> fogManType = Registries.ENTITY_TYPE.get(new Identifier("fogmod", "fog_man"));
            
            if (fogManType == null) {
                fogManType = Registries.ENTITY_TYPE.get(new Identifier("manfromthefog", "fog_man"));
            }

            if (fogManType != null) {
                var entity = fogManType.create(world);
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
            }
            source.sendError(Text.literal("§c[Hata] Yaratık ID'si bulunamadı!"));
        } catch (Exception e) {
            source.sendError(Text.literal("§c[Hata] Yaratık çağrılırken sorun oluştu!"));
        }
        return 0;
    }
}
