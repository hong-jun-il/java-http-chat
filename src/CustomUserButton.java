import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomUserButton extends JButton {

    private String nickname;
    private Color defaultColor = Color.WHITE;
    private Color hoverColor = new Color(245, 245, 245);

    public CustomUserButton(String nickname, Main mainApp) {
        super(nickname);
        this.nickname = nickname;

        setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        // ... (나머지 스타일 설정은 이전과 동일)

        addActionListener(e -> {
            // [핵심] Main 클래스의 1:1 채팅 시작 메소드를 호출
            mainApp.startPrivateChat(this.nickname);
        });
    }

    public String getNickname() {
        return nickname;
    }
}