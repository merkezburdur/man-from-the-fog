package com.manfromthefog.init;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;

// Modunun Entity ve Sound kayıt sınıflarını buraya import et:
// import com.manfromthefog.entity.TheFogManEntity;
// import com.manfromthefog.init.SoundRegistry;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        
        // 1. /fogspawn Komutu
        dispatcher.register(
            Commands.literal("fogspawn")
                .requires(source -> source.hasPermission(2)) // OP yetkisi
                .executes(context -> spawnFogMan(context.getSource()))
        );

        // 2. /fogman spawn Komutu
        dispatcher.register(
            Commands.literal("fogman")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("spawn")
                    .executes(context -> spawnFogMan(context.getSource()))
                )
        );

        // 3. /manfromthefog spawn Komutu
        dispatcher.register(
            Commands.literal("manfromthefog")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("spawn")
                    .executes(context -> spawnFogMan(context.getSource()))
                )
        );
    }

    // Yaratığı oyuncunun konumunda oluşturan ana fonksiyon
    private static int spawnFogMan(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = player.getLevel();

            // Yaratık nesnesini oluştur
            TheFogManEntity fogMan = EntityRegistry.FOG_MAN.get().create(level);

            if (fogMan != null) {
                // Oyuncunun tam konumuna ve bakış açısına yerleştir
                fogMan.moveTo(
                    player.getX(), 
                    player.getY(), 
                    player.getZ(), 
                    player.getYRot(), 
                    player.getXRot()
                );
                
                // Dünyaya ekle
                level.addFreshEntity(fogMan);

                // Korku sesini çal (bedrock_arrival.ogg veya thunder)
                level.playSound(
                    null, 
                    player.blockPosition(), 
                    SoundRegistry.BEDROCK_ARRIVAL.get(), 
                    SoundSource.HOSTILE, 
                    1.0F, 
                    1.0F
                );

                source.sendSuccess(Component.literal("§c[Man From The Fog] Sislerin içinden çağrıldı!"), true);
                return 1;
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c[Hata] Yaratık çağrılamadı. Oyun modunda / yetkide sorun olabilir."));
        }
        return 0;
    }
}
