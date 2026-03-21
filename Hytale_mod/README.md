# Reverie Engine - Plugin Hytale

Plugin Java pour Hytale qui connecte des PNJ au cerveau IA Python pour l'apprentissage par renforcement (Q-Learning).

## 🏗️ Architecture

```
Hytale_mod/
├── pom.xml                          # Configuration Maven
├── src/main/java/com/reverie/
│   ├── ReverieEnginePlugin.java     # Plugin principal
│   ├── arena/
│   │   └── TrainingArena.java       # Création de l'arène d'entraînement
│   ├── npc/
│   │   └── AIControlledNPC.java     # Contrôle du PNJ par l'IA
│   └── websocket/
│       └── AIBrainClient.java       # Client WebSocket vers Python
└── scripts/
    └── ai_client.js                 # [OBSOLÈTE] Prototype JavaScript
```

## ⚙️ Fonctionnalités

### 1. Arène d'entraînement automatique
- Construit automatiquement une plateforme 10x10 à Y=100
- Murs de 2 blocs de hauteur pour confiner le PNJ
- Spawn d'un PNJ Kweebec au centre

### 2. Collecte de données en temps réel
- **Position** : Coordonnées X, Y, Z du PNJ
- **HP** : Points de vie (actuellement simulé, en attente de l'API)
- **Hunger** : Niveau de faim (actuellement simulé, en attente de l'API)

### 3. Communication avec le cerveau IA
- Connexion WebSocket à `ws://localhost:8765`
- Envoi des données toutes les 2 secondes
- Réception et exécution des actions ("jump", "move", "idle")

### 4. Système d'actions
- **jump** : Fait sauter le PNJ
- **move** : Fait se déplacer le PNJ
- **idle** : Le PNJ reste immobile

## 📦 Installation

### Prérequis
- Java 17 ou supérieur
- Maven 3.8+
- Serveur Hytale en développement

### Étapes

1. **Cloner le projet**
```bash
cd "c:\Users\nzo\Desktop\Projet IA\Hytale_mod"
```

2. **Compiler le plugin**
```bash
mvn clean package
```

3. **Copier le JAR**
```bash
# Le fichier JAR sera dans target/reverie-engine-1.0-SNAPSHOT.jar
# Copiez-le dans le dossier plugins/ de votre serveur Hytale
```

4. **Démarrer le serveur Python**
```bash
cd "../ai_brain"
.\venv\Scripts\activate  # Windows
python server.py
```

5. **Lancer le serveur Hytale**
Le plugin se chargera automatiquement et créera l'arène d'entraînement.

## 🔧 Configuration

Le plugin se connecte automatiquement à `ws://localhost:8765`. Pour changer l'adresse du serveur IA, modifiez la ligne dans `ReverieEnginePlugin.java` :

```java
aiBrainClient = new AIBrainClient("ws://localhost:8765", getLogger());
```

## 📊 Flux de données

```
┌─────────────────┐      WebSocket      ┌──────────────────┐
│  Plugin Hytale  │ ←─────────────────→ │  Serveur Python  │
│                 │                      │   (Q-Learning)   │
│  AIControlledNPC│                      │                  │
│  - Position     │  ──── JSON ───→     │  - État          │
│  - HP           │                      │  - Récompense    │
│  - Hunger       │  ←──── Action ───   │  - Action        │
└─────────────────┘                      └──────────────────┘
```

## 🚧 TODOs (En attente de l'API finale)

### Placement de blocs
Actuellement commenté dans `TrainingArena.java` :
```java
// TODO: Utiliser l'API de placement de blocs quand disponible
// placeBlock(x, y, z, "Stone");
```

### Données du PNJ
Actuellement simulées dans `AIControlledNPC.java` :
```java
// TODO: Remplacer par la vraie API
// int hp = store.getComponent(npcRef, HealthComponent.getComponentType()).getHealth();
// int hunger = store.getComponent(npcRef, HungerComponent.getComponentType()).getHunger();
```

### Contrôle du PNJ
Actuellement des stubs dans `AIControlledNPC.java` :
```java
// TODO: Implémenter avec l'API Hytale
// npc.jump();
// npc.moveTo(targetPosition);
```

## 🐛 Débogage

Les logs du plugin sont affichés dans la console du serveur Hytale avec les préfixes :
- `🚀` : Démarrage
- `✅` : Succès
- `❌` : Erreur
- `📥` : Réception de données
- `📤` : Envoi de données
- `🎬` : Exécution d'action

## 📚 Documentation de référence

- [Hytale Modding API](https://hytalemodding.dev/en/docs)
- [Spawning Entities](https://hytalemodding.dev/en/docs/guides/plugin/spawning-entities)
- [Spawning NPCs](https://hytalemodding.dev/en/docs/guides/plugin/spawning-npcs)

## 🤝 Contribution

Ce projet est un prototype de recherche pour l'intégration d'IA dans Hytale. Les contributions et suggestions sont les bienvenues !

## 📄 Licence

Projet éducatif - Reverie Engine Team
