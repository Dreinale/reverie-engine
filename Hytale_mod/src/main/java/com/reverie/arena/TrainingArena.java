package com.reverie.arena;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.reverie.npc.AIControlledNPC;
import com.reverie.websocket.AIBrainClient;
import it.unimi.dsi.fastutil.Pair;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * TrainingArena - Crée et gère l'arène d'entraînement pour les PNJ IA
 */
public class TrainingArena {

    private final World world;
    private final HytaleLogger logger;
    private final Vector3d arenaCenter = new Vector3d(0, 100, 0);
    private final int arenaSize = 10;

    private AIControlledNPC traineeNPC;
    private ScheduledExecutorService scheduler;

    public TrainingArena(World world, HytaleLogger logger) {
        this.world = world;
        this.logger = logger;
    }

    /**
     * Construit l'arène d'entraînement (plateforme + murs)
     */
    public void build() {
        logger.at(Level.INFO).log("🏗️ Construction de l'arène d'entraînement...");

        world.execute(() -> {
            try {
                // TODO: La méthode exacte pour placer des blocs dépendra de l'API finale
                // Pour l'instant, on log la structure conceptuelle

                logger.at(Level.INFO).log("📐 Arène: Plateforme 10x10 à " + arenaCenter);
                logger.at(Level.INFO).log("📐 Arène: Murs de 2 blocs de hauteur");

                // Conceptuellement:
                // 1. Placer le sol (10x10 en pierre)
                // for (int x = -5; x <= 5; x++) {
                //     for (int z = -5; z <= 5; z++) {
                //         placeBlock(arenaCenter.x + x, arenaCenter.y, arenaCenter.z + z, "Stone");
                //     }
                // }

                // 2. Placer les murs (2 blocs de hauteur)
                // for (int y = 1; y <= 2; y++) {
                //     // Murs nord et sud
                //     for (int x = -5; x <= 5; x++) {
                //         placeBlock(arenaCenter.x + x, arenaCenter.y + y, arenaCenter.z - 5, "Stone");
                //         placeBlock(arenaCenter.x + x, arenaCenter.y + y, arenaCenter.z + 5, "Stone");
                //     }
                //     // Murs est et ouest
                //     for (int z = -4; z <= 4; z++) {
                //         placeBlock(arenaCenter.x - 5, arenaCenter.y + y, arenaCenter.z + z, "Stone");
                //         placeBlock(arenaCenter.x + 5, arenaCenter.y + y, arenaCenter.z + z, "Stone");
                //     }
                // }

                logger.at(Level.INFO).log("✅ Arène construite (structure conceptuelle)");

                // Spawner le PNJ d'entraînement
                spawnTraineeNPC();

            } catch (Exception e) {
                logger.at(Level.SEVERE).log("❌ Erreur lors de la construction de l'arène: " + e.getMessage());
            }
        });
    }

    /**
     * Fait apparaître le PNJ d'entraînement au centre de l'arène
     */
    private void spawnTraineeNPC() {
        logger.at(Level.INFO).log("👤 Spawn du PNJ d'entraînement...");

        world.execute(() -> {
            try {
                Store<EntityStore> store = world.getEntityStore().getStore();

                // Position au centre de l'arène
                Vector3d spawnPosition = new Vector3d(arenaCenter.x, arenaCenter.y + 1, arenaCenter.z);
                Vector3f rotation = new Vector3f(0, 0, 0);

                // Spawner un Kweebec comme PNJ de test
                var result = NPCPlugin.get().spawnNPC(
                    store,
                    "Kweebec_Sapling",
                    "", // Configuration vide
                    spawnPosition,
                    rotation
                );

                if (result != null) {
                    Ref<EntityStore> npcRef = result.left();
                    INonPlayerCharacter npc = result.right();

                    // Créer le wrapper de contrôle IA
                    traineeNPC = new AIControlledNPC(npcRef, npc, store, world, logger);

                    logger.at(Level.INFO).log("✅ PNJ spawné avec succès: " + npcRef.toString());
                } else {
                    logger.at(Level.SEVERE).log("❌ Échec du spawn du PNJ");
                }

            } catch (Exception e) {
                logger.at(Level.SEVERE).log("❌ Erreur lors du spawn du PNJ: " + e.getMessage());
            }
        });
    }

    /**
     * Démarre la boucle d'entraînement (envoie les données au cerveau IA)
     */
    public void startTraining(AIBrainClient aiClient) {
        if (traineeNPC == null) {
            logger.at(Level.SEVERE).log("❌ Impossible de démarrer l'entraînement: aucun PNJ n'est spawné");
            return;
        }

        logger.at(Level.INFO).log("🎓 Démarrage de l'entraînement IA...");

        scheduler = Executors.newScheduledThreadPool(1);

        // Envoyer les données du PNJ toutes les 2 secondes
        scheduler.scheduleAtFixedRate(() -> {
            world.execute(() -> {
                try {
                    // Récupérer les données du PNJ
                    String npcData = traineeNPC.collectData();

                    // Envoyer au cerveau IA
                    aiClient.sendData(npcData);

                } catch (Exception e) {
                    logger.at(Level.SEVERE).log("❌ Erreur pendant l'entraînement: " + e.getMessage());
                }
            });
        }, 0, 2, TimeUnit.SECONDS);

        // Écouter les actions du cerveau IA
        aiClient.setActionListener((action) -> {
            world.execute(() -> {
                traineeNPC.performAction(action);
            });
        });
    }

    /**
     * Arrête l'entraînement
     */
    public void stopTraining() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            logger.at(Level.INFO).log("⏹️ Entraînement arrêté");
        }
    }
}
