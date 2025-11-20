// LoginFrame.java (변경 없음)
// (이전 답변의 최종 LoginFrame.java 코드를 그대로 사용하세요.)
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField nicknameField;
    private JButton loginButton;
    private Main mainApp;

    public LoginFrame(Main mainApp) {
        this.mainApp = mainApp;
        setTitle("겜톡");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(380, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(254, 229, 0));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel logoLabel = new JLabel("TALK", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 48));
        gbc.insets = new Insets(50, 0, 50, 0);
        mainPanel.add(logoLabel, gbc);
        nicknameField = new JTextField("닉네임을 입력하세요");
        nicknameField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        nicknameField.setPreferredSize(new Dimension(300, 40));
        nicknameField.setForeground(Color.GRAY);
        nicknameField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        gbc.insets = new Insets(5, 0, 5, 0);
        mainPanel.add(nicknameField, gbc);
        loginButton = new JButton("로그인");
        loginButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(300, 40));
        loginButton.setBackground(new Color(240, 240, 240));
        loginButton.setBorder(new LineBorder(new Color(220, 220, 220)));
        loginButton.setFocusPainted(false);
        mainPanel.add(loginButton, gbc);
        loginButton.addActionListener(e -> performLogin());
        nicknameField.addActionListener(e -> performLogin());
        nicknameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (nicknameField.getText().equals("닉네임을 입력하세요")) {
                    nicknameField.setText("");
                    nicknameField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (nicknameField.getText().isEmpty()) {
                    nicknameField.setText("닉네임을 입력하세요");
                    nicknameField.setForeground(Color.GRAY);
                }
            }
        });
        add(mainPanel, BorderLayout.CENTER);
    }

    private void performLogin() {
        String nickname = nicknameField.getText();
        if (nickname.isEmpty() || nickname.equals("닉네임을 입력하세요")) {
            JOptionPane.showMessageDialog(this, "닉네임을 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        mainApp.attemptLogin(nickname);
    }
}
