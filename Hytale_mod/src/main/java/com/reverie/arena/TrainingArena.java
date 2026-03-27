package com.reverie.arena;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.reverie.npc.AIControlledNPC;
import com.reverie.websocket.AIBrainClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class TrainingArena {

    private static final String NPC_TYPE_ID = "Bob";

    private final World world;
    private final HytaleLogger logger;
    private final Vector3d arenaCenter;
    private final int arenaSize = 10;

    private AIControlledNPC traineeNPC;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> trainingTask;

    public TrainingArena(World world, HytaleLogger logger, Vector3d position) {
        this.world = world;
        this.logger = logger;
        this.arenaCenter = position;
    }

    public void build(AIBrainClient aiClient) {
        logger.at(Level.INFO).log("🏗️ Construction de l'arène d'entraînement...");

        world.execute(() -> {
            try {
                logger.at(Level.INFO).log("Arène: Plateforme 10x10 à " + arenaCenter);
                logger.at(Level.INFO).log("Arène: Murs de 2 blocs de hauteur");
                logger.at(Level.INFO).log("Arène construite (structure conceptuelle)");

                spawnTraineeNPC();

                if (traineeNPC != null) {
                    startTraining(aiClient);
                } else {
                    logger.at(Level.SEVERE).log("❌ traineeNPC est null — NPC '" + NPC_TYPE_ID + "' non trouvé.");
                }

            } catch (Exception e) {
                logger.at(Level.SEVERE).log("❌ Erreur construction arène: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void spawnTraineeNPC() {
        logger.at(Level.INFO).log("👤 Spawn du PNJ [" + NPC_TYPE_ID + "]...");

        try {
            Store<EntityStore> store = world.getEntityStore().getStore();
            Vector3d spawnPosition = new Vector3d(arenaCenter.x, arenaCenter.y + 1, arenaCenter.z);
            Vector3f rotation = new Vector3f(0, 0, 0);

            var result = NPCPlugin.get().spawnNPC(store, NPC_TYPE_ID, "", spawnPosition, rotation);

            if (result != null) {
                Ref<EntityStore> npcRef = result.left();
                INonPlayerCharacter npc = result.right();
                traineeNPC = new AIControlledNPC(npcRef, npc, store, world, logger);
                logger.at(Level.INFO).log("✅ Bob spawné avec succès !");
            } else {
                logger.at(Level.SEVERE).log("❌ spawnNPC a retourné null pour '" + NPC_TYPE_ID + "'");
            }

        } catch (Exception e) {
            logger.at(Level.SEVERE).log("❌ EXCEPTION lors du spawn PNJ [" + NPC_TYPE_ID + "]");
            logger.at(Level.SEVERE).log("   Type    : " + e.getClass().getName());
            logger.at(Level.SEVERE).log("   Message : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void startTraining(AIBrainClient aiClient) {
        if (traineeNPC == null) {
            logger.at(Level.SEVERE).log("❌ Impossible de démarrer l'entraînement: aucun PNJ spawné");
            return;
        }

        logger.at(Level.INFO).log("🧠 Démarrage de l'entraînement IA...");

        scheduler = Executors.newScheduledThreadPool(1);

        aiClient.setActionListener((action) -> {
            logger.at(Level.FINE).log("📥 Action reçue: " + action);
            world.execute(() -> {
                traineeNPC.performAction(action);
            });
        });

        trainingTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                world.execute(() -> {
                    try {
                        String npcData = traineeNPC.collectData();
                        aiClient.sendData(npcData);
                        logger.at(Level.FINE).log("📤 Données envoyées: " + npcData);
                    } catch (Exception e) {
                        logger.at(Level.SEVERE).log("❌ Erreur dans world.execute(): " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                logger.at(Level.SEVERE).log("❌ Erreur boucle training: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 2, TimeUnit.SECONDS);

        logger.at(Level.INFO).log("✅ Boucle d'entraînement active (cycle: 2s)");
    }

    public void stopTraining() {
        if (trainingTask != null && !trainingTask.isCancelled()) {
            trainingTask.cancel(false);
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        logger.at(Level.INFO).log("⏹ Entraînement arrêté");
    }
}
