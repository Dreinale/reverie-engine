/**
 * Reverie Engine - Client WebSocket pour Hytale
 * Ce script connecte le jeu au cerveau IA Python via WebSocket
 */

// Configuration
const AI_SERVER_URL = "ws://localhost:8765";
const UPDATE_INTERVAL = 2000; // Envoi des données toutes les 2 secondes

// Variables globales
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

        // Événement: Connexion établie
        ws.onopen = function() {
            console.log("✅ Connecté au Cerveau IA sur " + AI_SERVER_URL);
            isConnected = true;
            startDataLoop();
        };

        // Événement: Message reçu du serveur
        ws.onmessage = function(event) {
            try {
                const response = JSON.parse(event.data);
                console.log("📥 Action reçue du Cerveau:", response);

                // Traiter l'action reçue
                handleAIAction(response);

            } catch (error) {
                console.error("❌ Erreur lors du parsing de la réponse:", error);
            }
        };

        // Événement: Erreur de connexion
        ws.onerror = function(error) {
            console.error("❌ Erreur WebSocket:", error);
            isConnected = false;
        };

        // Événement: Déconnexion
        ws.onclose = function() {
            console.log("🔴 Déconnecté du Cerveau IA");
            isConnected = false;
            stopDataLoop();

            // Tentative de reconnexion après 5 secondes
            setTimeout(function() {
                console.log("🔄 Tentative de reconnexion...");
                connectToAI();
            }, 5000);
        };

    } catch (error) {
        console.error("❌ Erreur lors de la création du WebSocket:", error);
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
 * TODO: Remplacer les données simulées par les vraies données de l'API Hytale
 */
function sendEntityData() {
    if (!isConnected || !ws || ws.readyState !== WebSocket.OPEN) {
        console.log("⚠️ Connexion non disponible, impossible d'envoyer les données");
        return;
    }

    // --- DONNÉES SIMULÉES (à remplacer par l'API Hytale) ---
    const entityData = {
        entity_id: "pnj_01",
        hp: Math.floor(Math.random() * 20) + 1, // HP aléatoire entre 1 et 20
        hunger: Math.floor(Math.random() * 10), // Faim entre 0 et 10
        position: {
            x: Math.floor(Math.random() * 200) - 100, // Position X aléatoire
            y: 64, // Hauteur fixe
            z: Math.floor(Math.random() * 300) - 150  // Position Z aléatoire
        }
    };

    // TODO: Remplacer par quelque chose comme :
    // const entity = Game.getEntity("pnj_01");
    // const entityData = {
    //     entity_id: entity.id,
    //     hp: entity.getHealth(),
    //     hunger: entity.getHunger(),
    //     position: {
    //         x: entity.position.x,
    //         y: entity.position.y,
    //         z: entity.position.z
    //     }
    // };

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

    // --- SIMULATION D'ACTIONS (à remplacer par l'API Hytale) ---
    switch (response.action) {
        case "jump":
            console.log("⬆️ Le PNJ saute !");
            // TODO: Implémenter avec l'API Hytale
            // entity.jump();
            break;

        case "move":
            console.log("🚶 Le PNJ se déplace !");
            // TODO: Implémenter avec l'API Hytale
            // entity.moveTo(response.target);
            break;

        case "idle":
            console.log("💤 Le PNJ reste immobile");
            // TODO: Implémenter avec l'API Hytale
            // entity.setIdle();
            break;

        default:
            console.log("❓ Action inconnue:", response.action);
    }
}

/**
 * Ferme proprement la connexion WebSocket
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

// --- POINT D'ENTRÉE DU MOD ---
// Cette fonction sera appelée au démarrage du mod Hytale
function onModLoad() {
    console.log("🚀 Reverie Engine - Démarrage du client IA");
    connectToAI();
}

// Cette fonction sera appelée à l'arrêt du mod
function onModUnload() {
    console.log("👋 Reverie Engine - Arrêt du client IA");
    disconnect();
}

// Démarrage automatique (à adapter selon l'API Hytale)
onModLoad();

// Optionnel: Exposer les fonctions pour un contrôle manuel
module.exports = {
    connect: connectToAI,
    disconnect: disconnect,
    sendData: sendEntityData
};
