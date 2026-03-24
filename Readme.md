🧠 Reverie Engine - Hytale AI Mod
Bienvenue sur le projet Reverie Engine !
Ce projet est un mod pour Hytale couplé à un backend Python. Le but est d'injecter du Machine Learning In-Game (via Q-Learning / RL) pour créer des PNJ capables d'apprendre à survivre et à évoluer dans le monde d'Hytale.

🏗️ Architecture du Projet
Le projet est divisé en deux parties distinctes qui communiquent via WebSockets. Le Cerveau (Python) agit comme un Directeur qui envoie des intentions, et le Mod (Java) relaie ces intentions au moteur natif de Hytale.

Plaintext
PROJET IA/
├── ai_brain/            # 🧠 LE CERVEAU (Backend Python)
│   ├── venv/            # L'environnement virtuel (ignoré par Git)
│   ├── server.py        # Le serveur WebSocket qui écoute Hytale
│   ├── model.py         # La logique Q-Learning / Apprentissage
│   └── brain.json       # La mémoire sauvegardée de l'IA
│
└── Hytale_mod/          # 🎮 LE JEU (Plugin Hytale en Java)
    ├── pom.xml          # Configuration Maven et dépendances
    └── src/main/java/   # Code source du plugin (Pont WebSocket, ECS, Commandes)
🗺️ Feuille de Route (Roadmap)
Notre développement est divisé en 5 grandes phases.

Phase 1 : Architecture & Prototypage (✅ Terminée)
[x] Serveur WebSocket en Python (server.py).

[x] Client de test bidirectionnel.

[x] Cœur mathématique de l'IA (Q-Learning) avec sauvegarde persistante (brain.json).

Phase 2 : Intégration Hytale (🚧 En cours)
[x] Traduction du pont de communication en plugin Java (API officielle Hytale).

[x] Génération de l'environnement de test (L'arène 10x10 et le PNJ sont générés via la commande /spawnai).

[x] Remplacer les données simulées par les vraies valeurs du jeu (Récupération des vraies coordonnées X/Y/Z).

[ ] Récupération des vrais points de vie (HP) et de la faim via l'Entity Component System (ECS).

[ ] Connecter l'IA à la "State Machine" de Hytale. Pivot technique : Au lieu d'envoyer des touches physiques (Jump/Move), Python envoie des intentions stratégiques (Sleep, Eat, Attack) qui déclenchent les comportements natifs (JSON) du PNJ via le système de Beacons.

Phase 3 : Perception & Vision (À faire)
[ ] Ajouter un système de "Raycast" (Ligne de vue) pour que le PNJ détecte les obstacles (Murs, Trous).

[ ] Détection des blocs intéressants (ex: Pommes, Blocs à miner).

[ ] Enrichir l'État (State) de l'IA avec ces nouvelles informations de vision.

Phase 4 : Interactions Avancées (À faire)
[ ] Permettre au PNJ d'interagir avec le monde (Casser un bloc, poser un bloc).

[ ] Objectif de survie complexe : "Construire un abri basique avant la nuit".

[ ] Transition de Q-Learning (Tableau) vers du Deep Reinforcement Learning (Réseau de neurones si la Q-Table devient trop grosse).

Phase 5 : Polissage & Release CurseForge (À faire)
[ ] Nettoyage du code et optimisation des performances.

[ ] Création d'une interface en jeu pour paramétrer l'IA sans toucher au code.

[ ] Publication de la version Alpha publique de Reverie Engine.

🎯 Objectifs de l'Équipe
Thomas (Game Design / Ingénierie Hytale) :

Analyser la documentation PDF des PNJ et trouver le cœur/cerveau des NPC dans les fichiers JSON de Hytale.

Comprendre comment fonctionne la State Machine interne (Idle, Sleep, Combat, etc.).

Trouver comment utiliser les senseurs de type "Beacon" (Balises) pour que le mod Java puisse envoyer un signal invisible au PNJ et forcer un changement d'état natif sans casser ses animations et son framerate.