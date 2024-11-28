package seoulbin.service;

import seoulbin.browser.BrowserManager;
import seoulbin.utils.BinUtils;

public class HomeService {

    private final BrowserManager browserManager;
    private boolean isHomeMode = false;

    public HomeService(BrowserManager browserManager) {
        this.browserManager = browserManager;
        this.isHomeMode = false;
    }

    // HOME 위치 뷸러오기
    public void loadHomeLocation() {
        var home = BinUtils.getHomeLocation();
        if (home != null) {
            // 특수 문자 처리
            String safeAddress = home.getAddress().replace("'", "\\'");
            browserManager.executeJavaScript(String.format(
                    "setTimeout(() => { setHomeCenter(%f, %f); addHomeIcon(%f, %f, '%s'); }, 500);",
                    home.getLatitude(), home.getLongitude(),
                    home.getLatitude(), home.getLongitude(), safeAddress
            ));
            System.out.println("Home 위치로 초기화되었습니다: " + home.getAddress());
        } else {
            System.out.println("Home 위치가 설정되지 않았습니다.");
        }
    }

    // Home 위치 업데이트
    public void updateHomeLocation(double lat, double lng, String address) {
        int result = BinUtils.updateHomeLocation(lat, lng, address);
        if (result > 0) {
            System.out.println("Home 위치가 성공적으로 업데이트되었습니다.");
        } else {
            System.out.println("Home 위치 업데이트에 실패했습니다.");
        }
    }

    // HOME 세팅 모드 활성화 및 HOME 추가하기
    public void enableHomeSettingMode() {
        if (!isHomeMode) {
            isHomeMode = true;
            browserManager.executeJavaScript("addHome()");
        }
    }

    // Home 세팅 모드 비활성화
    public void disableHomeMode() {
        if (isHomeMode) {
            isHomeMode = false;
        }
    }

}
