package seoulbin;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;

public class Main extends JFrame {
    private static MapPanel mapPanel;
    public Main() {
        setSize(1000, 800);
        setLayout(new BorderLayout());

        mapPanel = new MapPanel();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mapPanel.engineClose();
            }
        });

//        add(mapPanel);
        add(mapPanel, BorderLayout.CENTER);
        
        // 마커 클릭 예시
        mapPanel.addMarkerClickEventListener(new MarkerClickEventListener() {
            @Override
            public void markerClicked(MarkerEvent e) { // MarkerEvent는 title, lat, lng, index, type정보를 갖고 있음
                System.out.println("이벤트 테스트용 : "+ e.title);
            }
        });
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setVisible(true);
    }

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                TestServer.main(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).run();

        new Main();
        mapPanel.addMarker("shku", 37.4886, 126.8247, 0);
        mapPanel.addMarker("shku1", 37.4886, 126.8297, 0);
    }
}

