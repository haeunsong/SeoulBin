// 마커 추가 함수
export function addMarker(title, lat, lng, type) {
    let imgSrc = 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_red.png';
    let imgSize = new kakao.maps.Size(64, 69);
    let imgOption = { offset: new kakao.maps.Point(27, 69) };

    let markerImage = new kakao.maps.MarkerImage(imgSrc, imgSize, imgOption);
    let markerPosition = new kakao.maps.LatLng(lat, lng);

    let marker = new kakao.maps.Marker({
        map: map,
        position: markerPosition,
        title: title,
        image: markerImage,
        type: type
    });

    let infowindow = new kakao.maps.InfoWindow({
        content: `<div style="padding:5px;">${type === 0 ? '일반 쓰레기통' : '재활용 쓰레기통'}</div>`,
        removable: true
    });

    kakao.maps.event.addListener(marker, 'click', function () {
        infowindow.open(map, marker);
    });

    markers.push(marker);
}