package com.reverie;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import com.reverie.arena.TrainingArena;
import com.reverie.websocket.AIBrainClient;

import java.util.logging.Level;

/**
 * Reverie Engine - Plugin principal
 * Système d'apprentissage IA pour PNJ Hytale utilisant Q-Learning
 */
public class ReverieEnginePlugin extends JavaPlugin {

    private AIBrainClient aiBrainClient;
    private TrainingArena trainingArena;

    public ReverieEnginePlugin(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void start() {
        getLogger().at(Level.INFO).log("🚀 Reverie Engine - Démarrage...");

        // Enregistrer l'événement de chargement des mondes
        getEventRegistry().register(AllWorldsLoadedEvent.class, this::onAllWorldsLoaded);

        getLogger().at(Level.INFO).log("✅ Reverie Engine activé !");
    }

    @Override
    protected void shutdown() {
        getLogger().at(Level.INFO).log("👋 Reverie Engine - Arrêt...");

        // Déconnexion du cerveau IA
        if (aiBrainClient != null) {
            aiBrainClient.disconnect();
        }

        getLogger().at(Level.INFO).log("✅ Reverie Engine désactivé !");
    }

    /**
     * Handler pour l'événement de chargement complet des mondes
     */
    private void onAllWorldsLoaded(AllWorldsLoadedEvent event) {
        getLogger().at(Level.INFO).log("🌍 Tous les mondes sont chargés, initialisation de l'arène d'entraînement...");

        // Récupérer le premier monde disponible
        for (World world : Universe.get().getWorlds().values()) {
            // Créer l'arène d'entraînement
            trainingArena = new TrainingArena(world, getLogger());
            trainingArena.build();

            // Initialiser la connexion au cerveau IA Python
            aiBrainClient = new AIBrainClient("ws://localhost:8765", getLogger());
            aiBrainClient.connect();

            // Démarrer l'entraînement
            trainingArena.startTraining(aiBrainClient);

            // On utilise seulement le premier monde
            break;
        }
    }
}
