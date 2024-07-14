package com.example.final_project;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/game/{roomId}")
        public class GameServer {
            private static final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();

            @OnOpen
            public void onOpen(Session session, @PathParam("roomId") String roomId) {

                if (rooms.containsKey(roomId)){

                    GameRoom gameRoom = rooms.get(roomId);
                    gameRoom.addSession(session);

                }
                else{
                    GameRoom gameRoom = new GameRoom(roomId);
                    rooms.put(roomId, gameRoom);
                    gameRoom.addSession(session);


                }
            }
    @OnMessage
    public void onMessage(Session session, String message, @PathParam("roomId") String roomId) throws IOException {
        GameRoom gameRoom = rooms.get(roomId);
        System.out.println(roomId);
        if (gameRoom != null) {
            JSONObject jsonMessage = new JSONObject(message);
            int x = jsonMessage.getInt("x");
            int y = jsonMessage.getInt("y");
            String player = jsonMessage.getString("player");
            if (gameRoom.makeMove(x, y, player)) {
                this.broadcast(gameRoom.getGameState(), gameRoom.getSessions());
//                if (!(gameRoom.getWinner().isEmpty())){
//                    session.getBasicRemote().sendText(player + " wins!");
//                    return;
//                }
            } else {
                session.getBasicRemote().sendText(new JSONObject().put("error", "Invalid move" + " " + player).toString());
            }
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("roomId") String roomId) {
        GameRoom gameRoom = rooms.get(roomId);
        if (gameRoom != null) {
            gameRoom.removeSession(session);
            if (gameRoom.getSessions().isEmpty()) {
                // Clean up the room if it's empty
                rooms.remove(roomId);
            }
        }
    }

    private void broadcast(JSONObject message, Set<Session> sessions) {
        sessions.forEach(session -> {
            try {
                session.getBasicRemote().sendText(message.toString());
            } catch (IOException e) {
               System.out.println("Error!");
            }
        });
    }
    @jakarta.ws.rs.ApplicationPath("/api")
    public static class GameApplication extends Application {

        @jakarta.ws.rs.Path("/game")
        public static class GameRest {
            @GET
            @Path("/activeRooms")
            @Produces(MediaType.APPLICATION_JSON)
            public Response getActiveRooms() {
                return Response.ok(new ArrayList<>(rooms.keySet())).build();
            }
        }
    }
    }



