import asyncio
import websockets
import json

async def hytale_connection(websocket):
    print("🟢 Un client Hytale s'est connecté au Cerveau !")
    try:
        # On écoute en boucle les messages envoyés par le jeu
        async for message in websocket:
            # On transforme le texte reçu en dictionnaire Python
            game_data = json.loads(message)
            print(f"📥 Données reçues du PNJ : {game_data}")

            # --- C'est ici que l'IA réfléchira plus tard ---
            # Pour l'instant, on envoie juste une action de test
            
            response = {
                "action": "jump", 
                "message": "Cerveau: J'ai bien reçu tes infos, saute !"
            }
            
            # On renvoie l'action au jeu
            await websocket.send(json.dumps(response))
            print(f"📤 Ordre envoyé : {response['action']}\n")
            
    except websockets.exceptions.ConnectionClosed:
        print("🔴 Le client Hytale s'est déconnecté.")

async def main():
    # On lance le serveur sur le port 8765 de ton PC local
    async with websockets.serve(hytale_connection, "localhost", 8765):
        print("🧠 Le Cerveau IA est allumé et écoute sur ws://localhost:8765...")
        await asyncio.Future()  # Fait tourner le serveur à l'infini

if __name__ == "__main__":
    asyncio.run(main())