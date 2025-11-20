// PlaceholderTextArea.java

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class PlaceholderTextArea extends JTextArea {
    private String placeholder;

    public PlaceholderTextArea(String placeholder) {
        this.placeholder = placeholder;
        addFocusListener(new PlaceholderFocusListener());
        showPlaceholder();
    }

    private void showPlaceholder() {
        setText(placeholder);
        setForeground(Color.GRAY);
    }

    // 외부에서 실제 텍스트를 가져갈 때 사용할 메소드
    public String getRealText() {
        if (getText().equals(placeholder)) {
            return ""; // 플레이스홀더가 보일 때는 빈 텍스트로 간주
        }
        return getText();
    }

    // 포커스 리스너를 처리하는 내부 클래스
    private class PlaceholderFocusListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
            // 포커스를 얻었을 때, 텍스트가 플레이스홀더와 같다면 비워줌
            if (getText().equals(placeholder)) {
                setText("");
                setForeground(Color.BLACK);
            }
        }
        @Override
        public void focusLost(FocusEvent e) {
            // 포커스를 잃었을 때, 텍스트가 비어있다면 플레이스홀더를 다시 보여줌
            if (getText().trim().isEmpty()) {
                showPlaceholder();
            }
        }
    }
}
