package seoulbin;

import static com.teamdev.jxbrowser.engine.RenderingMode.HARDWARE_ACCELERATED;

import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.navigation.event.*;
import com.teamdev.jxbrowser.view.swing.BrowserView;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;

public class MapPanel extends JPanel {
    private Browser browser;
    private Engine engine;

    public MapPanel() {
        engine = Engine.newInstance(EngineOptions.newBuilder(HARDWARE_ACCELERATED)
                .licenseKey("OK6AEKNYF2J46TGUSGVWFCL96HY8PYV11PUSURQ08A66MHBZA4TDH0Y6D09OZJ0L6BRGXSCIFY0F144KAMG1F7O5GHDM3PATIN4WNZCEGBXE0L3Y2UHDX3RGK4K1DSJUO9C6LYTJYAQG2NHON")
                .build());
        browser = engine.newBrowser(); // 탐색 브라우저
        BrowserView view = BrowserView.newInstance(browser); // swing에 표시하기 위함

        setLayout(new BorderLayout());
        add(view, BorderLayout.CENTER);

        browser.navigation().loadUrl("localhost:8088/map"); // page load
    }

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                TestServer.main(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).run();

        MapPanel myMap = new MapPanel();
        SwingUtilities.invokeLater(() -> {
            myMap.addMarker("skhu", 37.4878925, 126.825290, 0);
        });
    }

    public void engineClose() {
        engine.close();
    }

    public void addMarker(String title, double lat, double lng, int type) {
        /*
            title 이름
            lat 위도
            lng 경도
            type 쓰레기통 타입, 이미지 쓰레기통 분류해서 보여줄 때 사용
         */
        browser.navigation().on(LoadFinished.class, event -> { // 로딩 기다리기
            String script = String.format("addMarker('%s', %f, %f, %d)", title, lat, lng, type);
//            System.out.println(script);
            browser.mainFrame().ifPresent(frame -> {
                frame.executeJavaScript(script); //자바스크립트 실행
            });
        });
    }

}

