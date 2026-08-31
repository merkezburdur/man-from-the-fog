package com.fogmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;

public class FogMod implements ModInitializer {
    public static final String MOD_ID = "manfromthefog_custom";
    
    // Attığın özel Bedrock / Dweller 2.0 sesi
    public static final Identifier ARRIVAL_SOUND_ID = new Identifier(MOD_ID, "bedrock_arrival");
    public static SoundEvent ARRIVAL_SOUND_EVENT = SoundEvent.of(ARRIVAL_SOUND_ID);

    private int spawnTimer = 0;
    private int soundDurationTicks = 120; // ~6 saniye (sesin uzunluğuna göre ayarla)
    private boolean waitingForLightning = false;
    private BlockPos pendingSpawnPos = null;
    private ServerWorld targetWorld = null;

    @Override
    public void onInitialize() {
        Registry.register(Registries.SOUND_EVENT, ARRIVAL_SOUND_ID, ARRIVAL_SOUND_EVENT);

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (!world.isClient()) {
                ServerWorld serverWorld = (ServerWorld) world;
                
                // Doğma Zamanlayıcısı
                spawnTimer++;
                if (spawnTimer >= 6000) { // Her 5 dakikada bir rastgele tetikleme
                    spawnTimer = 0;
                    for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                        startArrivalSequence(serverWorld, player);
                    }
                }

                // Ses Bittiğinde Yıldırım Çaktırma Mantığı
                if (waitingForLightning) {
                    soundDurationTicks--;
                    if (soundDurationTicks <= 0) {
                        spawnLightning(targetWorld, pendingSpawnPos);
                        waitingForLightning = false;
                        soundDurationTicks = 120; // Reset
                    }
                }
            }
        });
    }

    private void startArrivalSequence(ServerWorld world, ServerPlayerEntity player) {
        BlockPos playerPos = player.getBlockPos();
        // Ağaçların / Ormanın arasından doğması için 15-25 blok mesafe
        BlockPos spawnPos = playerPos.add(world.random.nextBetween(-20, 20), 0, world.random.nextBetween(-20, 20));

        // 1. Önce Attığın Özel Ses Çalar
        world.playSound(null, spawnPos, ARRIVAL_SOUND_EVENT, SoundCategory.HOSTILE, 3.0F, 1.0F);

        // 2. Ses Bittiğinde Yıldırım Çakması İçin Zamanlayıcıyı Başlatır
        this.pendingSpawnPos = spawnPos;
        this.targetWorld = world;
        this.waitingForLightning = true;
    }

    public static void spawnLightning(ServerWorld world, BlockPos pos) {
        if (world != null && pos != null) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(pos));
                world.spawnEntity(lightning);
            }
        }
    }
}
[cite: 1]
