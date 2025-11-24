// PrivateChatFrame.java (진짜 진짜 최종 완성본)

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PrivateChatFrame extends JFrame {

    private JPanel chatPanel, chatHeaderPanel, chatDisplayContainer;
    private JScrollPane chatScrollPane;
    private PlaceholderTextArea messageArea;
    private JButton sendButton;

    private Main mainApp;
    private String partnerNickname;

    public PrivateChatFrame(Main mainApp, String partnerNickname) {
        this.mainApp = mainApp;
        this.partnerNickname = partnerNickname;

        setTitle(partnerNickname + "님과의 대화");
        setSize(500, 700);
        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        createChatPanel();
        add(chatPanel, BorderLayout.CENTER);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                mainApp.closePrivateChat(partnerNickname);
            }
        });
    }
    public void addChatMessage(ChatMessage chatMessage) {
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        ChatBubblePanel bubble = new ChatBubblePanel(chatMessage.getMessage(), chatMessage.isMine());
        JLabel timestampLabel = new JLabel(chatMessage.getTimestamp());
        timestampLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 9));
        timestampLabel.setForeground(Color.GRAY);
        timestampLabel.setBorder(new EmptyBorder(0, 5, 5, 5));

        if (chatMessage.isMine()) {
            JPanel myMessagePanel = new JPanel();
            myMessagePanel.setOpaque(false);
            myMessagePanel.setLayout(new BoxLayout(myMessagePanel, BoxLayout.Y_AXIS));
            bubble.setAlignmentX(Component.RIGHT_ALIGNMENT);
            timestampLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
            myMessagePanel.add(bubble);
            myMessagePanel.add(timestampLabel); // 누락되었던 코드 추가
            JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            flowPanel.setOpaque(false);
            flowPanel.add(myMessagePanel);
            wrapperPanel.add(flowPanel, BorderLayout.CENTER);
            wrapperPanel.setBorder(new EmptyBorder(5, 50, 5, 10));
        } else {
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
            bubbleAndTimePanel.add(timestampLabel); // 누락되었던 코드 추가
            container.add(senderLabel);
            container.add(bubbleAndTimePanel);
            wrapperPanel.add(container, BorderLayout.WEST);
            wrapperPanel.setBorder(new EmptyBorder(5, 10, 5, 50));
        }




        chatDisplayContainer.add(wrapperPanel);
        chatDisplayContainer.revalidate();
        chatDisplayContainer.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void createChatPanel() {
        chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(Color.WHITE);

        // 1. 헤더
        chatHeaderPanel = new JPanel(new BorderLayout());
        chatHeaderPanel.setBackground(new Color(245, 245, 245));
        chatHeaderPanel.setPreferredSize(new Dimension(0, 70));
        chatHeaderPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        JLabel chatPartnerName = new JLabel(partnerNickname);
        chatPartnerName.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        chatPartnerName.setBorder(new EmptyBorder(0, 20, 0, 0));
        chatHeaderPanel.add(chatPartnerName, BorderLayout.CENTER);

        // 2. 채팅 내용
        chatDisplayContainer = new JPanel();
        chatDisplayContainer.setLayout(new BoxLayout(chatDisplayContainer, BoxLayout.Y_AXIS));
        chatDisplayContainer.setBackground(new Color(172, 184, 196));
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(chatDisplayContainer, BorderLayout.NORTH);
        chatScrollPane = new JScrollPane(wrapperPanel);
        chatScrollPane.setBorder(null);
        chatScrollPane.getViewport().setBackground(new Color(172, 184, 196));
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 3. 메시지 입력부
        JPanel bottomOuterPanel = new JPanel(new BorderLayout());
        bottomOuterPanel.setBackground(Color.WHITE);
        bottomOuterPanel.setBorder(new EmptyBorder(5,10,10,10));
        RoundedPanel bottomInnerPanel = new RoundedPanel(new BorderLayout(), 15);
        bottomInnerPanel.setBackground(Color.WHITE);
        bottomInnerPanel.setBorder(new LineBorder(new Color(220,220,220)));
        messageArea = new PlaceholderTextArea("메시지 입력");
        messageArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        messageArea.setBorder(new EmptyBorder(8,10,8,10));
        messageArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (e.isShiftDown()) {
                        messageArea.append("\n");
                    } else {
                        e.consume();
                        sendMessage();
                    }
                }
            }
        });
        JScrollPane messageScrollPane = new JScrollPane(messageArea);
        messageScrollPane.setBorder(null);
        JPanel controlPanel = createControlPanel();
        bottomInnerPanel.add(messageScrollPane, BorderLayout.CENTER);
        bottomInnerPanel.add(controlPanel, BorderLayout.SOUTH);
        bottomOuterPanel.add(bottomInnerPanel, BorderLayout.CENTER);

        // ▼▼▼ [핵심 수정] 조립 코드를 다시 추가 ▼▼▼
        chatPanel.add(chatHeaderPanel, BorderLayout.NORTH);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(bottomOuterPanel, BorderLayout.SOUTH);
    }

    private void sendMessage() {
        String message = messageArea.getRealText();
        if (!message.trim().isEmpty()) {
            mainApp.getClient().sendMessage("PRIVATE_MSG::" + partnerNickname + "::" + message);
            messageArea.setText("");
            messageArea.requestFocusInWindow();
        }
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(new EmptyBorder(3, 4, 3, 4));
        controlPanel.add(Box.createHorizontalGlue());
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

    private void updateSendButtonState(boolean active) {
        if (active) {
            sendButton.setBackground(new Color(255, 235, 51));
            sendButton.setEnabled(true);
        } else {
            sendButton.setBackground(new Color(240, 240, 240));
            sendButton.setEnabled(false);
        }
    }
}
