package seoulbin.map;

import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.browser.callback.InjectJsCallback;
import com.teamdev.jxbrowser.js.JsAccessible;
import com.teamdev.jxbrowser.js.JsObject;
import com.teamdev.jxbrowser.navigation.event.*;
import com.teamdev.jxbrowser.view.swing.BrowserView;
import seoulbin.browser.BrowserManager;
import seoulbin.model.MarkerEvent;
import seoulbin.service.BinService;
import seoulbin.service.HomeService;
import seoulbin.utils.BinUtils;

import javax.swing.*;
import java.awt.*;

public class MapPanel extends JPanel {
    private final BrowserManager browserManager;
    private final Browser browser;
    private final BinService binService;
    private final HomeService homeService;

    private MarkerClickEventListener markerClickEventListener; // 마커 클릭 리스너 인터페이스

    public MapPanel(BinService binService, HomeService homeService, BrowserManager browserManager) {
        this.binService = binService;
        this.homeService = homeService;
        this.browserManager = browserManager;
        this.browser = browserManager.getBrowser();

        // JxBrowser UI 설정
        BrowserView view = BrowserView.newInstance(browserManager.getBrowser());
        setLayout(new BorderLayout());
        add(view, BorderLayout.CENTER);

        // HTML 페이지 로드
        browser.navigation().loadUrl("localhost:8088/map");

        // 브라우저 로드 완료 후 작업
        browser.navigation().on(LoadFinished.class, event -> {
            // 지도 초기화 (JavaScript에서 실행)
            browserManager.executeJavaScript("initMap();");
            // 데이터 로드 및 마커 표시
            binService.loadTrashBinData(); // Java에서 데이터 읽고 JavaScript로 전달
            // Home 위치 전달
            homeService.loadHomeLocation();
        });

        // JavaScript Callback Interface 주입
        browser.set(InjectJsCallback.class, params -> {
            JsObject window = params.frame().executeJavaScript("window");
            window.putProperty("java", new JavaMarkerObject());
            return InjectJsCallback.Response.proceed();
        });
    }

    // 마커 클릭 이벤트 인터페이스 구현
    public void addMarkerClickEventListener(MarkerClickEventListener markerClickEventListener) {
        this.markerClickEventListener = markerClickEventListener;
    }

    // ================ 지도 중심으로 이동 ================
    public void setCenter(double lat, double lng) {
        String script = String.format("setCenter(%f, %f)", lat, lng);
        browser.mainFrame().ifPresent(frame -> {
            frame.executeJavaScript(script); //자바스크립트 실행
        });
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
            BinAddDialog binAddDialog = new BinAddDialog(lat, lng, address);  // AddBtnAction을 호출하면서 주소를 전달
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
                int result = BinUtils.updateHomeLocation(lat, lng, address);

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
                    homeService.disableHomeMode();
                    browser.mainFrame().ifPresent(frame -> frame.executeJavaScript("document.body.style.cursor = 'default';"));
                    homeService.loadHomeLocation();
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
            return BinUtils.selectBinReview(bin_id);
        }
    }
}
