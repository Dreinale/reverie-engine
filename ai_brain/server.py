import asyncio
import websockets
import json
from model import QLearningAgent 

agent = QLearningAgent(actions=["Action_Wander", "Action_Turn", "Action_Eat", "Action_Sleep"])
agent.load()

previous_state = None
previous_action = None
previous_data = None

def calculate_reward(old_data, new_data, state, action):
    """Calcule les points (+/-) avec les nouvelles règles"""
    if old_data is None:
        return 0
    
    reward = 0
    if new_data["hp"] < old_data["hp"]:
        reward -= 10
        
    elif new_data["hunger"] > old_data["hunger"]:
        reward += 15 
        
    elif "blocked" in state and action == "Action_Wander":
        reward -= 2 
        
    elif "blocked" in state and action == "Action_Turn":
        reward += 5

    else:
        reward -= 0.1 
        
    return reward

async def hytale_connection(websocket):
    global previous_state, previous_action, previous_data
    
    print("🟢 Un client Hytale s'est connecté au Cerveau V3 (Vision & Murs) !")
    try:
        async for message in websocket:
            game_data = json.loads(message)
            
            current_state = agent.get_state(game_data)
            
            # On passe aussi l'état et l'action précédente pour punir les murs
            reward = calculate_reward(previous_data, game_data, previous_state, previous_action)
            
            if previous_state is not None and previous_action is not None:
                agent.learn(previous_state, previous_action, reward, current_state)
            
            action = agent.choose_action(current_state)
            
            previous_state = current_state
            previous_action = action
            previous_data = game_data
            
            response = {
                "action": action, 
                "message": f"État: {current_state} | Intention: {action}"
            }
            
            await websocket.send(json.dumps(response))
            
    except websockets.exceptions.ConnectionClosed:
        print("🔴 Le client Hytale s'est déconnecté.")
        agent.save()
        previous_state = previous_action = previous_data = None

async def main():
    async with websockets.serve(hytale_connection, "localhost", 8765):
        print("🧠 Le Cerveau IA est allumé et écoute sur ws://localhost:8765...")
        await asyncio.Future()

if __name__ == "__main__":
    try:
        # On lance le serveur
        asyncio.run(main())
    except KeyboardInterrupt:
        # Quand tu fais Ctrl+C, ce bloc s'active !
        print("\n🛑 Arrêt manuel demandé (Ctrl+C)...")
        print("💾 Sauvegarde du cerveau en cours...")
        agent.save()
        print("👋 Serveur arrêté proprement. À bientôt !")