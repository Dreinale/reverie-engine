# 🏗️ Architecture du Projet

Le projet est divisé en deux parties distinctes qui communiquent via WebSockets :

```text
PROJET/
├── ai_brain/            # 🧠 LE CERVEAU (Backend Python)
│   ├── venv/            # L'environnement virtuel (ignoré par Git)
│   ├── server.py        # Le serveur WebSocket
│   ├── model.py         # La logique Q-Learning (Maths)
│   └── brain.json       # La mémoire sauvegardée de l'IA (Ignoré par Git)
│
└── Hytale_mod/          # 🎮 LE JEU (Plugin Hytale en Java)
    ├── pom.xml          # Configuration Maven et dépendances
    └── src/main/java/   # Code source du plugin connecté à l'API Hytale
⚙️ Étape 1 : Prérequis (Très Important)
L'architecture repose sur 3 piliers. Installe-les dans cet ordre :

Python 3.x : python.org/downloads

⚠️ CRUCIAL : Coche bien la case "Add python.exe to PATH" lors de l'installation.

Java JDK 21 (Eclipse Adoptium) : adoptium.net

⚠️ CRUCIAL : Lors de l'installation, clique sur la croix rouge à côté de "Définir la variable JAVA_HOME" et choisis "Sera installé sur le disque dur local".

Apache Maven : maven.apache.org

Télécharge le .zip, extrais-le (ex: dans C:\maven), et ajoute le chemin du dossier bin (ex: C:\maven\bin) dans tes variables d'environnement Windows (Variable Path).

(Redémarre ton PC ou ton terminal après ces installations).

📦 Étape 2 : Récupérer et configurer le projet
Ouvre un terminal (PowerShell ou VS Code) et tape les commandes suivantes :

1. Cloner le repo
Bash
git clone <METTRE_LE_LIEN_GITHUB_ICI>
cd reverie-engine
2. Configurer le Cerveau (Python)
On crée un environnement virtuel pour isoler les bibliothèques de l'IA.

Bash
cd ai_brain
python -m venv venv

# Active l'environnement (Windows) :
.\venv\Scripts\activate

# Installe les dépendances requises
pip install websockets numpy
(Tu devrais voir un (venv) vert au début de ta ligne de commande).

3. Configurer le Plugin Hytale (Java)
L'API de Hytale est privée. Pour que le code compile, tu dois dire à Maven où se trouve le code du jeu sur ton PC. Ouvre un nouveau terminal et navigue dans le dossier du mod :

Bash
cd Hytale_mod
Exécute exactement cette commande (avec les guillemets) pour installer l'API Hytale dans ton cache local :

PowerShell
mvn install:install-file "-Dfile=$env:APPDATA\Hytale\install\release\package\game\latest\Server\HytaleServer.jar" "-DgroupId=com.hypixel.hytale" "-DartifactId=Server" "-Dversion=1.0.0" "-Dpackaging=jar" "-DgeneratePom=true"
(Si le chemin ne trouve pas le fichier, cherche HytaleServer.jar sur ton PC et remplace la valeur de -Dfile=).

Une fois que tu as un BUILD SUCCESS, compile notre mod :

Bash
mvn clean package
✅ Attendu : Un fichier reverie-engine-1.0-SNAPSHOT.jar est généré dans le dossier Hytale_mod/target/.

🚀 Étape 3 : Lancer la bête !
1. Activer le mod dans Hytale
Le jeu désactive les mods par défaut. Il faut l'installer et l'autoriser :

Copie le fichier reverie-engine-1.0-SNAPSHOT.jar dans le dossier des mods utilisateur : C:\Users\TON_NOM\AppData\Roaming\Hytale\UserData\Mods\

Va dans le dossier de ta sauvegarde Hytale (ex: UserData\Saves\Dev\).

Ouvre config.json et assure-toi que le mod est activé :

JSON
  "Mods": {
    "com.reverie:reverie-engine": {
      "Enabled": true
    }
  }
Ouvre permissions.json et ajoute ton UUID de joueur dans le groupe "OP" pour avoir le droit d'exécuter les commandes du mod.

2. Allumer le Cerveau (Python)
Dans le terminal où ton (venv) est activé :

Bash
cd ai_brain
python server.py
✅ Attendu : 🧠 Le Cerveau IA est allumé et écoute sur ws://localhost:8765...

3. Exécuter l'IA en jeu
Lance Hytale et connecte-toi à ton monde (ex: Default Flat).

Ouvre le chat avec Entrée.

Tape la commande : /spawnai

🎉 Résultat
Dès que la commande est lancée :

Une arène d'entraînement va se construire à tes pieds.

Un PNJ IA va apparaître.

Regarde la console Python : tu verras les données du PNJ arriver en temps réel et l'IA lui envoyer des ordres pour s'entraîner !