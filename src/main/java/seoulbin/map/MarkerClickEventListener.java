package seoulbin.map;

import seoulbin.model.MarkerEvent;

// 지도에 있는 마커 클릭 이벤트
@FunctionalInterface
public interface MarkerClickEventListener {
    public void markerClicked(MarkerEvent e);
}
