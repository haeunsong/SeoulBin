package seoulbin;

import static com.teamdev.jxbrowser.engine.RenderingMode.HARDWARE_ACCELERATED;
import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.browser.callback.InjectJsCallback;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.js.JsAccessible;
import com.teamdev.jxbrowser.js.JsObject;
import com.teamdev.jxbrowser.navigation.event.*;
import com.teamdev.jxbrowser.view.swing.BrowserView;
import java.awt.*;
import javax.swing.*;

/*
    addMarker(String title, double lat, double lng, int type) 마커 등록
    deleteMarker(int index) 마커 삭제
    resizeMap() -> 카카오맵 api 패널 지도 사이즈 변경 / 현재 패널 사이즈에 맞게 변경
    / 그렇지만 카카오맵이 너무 커지면 로딩이 안됨 / 패널 사이즈 변경 시 스크롤 제거를 위함

    마커 클릭 이벤트 예시
    mapPanel.addMarkerClickEventListener(new MarkerClickEventListener() {
        @Override
        public void markerClicked(MarkerEvent e) { // MarkerEvent는 title, lat, lng, index, type정보를 갖고 있음
            System.out.println("이벤트 테스트용 : "+ e.title);
        }
    });

*/

public class MapPanel extends JPanel {
    private Browser browser;
    private Engine engine;
    private MarkerClickEventListener markerClickEventListener; // 마커 클릭 리스너 인터페이스
    // markerClicked() 메서드를 갖는다.

    // 마커 클릭 이벤트 인터페이스 구현
    public void addMarkerClickEventListener(MarkerClickEventListener markerClickEventListener) {
        this.markerClickEventListener = markerClickEventListener;
    }

    public MapPanel() {
        engine = Engine.newInstance(EngineOptions.newBuilder(HARDWARE_ACCELERATED)
                .licenseKey("OK6AEKNYF2J46TGUSGVWFCL96HY8PYV11PUSURQ08A66MHBZA4TDH0Y6D09OZJ0L6BRGXSCIFY0F144KAMG1F7O5GHDM3PATIN4WNZCEGBXE0L3Y2UHDX3RGK4K1DSJUO9C6LYTJYAQG2NHON")
                .build());
        browser = engine.newBrowser(); // 탐색 브라우저
        BrowserView view = BrowserView.newInstance(browser); // swing에 표시하기 위함
        setLayout(new BorderLayout());
        add(view, BorderLayout.CENTER);

        browser.navigation().loadUrl("localhost:8088/map"); // page load

        browser.set(InjectJsCallback.class, params -> { // js 로딩전 콜백함수 삽입
            JsObject window = params.frame().executeJavaScript("window"); // window 객체 찾기
            window.putProperty("java", new JavaMarkerObject()); // 윈도우에 삽입
            return InjectJsCallback.Response.proceed();
        });

        // 맵 로딩 전처리
        preProcessing();
    }

    public void engineClose() { engine.close(); }

    // 맵 크기 변경 시 사용 >> 스크롤 제거를 위함 // 사이즈 변경할 때마다 호출
    public void resizeMap() {
        SwingUtilities.invokeLater(() -> { // 스윙 변경사항 기다리기
            Dimension size = getSize(); // 현재 패널사이즈 가져오기
            String script = String.format("resizeMap(%d, %d)", size.width, size.height);
//            System.out.println(script);
            browser.mainFrame().ifPresent(frame -> {
                frame.executeJavaScript(script); //자바스크립트 실행
            });
        });
    }

    // 처음에 표시할 마커 로드
    private void preProcessing() {
        browser.navigation().on(LoadFinished.class, event -> {
            resizeMap();
            // addMarkers();
            // 일단 테스트용
            addMarker("shku", 37.4886, 126.8247, 0);
            addMarker("shku1", 37.4886, 126.8297, 0);
        });
    }

    public void addMarker(String title, double lat, double lng, int type) {
        /*
            title 이름, lat 위도, lng 경도, type 쓰레기통 타입, 이미지 쓰레기통 분류해서 보여줄 때 사용
        */
        String script = String.format("addMarker('%s', %f, %f, %d)", title, lat, lng, type);
        browser.mainFrame().ifPresent(frame -> {
            frame.executeJavaScript(script); //자바스크립트 실행
        });
    }

    public void addMarkers() {
        /*
            처음 배열 받아서, 만들기용
        */
//        addMarker()
    }

    // 자바스크립트부터 자바로 데이터 받기
    public final class JavaMarkerObject {
        public MarkerEvent markerEvent;

        @JsAccessible // 자바스크립트에서 호출
        public void callJavaMarkerEvent(int index, String title, double lat, double lng, int type) { // 자바스크립트 호출때 사용할 이름
            markerEvent = new MarkerEvent(index, title, lat, lng, type);
            System.out.println(markerEvent.title + " " + markerEvent.lat + ", " + markerEvent.lng);

            if (markerClickEventListener != null) { // 마커클릭이벤트가 등록되면
                markerClickEventListener.markerClicked(markerEvent); // 마커 이벤트 전달 < 마커 클릭 이벤트 실행 여기서
            }
        }
    }

}



