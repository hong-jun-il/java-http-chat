// MainFrame.java (완전한 최종본)

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MainFrame extends JFrame {

    private JPanel userListPanel, userListContentPanel;
    private JPanel chatPanel, chatHeaderPanel, chatDisplayContainer;
    private JScrollPane chatScrollPane;
    private PlaceholderTextArea messageArea;
    private JButton sendButton;
    private Main mainApp;

    public MainFrame(Main mainApp) {
        this.mainApp = mainApp;
        setTitle("겜톡 로비 - " + mainApp.getMyNickname());
        setSize(620, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        createUserListPanel();
        add(userListPanel, BorderLayout.WEST);
        createChatPanel();
        add(chatPanel, BorderLayout.CENTER);
    }

    public void processServerMessage(String message) {
        String[] parts = message.split("::", 2);
        String command = parts[0];
        String data = parts.length > 1 ? parts[1] : "";
        switch (command) {
            case "USER_LIST":
                clearUserList();
                String[] users = data.split(",");
                for (String user : users) {
                    addUser(user);
                }
                break;
            case "NEW_USER":
                if (!data.equals(mainApp.getMyNickname())) {
                    addUser(data);
                }
                break;
            case "EXIT_USER":
                removeUser(data);
                break;
            case "PUBLIC_MSG":
                String[] msgParts = data.split("::", 2);
                String sender = msgParts[0];
                String msg = msgParts[1];
                addChatMessage(new ChatMessage(sender, msg, sender.equals(mainApp.getMyNickname())));
                break;
        }
    }

    private void createUserListPanel() {
        userListPanel = new JPanel(new BorderLayout());
        userListPanel.setPreferredSize(new Dimension(220, 0));
        userListPanel.setBackground(new Color(230, 230, 230));
        userListPanel.setBorder(new LineBorder(new Color(210, 210, 210)));
        JPanel userListHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userListHeaderPanel.setBackground(new Color(240, 240, 240));
        userListHeaderPanel.setPreferredSize(new Dimension(0, 70));
        userListHeaderPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        userListHeaderPanel.add(new JLabel("My Profile: " + mainApp.getMyNickname()));
        userListPanel.add(userListHeaderPanel, BorderLayout.NORTH);
        userListContentPanel = new JPanel();
        userListContentPanel.setLayout(new BoxLayout(userListContentPanel, BoxLayout.Y_AXIS));
        userListContentPanel.setBackground(Color.WHITE);
        JScrollPane userListScrollPane = new JScrollPane(userListContentPanel);
        userListScrollPane.setBorder(null);
        userListPanel.add(userListScrollPane, BorderLayout.CENTER);
    }

    private void createChatPanel() {
        chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(Color.WHITE);
        chatHeaderPanel = new JPanel(new BorderLayout());
        chatHeaderPanel.setBackground(new Color(245, 245, 245));
        chatHeaderPanel.setPreferredSize(new Dimension(0, 70));
        chatHeaderPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        JPanel leftHeaderPanel = new JPanel(new BorderLayout());
        leftHeaderPanel.setOpaque(false);
        leftHeaderPanel.setBorder(new EmptyBorder(0, 20, 0, 0));
        JPanel nameAndCountPanel = new JPanel();
        nameAndCountPanel.setOpaque(false);
        nameAndCountPanel.setLayout(new BoxLayout(nameAndCountPanel, BoxLayout.Y_AXIS));
        nameAndCountPanel.add(Box.createVerticalGlue());
        JLabel chatPartnerName = new JLabel("전체 채팅");
        chatPartnerName.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        JLabel memberCount = new JLabel("1");
        memberCount.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        memberCount.setForeground(Color.GRAY);
        nameAndCountPanel.add(chatPartnerName);
        nameAndCountPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        nameAndCountPanel.add(memberCount);
        nameAndCountPanel.add(Box.createVerticalGlue());
        leftHeaderPanel.add(nameAndCountPanel, BorderLayout.CENTER);
        chatHeaderPanel.add(leftHeaderPanel, BorderLayout.CENTER);
        chatPanel.add(chatHeaderPanel, BorderLayout.NORTH);

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
        messageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        JPanel controlPanel = createControlPanel();
        bottomInnerPanel.add(messageScrollPane, BorderLayout.CENTER);
        bottomInnerPanel.add(controlPanel, BorderLayout.SOUTH);
        bottomOuterPanel.add(bottomInnerPanel, BorderLayout.CENTER);
        chatPanel.add(bottomOuterPanel, BorderLayout.SOUTH);
    }

    private void sendMessage() {
        String message = messageArea.getRealText();
        if (!message.trim().isEmpty()) {
            mainApp.getClient().sendMessage("PUBLIC_MSG::" + message);
            messageArea.setText("");
            messageArea.requestFocusInWindow();
        }
    }

    public void addUser(String nickname) {
        CustomUserButton userButton = new CustomUserButton(nickname, mainApp);
        userListContentPanel.add(userButton);
        updateUserCount();
        userListContentPanel.revalidate();
        userListContentPanel.repaint();
    }

    public void removeUser(String nickname) {
        for (Component comp : userListContentPanel.getComponents()) {
            if (comp instanceof CustomUserButton && ((CustomUserButton) comp).getNickname().equals(nickname)) {
                userListContentPanel.remove(comp);
                break;
            }
        }
        updateUserCount();
        userListContentPanel.revalidate();
        userListContentPanel.repaint();
    }

    public void clearUserList() {
        userListContentPanel.removeAll();
        updateUserCount();
        userListContentPanel.revalidate();
        userListContentPanel.repaint();
    }

    private void updateUserCount() {
        // (미구현)
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
            myMessagePanel.add(timestampLabel);
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
            bubbleAndTimePanel.add(timestampLabel);
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

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(new EmptyBorder(3, 4, 3, 4));
        String[] iconTexts = {"😊", "📅", "💬", "📄", "🖼️", "🇹", "🎮"};
        for (String text : iconTexts) {
            JButton iconButton = new JButton(text);
            iconButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
            iconButton.setMargin(new Insets(0, 0, 0, 0));
            iconButton.setOpaque(false); iconButton.setContentAreaFilled(false); iconButton.setBorderPainted(false); iconButton.setFocusPainted(false);
            iconButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            controlPanel.add(iconButton);
            controlPanel.add(Box.createRigidArea(new Dimension(3, 0)));
        }
        controlPanel.add(Box.createHorizontalGlue());
        JSlider transparencySlider = new JSlider(0, 100);
        transparencySlider.setOpaque(false);
        transparencySlider.setPreferredSize(new Dimension(30, 20));
        transparencySlider.setMaximumSize(new Dimension(30, 20));
        controlPanel.add(transparencySlider);
        controlPanel.add(Box.createRigidArea(new Dimension(5, 0)));
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
