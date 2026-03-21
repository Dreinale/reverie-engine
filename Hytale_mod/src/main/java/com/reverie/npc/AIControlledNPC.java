package com.reverie.npc;

import com.google.gson.JsonObject;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Objects;
import java.util.logging.Level;

/**
 * AIControlledNPC - Wrapper pour un PNJ contrôlé par l'IA
 * Gère la collecte des données et l'exécution des actions
 */
public class AIControlledNPC {

    private final Ref<EntityStore> npcRef;
    private final INonPlayerCharacter npc;
    private final Store<EntityStore> store;
    private final World world;
    private final HytaleLogger logger;

    // Simulations temporaires (à remplacer par les vraies données de l'API)
    private int currentHP = 20;
    private int currentHunger = 5;

    public AIControlledNPC(Ref<EntityStore> npcRef, INonPlayerCharacter npc,
                           Store<EntityStore> store, World world, HytaleLogger logger) {
        this.npcRef = npcRef;
        this.npc = npc;
        this.store = store;
        this.world = world;
        this.logger = logger;
    }

    /**
     * Collecte les données du PNJ pour envoyer au cerveau IA
     * Format: {"entity_id": "pnj_01", "hp": 20, "hunger": 5, "position": {"x": 0, "y": 100, "z": 0}}
     */
    public String collectData() {
        JsonObject data = new JsonObject();

        // ID de l'entité
        data.addProperty("entity_id", npcRef.toString());

        // HP (santé)
        // TODO: Remplacer par la vraie API quand disponible
        // Exemple: int hp = store.getComponent(npcRef, HealthComponent.getComponentType()).getHealth();
        data.addProperty("hp", currentHP);

        // Hunger (faim)
        // TODO: Remplacer par la vraie API quand disponible
        // Exemple: int hunger = store.getComponent(npcRef, HungerComponent.getComponentType()).getHunger();
        data.addProperty("hunger", currentHunger);

        // Position
        try {
            TransformComponent transform = store.getComponent(
                npcRef,
                Objects.requireNonNull(TransformComponent.getComponentType())
            );

            if (transform != null) {
                Vector3d pos = transform.getPosition();
                JsonObject position = new JsonObject();
                position.addProperty("x", pos.x);
                position.addProperty("y", pos.y);
                position.addProperty("z", pos.z);
                data.add("position", position);
            }
        } catch (Exception e) {
            logger.at(Level.SEVERE).log("❌ Erreur lors de la récupération de la position: " + e.getMessage());

            // Position par défaut en cas d'erreur
            JsonObject position = new JsonObject();
            position.addProperty("x", 0);
            position.addProperty("y", 100);
            position.addProperty("z", 0);
            data.add("position", position);
        }

        return data.toString();
    }

    /**
     * Exécute une action reçue du cerveau IA
     * @param action L'action à effectuer ("jump", "move", "idle")
     */
    public void performAction(String action) {
        logger.at(Level.INFO).log("🎬 PNJ exécute l'action: " + action);

        switch (action) {
            case "jump":
                performJump();
                break;

            case "move":
                performMove();
                break;

            case "idle":
                performIdle();
                break;

            default:
                logger.at(Level.WARNING).log("❓ Action inconnue: " + action);
        }

        // Simulation de changements d'état (pour le Q-Learning)
        simulateStateChanges(action);
    }

    /**
     * Fait sauter le PNJ
     */
    private void performJump() {
        logger.at(Level.INFO).log("⬆️ Le PNJ saute !");

        // TODO: Implémenter avec l'API Hytale quand disponible
        // Exemple: npc.jump();
        // Ou: PhysicsComponent physics = store.getComponent(npcRef, PhysicsComponent.getComponentType());
        //     physics.applyVerticalVelocity(0.5);
    }

    /**
     * Fait se déplacer le PNJ
     */
    private void performMove() {
        logger.at(Level.INFO).log("🚶 Le PNJ se déplace !");

        // TODO: Implémenter avec l'API Hytale quand disponible
        // Exemple: npc.moveTo(targetPosition);
        // Ou: MovementComponent movement = store.getComponent(npcRef, MovementComponent.getComponentType());
        //     movement.moveForward(1.0);
    }

    /**
     * Le PNJ reste inactif
     */
    private void performIdle() {
        logger.at(Level.INFO).log("💤 Le PNJ reste immobile");

        // TODO: Implémenter avec l'API Hytale quand disponible
        // Exemple: npc.setIdle();
    }

    /**
     * Simule des changements d'état du PNJ pour l'apprentissage
     * (À RETIRER quand l'API fournira les vraies données)
     */
    private void simulateStateChanges(String action) {
        // Simulation: sauter consomme de la faim
        if ("jump".equals(action)) {
            currentHunger = Math.max(0, currentHunger - 1);
            logger.at(Level.FINE).log("🍽️ Faim: " + currentHunger);
        }

        // Simulation: bouger consomme de la faim et peut causer des dégâts
        if ("move".equals(action)) {
            currentHunger = Math.max(0, currentHunger - 1);

            // 10% de chance de prendre des dégâts (chute, collision, etc.)
            if (Math.random() < 0.1) {
                currentHP = Math.max(0, currentHP - 2);
                logger.at(Level.FINE).log("💔 HP: " + currentHP);
            }
        }

        // Simulation: rester idle récupère lentement la faim
        if ("idle".equals(action)) {
            currentHunger = Math.min(10, currentHunger + 1);
            logger.at(Level.FINE).log("🍽️ Faim récupérée: " + currentHunger);
        }
    }

    /**
     * Réinitialise l'état du PNJ
     */
    public void reset() {
        currentHP = 20;
        currentHunger = 5;
        logger.at(Level.INFO).log("🔄 État du PNJ réinitialisé");
    }
}
