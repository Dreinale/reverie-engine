package com.reverie.npc;

import com.google.gson.JsonObject;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;

import java.util.logging.Level;

/**
 * AIControlledNPC - Contrôle du PNJ marionnette par l'IA Python
 *
 * v4.0 : PNJ basé sur Test_Player_Hidden_Template (aucun State Machine).
 *         Animations jouées directement via AnimationUtils.playAnimation().
 */
public class AIControlledNPC {

    // =========================================================================
    // ANIMATIONS — Noms des clips du modèle Kweebec
    // =========================================================================
    private static final String ANIM_WALK  = "Walk";
    private static final String ANIM_EAT   = "Jump";
    private static final String ANIM_SLEEP = "Sleep";
    private static final String ANIM_JUMP  = "Jump";
    private static final String ANIM_IDLE  = "Idle";
    // =========================================================================

    private final Ref<EntityStore> npcRef;
    private final INonPlayerCharacter npc;
    private final Store<EntityStore> store;
    private final World world;
    private final HytaleLogger logger;

    private float simulatedHunger = 100.0f;
    private final String entityId;

    public AIControlledNPC(Ref<EntityStore> npcRef, INonPlayerCharacter npc,
                          Store<EntityStore> store, World world, HytaleLogger logger) {
        this.npcRef = npcRef;
        this.npc = npc;
        this.store = store;
        this.world = world;
        this.logger = logger;
        this.entityId = "npc_" + npc.getNPCTypeId() + "_" + npcRef.toString();
    }

    /**
     * Collecte les données du PNJ pour les envoyer au cerveau IA Python
     */
    public String collectData() {
        try {
            TransformComponent transform = store.getComponent(npcRef, TransformComponent.getComponentType());
            Vector3d position = transform != null ? transform.getPosition() : new Vector3d(0, 0, 0);

            EntityStatMap statMap = store.getComponent(npcRef, EntityStatMap.getComponentType());
            float currentHP = 100.0f;

            if (statMap != null) {
                EntityStatValue healthStat = statMap.get("Health");
                if (healthStat != null) {
                    currentHP = healthStat.get();
                } else {
                    logger.at(Level.WARNING).log("Stat 'Health' non trouvée dans EntityStatMap");
                }
            }

            simulatedHunger = Math.max(0, simulatedHunger - 0.5f);

            int blockX = (int) Math.floor(position.x);
            int blockY = (int) Math.floor(position.y);
            int blockZ = (int) Math.floor(position.z);

            int[] surroundings = new int[4];
            try {
                long chunkPos = ((long)(blockX >> 4) << 32) | ((long)(blockZ >> 4) & 0xFFFFFFFFL);
                BlockAccessor blockAccessor = world.getChunkIfLoaded(chunkPos);

                if (blockAccessor != null) {
                    surroundings[0] = blockAccessor.getBlock(blockX, blockY, blockZ + 1);
                    surroundings[1] = blockAccessor.getBlock(blockX, blockY, blockZ - 1);
                    surroundings[2] = blockAccessor.getBlock(blockX + 1, blockY, blockZ);
                    surroundings[3] = blockAccessor.getBlock(blockX - 1, blockY, blockZ);
                }
            } catch (Exception e) {
                logger.at(Level.WARNING).log("Impossible lire blocs environnants: " + e.getMessage());
            }

            JsonObject data = new JsonObject();
            data.addProperty("entity_id", entityId);
            data.addProperty("hp", currentHP);
            data.addProperty("hunger", simulatedHunger);

            JsonObject posJson = new JsonObject();
            posJson.addProperty("x", position.x);
            posJson.addProperty("y", position.y);
            posJson.addProperty("z", position.z);
            data.add("position", posJson);

            JsonObject surroundingsJson = new JsonObject();
            surroundingsJson.addProperty("north", surroundings[0]);
            surroundingsJson.addProperty("south", surroundings[1]);
            surroundingsJson.addProperty("east", surroundings[2]);
            surroundingsJson.addProperty("west", surroundings[3]);
            data.add("surroundings", surroundingsJson);

            return data.toString();

        } catch (Exception e) {
            logger.at(Level.SEVERE).log("Erreur collecte données NPC: " + e.getMessage());
            e.printStackTrace();

            JsonObject fallback = new JsonObject();
            fallback.addProperty("entity_id", entityId);
            fallback.addProperty("hp", 0);
            fallback.addProperty("hunger", 0);
            fallback.add("position", new JsonObject());
            fallback.add("surroundings", new JsonObject());
            return fallback.toString();
        }
    }

