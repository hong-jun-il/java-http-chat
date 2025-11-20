// PrivateChatFrame.java (완전한 최종본)

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PrivateChatFrame extends JFrame {

    // --- UI 컴포넌트 필드 ---
    private JPanel chatPanel;
    private JPanel chatHeaderPanel;
    private JPanel chatDisplayContainer;
    private JScrollPane chatScrollPane;
    private PlaceholderTextArea messageArea;
    private JButton sendButton;

    // --- 데이터 및 컨트롤러 참조 필드 ---
    private Main mainApp;
    private String partnerNickname; // 대화 상대의 닉네임

    /**
     * 1:1 채팅창을 생성합니다.
     * @param mainApp 창을 관리하는 Main 컨트롤러
     * @param partnerNickname 대화 상대방의 닉네임
     */
    public PrivateChatFrame(Main mainApp, String partnerNickname) {
        this.mainApp = mainApp;
        this.partnerNickname = partnerNickname;

        setTitle(partnerNickname + "님과의 대화");
        setSize(500, 700);
        setLocationByPlatform(true); // 창이 겹치지 않게 OS가 위치를 정해줌
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 이 창만 닫히도록 설정
        setLayout(new BorderLayout());

        createChatPanel();
        add(chatPanel, BorderLayout.CENTER);

        // 창이 닫힐 때 Main 클래스의 관리 목록에서 자신을 제거하도록 이벤트를 추가합니다.
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                mainApp.closePrivateChat(partnerNickname);
            }
        });
    }

    /**
     * Main 컨트롤러로부터 받은 ChatMessage 객체를 화면에 그려줍니다.
     * @param chatMessage 화면에 표시할 채팅 메시지 객체
     */
    public void addChatMessage(ChatMessage chatMessage) {
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        ChatBubblePanel bubble = new ChatBubblePanel(chatMessage.getMessage(), chatMessage.isMine());
        JLabel timestampLabel = new JLabel(chatMessage.getTimestamp());
        timestampLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 9));
        timestampLabel.setForeground(Color.GRAY);
        timestampLabel.setBorder(new EmptyBorder(0, 5, 5, 5));

        if (chatMessage.isMine()) {
            // 내가 보낸 메시지 (오른쪽 정렬, 노란색 말풍선)
            JPanel myMessagePanel = new JPanel();
            myMessagePanel.setOpaque(false);
            myMessagePanel.setLayout(new BoxLayout(myMessagePanel, BoxLayout.Y_AXIS));
            bubble.setAlignmentX(Component.RIGHT_ALIGNMENT);
            timestampLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
            myMessagePanel.add(bubble);
            myMessagePanel.add(timestampLabel);
            JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            flowPanel.setOpaque(false);
            flowPanel.add(myMessagePanel);
            wrapperPanel.add(flowPanel, BorderLayout.CENTER);
            wrapperPanel.setBorder(new EmptyBorder(5, 50, 5, 10));
        } else {
            // 상대방이 보낸 메시지 (왼쪽 정렬, 흰색 말풍선, 이름 표시)
            JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            container.setOpaque(false);
            container.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel senderLabel = new JLabel(chatMessage.getSender());
            senderLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            senderLabel.setBorder(new EmptyBorder(0, 5, 3, 0));
            JPanel bubbleAndTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            bubbleAndTimePanel.setOpaque(false);
            bubbleAndTimePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubbleAndTimePanel.add(bubble);
            bubbleAndTimePanel.add(timestampLabel);
            container.add(senderLabel); // 말풍선 위에 보낸 사람 이름 추가
            container.add(bubbleAndTimePanel);
            wrapperPanel.add(container, BorderLayout.WEST);
            wrapperPanel.setBorder(new EmptyBorder(5, 10, 5, 50));
        }

        chatDisplayContainer.add(wrapperPanel);
        chatDisplayContainer.revalidate();
        chatDisplayContainer.repaint();

        // 새 메시지가 추가되면 스크롤을 맨 아래로 이동
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    /**
     * 채팅창의 전체 UI를 구성하고 생성합니다.
     */
    private void createChatPanel() {
        chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(Color.WHITE);

        // --- 1. 헤더 ---
        chatHeaderPanel = new JPanel(new BorderLayout());
        chatHeaderPanel.setBackground(new Color(245, 245, 245));
        chatHeaderPanel.setPreferredSize(new Dimension(0, 70));
        chatHeaderPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        JLabel chatPartnerName = new JLabel(partnerNickname);
        chatPartnerName.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        chatPartnerName.setBorder(new EmptyBorder(0, 20, 0, 0));
        chatHeaderPanel.add(chatPartnerName, BorderLayout.CENTER);
        chatPanel.add(chatHeaderPanel, BorderLayout.NORTH);

        // --- 2. 채팅 내용 표시부 ---
        chatDisplayContainer = new JPanel();
        chatDisplayContainer.setLayout(new BoxLayout(chatDisplayContainer, BoxLayout.Y_AXIS));
        chatDisplayContainer.setBackground(new Color(172, 184, 196));
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(chatDisplayContainer, BorderLayout.NORTH);
        chatScrollPane = new JScrollPane(wrapperPanel);
        chatScrollPane.setBorder(null);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        // --- 3. 메시지 입력부 ---
        JPanel bottomOuterPanel = new JPanel(new BorderLayout());
        bottomOuterPanel.setBackground(Color.WHITE);
        bottomOuterPanel.setBorder(new EmptyBorder(5, 10, 10, 10));
        RoundedPanel bottomInnerPanel = new RoundedPanel(new BorderLayout(), 15);
        bottomInnerPanel.setBackground(Color.WHITE);
        bottomInnerPanel.setBorder(new LineBorder(new Color(220, 220, 220)));
        messageArea = new PlaceholderTextArea("메시지 입력");
        messageArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        messageArea.setBorder(new EmptyBorder(8, 10, 8, 10));

        // Enter 키 전송 기능 추가
        messageArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (e.isShiftDown()) {
                        messageArea.append("\n"); // Shift + Enter = 줄바꿈
                    } else {
                        e.consume(); // Enter 키의 기본 동작(줄바꿈)을 막음
                        sendMessage(); // 메시지 전송
                    }
                }
            }
        });

        JScrollPane messageScrollPane = new JScrollPane(messageArea);
        messageScrollPane.setBorder(null);
        messageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel controlPanel = createControlPanel();

        bottomInnerPanel.add(messageScrollPane, BorderLayout.CENTER);
        bottomInnerPanel.add(controlPanel, BorderLayout.SOUTH);
        bottomOuterPanel.add(bottomInnerPanel, BorderLayout.CENTER);
        chatPanel.add(bottomOuterPanel, BorderLayout.SOUTH);
    }

    /**
     * 메시지 입력창 하단의 컨트롤 패널(전송 버튼 등)을 생성합니다.
     */
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(new EmptyBorder(3, 4, 3, 4));
        controlPanel.add(Box.createHorizontalGlue()); // 전송 버튼을 오른쪽으로 밀기 위한 스프링
        sendButton = new JButton("전송");
        sendButton.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        sendButton.setPreferredSize(new Dimension(40, 25));
        sendButton.setMaximumSize(new Dimension(40, 25));
        sendButton.setFocusPainted(false);
        sendButton.setBorder(null);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateSendButtonState(false);
        controlPanel.add(sendButton);

        messageArea.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void update() {
                updateSendButtonState(!messageArea.getRealText().trim().isEmpty());
            }
        });

        sendButton.addActionListener(e -> sendMessage());
        return controlPanel;
    }

    /**
     * 입력된 메시지를 서버로 전송합니다.
     */
    private void sendMessage() {
        String message = messageArea.getRealText();
        if (!message.trim().isEmpty()) {
            mainApp.getClient().sendMessage("PRIVATE_MSG::" + partnerNickname + "::" + message);
            messageArea.setText("");
            messageArea.requestFocusInWindow();
        }
    }

    /**
     * 메시지 입력 내용에 따라 전송 버튼의 활성화/비활성화 상태를 업데이트합니다.
     */
    private void updateSendButtonState(boolean active) {
        if (active) {
            sendButton.setBackground(new Color(255, 235, 51)); // 노란색
            sendButton.setEnabled(true);
        } else {
            sendButton.setBackground(new Color(240, 240, 240)); // 회색
            sendButton.setEnabled(false);
        }
    }
}
