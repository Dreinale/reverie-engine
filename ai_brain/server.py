import asyncio
import websockets
import json
from model import QLearningAgent # <-- On importe notre cerveau mathématique !

# On initialise notre agent IA avec les 3 actions possibles dans Hytale
agent = QLearningAgent(actions=["jump", "move", "idle"])

# Chargement de la mémoire sauvegardée (si elle existe)
agent.load()

# Variables globales pour mémoriser le tour précédent (Mémoire courte du PNJ)
previous_state = None
previous_action = None
previous_data = None

def calculate_reward(old_data, new_data):
    """Calcule les points (+/-) en comparant l'ancien et le nouvel état du PNJ"""
    if old_data is None:
        return 0
    
    reward = 0
    # S'il a perdu de la vie, on le punit violemment
    if new_data["hp"] < old_data["hp"]:
        reward -= 10
    # S'il a moins faim (donc il a réussi à manger), on le récompense grandement
    elif new_data["hunger"] < old_data["hunger"]:
        reward += 10
    # Petite pénalité de temps pour l'inciter à agir et ne pas rester inactif
    else:
        reward -= 0.1 
        
    return reward

async def hytale_connection(websocket):
    global previous_state, previous_action, previous_data
    
    print("🟢 Un client Hytale s'est connecté au Cerveau !")
    try:
        async for message in websocket:
            print(f"DEBUG: Message brut reçu: {message}")
            game_data = json.loads(message)
            
            # 1. L'IA traduit les données brutes en "Concept" (ex: "healthy_hungry")
            current_state = agent.get_state(game_data)
            
            # 2. On calcule la récompense basée sur le tour précédent
            reward = calculate_reward(previous_data, game_data)
            
            # 3. L'IA APPREND (mise à jour de la Q-Table)
            if previous_state is not None and previous_action is not None:
                agent.learn(previous_state, previous_action, reward, current_state)
            
            # 4. L'IA choisit sa prochaine action grâce aux maths
            action = agent.choose_action(current_state)
            
            # 5. On sauvegarde la situation actuelle pour le prochain tour
            previous_state = current_state
            previous_action = action
            previous_data = game_data
            
            # 6. On renvoie l'action au jeu
            response = {
                "action": action, 
                "message": f"État: {current_state} | Action choisie: {action}"
            }
            
            await websocket.send(json.dumps(response))
            print(f"📤 Ordre envoyé : {action}\n")
            
    except websockets.exceptions.ConnectionClosed:
        print("🔴 Le client Hytale s'est déconnecté.")
        # Sauvegarde automatique de la Q-Table à la déconnexion
        agent.save()
        # On efface la mémoire courte à la déconnexion
        previous_state = previous_action = previous_data = None

async def main():
    async with websockets.serve(hytale_connection, "localhost", 8765):
        print("🧠 Le Cerveau IA est allumé et écoute sur ws://localhost:8765...")
        await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())