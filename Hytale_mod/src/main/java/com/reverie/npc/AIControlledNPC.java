package com.reverie.npc;

import com.google.gson.JsonObject;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.StateSupport;

import java.util.logging.Level;

public class AIControlledNPC {

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
                    logger.at(Level.WARNING).log("Stat 'Health' non trouvee dans EntityStatMap");
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
                } else {
                    surroundings[0] = 0;
                    surroundings[1] = 0;
                    surroundings[2] = 0;
                    surroundings[3] = 0;
                }
            } catch (Exception e) {
                logger.at(Level.WARNING).log("Impossible lire blocs environnants: " + e.getMessage());
                surroundings[0] = 0;
                surroundings[1] = 0;
                surroundings[2] = 0;
                surroundings[3] = 0;
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
            logger.at(Level.SEVERE).log("Erreur collecte donnees NPC: " + e.getMessage());
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

    public void performAction(String action) {
        logger.at(Level.INFO).log("Execution action: " + action);

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
                    logger.at(Level.WARNING).log("Action inconnue: " + action);
                    break;
            }

        } catch (Exception e) {
            logger.at(Level.SEVERE).log("Erreur execution action '" + action + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void performWander() {
        logger.at(Level.INFO).log("NPC wander (Action_Wander)");
        logger.at(Level.INFO).log("Le NPC retourne a son comportement par defaut");
    }

    private void performEat() {
        logger.at(Level.INFO).log("NPC mange (Action_Eat)");
        simulatedHunger = Math.min(100.0f, simulatedHunger + 30.0f);

        try {
            NPCEntity npcEntity = store.getComponent(npcRef, NPCEntity.getComponentType());
            if (npcEntity != null && npcEntity.getRole() != null) {
                StateSupport stateSupport = npcEntity.getRole().getStateSupport();
                stateSupport.setState(npcRef, "Kweebec_Sapling_Eat", "", store);
                logger.at(Level.INFO).log("Etat 'Kweebec_Sapling_Eat' force (faim: " + simulatedHunger + ")");
            } else {
                logger.at(Level.WARNING).log("NPCEntity ou Role non trouve");
            }
        } catch (Exception e) {
            logger.at(Level.WARNING).log("Erreur performEat: " + e.getMessage());
        }
    }

    private void performSleep() {
        logger.at(Level.INFO).log("NPC dort (Action_Sleep)");

        try {
            EntityStatMap statMap = store.getComponent(npcRef, EntityStatMap.getComponentType());
            if (statMap != null) {
                EntityStatValue healthStat = statMap.get("Health");
                if (healthStat != null) {
                    float newHP = Math.min(healthStat.get() + 20.0f, healthStat.getMax());
                    statMap.setStatValue(healthStat.getIndex(), newHP);
                    logger.at(Level.INFO).log("Vie restauree: +20 HP (nouvelle valeur: " + newHP + ")");
                }
            }
        } catch (Exception e) {
            logger.at(Level.WARNING).log("Impossible restaurer vie: " + e.getMessage());
        }

        try {
            NPCEntity npcEntity = store.getComponent(npcRef, NPCEntity.getComponentType());
            if (npcEntity != null && npcEntity.getRole() != null) {
                StateSupport stateSupport = npcEntity.getRole().getStateSupport();
                stateSupport.setState(npcRef, "Kweebec_Sapling_Sleep", "", store);
                logger.at(Level.INFO).log("Etat 'Kweebec_Sapling_Sleep' force");
            } else {
                logger.at(Level.WARNING).log("NPCEntity ou Role non trouve");
            }
        } catch (Exception e) {
            logger.at(Level.WARNING).log("Erreur performSleep: " + e.getMessage());
        }
    }

    private void performJump() {
        logger.at(Level.INFO).log("NPC saute (Action_Jump)");

        try {
            NPCEntity npcEntity = store.getComponent(npcRef, NPCEntity.getComponentType());
            if (npcEntity != null && npcEntity.getRole() != null) {
                StateSupport stateSupport = npcEntity.getRole().getStateSupport();
                stateSupport.setState(npcRef, "Kweebec_Sapling_Jump", "", store);
                logger.at(Level.INFO).log("Etat 'Kweebec_Sapling_Jump' force");
            } else {
                logger.at(Level.WARNING).log("NPCEntity ou Role non trouve");
            }
        } catch (Exception e) {
            logger.at(Level.WARNING).log("Erreur performJump: " + e.getMessage());
        }
    }

    private void performTurn() {
        logger.at(Level.INFO).log("NPC tourne (Action_Turn)");

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
                logger.at(Level.INFO).log("Rotation modifiee: " + newRotation);
            } else {
                logger.at(Level.WARNING).log("TransformComponent non trouve");
            }
        } catch (Exception e) {
            logger.at(Level.WARNING).log("Erreur performTurn: " + e.getMessage());
        }
    }

    public String getEntityId() {
        return entityId;
    }
}
