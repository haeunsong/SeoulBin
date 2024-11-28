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
import seoulbin.mapdata.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class MapPanel extends JPanel {
    private final Browser browser;
    private final Engine engine;
    private MarkerClickEventListener markerClickEventListener; // 마커 클릭 리스너 인터페이스
    private boolean isAddingBin = false;
    private boolean isHomeMode = false;

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
            // Home 위치 전달
            getHomeLocation();
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

    public void engineClose() {
        engine.close();
    }

    // ================  쓰레기통 추가  =================
    public void enableBinAddingMode() {
        isAddingBin = true;
        browser.mainFrame().ifPresent(frame -> frame.executeJavaScript("addNewBin()"));
    }

    // ================ 마커 찍기 모드 비활성화 =================
    public void disableBinAddingMode() {
        browser.mainFrame().ifPresent(frame -> frame.executeJavaScript("removeBinAddingMode()"));
        isAddingBin = false;  // 마커 추가 모드 비활성화
        System.out.println("쓰레기통 추가 모드가 종료되었습니다.");
    }

    // ================ 쓰레기통 삭제 ================
    public void deleteBin(int markerIndex) {
        int result = Utils.deleteBinData(markerIndex);

        if (result == 0) {
            System.out.println("쓰레기통이 정상적으로 삭제되었습니다.");
        } else if (result == -1) {
            System.out.println("쓰레기통이 정상적으로 삭제되지 않았습니다.");
        }
    }

    // ================ 지도 중심으로 이동 ================
    public void setCenter(double lat, double lng) {
        String script = String.format("setCenter(%f, %f)", lat, lng);
        browser.mainFrame().ifPresent(frame -> {
            frame.executeJavaScript(script); //자바스크립트 실행
        });
    }

    // ================ HOME 위치 받아오기 ================
    public void getHomeLocation() {
        HomeLocation home = Utils.getHomeLocation();

        if (home != null) {
            // 특수 문자 처리
            String safeAddress = home.getAddress().replace("'", "\\'").replace("\"", "\\\"");

            // JavaScript로 Home 위치 전달
            String script = String.format(
                    "setTimeout(() => { setHomeCenter(%f, %f); addHomeIcon(%f, %f, '%s'); }, 500);",
                    home.getLatitude(), home.getLongitude(),
                    home.getLatitude(), home.getLongitude(), safeAddress
            );

            browser.mainFrame().ifPresent(frame -> frame.executeJavaScript(script));
            System.out.println("Home 위치로 초기화되었습니다: " + home.getAddress());
        } else {
            System.out.println("Home 위치가 설정되지 않았습니다. 기본 위치를 사용합니다.");
        }
    }

    // HOME 설정
    public void enableHomeSettingMode() {
        isHomeMode = true;
        browser.mainFrame().ifPresent(frame -> frame.executeJavaScript("addHome()"));
    }

    //================== 쓰레기통 마커 초기화(클릭해체) ===================
    public void resetMarkerImage() {
        browser.mainFrame().ifPresent(frame -> frame.executeJavaScript("resetMarkerImage()"));
    }

    // ================ 맵 사이즈 재설정 ===================
    public void resizeMap() {
        SwingUtilities.invokeLater(() -> { // 스윙 변경사항 기다리기
            Dimension size = getSize(); // 현재 패널사이즈 가져오기
            String script = String.format("resizeMap(%d, %d)", size.width, size.height);
            browser.mainFrame().ifPresent(frame -> {
                frame.executeJavaScript(script); //자바스크립트 실행
            });
        });
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

        @JsAccessible // 자바스크립트에서 호출
        public void showAddBinDialog(double lat, double lng, String address) {
            // 기존 AddBtnAction을 호출하는 코드
            AddBtnAction addBtnAction = new AddBtnAction(lat, lng, address);  // AddBtnAction을 호출하면서 주소를 전달
        }

        @JsAccessible
        public void showAddHomeDialog(double lat, double lng, String address) {
            // 사용자 확인 다이얼로그
            int confirm = JOptionPane.showConfirmDialog(
                    null,
                    "현재 위치를 HOME으로 설정하시겠습니까?\n주소: " + address,
                    "HOME 설정",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                // Home 위치 업데이트 시도 - 항상 id 1 번에 저장된다.
                int result = Utils.updateHomeLocation(lat, lng, address);

                if (result > 0) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Home 위치가 성공적으로 저장되었습니다!",
                            "HOME 설정 완료",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    // Home 위치에 아이콘 추가
                    String safeAddress = address.replace("'", "\\'");
                    String script = String.format("addHomeIcon(%f, %f, '%s');", lat, lng, safeAddress);
                    browser.mainFrame().ifPresent(frame -> {
                        frame.executeJavaScript(script);
                    });
                    // Home 설정 모드 비활성화
                    isHomeMode = false;
                    browser.mainFrame().ifPresent(frame -> frame.executeJavaScript("document.body.style.cursor = 'default';"));
                    getHomeLocation();
                } else {
                    JOptionPane.showMessageDialog(
                            null,
                            "Home 위치 저장에 실패했습니다. 다시 시도해주세요.",
                            "HOME 설정 실패",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "HOME 설정이 취소되었습니다.",
                        "HOME 설정 취소",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        }

        @JsAccessible
        public double callJavaGetReview(int bin_id) {
            return Utils.selectBinReview(bin_id);
        }
    }


}
