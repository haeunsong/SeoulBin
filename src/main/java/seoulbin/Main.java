package seoulbin;

import org.json.JSONObject;
import seoulbin.stamp.Stamp;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.format.    DateTimeFormatter;
import javax.swing.*;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Scanner;

public class Main extends JFrame {
    private JPanel mainPanel;
    private JLabel dateTimeLabel;
    private MapPanel mapPanel;
    private Stamp stamp;
    private Integer markerIndex; // 삭제를 위한 마커 인덱스

    public Main() throws IOException {
        setSize(1000, 800);
        setLayout(new BorderLayout());

        mainPanel = new JPanel(new BorderLayout());
        mapPanel = new MapPanel();

        // 왼쪽 패널 설정
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(270, 800));
        leftPanel.setLayout(null); // 절대 위치 배치
        leftPanel.setBackground(Color.decode("#edede9"));

        // 날짜 및 시간 표시 라벨
        dateTimeLabel = new JLabel(getCurrentDateTime());
        dateTimeLabel.setFont(new Font("Malgun gothic", Font.BOLD, 20));
        dateTimeLabel.setBounds(37, 20, 200, 50); // 두 줄로 표시되므로 높이 조정
        leftPanel.add(dateTimeLabel);

        // 검색 안내 라벨
        JLabel searchLabel = new JLabel("원하는 위치를 검색하세요.");
        searchLabel.setBounds(37, 80, 200, 20);
        searchLabel.setFont(new Font("Malgun gothic", Font.BOLD, 16));
        leftPanel.add(searchLabel);

        // 검색 필드
        JTextField searchField = new JTextField();
        searchField.setBounds(35, 120, 200, 40);
        searchField.setFont(new Font("Malgun gothic", Font.BOLD, 16));
        leftPanel.add(searchField);

        // ================ 검색 버튼  =================
        JButton searchButton = new JButton("검색");
        searchButton.setBounds(35, 160, 80, 30);
        searchButton.setFont(new Font("Malgun gothic", Font.PLAIN, 16));
        leftPanel.add(searchButton);

        // 검색 버튼 이벤트 추가
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(Main.this, "검색어를 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            } else {
                // JavaScript의 searchPlaces 함수 호출
                mapPanel.searchPlaces(keyword);
            }
        });
        // "현재 위치" 버튼 추가
        JButton currentLocationButton = new JButton("현재 위치");
        currentLocationButton.setBounds(35, 200, 200, 40);
        currentLocationButton.setFont(new Font("Arial", Font.PLAIN, 16));
        leftPanel.add(currentLocationButton);

        // 현재 위치 버튼 이벤트
        currentLocationButton.addActionListener(e -> {
            mapPanel.getCurrentLocation();
        });

        // ================ 쓰레기통 추가 버튼  =================
        // "쓰레기통 추가" 버튼 생성
        JButton addBinButton = new JButton("쓰레기통 추가");
        addBinButton.setBounds(35, 460, 200, 40); // 검색 버튼 아래에 위치
        addBinButton.setFont(new Font("Malgun gothic", Font.PLAIN, 16));
        leftPanel.add(addBinButton);

        // "쓰레기통 추가" 버튼 이벤트
        addBinButton.addActionListener(e -> mapPanel.enableBinAddingMode());

        // ================ 쓰레기통 삭제 버튼  =================
        JButton deleteBinButton = new JButton("쓰레기통 삭제");
        deleteBinButton.setBounds(35, 500, 200, 40); // "쓰레기통 추가" 버튼 바로 아래에 위치
        deleteBinButton.setFont(new Font("Malgun gothic", Font.PLAIN, 16));
        leftPanel.add(deleteBinButton);

        // 쓰레기통 삭제 버튼 이벤트
        deleteBinButton.addActionListener(e -> {
            if (markerIndex != null) {
                int result = JOptionPane.showConfirmDialog(this,
                        "정말 삭제하시겠습니까?", "Mesaage", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    // 삭제하는 부분
//                    System.out.println(markerIndex + "삭제");
                    mapPanel.deleteBin(markerIndex);
                    markerIndex = null;
                    // 쓰레기통 로딩 다시
                    mapPanel.loadTrashBinData();
                }
            } else {
                JOptionPane.showMessageDialog(this, "마커를 클릭하세요.",
                        "Message", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 오른쪽 패널 설정 (지도 표시 영역)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(730, 800));

        // 오른쪽 패널에 mapPanel 추가
        rightPanel.add(mapPanel, BorderLayout.CENTER);

        // "스탬프 페이지 이동" 버튼 생성
        JButton stampPageButton = new JButton("스탬프 페이지");
        stampPageButton.setBounds(35, 540, 200, 40);
        stampPageButton.setFont(new Font("Malgun gothic", Font.PLAIN, 16));
        leftPanel.add(stampPageButton);

        stampPageButton.addActionListener(e -> showStampPage());
        stamp = new Stamp(this);

        // 윈도우 닫을 때 엔진 종료
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mapPanel.engineClose();
            }
        });

        // 마커 클릭 예시
        mapPanel.addMarkerClickEventListener(new MarkerClickEventListener() {
            @Override
            public void markerClicked(MarkerEvent e) { // MarkerEvent.index
                System.out.println("이벤트 테스트용 : "+ e.index);
                markerIndex = e.index;
            }
        });

        // 프레임에 패널 추가
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        add(mainPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setVisible(true);

        // 날짜와 시간을 주기적으로 업데이트하는 타이머
        Timer timer = new Timer(1000, e -> dateTimeLabel.setText(getCurrentDateTime()));
        timer.start();
    }

    public void showStampPage() {
        getContentPane().removeAll();
        add(stamp);
        revalidate();
        repaint();
    }

//    // 메인 페이지로 복귀
    public void showMainPage() {
        getContentPane().removeAll();
        add(mainPanel);
        revalidate();
        repaint();
    }

    // 현재 날짜 및 시간 포맷
    private String getCurrentDateTime() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.E", Locale.KOREAN);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        // HTML 태그로 줄바꿈 적용
        return "<html>" + now.format(dateFormatter) + "<br>" + now.format(timeFormatter) + "</html>";
    }

    public static void main(String[] args) throws IOException {
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

