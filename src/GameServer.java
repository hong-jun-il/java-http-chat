// GameServer.java (최종본)

import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private static final int PORT = 9999;
    private static final Map<String, ClientHandler> clients = Collections.synchronizedMap(new HashMap<>());
    private static NumberBaseballGame currentGame;
    private static List<String> gameParticipants = new ArrayList<>();
    private static int currentPlayerIndex = 0;
    private static StringBuilder gameHistory = new StringBuilder();

    public static void main(String[] args) {
        System.out.println("[서버] 시작. 포트: " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void createGame(String gameType, String creator) {
        if (currentGame != null) {
            sendToOne(creator, "GAME_INFO::이미 진행 중인 게임이 있습니다.");
            return;
        }
        if (gameType.equals("NUMBER_BASEBALL")) {
            currentGame = new NumberBaseballGame();
            gameParticipants.clear();
            gameHistory.setLength(0);
            gameParticipants.addAll(clients.keySet());
            if (gameParticipants.isEmpty()) {
                sendToOne(creator, "GAME_INFO::게임에 참여할 유저가 부족합니다.");
                currentGame = null;
                return;
            }
            Collections.shuffle(gameParticipants);
            currentPlayerIndex = 0;
            gameHistory.append("--- 숫자 야구 게임 시작 ---\n");
            broadcast("GAME_START_SUCCESS::NUMBER_BASEBALL");
            broadcast("GAME_BOARD_UPDATE::" + gameHistory.toString());
            nextTurn();
        }
    }

    public static synchronized void handleGameAction(String player, String action) {
        if (currentGame == null || gameParticipants.isEmpty()) return;
        if (!gameParticipants.get(currentPlayerIndex).equals(player)) {
            sendToOne(player, "GAME_INFO::당신의 턴이 아닙니다.");
            return;
        }
        String result = currentGame.checkGuess(action);
        gameHistory.append(player).append(" -> ").append(action).append(" : ").append(result).append("\n");
        broadcast("GAME_BOARD_UPDATE::" + gameHistory.toString());
        if (result.contains("4S")) {
            broadcast("GAME_INFO::" + player + "님이 정답(" + currentGame.getAnswerString() + ")을 맞췄습니다!");
            broadcast("GAME_END::" + player);
            currentGame = null;
        } else {
            nextTurn();
        }
    }

    private static void nextTurn() {
        if (gameParticipants.isEmpty()) {
            broadcast("GAME_END::모두 나감");
            currentGame = null;
            return;
        }
        currentPlayerIndex = (currentPlayerIndex + 1) % gameParticipants.size();
        String nextPlayer = gameParticipants.get(currentPlayerIndex);
        broadcast("GAME_INFO::" + nextPlayer + "님의 차례입니다. (서로 다른 4자리 숫자)");
    }

    public static synchronized void playerLeft(String nickname) {
        if (clients.containsKey(nickname)) {
            clients.remove(nickname);
            broadcast("EXIT_USER::" + nickname);
            System.out.println("[서버] " + nickname + " 님이 나갔습니다.");
        }
        if (currentGame != null && gameParticipants.contains(nickname)) {
            boolean wasCurrentPlayer = gameParticipants.get(currentPlayerIndex).equals(nickname);
            gameParticipants.remove(nickname);
            broadcast("GAME_INFO::" + nickname + "님이 게임을 떠났습니다.");
            if (gameParticipants.isEmpty()) {
                broadcast("GAME_END::모두 나감");
                currentGame = null;
            } else if (wasCurrentPlayer) {
                currentPlayerIndex %= gameParticipants.size(); // 턴 재조정
                nextTurn();
            }
        }
    }

    public static void broadcast(String message) {
        clients.values().forEach(c -> c.sendMessage(message));
    }

    public static void sendToOne(String nickname, String message) {
        ClientHandler target = clients.get(nickname);
        if (target != null) target.sendMessage(message);
    }

    public static void sendPrivateMessage(String sender, String target, String msg) {
        String fullMessage = "PRIVATE_MSG::" + sender + "::" + target + "::" + msg;
        sendToOne(target, fullMessage);
        sendToOne(sender, fullMessage);
    }

    public static boolean isNicknameTaken(String nickname) {
        return clients.containsKey(nickname);
    }

    public static void addClient(String nickname, ClientHandler handler) {
        clients.put(nickname, handler);
    }

    public static String getUserList() {
        synchronized(clients) {
            if (clients.isEmpty()) return "";
            return String.join(",", clients.keySet());
        }
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String request = in.readLine();
            if (request != null && request.startsWith("LOGIN::")) {
                String potentialNickname = request.substring(7);
                if (GameServer.isNicknameTaken(potentialNickname)) {
                    sendMessage("LOGIN_FAIL::이미 사용 중인 닉네임입니다.");
                    return;
                }
                this.nickname = potentialNickname;
                GameServer.addClient(nickname, this);
                sendMessage("LOGIN_SUCCESS::");
                GameServer.sendToOne(nickname, "USER_LIST::" + GameServer.getUserList());
                GameServer.broadcast("NEW_USER::" + nickname);
                System.out.println("[서버] " + nickname + " 님이 접속했습니다.");
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("[서버 수신] " + nickname + ": " + message);
                    if (message.startsWith("PUBLIC_MSG::"))
                        GameServer.broadcast("PUBLIC_MSG::" + nickname + "::" + message.substring(12));
                    else if (message.startsWith("PRIVATE_MSG::")) {
                        String[] p = message.split("::", 3);
                        GameServer.sendPrivateMessage(nickname, p[1], p[2]);
                    } else if (message.startsWith("GAME_CREATE_REQUEST::"))
                        GameServer.createGame(message.split("::")[1], nickname);
                    else if (message.startsWith("GAME_ACTION::"))
                        GameServer.handleGameAction(nickname, message.split("::")[1]);
                }
            }
        } catch (IOException e) {
            // 접속 종료
        } finally {
            if (nickname != null) {
                GameServer.playerLeft(nickname);
            }
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }
    public void sendMessage(String message) {
        out.println(message);
    }
}
