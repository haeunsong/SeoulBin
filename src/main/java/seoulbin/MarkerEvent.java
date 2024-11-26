package seoulbin;

/*
mapPanel의 자바스크립트 마커클릭 이벤트 발생 시
클릭한 정보를 전달하기 위한 클래스
*/

public class MarkerEvent {
    public Integer index;
    //public String title;
    public double lat;
    public double lng;
    public int type;

    public MarkerEvent(Integer index) {
        this.index = index;
    //    this.title = title;
    //    this.lat = lat;
    //    this.lng = lng;
    //    this.type = type;
    }

    public MarkerEvent(Integer index, double lat, double lng, int type) {
        this.index = index;
        this.lat = lat;
        this.lng = lng;
        this.type = type;
    }
}
