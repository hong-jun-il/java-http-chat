
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomUserButton extends JButton {

    private String nickname;
    private JLabel nameLabel; // 닉네임 표시용 라벨
    private JLabel notificationLabel; // (N) 알림 표시용 라벨
    private Color defaultColor = Color.WHITE;
    private Color hoverColor = new Color(245, 245, 245);

    public CustomUserButton(String nickname, Main mainApp) {
        super(); // JButton의 기본 텍스트는 사용하지 않음
        this.nickname = nickname;

        setLayout(new BorderLayout()); // 레이아웃 변경

        nameLabel = new JLabel(nickname);
        nameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        notificationLabel = new JLabel();
        notificationLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        notificationLabel.setForeground(Color.RED);

        add(nameLabel, BorderLayout.CENTER);
        add(notificationLabel, BorderLayout.EAST);

        // --- 나머지 스타일 및 이벤트 리스너는 이전과 동일 ---
        setHorizontalAlignment(SwingConstants.LEFT);
        setPreferredSize(new Dimension(0, 50));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        setBackground(defaultColor);
        setBorder(new EmptyBorder(10, 15, 10, 15));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { setBackground(hoverColor); }
            @Override
            public void mouseExited(MouseEvent e) { setBackground(defaultColor); }
        });

        addActionListener(e -> {
            mainApp.startPrivateChat(this.nickname);
            // 버튼을 클릭하면 알림을 제거
            setNotification(false);
        });
    }

    public String getNickname() {
        return nickname;
    }

    // 알림 뱃지를 켜고 끄는 메소드
    public void setNotification(boolean hasNewMessage) {
        if (hasNewMessage) {
            notificationLabel.setText(" (N)");
        } else {
            notificationLabel.setText("");
        }
    }
}
