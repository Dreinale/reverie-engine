# 🧠 Reverie Engine - Hytale AI Mod

Bienvenue sur le projet **Reverie Engine** ! 
Ce projet est un mod pour Hytale couplé à un backend Python. Le but est d'injecter du Machine Learning In-Game (via Q-Learning / RL) pour créer des PNJ capables d'apprendre à survivre et à évoluer dans le monde d'Hytale.

---

## 🏗️ Architecture du Projet

Le projet est divisé en deux parties distinctes qui communiquent via WebSockets :

```text
PROJET IA/
├── ai_brain/            # 🧠 LE CERVEAU (Backend Python)
│   ├── venv/            # L'environnement virtuel (ignoré par Git)
│   ├── server.py        # Le serveur WebSocket qui écoute Hytale
│   └── model.py         # La logique Q-Learning / Apprentissage
│
└── hytale_mod/          # 🎮 LE JEU (Client Hytale)
    ├── scripts/         
    │   └── ai_client.js # Le script qui extrait la data du PNJ et l'envoie au serveur
    └── config.json      # Configuration du mod

    ## 🗺️ Feuille de Route (Roadmap)

Notre développement est divisé en 5 grandes phases. 

### Phase 1 : Architecture & Prototypage (✅ Terminée)
- [x] Serveur WebSocket en Python (`server.py`).
- [x] Client de test bidirectionnel.
- [x] Cœur mathématique de l'IA (Q-Learning) avec sauvegarde persistante (`brain.json`).

### Phase 2 : Intégration Hytale (🚧 En cours)
- [ ] Traduction du pont de communication en plugin **Java** (API officielle Hytale).
- [ ] Génération de l'environnement de test (Enclos plat 10x10).
- [ ] Remplacer les données simulées par les vraies valeurs du jeu (HP, Faim, Position X/Y/Z).
- [ ] Connecter les actions de l'IA aux vrais mouvements du PNJ (Jump, Move, etc.).

### Phase 3 : Perception & Vision (À faire)
- [ ] Ajouter un système de "Raycast" (Ligne de vue) pour que le PNJ détecte les obstacles (Murs, Trous).
- [ ] Détection des blocs intéressants (ex: Pommes, Blocs à miner).
- [ ] Enrichir l'État (State) de l'IA avec ces nouvelles informations de vision.

### Phase 4 : Interactions Avancées (À faire)
- [ ] Permettre au PNJ d'interagir avec le monde (Casser un bloc, poser un bloc).
- [ ] Objectif de survie complexe : "Construire un abri basique avant la nuit".
- [ ] Transition de Q-Learning (Tableau) vers du Deep Reinforcement Learning (Réseau de neurones si la Q-Table devient trop grosse).

### Phase 5 : Polissage & Release CurseForge (À faire)
- [ ] Nettoyage du code et optimisation des performances.
- [ ] Création d'une interface en jeu pour paramétrer l'IA sans toucher au code.
- [ ] Publication de la version Alpha publique de **Reverie Engine**.