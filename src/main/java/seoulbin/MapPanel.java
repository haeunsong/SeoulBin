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
import mapdata.utils;

import java.awt.*;
import javax.swing.*;

import mapdata.utils;
import java.util.Map;
import java.util.List;

/*
    addMarker(String title, double lat, double lng, int type) 마커 등록
    deleteMarker(int index) 마커 삭제
    resizeMap() -> 카카오맵 api 패널 지도 사이즈 변경 / 현재 패널 사이즈에 맞게 변경
    / 그렇지만 카카오맵이 너무 커지면 로딩이 안됨 / 패널 사이즈 변경 시 스크롤 제거를 위함
    setCenter(double lat, double lng) 해당 좌표로 지도 중심 이동

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
    private List<Map<String, Object>> binList; // 쓰레기통 리스트

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
    
    /**
     * 맵 크기 변경 시 사용 > JPanel 사이즈 변경 시 호출해서 스크롤 제거
     */
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

    /**
     * 처음에 표시할 마커 로드
     */
    private void preProcessing() {
        binList = utils.allBinSelector();

        browser.navigation().on(LoadFinished.class, event -> {
            resizeMap();
            addMarkers();
        });
    }

    /**
     *  지도에 마커를 추가하는 함수 return -> void
     * @param title 이름
     * @param lat 위도
     * @param lng 경도
     * @param type 쓰레기통 타입
     */
    public void addMarker(String title, double lat, double lng, int type) {
        String script = String.format("addMarker('%s', %f, %f, %d)", title, lat, lng, type);
        browser.mainFrame().ifPresent(frame -> {
            frame.executeJavaScript(script); //자바스크립트 실행
        });
    }

    private void addMarkers() {
        /*
            처음 배열 받아서, 만들기용
        */
        for (Map<String, Object> binData : binList) {
//            System.out.println("Bin_Id: " + binData.get("bin_id") +
//                    ", Longitude: " + binData.get("longitude") +
//                    ", Latitude: " + binData.get("latitude") +
//                    ", Bin Type: " + binData.get("bin_type"));
            addMarker(Integer.toString(
                    (int)binData.get("bin_id")),
                    (double)binData.get("latitude"),
                    (double)binData.get("longitude"),
                    Integer.parseInt((String)binData.get("bin_type")));
        }
    }

    /**
     *  좌표로 지도 중심 이동시키는 함수 return -> void
     * @param lat double latitude 위도
     * @param lng double longitude 경도
     */
    public void setCenter(double lat, double lng) {
        String script = String.format("setCenter(%f, %f)", lat, lng);
        browser.mainFrame().ifPresent(frame -> {
            frame.executeJavaScript(script); //자바스크립트 실행
        });
    }

    // 자바스크립트부터 자바로 데이터 받기
    public final class JavaMarkerObject {
        public MarkerEvent markerEvent;

        @JsAccessible // 자바스크립트에서 호출
        public void callJavaMarkerEvent(int index, String title, double lat, double lng, int type) { // 자바스크립트 호출때 사용할 이름
            markerEvent = new MarkerEvent(index, title, lat, lng, type);

            if (markerClickEventListener != null) { // 마커클릭이벤트가 등록되면
                markerClickEventListener.markerClicked(markerEvent); // 마커 이벤트 전달 < 마커 클릭 이벤트 실행 여기서
            }
        }
    }

}



