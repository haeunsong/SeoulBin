package seoulbin;

@FunctionalInterface
public interface MapClickEventListener {
    /**
     * lat과 lng을 갖는 return void 함수
     * @param lat 위도
     * @param lng 경도
     */
    public void mapClicked(double lat, double lng);
}