// ChatBubblePanel.java (최종 완성본)

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChatBubblePanel extends JPanel {
    private JLabel messageLabel;
    private boolean isMine;

    public ChatBubblePanel(String message, boolean isMine) {
        this.isMine = isMine;
        setLayout(new BorderLayout());
        setOpaque(false); // 패널 자체는 투명하게

        messageLabel = new JLabel("<html><body style='width: 150px;'>" + message.replaceAll("\n", "<br>") + "</body></html>");
        messageLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        messageLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 글자색은 항상 검은색
        messageLabel.setForeground(Color.BLACK);

        add(messageLabel, BorderLayout.CENTER);
    }

    // ▼▼▼ [핵심 수정] 말풍선을 그리는 이 메소드를 수정합니다. ▼▼▼
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // isMine 값에 따라 배경색을 동적으로 결정
        if (isMine) {
            g2.setColor(new Color(255, 235, 51)); // 카카오톡 노란색
        } else {
            g2.setColor(Color.WHITE); // 상대방은 흰색
        }

        int width = getWidth();
        int height = getHeight();
        g2.fillRoundRect(0, 0, width, height, 20, 20);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width = messageLabel.getPreferredSize().width + 20;
        size.height = messageLabel.getPreferredSize().height + 20;
        return size;
    }
}
