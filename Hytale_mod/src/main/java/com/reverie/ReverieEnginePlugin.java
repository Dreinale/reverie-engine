package com.reverie;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.reverie.arena.TrainingArena;
import com.reverie.commands.SpawnAICommand;
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
    protected void setup() {
        getLogger().at(Level.INFO).log("Reverie Engine - Setup...");

        getCommandRegistry().registerCommand(new SpawnAICommand(this));

        getLogger().at(Level.INFO).log("Commande /spawnai enregistrée !");
    }

    @Override
    protected void start() {
        getLogger().at(Level.INFO).log("Reverie Engine - Démarrage...");
        getLogger().at(Level.INFO).log("Reverie Engine activé ! Utilisez /spawnai pour créer l'arène d'entraînement.");
    }

    @Override
    protected void shutdown() {
        getLogger().at(Level.INFO).log("Reverie Engine - Arrêt...");

        if (aiBrainClient != null) {
            aiBrainClient.disconnect();
        }

        getLogger().at(Level.INFO).log("Reverie Engine désactivé !");
    }

    public AIBrainClient getAIBrainClient() {
        return aiBrainClient;
    }

    public void setAIBrainClient(AIBrainClient aiBrainClient) {
        this.aiBrainClient = aiBrainClient;
    }

    public void setTrainingArena(TrainingArena trainingArena) {
        this.trainingArena = trainingArena;
    }
}
