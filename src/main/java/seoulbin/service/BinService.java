package seoulbin.service;

import com.google.gson.Gson;
import seoulbin.browser.BrowserManager;
import seoulbin.utils.BinUtils;

import java.util.List;
import java.util.Map;

public class BinService {

    private final BrowserManager browserManager;
    private boolean isAddingBin = false;

    public BinService(BrowserManager browserManager) {
        this.browserManager = browserManager;
        this.isAddingBin = false;
    }

    // 전체 쓰레기통 위치 불러오기 + 마커 표시
    public void loadTrashBinData() {
        List<Map<String, Object>> binData = BinUtils.allBinSelector();
        String jsonData = new Gson().toJson(binData);
        browserManager.executeJavaScript(String.format("loadTrashBins(%s);", jsonData));
    }

    // 쓰레기통 삭제
    public void deleteBin(int markerIndex) {
        int result = BinUtils.deleteBinData(markerIndex);

        if (result == 0) {
//            System.out.println("쓰레기통이 정상적으로 삭제되었습니다.");
        } else if (result == -1) {
            System.out.println("쓰레기통이 정상적으로 삭제되지 않았습니다.");
        }
    }

    // 쓰레기통 추가 모드 활성화
    public void enableBinAddingMode() {
        isAddingBin = true;
        browserManager.executeJavaScript("addNewBin()");
    }

    // 쓰레기통 추가 모드 비활성화
    public void disableBinAddingMode() {
        browserManager.executeJavaScript("removeBinAddingMode()");
        isAddingBin = false;  // 마커 추가 모드 비활성화
        System.out.println("쓰레기통 추가 모드가 종료되었습니다.");
    }

    // 쓰레기통 위치 검색 (장소 검색)
    public void searchPlaces(String keyword) {
        browserManager.executeJavaScript(String.format("searchPlaces('%s');",keyword));
    }
}
