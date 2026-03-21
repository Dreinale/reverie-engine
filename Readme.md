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