import javax.swing.*;
import java.awt.*;

public class GameSelectionDialog extends JDialog {
    private Main mainApp;

    public GameSelectionDialog(Frame owner, Main mainApp) {
        super(owner, "게임 선택", true); // true: 이 창이 떠있을 땐 다른 창 조작 불가
        this.mainApp = mainApp;

        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        // --- 숫자 야구 게임 버튼 ---
        JButton baseballButton = new JButton("숫자 야구");
        baseballButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        baseballButton.setPreferredSize(new Dimension(150, 80));
        baseballButton.addActionListener(e -> {
            // 서버에 숫자 야구 게임 생성 요청
            mainApp.getClient().sendMessage("GAME_CREATE_REQUEST::NUMBER_BASEBALL");
            dispose(); // 창 닫기
        });
        add(baseballButton);

        // --- 나중에 추가될 게임 버튼 (지금은 비활성화) ---
        JButton comingSoonButton = new JButton("<html>끝말잇기<br>(준비 중)</html>");
        comingSoonButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        comingSoonButton.setPreferredSize(new Dimension(150, 80));
        comingSoonButton.setEnabled(false);
        add(comingSoonButton);

        setSize(400, 180);
        setLocationRelativeTo(owner); // 부모 창(MainFrame) 중앙에 위치
    }
}
