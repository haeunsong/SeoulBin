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
import mapdata.utils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class MapPanel extends JPanel {
    private final Browser browser;
    private final Engine engine;
    private List<Map<String, Object>> binList; // 쓰레기통 리스트
    private MarkerClickEventListener markerClickEventListener; // 마커 클릭 리스너 인터페이스
    private boolean isAddingBin = false;
    private JsObject currentMarker = null;  // 현재 마커를 저장할 변수
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
        List<Map<String, Object>> binData = utils.allBinSelector();
        // {"bin_id":3437,"bin_type":"1","latitude":37.48328556,"longitude":126.8789442}
        String jsonData = new Gson().toJson(binData);

        browser.mainFrame().ifPresent(frame -> {
            frame.executeJavaScript(String.format("loadTrashBins(%s);", jsonData));
        });
        
        resizeMap();
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

    public void engineClose() {
        engine.close();
    }

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

    // ================  쓰레기통 추가  =================
    public void enableBinAddingMode() {
    	isAddingBin=true;
        browser.mainFrame().ifPresent(frame -> frame.executeJavaScript("addNewBin()"));
    }
    // ================ 마커 찍기 모드 비활성화 =================
    public void disableBinAddingMode() {
    	browser.mainFrame().ifPresent(frame -> frame.executeJavaScript("removeBinAddingMode()"));
    	isAddingBin = false;  // 마커 추가 모드 비활성화
        System.out.println("마커 추가 모드 종료");
    }
    

    public final class JavaMarkerObject {
        public MarkerEvent markerEvent;

        @JsAccessible // 자바스크립트에서 호출
        public void callJavaMarkerEvent(int index, String title, double lat, double lng, int type) { // 자바스크립트 호출때 사용할 이름
            markerEvent = new MarkerEvent(index, title, lat, lng, type);

            if (markerClickEventListener != null) { // 마커클릭이벤트가 등록되면
                markerClickEventListener.markerClicked(markerEvent); // 마커 이벤트 전달 < 마커 클릭 이벤트 실행 여기서
            }
        }

        @JsAccessible
        public void addBin(double lat, double lng) {
            SwingUtilities.invokeLater(() -> {
                int type = JOptionPane.showOptionDialog(
                        null,
                        "추가할 쓰레기통의 타입을 선택하세요:",
                        "쓰레기통 추가",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[]{"일반", "재활용"},
                        "일반"
                );

                if (type == JOptionPane.CLOSED_OPTION) return;

                int result = utils.addBinData(lat, lng, type);
                if (result == 0) {
                    JOptionPane.showMessageDialog(null, "쓰레기통이 성공적으로 추가되었습니다!");
                    loadTrashBinData();
                } else {
                    JOptionPane.showMessageDialog(null, "쓰레기통 추가에 실패했습니다.");
                }
            });
        }
    }
}