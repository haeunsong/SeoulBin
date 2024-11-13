package seoulbin;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import java.time.LocalDateTime;
import java.util.Locale;

public class Main extends JFrame {
    private JLabel dateTimeLabel;

    public Main() {
        setSize(1000, 800);
        setLayout(new BorderLayout());

        // 왼쪽 패널 설정
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(270, 800));
        leftPanel.setLayout(null); // 절대 위치 배치
        leftPanel.setBackground(Color.decode("#edede9"));

        // 날짜 및 시간 표시 라벨
        dateTimeLabel = new JLabel(getCurrentDateTime());
        dateTimeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        dateTimeLabel.setBounds(20, 20, 200, 40); // 두 줄로 표시되므로 높이 조정
        leftPanel.add(dateTimeLabel);

        // 검색 안내 라벨
        JLabel searchLabel = new JLabel("원하는 위치를 검색하세요.");
        searchLabel.setBounds(20, 80, 200, 20);
        searchLabel.setFont(new Font("Arial", Font.BOLD, 16));
        leftPanel.add(searchLabel);

        // 검색 필드
        JTextField searchField = new JTextField();
        searchField.setBounds(20, 110, 200, 30);
        leftPanel.add(searchField);

        // 검색 버튼
        JButton searchButton = new JButton("검색");
        searchButton.setBounds(20, 150, 80, 30);
        leftPanel.add(searchButton);

        // 오른쪽 패널 설정 (지도 표시 영역)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(730, 800));

        // 오른쪽 패널에 mapPanel 추가
        MapPanel mapPanel = new MapPanel();
        rightPanel.add(mapPanel, BorderLayout.CENTER);

        // 윈도우 닫을 때 엔진 종료
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mapPanel.engineClose();
            }
        });

        // 프레임에 패널 추가
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setVisible(true);

        // 날짜와 시간을 주기적으로 업데이트하는 타이머
        Timer timer = new Timer(1000, e -> dateTimeLabel.setText(getCurrentDateTime()));
        timer.start();
    }

    // 현재 날짜 및 시간 포맷
    private String getCurrentDateTime() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.E", Locale.KOREAN);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        // HTML 태그로 줄바꿈 적용
        return "<html>" + now.format(dateFormatter) + "<br>" + now.format(timeFormatter) + "</html>";
    }

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                // 로컬 HTTP 서버 시작
                TestServer.main(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).run();

        new Main();
    }
}
