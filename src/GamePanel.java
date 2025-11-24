import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GamePanel extends JPanel {
    private Main mainApp;
    private JLabel infoLabel;
    private JTextArea historyArea;
    private JTextField inputField;
    private JButton submitButton;

    // GamePanel.java 파일의 생성자 부분만 이 코드로 교체하세요.

    public GamePanel(Main mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 상단 정보 라벨
        infoLabel = new JLabel("숫자 야구 게임에 오신 것을 환영합니다!", SwingConstants.CENTER);
        infoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        add(infoLabel, BorderLayout.NORTH);

        // 중앙 추측 기록
        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(historyArea);
        add(scrollPane, BorderLayout.CENTER);

        // 하단 입력 패널
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        inputField = new JTextField();
        inputField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        submitButton = new JButton("입력");

        // ▼▼▼ [핵심 수정] Enter키와 '입력' 버튼 모두에 기능을 연결합니다. ▼▼▼
        inputField.addActionListener(e -> submitGuess());
        submitButton.addActionListener(e -> submitGuess()); // 누락되었던 이 한 줄을 추가!

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(submitButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }


    private void submitGuess() {
        String guess = inputField.getText();
        // (입력값 유효성 검사 - 3자리 숫자인지 등 - 은 나중에 추가)
        mainApp.getClient().sendMessage("GAME_ACTION::" + guess);
        inputField.setText("");
    }

    // 서버로부터 정보를 받아 UI를 업데이트하는 메소드들
    public void setInfoText(String text) {
        infoLabel.setText(text);
    }

    public void updateHistory(String history) {
        historyArea.setText(history);
    }

    public void setInputEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
        submitButton.setEnabled(enabled);
    }
}
