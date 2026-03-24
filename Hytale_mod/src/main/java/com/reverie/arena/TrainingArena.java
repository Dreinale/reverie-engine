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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * TrainingArena - Crée et gère l'arène d'entraînement pour les PNJ IA
 */
public class TrainingArena {

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

    /**
     * Construit l'arène d'entraînement (plateforme + murs) et démarre l'entraînement
     */
    public void build(AIBrainClient aiClient) {
        logger.at(Level.INFO).log("🏗️ Construction de l'arène d'entraînement...");

        world.execute(() -> {
            try {
                logger.at(Level.INFO).log("Arène: Plateforme 10x10 à " + arenaCenter);
                logger.at(Level.INFO).log("Arène: Murs de 2 blocs de hauteur");
                logger.at(Level.INFO).log("Arène construite (structure conceptuelle)");

                spawnTraineeNPC();

                logger.at(Level.INFO).log("DEBUG: Vérification traineeNPC avant startTraining: " + traineeNPC);
                if (traineeNPC != null) {
                    startTraining(aiClient);
                } else {
                    logger.at(Level.SEVERE).log("ERREUR: traineeNPC est null après spawnTraineeNPC()");
                }

            } catch (Exception e) {
                logger.at(Level.SEVERE).log("Erreur lors de la construction de l'arène: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Fait apparaître le PNJ d'entraînement au centre de l'arène
     */
    private void spawnTraineeNPC() {
        logger.at(Level.INFO).log("👤 Spawn du PNJ d'entraînement...");

        try {
            Store<EntityStore> store = world.getEntityStore().getStore();

            Vector3d spawnPosition = new Vector3d(arenaCenter.x, arenaCenter.y + 1, arenaCenter.z);
            Vector3f rotation = new Vector3f(0, 0, 0);

            var result = NPCPlugin.get().spawnNPC(
                store,
                "Kweebec_Sapling",
                "",
                spawnPosition,
                rotation
            );

            if (result != null) {
                Ref<EntityStore> npcRef = result.left();
                INonPlayerCharacter npc = result.right();

                traineeNPC = new AIControlledNPC(npcRef, npc, store, world, logger);

                logger.at(Level.INFO).log("PNJ spawné avec succès: " + npcRef.toString());
            } else {
                logger.at(Level.SEVERE).log("Échec du spawn du PNJ");
            }

        } catch (Exception e) {
            logger.at(Level.SEVERE).log("Erreur lors du spawn du PNJ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Démarre la boucle d'entraînement (envoie les données au cerveau IA)
     */
    public void startTraining(AIBrainClient aiClient) {
        if (traineeNPC == null) {
            logger.at(Level.SEVERE).log("Impossible de démarrer l'entraînement: aucun PNJ n'est spawné");
            return;
        }

        logger.at(Level.INFO).log("Démarrage de l'entraînement IA...");
        logger.at(Level.INFO).log("DEBUG: traineeNPC = " + traineeNPC);
        logger.at(Level.INFO).log("DEBUG: aiClient = " + aiClient);

        scheduler = Executors.newScheduledThreadPool(1);
        logger.at(Level.INFO).log("DEBUG: Scheduler créé");

        aiClient.setActionListener((action) -> {
            logger.at(Level.INFO).log("DEBUG: Action reçue de l'IA: " + action);
            world.execute(() -> {
                logger.at(Level.INFO).log("DEBUG: Exécution de l'action dans world.execute()");
                traineeNPC.performAction(action);
            });
        });

        logger.at(Level.INFO).log("DEBUG: Action listener enregistré");

        trainingTask = scheduler.scheduleAtFixedRate(() -> {
            logger.at(Level.INFO).log("DEBUG: Timer déclenché (avant world.execute)");

            try {
                world.execute(() -> {
                    logger.at(Level.INFO).log("DEBUG: Entrée dans world.execute()");

                    try {
                        logger.at(Level.INFO).log("DEBUG: Début collecte des données...");

                        String npcData = traineeNPC.collectData();

                        logger.at(Level.INFO).log("DEBUG: Données collectées: " + npcData);

                        logger.at(Level.INFO).log("DEBUG: Envoi des données à l'IA...");
                        aiClient.sendData(npcData);

                        logger.at(Level.INFO).log("Données envoyées au cerveau IA: " + npcData);

                    } catch (Exception e) {
                        logger.at(Level.SEVERE).log("ERREUR CACHÉE dans world.execute(): " + e.getMessage());
                        logger.at(Level.SEVERE).log("Stack trace complète:");
                        e.printStackTrace();
                    }
                });

                logger.at(Level.INFO).log("DEBUG: Sortie de world.execute()");

            } catch (Exception e) {
                logger.at(Level.SEVERE).log("ERREUR CACHÉE lors de l'appel à world.execute(): " + e.getMessage());
                logger.at(Level.SEVERE).log("Stack trace complète:");
                e.printStackTrace();
            }
        }, 0, 2, TimeUnit.SECONDS);

        logger.at(Level.INFO).log("Boucle d'entraînement démarrée (envoi toutes les 2 secondes)");
        logger.at(Level.INFO).log("DEBUG: trainingTask = " + trainingTask);
    }

    /**
     * Arrête l'entraînement
     */
    public void stopTraining() {
        if (trainingTask != null && !trainingTask.isCancelled()) {
            trainingTask.cancel(false);
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        logger.at(Level.INFO).log("⏹Entraînement arrêté");
    }
}
