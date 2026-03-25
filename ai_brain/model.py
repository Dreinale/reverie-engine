import numpy as np
import random
import json
import os

class QLearningAgent:
    def __init__(self, actions, learning_rate=0.1, discount_factor=0.9, epsilon=0.2):
        self.actions = actions  # Liste des actions possibles (ex: ["jump", "move", "idle"])
        self.lr = learning_rate # Vitesse d'apprentissage (Alpha)
        self.gamma = discount_factor # Importance des récompenses futures (Gamma)
        self.epsilon = epsilon  # Taux d'exploration (20% du temps, il fait n'importe quoi pour découvrir)
        
        # La Q-Table : un dictionnaire qui associe un "état" à un tableau de scores
        self.q_table = {}

    def get_state(self, game_data):
        """
        Transforme les données brutes du jeu en un État simplifié pour la Q-Table.
        Maintenant avec la VISION !
        """
        # On simplifie les HP (Vivant/Blessé) et la Faim (Plein/Affamé)
        hp_status = "healthy" if game_data.get("hp", 20) > 10 else "hurt"
        hunger_status = "full" if game_data.get("hunger", 10) > 5 else "hungry"
        
        # On analyse le radar (Vision)
        surroundings = game_data.get("surroundings", {})
        
        # S'il y a un bloc différent de 0 (Air) autour de lui, il est près d'un mur
        is_near_wall = "clear"
        if surroundings.get("north", 0) != 0 or surroundings.get("south", 0) != 0 or \
           surroundings.get("east", 0) != 0 or surroundings.get("west", 0) != 0:
            is_near_wall = "blocked"
            
        # Le nouvel état mental de l'IA ressemble à ça : "healthy_hungry_clear" ou "hurt_full_blocked"
        return f"{hp_status}_{hunger_status}_{is_near_wall}"

    def get_q_values(self, state):
        """
        Récupère les scores des actions pour un état donné.
        Si l'état est inconnu, on le crée avec des scores à 0.
        """
        if state not in self.q_table:
            self.q_table[state] = np.zeros(len(self.actions))
        return self.q_table[state]

    def choose_action(self, state):
        """
        L'IA choisit une action basée sur l'exploration (hasard) ou l'exploitation (meilleur score).
        """
        if random.uniform(0, 1) < self.epsilon:
            # Mode EXPLORATION : Action au hasard pour tester de nouvelles choses
            action_idx = random.randint(0, len(self.actions) - 1)
        else:
            # Mode EXPLOITATION : On prend l'action avec le score le plus élevé
            q_values = self.get_q_values(state)
            action_idx = np.argmax(q_values)
        
        return self.actions[action_idx]

    def learn(self, state, action, reward, next_state):
        """
        Applique la formule de Bellman (Q-Learning) pour mettre à jour le cerveau après une action.
        """
        q_values = self.get_q_values(state)
        next_q_values = self.get_q_values(next_state)
        
        action_idx = self.actions.index(action)
        
        # Le coeur mathématique du Q-Learning :
        td_target = reward + self.gamma * np.max(next_q_values)
        td_error = td_target - q_values[action_idx]
        
        # Mise à jour de la case dans la Q-Table
        self.q_table[state][action_idx] += self.lr * td_error
        
        # Petit log pour le debug
        print(f"🧠 Apprentissage -> État: {state} | Action: {action} | Récompense: {reward} | Nouveau Score: {round(self.q_table[state][action_idx], 3)}")

    def save(self, filename="brain.json"):
        """
        Sauvegarde la Q-Table dans un fichier JSON pour persister l'apprentissage.
        """
        # Convertit les tableaux numpy en listes pour le JSON
        q_table_serializable = {state: q_values.tolist() for state, q_values in self.q_table.items()}
        with open(filename, 'w') as f:
            json.dump(q_table_serializable, f, indent=4)
        print("💾 Cerveau sauvegardé (brain.json) !")

    def load(self, filename="brain.json"):
        """
        Charge la Q-Table depuis un fichier JSON pour reprendre l'apprentissage.
        """
        if os.path.exists(filename):
            with open(filename, 'r') as f:
                data = json.load(f)
                self.q_table = {state: np.array(q_values) for state, q_values in data.items()}
            print("🧠 Cerveau chargé avec succès !")
        else:
            print("🌱 Nouveau cerveau, l'IA repart de zéro.")