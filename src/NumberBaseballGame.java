// NumberBaseballGame.java (4글자 버전 최종본)

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NumberBaseballGame {
    private List<Integer> answer;

    public NumberBaseballGame() {
        generateAnswer();
    }

    // 겹치지 않는 4자리 정답 숫자 생성
    private void generateAnswer() {
        answer = new ArrayList<>();
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        // ▼▼▼ [수정] 3 -> 4 ▼▼▼
        for (int i = 0; i < 4; i++) {
            answer.add(numbers.get(i));
        }
        System.out.println("[게임 엔진] 정답 생성: " + answer);
    }

    // 추측한 숫자와 정답을 비교하여 결과 반환
    public String checkGuess(String guess) {
        // ▼▼▼ [수정] 3 -> 4 ▼▼▼
        if (guess.length() != 4) {
            return "4자리 숫자만 입력하세요.";
        }
        if (!guess.matches("\\d+")) { // 숫자만 포함되어 있는지 검사
            return "숫자만 입력하세요.";
        }

        List<Integer> guessNumbers = new ArrayList<>();
        for (char c : guess.toCharArray()) {
            guessNumbers.add(Integer.parseInt(String.valueOf(c)));
        }

        // 중복된 숫자가 있는지 검사
        long distinctCount = guessNumbers.stream().distinct().count();
        if (distinctCount < 4) {
            return "서로 다른 4자리 숫자를 입력하세요.";
        }

        int strikes = 0;
        int balls = 0;
        // ▼▼▼ [수정] 3 -> 4 ▼▼▼
        for (int i = 0; i < 4; i++) {
            if (guessNumbers.get(i).equals(answer.get(i))) {
                strikes++;
            } else if (answer.contains(guessNumbers.get(i))) {
                balls++;
            }
        }

        // ▼▼▼ [수정] 3S -> 4S ▼▼▼
        if (strikes == 4) {
            return "4S - 정답입니다!";
        }
        return strikes + "S " + balls + "B";
    }

    public String getAnswerString() {
        // ▼▼▼ [수정] 4자리 숫자를 문자열로 변환 ▼▼▼
        return "" + answer.get(0) + answer.get(1) + answer.get(2) + answer.get(3);
    }
}
