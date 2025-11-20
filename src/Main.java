// Main.java (진짜 최종 완성본)

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    private String myNickname;
    private LoginFrame loginFrame;
    private MainFrame mainFrame;
    private Map<String, PrivateChatFrame> privateChatFrames = new HashMap<>();
    private Client client;
    private Map<String, List<ChatMessage>> chatHistories = new HashMap<>();

    public Main() {
        this.client = new Client(this);
        this.loginFrame = new LoginFrame(this);
        this.loginFrame.setVisible(true);
    }

    public void attemptLogin(String nickname) {
        if (client.connect(nickname)) {
            this.myNickname = nickname;
            loginFrame.dispose();
            mainFrame = new MainFrame(this);
            mainFrame.setTitle("겜톡 로비 - " + nickname);
            mainFrame.setVisible(true);
        }
    }

    public void startPrivateChat(String partnerNickname) {
        if (myNickname.equals(partnerNickname)) return;

        PrivateChatFrame frame = privateChatFrames.get(partnerNickname);
        if (frame != null) {
            frame.toFront();
        } else {
            PrivateChatFrame newChatFrame = new PrivateChatFrame(this, partnerNickname);
            privateChatFrames.put(partnerNickname, newChatFrame);

            List<ChatMessage> history = chatHistories.get(partnerNickname);
            if (history != null) {
                for (ChatMessage msg : history) {
                    newChatFrame.addChatMessage(msg);
                }
            }
            newChatFrame.setVisible(true);
        }
    }

    public void closePrivateChat(String partnerNickname) {
        privateChatFrames.remove(partnerNickname);
        System.out.println(partnerNickname + "님과의 채팅창을 닫았습니다.");
    }

    public void handleServerMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String[] parts = message.split("::", 2);
            String command = parts[0];
            String data = parts.length > 1 ? parts[1] : "";

            switch (command) {
                case "USER_LIST":
                case "NEW_USER":
                case "EXIT_USER":
                    if (mainFrame != null) mainFrame.processServerMessage(message);
                    break;
                case "PUBLIC_MSG":
                    String[] msgParts = data.split("::", 2);
                    String publicSender = msgParts[0];
                    String publicMsg = msgParts[1];
                    ChatMessage publicChatMessage = new ChatMessage(publicSender, publicMsg, publicSender.equals(myNickname));
                    chatHistories.computeIfAbsent("전체", k -> new ArrayList<>()).add(publicChatMessage);
                    if (mainFrame != null) mainFrame.addChatMessage(publicChatMessage);
                    break;
                case "PRIVATE_MSG":
                    // ▼▼▼ [핵심 버그 수정] 1:1 메시지 처리 로직 완벽 수정 ▼▼▼
                    String[] pmParts = data.split("::", 3);
                    String sender = pmParts[0];
                    String receiver = pmParts[1];
                    String pm = pmParts[2];

                    // 이 대화가 나와 관련된 대화인지 확인 (내가 보냈거나, 내가 받았거나)
                    if (sender.equals(myNickname) || receiver.equals(myNickname)) {
                        // 대화 상대방 닉네임 찾기 (내가 아니면 무조건 상대방)
                        String chatPartner = sender.equals(myNickname) ? receiver : sender;

                        // isMine 플래그를 정확하게 설정
                        boolean isMine = sender.equals(myNickname);

                        ChatMessage privateChatMessage = new ChatMessage(sender, pm, isMine);

                        // 대화 기록 저장
                        chatHistories.computeIfAbsent(chatPartner, k -> new ArrayList<>()).add(privateChatMessage);

                        // 해당 채팅창이 열려있는지 확인
                        PrivateChatFrame targetFrame = privateChatFrames.get(chatPartner);

                        if (targetFrame != null) {
                            // 열려있으면 메시지 표시
                            targetFrame.addChatMessage(privateChatMessage);
                        } else {
                            // 안 열려있으면, 내가 보낸 메시지가 아닐 때만 새로 열어줌
                            if (!isMine) {
                                startPrivateChat(chatPartner);
                            }
                        }
                    }
                    break;
                case "LOGIN_FAIL":
                    loginFailed(data);
                    break;
                case "SERVER_DOWN":
                    JOptionPane.showMessageDialog(null, "서버와의 연결이 끊어졌습니다.", "연결 오류", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                    break;
            }
        });
    }

    public void loginFailed(String reason) { JOptionPane.showMessageDialog(loginFrame, reason, "로그인 실패", JOptionPane.ERROR_MESSAGE); }
    public Client getClient() { return client; }
    public String getMyNickname() { return myNickname; }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { e.printStackTrace(); }
        SwingUtilities.invokeLater(Main::new);
    }
}
