// Client.java (변경 없음)
// (이전 답변의 최종 Client.java 코드를 그대로 사용하세요.)
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Main mainApp;

    public Client(Main mainApp) {
        this.mainApp = mainApp;
    }

    public boolean connect(String nickname) {
        try {
            socket = new Socket("localhost", 9999);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("LOGIN::" + nickname);
            String response = in.readLine();
            if ("LOGIN_SUCCESS::".equals(response)) {
                startListening();
                return true;
            } else {
                String reason = response.substring(12);
                mainApp.loginFailed(reason);
                disconnect();
                return false;
            }
        } catch (UnknownHostException e) {
            mainApp.loginFailed("서버를 찾을 수 없습니다. (localhost:9999)");
            return false;
        } catch (IOException e) {
            mainApp.loginFailed("서버 연결에 실패했습니다.");
            return false;
        }
    }

    private void startListening() {
        new Thread(() -> {
            try {
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    mainApp.handleServerMessage(serverMessage);
                }
            } catch (IOException e) {
            } finally {
                mainApp.handleServerMessage("SERVER_DOWN::");
            }
        }).start();
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {}
    }
}
