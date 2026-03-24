/**
 * Reverie Engine - Client WebSocket pour Hytale
 * Ce script connecte le jeu au cerveau IA Python via WebSocket
 */

let WebSocket;
let isNodeEnvironment = false;

if (typeof window === 'undefined' && typeof process !== 'undefined' && process.versions && process.versions.node) {
    isNodeEnvironment = true;
    try {
        WebSocket = require('ws');
        console.log("Détection: Environnement Node.js - Utilisation de la bibliothèque 'ws'");
    } catch (error) {
        console.error("Erreur: La bibliothèque 'ws' n'est pas installée. Exécutez: npm install ws");
        process.exit(1);
    }
} else {
    WebSocket = globalThis.WebSocket || window.WebSocket;
    console.log("Détection: Environnement Hytale/Browser - Utilisation du WebSocket natif");
}

const AI_SERVER_URL = "ws://localhost:8765";
const UPDATE_INTERVAL = 2000;

let ws = null;
let updateTimer = null;
let isConnected = false;

/**
 * Initialise la connexion WebSocket avec le serveur IA
 */
function connectToAI() {
    console.log("🔌 Tentative de connexion au Cerveau IA...");

    try {
        ws = new WebSocket(AI_SERVER_URL);

        ws.onopen = function() {
            console.log("✅ Connecté au Cerveau IA sur " + AI_SERVER_URL);
            isConnected = true;
            startDataLoop();
        };

        ws.onmessage = function(event) {
            try {
                const response = JSON.parse(event.data);
                console.log("📥 Action reçue du Cerveau:", response);

                handleAIAction(response);

            } catch (error) {
                console.error("❌ Erreur lors du parsing de la réponse:", error);
            }
        };

        ws.onerror = function(error) {
            console.error("❌ Erreur WebSocket:", error);
            isConnected = false;
        };

        ws.onclose = function() {
            console.log("Déconnecté du Cerveau IA");
            isConnected = false;
            stopDataLoop();

            setTimeout(function() {
                console.log("Tentative de reconnexion...");
                connectToAI();
            }, 5000);
        };

    } catch (error) {
        console.error("Erreur lors de la création du WebSocket:", error);
        isConnected = false;
    }
}

/**
 * Démarre la boucle d'envoi des données
 */
function startDataLoop() {
    if (updateTimer) {
        clearInterval(updateTimer);
    }

    console.log("🔄 Démarrage de la boucle d'envoi des données (intervalle: " + UPDATE_INTERVAL + "ms)");

    updateTimer = setInterval(function() {
        sendEntityData();
    }, UPDATE_INTERVAL);
}

/**
 * Arrête la boucle d'envoi des données
 */
function stopDataLoop() {
    if (updateTimer) {
        clearInterval(updateTimer);
        updateTimer = null;
        console.log("⏹️ Boucle d'envoi arrêtée");
    }
}

/**
 * Collecte et envoie les données d'un PNJ au serveur IA
 */
function sendEntityData() {
    if (!isConnected || !ws || ws.readyState !== WebSocket.OPEN) {
        console.log("⚠️ Connexion non disponible, impossible d'envoyer les données");
        return;
    }

    const entityData = {
        entity_id: "pnj_01",
        hp: Math.floor(Math.random() * 20) + 1,
        hunger: Math.floor(Math.random() * 10),
        position: {
            x: Math.floor(Math.random() * 200) - 100,
            y: 64, // Hauteur fixe
            z: Math.floor(Math.random() * 300) - 150
        }
    };

    try {
        const message = JSON.stringify(entityData);
        ws.send(message);
        console.log("📤 Données envoyées:", entityData);
    } catch (error) {
        console.error("❌ Erreur lors de l'envoi des données:", error);
    }
}

/**
 * Traite une action reçue du serveur IA
 * @param {Object} response - La réponse du serveur contenant l'action
 */
function handleAIAction(response) {
    if (!response.action) {
        console.log("⚠️ Aucune action spécifiée dans la réponse");
        return;
    }

    console.log("🎬 Exécution de l'action:", response.action);

    if (response.message) {
        console.log("💬 Message:", response.message);
    }

    switch (response.action) {
        case "jump":
            console.log("⬆️ Le PNJ saute !");
            break;

        case "move":
            console.log("🚶 Le PNJ se déplace !");
            break;

        case "idle":
            console.log("💤 Le PNJ reste immobile");
            break;

        default:
            console.log("❓ Action inconnue:", response.action);
    }
}

/**
 * Ferme la connexion WebSocket
 */
function disconnect() {
    console.log("🔌 Fermeture de la connexion...");
    stopDataLoop();

    if (ws) {
        ws.close();
        ws = null;
    }

    isConnected = false;
}

function onModLoad() {
    console.log("Reverie Engine - Démarrage du client IA");
    connectToAI();
}

function onModUnload() {
    console.log("👋 Reverie Engine - Arrêt du client IA");
    disconnect();
}

onModLoad();

module.exports = {
    connect: connectToAI,
    disconnect: disconnect,
    sendData: sendEntityData
};
