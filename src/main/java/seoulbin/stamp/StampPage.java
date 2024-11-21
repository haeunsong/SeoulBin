package seoulbin.stamp;

import seoulbin.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class StampPage extends JPanel {
    private Main mainFrame;
    private HashMap<Integer, Boolean> stampData; // 날짜별 스탬프 저장

    public StampPage(Main mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1000, 600));

        // 상단 영역: 뒤로가기 버튼
        JPanel topPanel = new JPanel();
        JButton backButton = new JButton("뒤로가기");
        backButton.addActionListener(e -> mainFrame.showMainPage()); // 메인 페이지로 복귀
        topPanel.add(backButton);
        add(topPanel, BorderLayout.NORTH);

        // 중앙 영역: 스탬프 버튼
        JPanel stampPanel = new JPanel(new GridLayout(5, 7, 10, 10)); // 5x7 그리드 (최대 35일)
        stampData = new HashMap<>();

        for (int i = 1; i <= 35; i++) {
            JButton stampButton = new JButton("Day " + i);
            stampButton.setEnabled(!stampData.getOrDefault(i, false)); // 이미 찍은 날짜는 비활성화
            int day = i; // 람다식 내부에서 사용할 final 변수
            stampButton.addActionListener(e -> {
                stampData.put(day, true); // 스탬프 찍기 기록
                stampButton.setEnabled(false); // 버튼 비활성화
                JOptionPane.showMessageDialog(this, "Day " + day + " 스탬프를 찍었습니다!");
            });
            stampPanel.add(stampButton);
        }

        add(stampPanel, BorderLayout.CENTER);

        // 하단 영역: 스탬프 찍기 안내
        JLabel infoLabel = new JLabel("하루에 하나의 스탬프만 찍을 수 있습니다.", JLabel.CENTER);
        add(infoLabel, BorderLayout.SOUTH);
    }
}
