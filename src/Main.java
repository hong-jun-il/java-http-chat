// Main.java (최종본)

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private String myNickname; private LoginFrame loginFrame; private MainFrame mainFrame;
    private Map<String, PrivateChatFrame> privateChatFrames = new HashMap<>();
    private Client client; private Map<String, List<ChatMessage>> chatHistories = new HashMap<>();
    public Main() {
        this.client = new Client(this); this.loginFrame = new LoginFrame(this); loginFrame.setVisible(true);
    }
    public void attemptLogin(String nickname) {
        if (client.connect(nickname)) {
            this.myNickname = nickname; loginFrame.dispose(); mainFrame = new MainFrame(this); mainFrame.setVisible(true);
        }
    }
    public void startPrivateChat(String partner) {
        if (myNickname.equals(partner)) return;
        privateChatFrames.computeIfAbsent(partner, p -> {
            PrivateChatFrame newChatFrame = new PrivateChatFrame(this, p);
            List<ChatMessage> history = chatHistories.get(p);
            if (history != null) history.forEach(newChatFrame::addChatMessage);
            newChatFrame.setVisible(true);
            return newChatFrame;
        }).toFront();
    }
    public void closePrivateChat(String partner) { privateChatFrames.remove(partner); }
    public void handleServerMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (mainFrame == null && !message.startsWith("LOGIN_FAIL")) return;
            String[] parts = message.split("::", 2);
            String command = parts[0]; String data = parts.length > 1 ? parts[1] : "";
            switch (command) {
                case "GAME_START_SUCCESS": mainFrame.showGame(); break;
                case "GAME_INFO":
                    mainFrame.getGamePanel().setInfoText(data);
                    mainFrame.getGamePanel().setInputEnabled(data.contains(myNickname + "님의 차례입니다"));
                    break;
                case "GAME_BOARD_UPDATE": mainFrame.getGamePanel().updateHistory(data); break;
                case "GAME_END":
                    mainFrame.getGamePanel().setInfoText("게임 종료! 승자: " + data);
                    mainFrame.getGamePanel().setInputEnabled(false);
                    JOptionPane.showMessageDialog(mainFrame, "게임 종료!\n승자: " + data, "게임 종료", JOptionPane.INFORMATION_MESSAGE);
                    mainFrame.showChat();
                    break;
                case "USER_LIST": case "NEW_USER": case "EXIT_USER": mainFrame.processServerMessage(message); break;
                case "PUBLIC_MSG":
                    String[] msgP = data.split("::", 2);
                    ChatMessage pubMsg = new ChatMessage(msgP[0], msgP[1], msgP[0].equals(myNickname));
                    chatHistories.computeIfAbsent("전체", k -> new ArrayList<>()).add(pubMsg);
                    mainFrame.addChatMessage(pubMsg);
                    break;
                case "PRIVATE_MSG":
                    String[] pmP = data.split("::", 3);
                    String sender = pmP[0], receiver = pmP[1], pm = pmP[2];
                    if (sender.equals(myNickname) || receiver.equals(myNickname)) {
                        String partner = sender.equals(myNickname) ? receiver : sender;
                        ChatMessage prvMsg = new ChatMessage(sender, pm, sender.equals(myNickname));
                        chatHistories.computeIfAbsent(partner, k -> new ArrayList<>()).add(prvMsg);
                        PrivateChatFrame targetFrame = privateChatFrames.get(partner);
                        if (targetFrame != null) targetFrame.addChatMessage(prvMsg);
                        else if (!sender.equals(myNickname)) { mainFrame.showNotificationFor(partner); startPrivateChat(partner); }
                    }
                    break;
                case "LOGIN_FAIL": loginFailed(data); break;
                case "SERVER_DOWN": JOptionPane.showMessageDialog(null, "서버 연결 끊김.", "연결 오류", JOptionPane.ERROR_MESSAGE); System.exit(0); break;
            }
        });
    }
    public void loginFailed(String reason) { JOptionPane.showMessageDialog(loginFrame, reason, "로그인 실패", JOptionPane.ERROR_MESSAGE); }
    public Client getClient() { return client; }
    public String getMyNickname() { return myNickname; }
    public static void main(String[] args) { try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {} SwingUtilities.invokeLater(Main::new); }
}
