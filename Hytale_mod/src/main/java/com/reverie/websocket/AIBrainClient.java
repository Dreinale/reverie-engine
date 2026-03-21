package com.reverie.websocket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.logger.HytaleLogger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * AIBrainClient - Client WebSocket pour communiquer avec le cerveau IA Python
 */
public class AIBrainClient extends WebSocketClient {

    private final HytaleLogger logger;
    private Consumer<String> actionListener;

    public AIBrainClient(String serverUri, HytaleLogger logger) {
        super(URI.create(serverUri));
        this.logger = logger;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.at(Level.INFO).log("✅ Connecté au Cerveau IA sur " + getURI());
    }

    @Override
    public void onMessage(String message) {
        logger.at(Level.INFO).log("📥 Message reçu du Cerveau: " + message);

        try {
            JsonObject response = JsonParser.parseString(message).getAsJsonObject();

            if (response.has("action")) {
                String action = response.get("action").getAsString();

                // Afficher le message si présent
                if (response.has("message")) {
                    logger.at(Level.INFO).log("💬 " + response.get("message").getAsString());
                }

                // Notifier le listener
                if (actionListener != null) {
                    actionListener.accept(action);
                }
            }

        } catch (Exception e) {
            logger.at(Level.SEVERE).log("❌ Erreur lors du parsing de la réponse: " + e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.at(Level.WARNING).log("🔴 Déconnecté du Cerveau IA: " + reason);

        // Tentative de reconnexion après 5 secondes
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                logger.at(Level.INFO).log("🔄 Tentative de reconnexion...");
                reconnect();
            } catch (InterruptedException e) {
                logger.at(Level.SEVERE).log("❌ Erreur lors de la reconnexion: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void onError(Exception ex) {
        logger.at(Level.SEVERE).log("❌ Erreur WebSocket: " + ex.getMessage());
    }

    /**
     * Envoie les données du PNJ au cerveau IA
     * @param jsonData Données JSON du PNJ
     */
    public void sendData(String jsonData) {
        if (isOpen()) {
            send(jsonData);
            logger.at(Level.FINE).log("📤 Données envoyées: " + jsonData);
        } else {
            logger.at(Level.WARNING).log("⚠️ WebSocket non connecté, impossible d'envoyer les données");
        }
    }

    /**
     * Définit le listener pour les actions reçues du cerveau IA
     * @param listener Callback qui reçoit l'action à effectuer
     */
    public void setActionListener(Consumer<String> listener) {
        this.actionListener = listener;
    }

    /**
     * Ferme proprement la connexion
     */
    public void disconnect() {
        logger.at(Level.INFO).log("🔌 Fermeture de la connexion au Cerveau IA...");
        close();
    }
}
