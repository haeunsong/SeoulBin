package seoulbin;

import seoulbin.browser.BrowserManager;
import seoulbin.review.ReviewDialog;
import seoulbin.model.MarkerEvent;
import seoulbin.map.*;
import seoulbin.service.BinService;
import seoulbin.service.HomeService;
import seoulbin.stamp.Stamp;
import seoulbin.review.*;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import java.time.LocalDateTime;
import java.util.Locale;
import com.formdev.flatlaf.FlatLightLaf;

public class Main extends JFrame {
    private BrowserManager browserManager;
    private BinService binService;
    private HomeService homeService;

    private JPanel mainPanel;
    private JPanel leftPanel;
    private JPanel rightPanel;

    private JLabel dateTimeLabel;
    private MapPanel mapPanel;
    private Stamp stamp;
    private MarkerEvent marker;
    private ReviewButton reviewButton;
    private JButton endPinButton;

    public Main() throws IOException {
        this.browserManager = new BrowserManager();
        this.binService = new BinService(browserManager);
        this.homeService = new HomeService(browserManager);

        setSize(1000, 800);
        setTitle("SeoulBin 서울시 쓰레기통 위치 제공 서비스");
        setLayout(new BorderLayout());

        mainPanel = new JPanel(new BorderLayout());
        mapPanel = new MapPanel(binService, homeService, browserManager);
        leftPanel = new JPanel(new BorderLayout());
        rightPanel = new JPanel(new BorderLayout());

        drawLeftPanel();
        drawRightPanel();

        // 윈도우 닫을 때 엔진 종료
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                browserManager.closeEngine();
            }
        });

        // 마커 클릭 시
        mapPanel.addMarkerClickEventListener(new MarkerClickEventListener() {
            @Override
            public void markerClicked(MarkerEvent e) { // MarkerEvent.index
                marker = e;
                if (e != null) {
                    reviewButton.loadReview(e.index);
                }
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


    private void drawLeftPanel() throws IOException {

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

        // 검색 필드 엔터 검색 이벤트
        searchField.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(Main.this, "검색어를 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            } else {
                // JavaScript의 searchPlaces 함수 호출
                binService.searchPlaces(keyword);
            }
        });

        // 검색 버튼
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
                binService.searchPlaces(keyword);
            }
        });

        // 쓰레기통 리뷰 버튼
        reviewButton = new ReviewButton("별점 선택");
        reviewButton.setBounds(35, 420, 200, 40); // 검색 버튼 아래에 위치
        reviewButton.setFont(new Font("Malgun gothic", Font.PLAIN, 16));
        leftPanel.add(reviewButton);
        // "쓰레기통 리뷰" 버튼 이벤트
        reviewButton.addActionListener(e -> {
            if (marker != null) {
                new ReviewDialog(this, reviewButton, marker.index);
                marker = null;
                mapPanel.resetMarkerImage();
            } else {
                JOptionPane.showMessageDialog(this, "마커를 클릭하세요.",
                        "Message", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 쓰레기통 추가 버튼
        JButton addBinButton = new JButton("쓰레기통 추가");
        addBinButton.setBounds(35, 460, 200, 40); // 검색 버튼 아래에 위치
        addBinButton.setFont(new Font("Malgun gothic", Font.PLAIN, 16));
        leftPanel.add(addBinButton);
        // "쓰레기통 추가" 버튼 이벤트
        addBinButton.addActionListener(e -> {
            int option=JOptionPane.showConfirmDialog(Main.this, "원하는 위치에 마커를 꽂아주세요.","쓰레기통 추가",JOptionPane.YES_NO_OPTION);;

            if(option == JOptionPane.YES_OPTION) {
                binService.enableBinAddingMode();
                endPinButton.setVisible(true);  // 핀 찍기 종료 버튼 보이기
                mapPanel.resizeMap();
            }
        });

        // 쓰레기통 삭제 버튼
        JButton deleteBinButton = new JButton("쓰레기통 삭제");
        deleteBinButton.setBounds(35, 500, 200, 40); // "쓰레기통 추가" 버튼 바로 아래에 위치
        deleteBinButton.setFont(new Font("Malgun gothic", Font.PLAIN, 16));
        leftPanel.add(deleteBinButton);
        // 쓰레기통 삭제 버튼 이벤트
        deleteBinButton.addActionListener(e -> {
            if (marker != null) {
                int result = JOptionPane.showConfirmDialog(this,
                        "정말 삭제하시겠습니까?", "Mesaage", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    binService.deleteBin(marker.index);
                    marker = null;
                    binService.loadTrashBinData();
                }
            } else {
                JOptionPane.showMessageDialog(this, "마커를 클릭하세요.",
                        "Message", JOptionPane.ERROR_MESSAGE);
            }
        });
        // "스탬프 페이지 이동" 버튼 생성
        JButton stampPageButton = new JButton("스탬프 페이지");
        stampPageButton.setBounds(35, 540, 200, 40);
        stampPageButton.setFont(new Font("Malgun gothic", Font.PLAIN, 16));
        leftPanel.add(stampPageButton);

        stampPageButton.addActionListener(e -> showStampPage());
        stamp = new Stamp(this);

        // 집 아이콘 버튼 생성
        JButton homeButton = new JButton();
        ImageIcon homeIcon = new ImageIcon("img/home.png"); // 집 아이콘 경로 설정
        // 아이콘 크기 조정
        int iconWidth = 60;  // 원하는 아이콘 너비
        int iconHeight = 60; // 원하는 아이콘 높이
        Image scaledImage = homeIcon.getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH);
        ImageIcon scaledHomeIcon = new ImageIcon(scaledImage);

        homeButton.setIcon(scaledHomeIcon);
        homeButton.setBounds(35, 610, 200, 60); // 적절한 위치로 배치
        homeButton.setFocusPainted(false); // 버튼 포커스 테두리 제거
        homeButton.setBorderPainted(false); // 버튼 테두리 제거
        homeButton.setContentAreaFilled(false); // 배경 제거
        leftPanel.add(homeButton);

        homeButton.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(Main.this,"HOME 으로 지정할 위치에 마커를 꽂아주세요. 프로그램 시작 시 초기 위치로 지정됩니다.","HOME 지정", JOptionPane.YES_NO_OPTION);
            if(option == JOptionPane.YES_OPTION) {
                homeService.enableHomeSettingMode();
            }
        });
    }

    private void drawRightPanel() {
        // 핀 찍기 종료 버튼
        endPinButton = new JButton("핀 찍기 종료");
        endPinButton.setBounds(35, 510, 200, 40); // 위치 설정
        endPinButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
        endPinButton.setBorder(BorderFactory.createLineBorder(Color.RED
                ,3));
        endPinButton.setVisible(false);  // 처음에는 보이지 않음
        rightPanel.add(endPinButton);

        // "핀 찍기 종료" 버튼 클릭 이벤트
        endPinButton.addActionListener(e -> {
            binService.disableBinAddingMode();  // 마커 찍기 모드 비활성화
            endPinButton.setVisible(false);  // 핀 찍기 종료 버튼 숨기기
            mapPanel.resizeMap();
            binService.loadTrashBinData();
        });

        // 오른쪽 패널 설정 (지도 표시 영역)
        rightPanel.setPreferredSize(new Dimension(730, 800));

        // 오른쪽 패널에 mapPanel 추가
        rightPanel.add(mapPanel, BorderLayout.CENTER);

        rightPanel.add(endPinButton, BorderLayout.NORTH);

    }
    public void showStampPage() {
        getContentPane().removeAll();
        add(stamp);
        revalidate();
        repaint();
    }

    // 메인 페이지로 복귀
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
    	try {
            // Look and Feel 설정
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

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

