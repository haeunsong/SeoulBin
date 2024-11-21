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
    private Browser browser;
    private Engine engine;
    private List<Map<String, Object>> binList; // 쓰레기통 리스트
    private MarkerClickEventListener markerClickEventListener; // 마커 클릭 리스너 인터페이스


    public MapPanel() {
        engine = Engine.newInstance(EngineOptions.newBuilder(HARDWARE_ACCELERATED)
                .licenseKey("OK6AEKNYF2J46TGUSGVWFCL96HY8PYV11PUSURQ08A66MHBZA4TDH0Y6D09OZJ0L6BRGXSCIFY0F144KAMG1F7O5GHDM3PATIN4WNZCEGBXE0L3Y2UHDX3RGK4K1DSJUO9C6LYTJYAQG2NHON")
                .build());
        browser = engine.newBrowser();
        browser.devTools().show();

        BrowserView view = BrowserView.newInstance(browser);
        setLayout(new BorderLayout());
        add(view, BorderLayout.CENTER);

        browser.navigation().loadUrl("localhost:8088/map");

        browser.set(InjectJsCallback.class, params -> {
            JsObject window = params.frame().executeJavaScript("window");
            window.putProperty("java", new JavaMarkerObject());
            return InjectJsCallback.Response.proceed();
        });
        preProcessing();
    }
    private void preProcessing() {
        try {
            // 1. DB에서 데이터를 로드
            binList = utils.allBinSelector();
            if (binList.isEmpty()) {
                System.out.println("No bin data found."); // 로그 추가
                return;
            }
            // 2. 브라우저 로드 이벤트에서 지도 작업 실행
            browser.navigation().on(LoadFinished.class, event -> {
                if (binList == null || binList.isEmpty()) {
                    System.err.println("No data to display on the map.");
                    return;
                }
               // resizeMap();
                 addMarkers();
                // loadTrashBinData();
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error during preProcessing: " + e.getMessage());
        }
    }

    public void addMarkers() {
        if (binList == null || binList.isEmpty()) {
            System.out.println("No bins to add."); // 로그 추가
            return;
        }
        for (Map<String, Object> binData : binList) {
            try {
                String binId = Integer.toString((int) binData.get("bin_id"));
                double latitude = (double) binData.get("latitude");
                double longitude = (double) binData.get("longitude");
                int binType = Integer.parseInt(binData.get("bin_type").toString()); // 안전한 변환

                addMarker(binId, latitude, longitude, binType);
            } catch (Exception e) {
                System.err.println("Error adding marker: " + e.getMessage());
            }
        }
    }

    public void loadTrashBinData() {
        List<Map<String, Object>> binData = utils.allBinSelector();
        String jsonData = new Gson().toJson(binData);
        browser.mainFrame().ifPresent(frame -> {
            // initMap 실행 후 loadTrashBins 실행
            frame.executeJavaScript("initMap();");
            frame.executeJavaScript("loadTrashBins(" + jsonData + ");");
        });

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



    // ================ 장소 검색  =================
    public void searchPlaces(String keyword) {
        String script = String.format("searchPlaces('%s')", keyword.replace("'", "\\'")); // 안전한 문자열 처리
        browser.mainFrame().ifPresent(frame -> frame.executeJavaScript(script));
    }

    // ================  쓰레기통 추가  =================
    public void enableBinAddingMode() {
        browser.mainFrame().ifPresent(frame -> frame.executeJavaScript("addNewBin()"));
    }

    public void addMarker(String title, double lat, double lng, int type) {
        String script = String.format("addMarker('%s', %f, %f, %d)", title, lat, lng, type);
        browser.mainFrame().ifPresent(frame -> {
            frame.executeJavaScript(script); //자바스크립트 실행
        });
    }
    public void setCenter(double lat, double lng) {
        String script = String.format("setCenter(%f, %f)", lat, lng);
        browser.mainFrame().ifPresent(frame -> {
            frame.executeJavaScript(script); //자바스크립트 실행
        });
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

//package seoulbin;
//
//import static com.teamdev.jxbrowser.engine.RenderingMode.HARDWARE_ACCELERATED;
//
//import com.google.gson.Gson;
//import com.teamdev.jxbrowser.browser.Browser;
//import com.teamdev.jxbrowser.browser.callback.InjectJsCallback;
//import com.teamdev.jxbrowser.engine.Engine;
//import com.teamdev.jxbrowser.engine.EngineOptions;
//import com.teamdev.jxbrowser.js.JsAccessible;
//import com.teamdev.jxbrowser.js.JsObject;
//import com.teamdev.jxbrowser.navigation.event.*;
//import com.teamdev.jxbrowser.view.swing.BrowserView;
//import mapdata.utils;
//
//import java.awt.*;
//import javax.swing.*;
//
//import mapdata.utils;
//import java.util.Map;
//import java.util.List;
//
/// *
//    addMarker(String title, double lat, double lng, int type) 마커 등록
//    deleteMarker(int index) 마커 삭제
//    resizeMap() -> 카카오맵 api 패널 지도 사이즈 변경 / 현재 패널 사이즈에 맞게 변경
//    / 그렇지만 카카오맵이 너무 커지면 로딩이 안됨 / 패널 사이즈 변경 시 스크롤 제거를 위함
//    setCenter(double lat, double lng) 해당 좌표로 지도 중심 이동
//
//    마커 클릭 이벤트 예시
//    mapPanel.addMarkerClickEventListener(new MarkerClickEventListener() {
//        @Override
//        public void markerClicked(MarkerEvent e) { // MarkerEvent는 title, lat, lng, index, type정보를 갖고 있음
//            System.out.println("이벤트 테스트용 : "+ e.title);
//        }
//    });
//
//*/
//
//public class MapPanel extends JPanel {
//    private Browser browser;
//    private Engine engine;
//    private MarkerClickEventListener markerClickEventListener; // 마커 클릭 리스너 인터페이스
//    // markerClicked() 메서드를 갖는다.
//    private List<Map<String, Object>> binList; // 쓰레기통 리스트
//    private boolean isBinAddingMode = false;
//
//    // 마커 클릭 이벤트 인터페이스 구현
//    public void addMarkerClickEventListener(MarkerClickEventListener markerClickEventListener) {
//        this.markerClickEventListener = markerClickEventListener;
//    }
//
//    public MapPanel() {
//        engine = Engine.newInstance(EngineOptions.newBuilder(HARDWARE_ACCELERATED)
//                .licenseKey("OK6AEKNYF2J46TGUSGVWFCL96HY8PYV11PUSURQ08A66MHBZA4TDH0Y6D09OZJ0L6BRGXSCIFY0F144KAMG1F7O5GHDM3PATIN4WNZCEGBXE0L3Y2UHDX3RGK4K1DSJUO9C6LYTJYAQG2NHON")
//                .build());
//        browser = engine.newBrowser(); // 탐색 브라우저
//
//        browser.devTools().show();
//
//        BrowserView view = BrowserView.newInstance(browser); // swing에 표시하기 위함
//        setLayout(new BorderLayout());
//        add(view, BorderLayout.CENTER);
//
//        browser.navigation().loadUrl("localhost:8088/map"); // page load
//
//        browser.set(InjectJsCallback.class, params -> { // js 로딩전 콜백함수 삽입
//            JsObject window = params.frame().executeJavaScript("window"); // window 객체 찾기
//            window.putProperty("java", new JavaMarkerObject()); // 윈도우에 삽입
//            return InjectJsCallback.Response.proceed();
//        });
//        browser.navigation().on(LoadFinished.class, event -> {
//            loadTrashBinData();
//        });
//    }
//    public void loadTrashBinData() {
//        // 데이터베이스에서 쓰레기통 데이터 가져오기
//        List<Map<String, Object>> binData = mapdata.utils.allBinSelector();
//        if (binData.isEmpty()) {
//            System.err.println("No trash bin data found!");
//            return;
//        }
//
//        // 데이터 JSON으로 변환
//        String jsonData = new Gson().toJson(binData);
//
//        // JavaScript 함수 호출하여 데이터 전달
//        browser.mainFrame().ifPresent(frame -> frame.executeJavaScript("loadTrashBins(" + jsonData + ");"));
//    }
//
//    public void engineClose() { engine.close(); }
//
//    /**
//     * 맵 크기 변경 시 사용 > JPanel 사이즈 변경 시 호출해서 스크롤 제거
//     */
//    public void resizeMap() {
//        SwingUtilities.invokeLater(() -> { // 스윙 변경사항 기다리기
//            Dimension size = getSize(); // 현재 패널사이즈 가져오기
//            String script = String.format("resizeMap(%d, %d)", size.width, size.height);
////            System.out.println(script);
//            browser.mainFrame().ifPresent(frame -> {
//                frame.executeJavaScript(script); //자바스크립트 실행
//            });
//        });
//    }
//
//    /**
//     * 처음에 표시할 마커 로드
//     */
//    private void preProcessing() {
//        binList = utils.allBinSelector();
//
//        browser.navigation().on(LoadFinished.class, event -> {
//            resizeMap();
//            addMarkers();
//        });
//    }
//
//    /**
//     *  지도에 마커를 추가하는 함수 return -> void
//     * @param title 이름
//     * @param lat 위도
//     * @param lng 경도
//     * @param type 쓰레기통 타입
//     */
//    public void addMarker(String title, double lat, double lng, int type) {
//        String script = String.format("addMarker('%s', %f, %f, %d)", title, lat, lng, type);
//        browser.mainFrame().ifPresent(frame -> {
//            frame.executeJavaScript(script); //자바스크립트 실행
//        });
//    }
//
//    private void addMarkers() {
//        /*
//            처음 배열 받아서, 만들기용
//        */
//        for (Map<String, Object> binData : binList) {
////            System.out.println("Bin_Id: " + binData.get("bin_id") +
////                    ", Longitude: " + binData.get("longitude") +
////                    ", Latitude: " + binData.get("latitude") +
////                    ", Bin Type: " + binData.get("bin_type"));
//            addMarker(Integer.toString(
//                    (int)binData.get("bin_id")),
//                    (double)binData.get("latitude"),
//                    (double)binData.get("longitude"),
//                    Integer.parseInt((String)binData.get("bin_type")));
//        }
//    }
//    // 장소 검색
//    public void searchPlaces(String keyword) {
//        String script = String.format("searchPlaces('%s')", keyword.replace("'", "\\'")); // 안전한 문자열 처리
//        browser.mainFrame().ifPresent(frame -> frame.executeJavaScript(script));
//    }
//    // 쓰레기통 추가 모드 활성화
////    public void enableBinAddingMode() {
////        System.out.println("enableBinAddingMode");
////        browser.mainFrame().ifPresent(frame ->
////                frame.executeJavaScript("enableBinAddingMode();")
////        );
////    }
//    public void enableBinAddingMode() {
//        System.out.println("enableBinAddingMode() 호출");
//        browser.mainFrame().ifPresent(frame ->
//                frame.executeJavaScript("console.log('enableBinAddingMode 실행'); enableBinAddingMode();")
//        );
//    }
//
//    /**
//     *  좌표로 지도 중심 이동시키는 함수 return -> void
//     * @param lat double latitude 위도
//     * @param lng double longitude 경도
//     */
//    public void setCenter(double lat, double lng) {
//        String script = String.format("setCenter(%f, %f)", lat, lng);
//        browser.mainFrame().ifPresent(frame -> {
//            frame.executeJavaScript(script); //자바스크립트 실행
//        });
//    }
//
//
//
//    // 자바스크립트부터 자바로 데이터 받기
//    public final class JavaMarkerObject {
//        public MarkerEvent markerEvent;
//
//        @JsAccessible // 자바스크립트에서 호출
//        public void callJavaMarkerEvent(int index, String title, double lat, double lng, int type) { // 자바스크립트 호출때 사용할 이름
//            markerEvent = new MarkerEvent(index, title, lat, lng, type);
//
//            if (markerClickEventListener != null) { // 마커클릭이벤트가 등록되면
//                markerClickEventListener.markerClicked(markerEvent); // 마커 이벤트 전달 < 마커 클릭 이벤트 실행 여기서
//            }
//        }
//        @JsAccessible
//        public boolean isBinAddingMode() {
//            return isBinAddingMode;
//        }
//
//        @JsAccessible
//        public void addBin(double lat, double lng) {
//            SwingUtilities.invokeLater(() -> {
//                int type = JOptionPane.showOptionDialog(
//                        null,
//                        "추가할 쓰레기통의 타입을 선택하세요:",
//                        "쓰레기통 추가",
//                        JOptionPane.DEFAULT_OPTION,
//                        JOptionPane.QUESTION_MESSAGE,
//                        null,
//                        new String[]{"일반", "재활용"},
//                        "일반"
//                );
//
//                if (type == JOptionPane.CLOSED_OPTION) {
//                    JOptionPane.showMessageDialog(null, "추가가 취소되었습니다.");
//                    return;
//                }
//
//                // DB에 추가
//                int result = utils.addBinData(lat, lng, type);
//                if (result == 0) {
//                    JOptionPane.showMessageDialog(null, "쓰레기통이 성공적으로 추가되었습니다!");
//                    loadTrashBinData(); // 새로 추가된 쓰레기통을 지도에 표시
//                } else {
//                    JOptionPane.showMessageDialog(null, "쓰레기통 추가에 실패했습니다.");
//                }
//
//                // 추가 모드 종료
//                isBinAddingMode = false;
//            });
//        }
//    }
//
//
//}
//
//
//
