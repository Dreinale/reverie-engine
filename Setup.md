# 🛠️ Setup de l'environnement

Bienvenue sur le projet **Reverie Engine** !
Ce projet est un mod pour Hytale couplé à un backend Python. Le but est d'injecter du Machine Learning In-Game (via Q-Learning / RL) pour créer des PNJ capables d'apprendre à survivre et à évoluer dans le monde d'Hytale.

---

## 🏗️ Architecture du Projet

Le projet est divisé en deux parties distinctes qui communiquent via WebSockets :

```text
PROJET/
├── ai_brain/            # 🧠 LE CERVEAU (Backend Python)
│   ├── venv/            # L'environnement virtuel (ignoré par Git)
│   ├── server.py        # Le serveur WebSocket qui écoute Hytale
│   └── model.py         # La logique Q-Learning / Apprentissage
│
└── hytale_mod/          # 🎮 LE JEU (Client Hytale)
    ├── scripts/
    │   └── ai_client.js # Le script qui extrait la data du PNJ et l'envoie au serveur
    └── config.json      # Configuration du mod
```

---

## ⚙️ Étape 1 : Prérequis

Télécharge et installe la dernière version de Python : [python.org/downloads](https://python.org/downloads)

⚠️ **CRUCIAL** : Lors de l'installation de Python, assure-toi de bien cocher la case **"Add python.exe to PATH"** en bas de la toute première fenêtre.

Installe **Node.js** (On en a besoin temporairement pour tester le script JS sans avoir à lancer Hytale à chaque fois).

---

## 📦 Étape 2 : Récupérer et configurer le projet

Ouvre un terminal (PowerShell ou VS Code) et tape les commandes suivantes :

### 1. Cloner le repo
```bash
git clone <METTRE_LE_LIEN_GITHUB_ICI>
cd "Projet IA"
```

### 2. Configurer le Cerveau (Python)
On va créer un environnement virtuel pour isoler les bibliothèques IA de ton PC.

```bash
# Va dans le dossier de l'IA
cd ai_brain

# Crée l'environnement virtuel (venv)
python -m venv venv

# Active l'environnement virtuel :
# -> Sur Windows :
.\venv\Scripts\activate
# -> Sur Mac/Linux :
# source venv/bin/activate

# Installe les dépendances requises
pip install websockets numpy
```

(Si tout s'est bien passé, tu devrais voir un petit `(venv)` vert au début de ta ligne de commande).

---

## 🚀 Étape 3 : Lancer le test de connexion

On va vérifier que le jeu et l'IA arrivent bien à se parler. Il te faudra **deux terminaux ouverts**.

### Terminal 1 : Allumer le Serveur Python
Assure-toi que ton environnement `(venv)` est activé, puis lance :

```bash
python server.py
```

✅ **Attendu** : Le terminal affiche `🧠 Le Cerveau IA est allumé et écoute sur ws://localhost:8765...`

### Terminal 2 : Lancer le Client (Simulation Hytale)
Ouvre un nouveau terminal (sans le venv) et navigue dans le dossier des scripts Hytale :

```bash
cd hytale_mod/scripts
```

Installe la librairie WebSocket pour Node.js (juste pour le test local) :

```bash
npm install ws
```

(Note : Assure-toi que la ligne `const WebSocket = require('ws');` est bien présente en haut de `ai_client.js` pour le test).

Puis lance la simulation du PNJ :

```bash
node ai_client.js
```

---

## 🎉 Résultat

Dans le **Terminal 1 (Python)**, tu devrais voir arriver les stats du PNJ (HP, Faim, Position) toutes les 2 secondes, et le serveur répondre "Jump!".
