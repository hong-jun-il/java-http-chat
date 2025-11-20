// GameServer.java (진짜 최종 완성본)

import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private static final int PORT = 9999;
    private static Map<String, ClientHandler> clients = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        System.out.println("[서버] 시작. 포트: " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients.values()) {
                client.sendMessage(message);
            }
        }
    }

    public static void sendToOne(String nickname, String message) {
        synchronized(clients) {
            ClientHandler target = clients.get(nickname);
            if (target != null) {
                target.sendMessage(message);
            }
        }
    }

    // ▼▼▼ [핵심 수정] 새로운 프로토콜에 맞춰 메시지를 생성하여 전송 ▼▼▼
    public static void sendPrivateMessage(String senderNickname, String targetNickname, String message) {
        String fullMessage = "PRIVATE_MSG::" + senderNickname + "::" + targetNickname + "::" + message;
        sendToOne(targetNickname, fullMessage);
        sendToOne(senderNickname, fullMessage);
    }

    public static boolean isNicknameTaken(String nickname) { return clients.containsKey(nickname); }
    public static void addClient(String nickname, ClientHandler clientHandler) { clients.put(nickname, clientHandler); }
    public static void removeClient(String nickname) {
        clients.remove(nickname);
        System.out.println("[서버] " + nickname + " 님이 나갔습니다. 현재 접속자: " + clients.size());
        broadcast("EXIT_USER::" + nickname);
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

    public ClientHandler(Socket socket) { this.socket = socket; }
    public String getNickname() { return nickname; }

    @Override
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
                System.out.println("[서버] " + nickname + " 님이 접속했습니다. 현재 접속자: " + GameServer.getUserList());

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("[서버 수신] " + nickname + ": " + message);
                    if (message.startsWith("PUBLIC_MSG::")) {
                        String msg = message.substring(12);
                        GameServer.broadcast("PUBLIC_MSG::" + nickname + "::" + msg);
                    } else if (message.startsWith("PRIVATE_MSG::")) {
                        String[] parts = message.split("::", 3);
                        String target = parts[1];
                        String pm = parts[2];
                        GameServer.sendPrivateMessage(nickname, target, pm);
                    }
                }
            }
        } catch (IOException e) {
        } finally {
            if (nickname != null) { GameServer.removeClient(nickname); }
            try { socket.close(); } catch (IOException e) {}
        }
    }
    public void sendMessage(String message) { out.println(message); }
}