    /**
     * Exécute une action reçue du cerveau IA Python.
     * Comme le NPC est une "coquille vide" (Instructions: []),
     * aucun behavior natif ne viendra écraser notre setState().
     */
    public void performAction(String action) {
        logger.at(Level.INFO).log("🎬 Action: " + action);

        try {
            switch (action) {
                case "Action_Wander":
                    performWander();
                    break;
                case "Action_Eat":
                    performEat();
                    break;
                case "Action_Sleep":
                    performSleep();
                    break;
                case "Action_Jump":
                    performJump();
                    break;
                case "Action_Turn":
                    performTurn();
                    break;
                default:
                    logger.at(Level.WARNING).log("⚠️ Action inconnue: " + action);
                    break;
            }
        } catch (Exception e) {
            logger.at(Level.SEVERE).log("❌ Erreur action '" + action + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================================
    // MÉTHODE UTILITAIRE : Joue une animation directement via ModelComponent
    // =========================================================================

    /**
     * Joue une animation sur le PNJ marionnette.
     * Test_Player_Hidden_Template n'ayant pas de State Machine,
     * on passe directement par ModelComponent.setAnimation().
     *
     * @param animName Nom du clip d'animation (ex: "Walk", "Eat", "Sleep")
     */
    private void playAnimation(String animName) {
        try {
            AnimationUtils.playAnimation(npcRef, AnimationSlot.Action, animName, store);
            logger.at(Level.INFO).log("   → Animation: " + animName);
        } catch (Exception e) {
            logger.at(Level.WARNING).log("⚠️ Erreur playAnimation(" + animName + "): " + e.getMessage());
        }
    }

    // =========================================================================
    // ACTIONS INDIVIDUELLES
    // =========================================================================

    private void performWander() {
        logger.at(Level.INFO).log("🚶 NPC marche (Action_Wander)");
        playAnimation(ANIM_WALK);
    }

    private void performEat() {
        logger.at(Level.INFO).log("🍽️ NPC mange (Action_Eat)");
        simulatedHunger = Math.min(100.0f, simulatedHunger + 30.0f);
        playAnimation(ANIM_EAT);
        logger.at(Level.INFO).log("   → Faim: " + simulatedHunger);
    }

    private void performSleep() {
        logger.at(Level.INFO).log("💤 NPC dort (Action_Sleep)");

        try {
            EntityStatMap statMap = store.getComponent(npcRef, EntityStatMap.getComponentType());
            if (statMap != null) {
                EntityStatValue healthStat = statMap.get("Health");
                if (healthStat != null) {
                    float newHP = Math.min(healthStat.get() + 20.0f, healthStat.getMax());
                    statMap.setStatValue(healthStat.getIndex(), newHP);
                    logger.at(Level.INFO).log("   → Vie restaurée: " + newHP + " HP");
                }
            }
        } catch (Exception e) {
            logger.at(Level.WARNING).log("⚠️ Impossible restaurer vie: " + e.getMessage());
        }

        playAnimation(ANIM_SLEEP);
    }

    private void performJump() {
        logger.at(Level.INFO).log("⬆️ NPC saute (Action_Jump)");
        playAnimation(ANIM_JUMP);
    }

    private void performTurn() {
        logger.at(Level.INFO).log("🔄 NPC tourne (Action_Turn)");

        try {
            TransformComponent transform = store.getComponent(npcRef, TransformComponent.getComponentType());
            if (transform != null) {
                Vector3f currentRotation = transform.getRotation();
                float newYaw = currentRotation.x + 90.0f;
                if (newYaw >= 360.0f) {
                    newYaw -= 360.0f;
                }
                Vector3f newRotation = new Vector3f(newYaw, currentRotation.y, currentRotation.z);
                transform.setRotation(newRotation);
                logger.at(Level.INFO).log("   → Rotation: " + newRotation);
            }
        } catch (Exception e) {
            logger.at(Level.WARNING).log("⚠️ Erreur rotation: " + e.getMessage());
        }

        playAnimation(ANIM_WALK);
    }

    public String getEntityId() {
        return entityId;
    }
}
