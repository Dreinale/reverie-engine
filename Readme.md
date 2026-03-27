# 🧠 Reverie Engine - Hytale AI Mod

Bienvenue sur le projet Reverie Engine !
Ce projet est un mod pour Hytale couplé à un backend Python. Le but est d'injecter du Machine Learning In-Game (via Q-Learning / RL) pour créer des PNJ capables d'apprendre à survivre et à évoluer dans le monde d'Hytale.

## 🏗️ Architecture du Projet
Le projet est divisé en deux parties distinctes qui communiquent via WebSockets. Le Cerveau (Python) agit comme un Directeur qui envoie des intentions, et le Mod (Java) relaie ces intentions au moteur natif de Hytale en prenant le contrôle direct du corps du PNJ.

```text
PROJET IA/
├── ai_brain/            # 🧠 LE CERVEAU (Backend Python)
│   ├── venv/            # L'environnement virtuel (ignoré par Git)
│   ├── server.py        # Le serveur WebSocket qui écoute Hytale
│   ├── model.py         # La logique Q-Learning / Apprentissage
│   └── brain.json       # La mémoire sauvegardée de l'IA
│
└── Hytale_mod/          # 🎮 LE JEU (Plugin Hytale en Java)
    ├── pom.xml          # Configuration Maven et dépendances
    └── src/main/
        ├── java/        # Code source du plugin (Pont WebSocket, ECS, Commandes)
        └── resources/   # Fichiers intégrés au mod (ex: Le JSON du PNJ "Bob")


## 🗺️ Roadmap
Notre développement est divisé en 5 grandes phases.

Phase 1 : Architecture & Prototypage (✅ Terminée)
- [x] Serveur WebSocket en Python (server.py).
- [x] Client de test bidirectionnel.
- [x] Cœur mathématique de l'IA (Q-Learning) avec sauvegarde persistante (brain.json).

Phase 2 : Intégration Hytale (✅ Terminée)
- [x] Traduction du pont de communication en plugin Java (API officielle Hytale).
- [x] Génération de l'environnement de test (L'arène 10x10 et le PNJ sont générés via la commande /spawnai).
- [x] Remplacer les données simulées par les vraies valeurs du jeu (Coordonnées X/Y/Z, HP, Faim via l'ECS).
- [x] Connecter l'IA au corps du PNJ. Pivot technique majeur : Au lieu d'utiliser la "State Machine" complexe d'Hytale, nous avons créé un "Variant" JSON de PNJ totalement lobotomisé (sens à 0). Le Java contrôle directement le modèle 3D en forçant la lecture des animations (AnimationUtils.playAnimation).

Phase 3 : Perception & Vision (🚧 En cours)
[ ] Ajouter un système de "Raycast" (Ligne de vue) pour que le PNJ détecte les obstacles (Murs, Trous) depuis le code Java.
[ ] Détection des blocs intéressants environnants (ex: Pommes, Blocs à miner).
[ ] Transmettre ces données via WebSocket pour enrichir l'État (State) de l'IA.

Phase 4 : Interactions Avancées (À faire)
[ ] Permettre au PNJ d'interagir avec le monde physique (Casser un bloc, poser un bloc) via l'API Hytale.
[ ] Nouvel objectif d'entraînement : "Construire un abri basique avant la nuit".
[ ] Transition de Q-Learning (Tableau) vers du Deep Reinforcement Learning (Réseau de neurones) si le nombre d'états devient trop grand.

Phase 5 : Polissage & Release CurseForge (À faire)
[ ] Nettoyage du code et optimisation des performances réseau.
[ ] Création d'une interface en jeu pour paramétrer l'IA sans toucher au code.
[ ] Publication de la version Alpha publique de Reverie Engine.