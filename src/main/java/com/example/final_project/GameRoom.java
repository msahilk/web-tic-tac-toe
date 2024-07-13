package final_project;

import jakarta.websocket.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GameRoom {
    private final String roomId;
    private final String[][] board = new String[3][3];
    private String currentPlayer = "X"; // Player X always starts
    private final Set<Session> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private String winner;
    private boolean gameActive = true;

    public GameRoom(String roomId) {
        this.roomId = roomId;
        for (String[] row : board) {
            java.util.Arrays.fill(row, "");
        }
    }

    public synchronized boolean makeMove(int x, int y, String player) {
        if (!gameActive || !board[x][y].isEmpty() || !currentPlayer.equals(player)) {
            return false;
        }

        board[x][y] = player;
        boolean win = checkWinner();
        boolean draw = isBoardFull();

        if (win) {
            gameActive = false;
            winner = currentPlayer;
        } else if (draw) {
            gameActive = false;
            winner = "D"; // D for Draw
        } else {
            currentPlayer = currentPlayer.equals("X") ? "O" : "X";
        }

        return true;
    }

    private boolean checkWinner() {
        for (int i = 0; i < 3; i++) {
            if (!board[i][0].isEmpty() && board[i][0].equals(board[i][1]) && board[i][0].equals(board[i][2])) {
                return true;
            }
            if (!board[0][i].isEmpty() && board[0][i].equals(board[1][i]) && board[0][i].equals(board[2][i])) {
                return true;
            }
        }
        if (!board[0][0].isEmpty() && board[0][0].equals(board[1][1]) && board[0][0].equals(board[2][2])) {
            return true;
        }
        return !board[0][2].isEmpty() && board[0][2].equals(board[1][1]) && board[0][2].equals(board[2][0]);
    }

    private boolean isBoardFull() {
        for (String[] row : board) {
            for (String cell : row) {
                if (cell.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public String getRoomId() {
        return roomId;
    }

    public String[][] getBoard() {
        return board;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public String getWinner() {
        return winner;
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public void addSession(Session session) {
        sessions.add(session);
    }

    public void removeSession(Session session) {
        sessions.remove(session);
    }

    public Set<Session> getSessions() {
        return sessions;
    }

    public JSONObject getGameState() {
        JSONObject state = new JSONObject();
        JSONArray jsonBoard = new JSONArray();
        for (String[] row : board) {
            JSONArray jsonRow = new JSONArray();
            for (String cell : row) {
                jsonRow.put(cell);
            }
            jsonBoard.put(jsonRow);
        }
        state.put("board", jsonBoard);
        state.put("currentPlayer", currentPlayer);
        state.put("winner", winner != null ? winner : JSONObject.NULL);
        state.put("gameActive", gameActive);
        return state;
    }
}
