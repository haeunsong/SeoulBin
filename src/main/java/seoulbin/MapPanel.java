package seoulbin;

import static com.teamdev.jxbrowser.engine.RenderingMode.HARDWARE_ACCELERATED;

import com.google.gson.Gson;
import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.browser.callback.InjectJsCallback;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.js.JsAccessible;
import com.teamdev.jxbrowser.js.JsObject;
import com.teamdev.jxbrowser.navigation.event.*;
import com.teamdev.jxbrowser.view.swing.BrowserView;
import mapdata.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class MapPanel extends JPanel {
    private final Browser browser;
    private final Engine engine;
    private MarkerClickEventListener markerClickEventListener; // 마커 클릭 리스너 인터페이스
    private MapClickEventListener mapClickEventListener;

    public MapPanel() {
        // 1. JxBrowser 엔진 초기화
        engine = Engine.newInstance(EngineOptions.newBuilder(HARDWARE_ACCELERATED)
                .licenseKey("OK6AEKNYF2J46TGUSGVWFCL96HY8PYV11PUSURQ08A66MHBZA4TDH0Y6D09OZJ0L6BRGXSCIFY0F144KAMG1F7O5GHDM3PATIN4WNZCEGBXE0L3Y2UHDX3RGK4K1DSJUO9C6LYTJYAQG2NHON")
                .build());
        browser = engine.newBrowser();
        browser.devTools().show();

        // 2. JxBrowser UI 설정
        BrowserView view = BrowserView.newInstance(browser);
        setLayout(new BorderLayout());
        add(view, BorderLayout.CENTER);

        // 3. HTML 페이지 로드
        browser.navigation().loadUrl("localhost:8088/map");

        // 4. 브라우저 로드 완료 후 작업
        browser.navigation().on(LoadFinished.class, event -> {
            // 지도 초기화 (JavaScript에서 실행)
            browser.mainFrame().ifPresent(frame -> frame.executeJavaScript("initMap();"));
            // 데이터 로드 및 마커 표시
            loadTrashBinData(); // Java에서 데이터 읽고 JavaScript로 전달
        });

        browser.set(InjectJsCallback.class, params -> {
            JsObject window = params.frame().executeJavaScript("window");
            window.putProperty("java", new JavaMarkerObject());
            return InjectJsCallback.Response.proceed();
        });
    }
    // ================ 전체 쓰레기통 위치 불러오기 + 마커 표시  =================
    public void loadTrashBinData() {
        List<Map<String, Object>> binData = Utils.allBinSelector();
        // {"bin_id":4051,"bin_type":"0","city":"강동구","latitude":37.55174312,"detail":"주양쇼핑 따릉이 대여소(1036) 앞\r","longitude":127.1545325}
        String jsonData = new Gson().toJson(binData);

        browser.mainFrame().ifPresent(frame -> {
            frame.executeJavaScript(String.format("loadTrashBins(%s);", jsonData));
        });
    }

    // ================ 장소 검색  =================
    public void searchPlaces(String keyword) {
        String script = String.format("searchPlaces('%s')", keyword.replace("'", "\\'")); // 안전한 문자열 처리
        browser.mainFrame().ifPresent(frame -> frame.executeJavaScript(script));
    }

    // 마커 클릭 이벤트 인터페이스 구현
    public void addMarkerClickEventListener(MarkerClickEventListener markerClickEventListener) {
        this.markerClickEventListener = markerClickEventListener;
    }

    // 맵 클릭 이벤트 인터페이스 구현
    public void addMapClickEventListener(MapClickEventListener mapClickEventListener) {
        this.mapClickEventListener = mapClickEventListener;
    }

    /**
     * 맵 클릭 여부 변경
     * false로 변경시 맵에 찍힌 마커도 삭제됨
     * @param true_or_false
     */
    public void setMapClickEnable(boolean true_or_false) {
        String script = String.format("setMapClickEnable(%b)", true_or_false);
        browser.mainFrame().ifPresent(frame -> {
            frame.executeJavaScript(script); //자바스크립트 실행
        });
    }

    public void engineClose() {
        engine.close();
    }

    // ================  쓰레기통 추가  =================
    public void enableBinAddingMode() {
        browser.mainFrame().ifPresent(frame -> frame.executeJavaScript("addNewBin()"));
    }
    
    // ================ 쓰레기통 삭제 ================
    public void deleteBin(int markerIndex) {
        int result = Utils.deleteBinData(markerIndex);

        System.out.println("삭제 여부: " + result); // 삭제 0 오류시 -1
    }
    
    //================  쓰레기통 삭제  =================
    public void deleteBin(MarkerEvent marker, String imagePath) {
        int result = Utils.deleteBinData(marker.lat, marker.lng, marker.type, imagePath);

        System.out.println("삭제 여부: " + result); // 삭제 0 오류시 -1
    }


    public void setCenter(double lat, double lng) {
        String script = String.format("setCenter(%f, %f)", lat, lng);
        browser.mainFrame().ifPresent(frame -> {
            frame.executeJavaScript(script); //자바스크립트 실행
        });
    }

    public void getCurrentLocation() {
        String script = String.format("getCurrentLocation()");
        browser.mainFrame().ifPresent(frame -> {
            frame.executeJavaScript(script);
        });
    }
    
  //================== 쓰레기통 마커 초기화(클릭해체)-------
    public void resetMarkerImage() {
        browser.mainFrame().ifPresent(frame -> frame.executeJavaScript("resetMarkerImage()"));
    }

    public final class JavaMarkerObject {
        public MarkerEvent markerEvent;

        @JsAccessible // 자바스크립트에서 호출
        public void callJavaMarkerEvent(Integer index, Double lat, Double lng, Integer type) { // 자바스크립트 호출때 사용할 이름
            markerEvent = new MarkerEvent(index, lat, lng, type);

            if (markerClickEventListener != null) { // 마커클릭이벤트가 등록되면
                markerClickEventListener.markerClicked(markerEvent); // 마커 이벤트 전달 < 마커 클릭 이벤트 실행 여기서
            }
        }
        
        @JsAccessible // 자바스크립트에서 호출
        public void callJavaMarkerEvent(Object nullCheck) { // null 받기
            markerEvent = (MarkerEvent) nullCheck;

            if (markerClickEventListener != null) { // 마커클릭이벤트가 등록되면
                markerClickEventListener.markerClicked(markerEvent); // 마커 이벤트 전달 < 마커 클릭 이벤트 실행 여기서
            }
        }

        @JsAccessible // 자바스크립트에서 호출 맵클릭
        public void callJavaMapEvent(double lat, double lng) { // null 받기
            if (mapClickEventListener != null) { // 맵클릭이벤트가 등록되면
                mapClickEventListener.mapClicked(lat, lng); // 맵 클릭 이벤트 전달 < 마커 클릭 이벤트 실행 여기서
            }
        }
    }
    
    
}
