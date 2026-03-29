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
import com.hypixel.hytale.server.npc.components.StepComponent;
import com.hypixel.hytale.server.npc.components.Timers;
import com.hypixel.hytale.server.npc.decisionmaker.stateevaluator.StateEvaluator;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.core.modules.entity.component.NewSpawnComponent;
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
                    logger.at(Level.SEVERE).log("traineeNPC est null — NPC '" + NPC_TYPE_ID + "' non trouvé.");
                }

            } catch (Exception e) {
                logger.at(Level.SEVERE).log("Erreur construction arène: " + e.getMessage());
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

                // === LOBOTOMIE : Désactivation de l'IA native d'Hytale ===
                lobotomizeNPC(store, npcRef);

                traineeNPC = new AIControlledNPC(npcRef, npc, store, world, logger);
                logger.at(Level.INFO).log("Bob spawné avec succès !");
            } else {
                logger.at(Level.SEVERE).log("spawnNPC a retourné null pour '" + NPC_TYPE_ID + "'");
            }

        } catch (Exception e) {
            logger.at(Level.SEVERE).log("EXCEPTION lors du spawn PNJ [" + NPC_TYPE_ID + "]");
            logger.at(Level.SEVERE).log("   Type    : " + e.getClass().getName());
            logger.at(Level.SEVERE).log("   Message : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void startTraining(AIBrainClient aiClient) {
        if (traineeNPC == null) {
            logger.at(Level.SEVERE).log("Impossible de démarrer l'entraînement: aucun PNJ spawné");
            return;
        }

        logger.at(Level.INFO).log("Démarrage de l'entraînement IA...");

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
                        logger.at(Level.FINE).log("Données envoyées: " + npcData);
                    } catch (Exception e) {
                        logger.at(Level.SEVERE).log("Erreur dans world.execute(): " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                logger.at(Level.SEVERE).log("Erreur boucle training: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 2, TimeUnit.SECONDS);

        logger.at(Level.INFO).log("Boucle d'entraînement active (cycle: 2s)");
    }

    public void stopTraining() {
        if (trainingTask != null && !trainingTask.isCancelled()) {
            trainingTask.cancel(false);
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        logger.at(Level.INFO).log("Entraînement arrêté");
    }

    /**
     * "Lobotomise" le NPC en désactivant/supprimant les composants ECS
     * responsables de l'IA native d'Hytale (State Machine).
     *
     * Cela empêche Hytale de contrôler les animations et le comportement du NPC,
     * nous laissant le contrôle total via notre IA Python.
     */
    private void lobotomizeNPC(Store<EntityStore> store, Ref<EntityStore> npcRef) {
        logger.at(Level.INFO).log("Lobotomie du PNJ - Désactivation de l'IA native...");

        try {
            // 1. Supprimer NewSpawnComponent (arrête l'animation de spawn en boucle)
            if (store.removeComponentIfExists(npcRef, NewSpawnComponent.getComponentType())) {
                logger.at(Level.INFO).log("  NewSpawnComponent supprimé (animation spawn)");
            } else {
                logger.at(Level.WARNING).log("  NewSpawnComponent non trouvé");
            }

            // 2. Récupérer NPCEntity (pour info - on ne touche pas au Role car setRole(null) crash)
            NPCEntity npcEntity = store.getComponent(npcRef, NPCEntity.getComponentType());
            if (npcEntity != null) {
                // Note: setRole(null) cause un crash, on laisse le Role mais on supprime les composants qui l'exécutent
                logger.at(Level.INFO).log("  NPCEntity trouvé: " + npcEntity.getNPCTypeId());
            } else {
                logger.at(Level.WARNING).log("  NPCEntity non trouvé");
            }

            // 3. Désactiver et supprimer le StateEvaluator (cerveau décisionnel)
            StateEvaluator stateEvaluator = store.getComponent(npcRef, StateEvaluator.getComponentType());
            if (stateEvaluator != null) {
                stateEvaluator.setActive(false);
                logger.at(Level.INFO).log("  StateEvaluator désactivé");
            }
            if (store.removeComponentIfExists(npcRef, StateEvaluator.getComponentType())) {
                logger.at(Level.INFO).log("  StateEvaluator supprimé");
            }

            // 4. Supprimer le StepComponent (tick du comportement NPC)
            if (store.removeComponentIfExists(npcRef, StepComponent.getComponentType())) {
                logger.at(Level.INFO).log("  StepComponent supprimé");
            } else {
                logger.at(Level.WARNING).log("  StepComponent non trouvé");
            }

            // 5. Supprimer les Timers (temporisateurs internes)
            if (store.removeComponentIfExists(npcRef, Timers.getComponentType())) {
                logger.at(Level.INFO).log("  Timers supprimé");
            } else {
                logger.at(Level.WARNING).log("  Timers non trouvé");
            }

            logger.at(Level.INFO).log("Lobotomie terminée - Bob est maintenant une marionnette !");

        } catch (Exception e) {
            logger.at(Level.SEVERE).log("Erreur lors de la lobotomie: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
