package com.reverie.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.reverie.ReverieEnginePlugin;
import com.reverie.arena.TrainingArena;
import com.reverie.websocket.AIBrainClient;

import java.util.Objects;
import java.util.logging.Level;

/**
 * Commande /spawnai - Spawne l'arène d'entraînement et le PNJ IA à la position du joueur
 */
public class SpawnAICommand extends AbstractPlayerCommand {

    private final ReverieEnginePlugin plugin;

    public SpawnAICommand(ReverieEnginePlugin plugin) {
        super("spawnai", "Spawne l'arène d'entraînement IA à votre position");
        this.plugin = plugin;

        // Donner accès à tous les modes de jeu (pas de permission requise)
        setPermissionGroups("default");
    }

    @Override
    protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> playerEntityRef,
                          PlayerRef playerRef, World world) {

        plugin.getLogger().at(Level.INFO).log("🎮 Commande /spawnai exécutée");

        try {
            // Récupérer la position du joueur
            TransformComponent playerTransform = store.getComponent(
                playerEntityRef,
                Objects.requireNonNull(TransformComponent.getComponentType())
            );

            if (playerTransform == null) {
                context.sendMessage(Message.raw("❌ Impossible de récupérer votre position !"));
                return;
            }

            Vector3d playerPos = playerTransform.getPosition();

            // Informer le joueur
            context.sendMessage(Message.raw("🏗️ Construction de l'arène d'entraînement IA..."));
            plugin.getLogger().at(Level.INFO).log("📍 Position du joueur: " + playerPos);

            // Initialiser le client WebSocket s'il n'existe pas encore
            if (plugin.getAIBrainClient() == null) {
                context.sendMessage(Message.raw("🔌 Connexion au cerveau IA..."));
                AIBrainClient aiClient = new AIBrainClient("ws://localhost:8765", plugin.getLogger());
                aiClient.connect();
                plugin.setAIBrainClient(aiClient);
            }

            // Créer l'arène à la position du joueur
            // IMPORTANT: build() va spawner le NPC et démarrer l'entraînement automatiquement
            TrainingArena arena = new TrainingArena(world, plugin.getLogger(), playerPos);
            plugin.setTrainingArena(arena);

            // Construire l'arène et démarrer l'entraînement (tout se fait dans world.execute)
            arena.build(plugin.getAIBrainClient());

            context.sendMessage(Message.raw("✅ Arène d'entraînement créée avec succès !"));
            context.sendMessage(Message.raw("🤖 Le PNJ IA est maintenant connecté au cerveau Python."));

        } catch (Exception e) {
            plugin.getLogger().at(Level.SEVERE).log("❌ Erreur lors de l'exécution de /spawnai: " + e.getMessage());
            context.sendMessage(Message.raw("❌ Erreur: " + e.getMessage()));
            e.printStackTrace();
        }
    }
}
