package seoulbin.stamp;

import seoulbin.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class StampPage extends JPanel {
    private Main mainFrame;
    private HashMap<Integer, Boolean> stampData; // 날짜별 스탬프 저장
    private LocalDate lastStampedDate; // 마지막으로 찍은 날짜 저장

    public StampPage(Main mainFrame) {

        // JOptionPane 텍스트 글꼴 및 크기 설정
        UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 20)); // 텍스트
       // UIManager.put("OptionPane.buttonFont", new Font("Arial", Font.PLAIN, 18)); // 버튼 텍스트

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
            stampButton.setOpaque(true); // 배경색 변경 허용
            stampButton.setContentAreaFilled(true); // 버튼 내부 채우기 활성화
            stampButton.setBorderPainted(false); // 테두리 비활성화

            int day = i; // 람다식 내부에서 사용할 final 변수

            stampButton.addActionListener(e -> {
                LocalDate today = LocalDate.now();

                // 오늘 이미 한 번 찍었는지 확인
//                if (lastStampedDate != null && lastStampedDate.equals(today)) {
//                    JOptionPane.showMessageDialog(this, "오늘은 이미 스탬프를 찍었습니다!");
//                    return;
//                }

                stampData.put(day, true); // 스탬프 찍기 기록
                stampButton.setText(today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                stampButton.setBackground(Color.GREEN); // 색상 변경
                lastStampedDate = today;


                if(day==7) {
                    JOptionPane.showMessageDialog(
                            this,
                            "☘☘ 새싹지킴이 타이틀 획득! ☘☘ ",
                            "DAY 7", // 대화상자 제목
                            JOptionPane.PLAIN_MESSAGE // 기본 아이콘 제거
                    );
                }else if(day==14) {
                    JOptionPane.showMessageDialog(
                            this,
                            "☘☘ 풀잎지킴이 타이틀 획득! ☘☘ ",
                            "DAY 14", // 대화상자 제목
                            JOptionPane.PLAIN_MESSAGE // 기본 아이콘 제거
                    );
                }else if(day==21) {
                    JOptionPane.showMessageDialog(
                            this,
                            "☘☘ 나무지킴이 타이틀 획득! ☘☘ ",
                            "DAY 21", // 대화상자 제목
                            JOptionPane.PLAIN_MESSAGE // 기본 아이콘 제거
                    );
                }else if(day==28) {
                    JOptionPane.showMessageDialog(
                            this,
                            "☘☘ 숲의 수호자 타이틀 획득! ☘☘ ",
                            "DAY 28", // 대화상자 제목
                            JOptionPane.PLAIN_MESSAGE // 기본 아이콘 제거
                    );
                }else if(day==35) {
                    JOptionPane.showMessageDialog(
                            this,
                            "☘☘ 지구의 영웅 타이틀 획득! ☘☘ ",
                            "DAY 35", // 대화상자 제목
                            JOptionPane.PLAIN_MESSAGE // 기본 아이콘 제거
                    );
                }

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
