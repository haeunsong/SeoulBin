let map;
let places;
let markers = [];
let searchMarkers = [];
let isMarkerClickEnable = true; // 마커 클릭 이벤트 활성화 여부
let isMapClickEnabel = false; // 맵 클릭 이벤트 활성화 여부
var clickedMarker = null; // 마커를 하나만 클릭하기 위함

// 1. 맵 초기화
function initMap() {
    const mapContainer = document.getElementById('map');
    const mapOptions = {
        center: new kakao.maps.LatLng(37.4878925, 126.8252908),
        level: 5
    };
    map = new kakao.maps.Map(mapContainer, mapOptions);
    map.setLevel(5);
    places = new kakao.maps.services.Places(map);

    console.log("intiMap() 호출됨");
}

// 현재 위치 가져오기
function getCurrentLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function (position) {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;

            // 현재 위치 마커 추가
            const currentMarker = new kakao.maps.Marker({
                map: map,
                position: new kakao.maps.LatLng(lat, lng),
                title: "현재 위치",
            });

            // 지도 중심 이동
            map.setCenter(new kakao.maps.LatLng(lat, lng));

            // 인포윈도우 표시
            const infowindow = new kakao.maps.InfoWindow({
                content: '<div style="padding:5px;">현재 위치</div>'
            });
            infowindow.open(map, currentMarker);

            console.log("현재 위치:", lat, lng);
        }, function (error) {
            alert("위치 정보를 가져올 수 없습니다. 오류 코드: " + error.code);
        });
    } else {
        alert("Geolocation을 지원하지 않는 브라우저입니다.");
    }
}

// 2. 쓰레기통 데이터 로드 및 마커 표시
// {"bin_id":1,"bin_type":"0","city":"종로구","latitude":37.57432075,"detail":"경복궁역 4번출구\r","longitude":126.9671281},
// {"bin_id":2,"bin_type":"1","city":"종로구","latitude":37.57432075,"detail":"경복궁역 4번출구\r","longitude":126.9671281}
function loadTrashBins(data) {
    if (!map) {
        console.error("Map is not initialized.");
        return;
    }
    // 기존 마커 초기화
    markers.forEach(marker => marker.setMap(null));
    markers = [];
    // 클릭된 마커 초기화
    clickedMarker = null;

    // // 새로운 마커 추가
    data.forEach(bin => {
        let type = bin.bin_type;

        // data 가 배열이라서 무조건 순회하면서 값 추출해야함!
        const imageSrc = type === "0"
            ? 'general.png' // 일반 쓰레기통
            : 'recycle.png'; // 재활용 쓰레기통
        const imageSize = new kakao.maps.Size(36, 36); // 마커 이미지 크기
        const imageOption = {offset: new kakao.maps.Point(18, 36)}; // 마커 중심 좌표 설정
        const markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize, imageOption);

        const markerPosition = new kakao.maps.LatLng(bin.latitude, bin.longitude);
        var marker = new kakao.maps.Marker({
            map: map,
            position: markerPosition,
            title: bin.detail,
            image: markerImage
        });
        // maker 에 type 추가 - 필터링 시 이용
        marker.type = type === "0" ? "general" : "recycle";

        const infowindow = new kakao.maps.InfoWindow({
            content: `
        <div style="padding:5px; width:180px; font-family:Arial, sans-serif;">
            <div style="font-size:13px; color:gray; margin-bottom:5px;">
                ${type === "0" ? '일반쓰레기' : '재활용쓰레기'}
            </div>
            <div style="font-size:17px; color:black;">
                ${bin.detail}
            </div>
        </div>
    `
        });


        // 마우스를 올렸을 때 정보창 열기
        kakao.maps.event.addListener(marker, 'mouseover', () => {
            infowindow.open(map, marker);
        });

        // 마우스를 뗐을 때 정보창 닫기
        kakao.maps.event.addListener(marker, 'mouseout', () => {
            infowindow.close();
        });

        kakao.maps.event.addListener(marker, 'click', () => {
            // 마커 클릭한 부분이 이전에 클릭한 마커라면
            if (clickedMarker === marker) {
                marker.setImage(new kakao.maps.MarkerImage(imageSrc, imageSize, imageOption)); // 원래 이미지로 돌아가기
                clickedMarker = null; // 클릭마커 초기화
                isMarkerClickEnable = false; // 원본이미지로 바뀌면 null 보내기 (체크가 빠지면)
            } else {
                if (clickedMarker) { // 이전에 클릭한 마커가 있는 데, 다른 마커를 클릭한다면
                    clickedMarker.setImage(new kakao.maps.MarkerImage(clickedMarker.type === "general" ? 'general.png': 'recycle.png',
                         imageSize, imageOption)); // 이전에 클릭한 마커는 원본 이미지로 돌아가기
                }

                clickedMarker = marker; // 클릭한 마커를 클릭마커에 주소복사
                marker.setImage(new kakao.maps.MarkerImage(marker.type === "general" ? 'generalCheck.png': 'recycleCheck.png',
                     imageSize, imageOption)); // 클릭이미지로 바꾸기
                isMarkerClickEnable = true; // 클릭이미지로 바뀌고 > 그 bin_id 보내기
            }

            if (isMarkerClickEnable===true) {
                //window.java.callJavaMarkerEvent(parseInt(bin.bin_id));
				window.java.callJavaMarkerEvent(parseInt(bin.bin_id), parseFloat(bin.latitude), parseFloat(bin.longitude), parseInt(bin.bin_type));
            } else if (isMarkerClickEnable === false) {
                window.java.callJavaMarkerEvent(null);
            }
        });

        markers.push(marker);
    });

    // filter 초기화 >> 삭제 후 재 로딩할 때, 필터도 초기화 시키기 위함
    filterMarkers('all');
    console.log("All markers added to the map.");
}

// 3. 장소 검색
function searchPlaces(keyword) {

    clearMarkers();

    places.keywordSearch(keyword, (result, status) => {
        if (status === kakao.maps.services.Status.OK) {
            let place = result[0];
            let lat = place.y;
            let lng = place.x;
            let name = place.place_name;

            const marker = new kakao.maps.Marker({
                position: new kakao.maps.LatLng(lat, lng),
                map: map
            });
            searchMarkers.push(marker);
            map.setCenter(new kakao.maps.LatLng(lat, lng));
            map.level()

        } else {
            console.log("Search failed with status:", status);
        }
    });
}

function clearMarkers() {
    searchMarkers.forEach(marker => marker.setMap(null)); // 지도에서 제거
    searchMarkers = []; // 배열 초기화
}

// 마커 필터링 함수
function filterMarkers(type) {
    markers.forEach(marker => {
        console.log(map);
        if (type === 'all') {
            marker.setMap(map); // 지도에 표시
        } else if (marker.type === type) {
            marker.setMap(map);
        } else {
            marker.setMap(null);
        }
    });

    // 카테고리 버튼 스타일 업데이트
    document.querySelectorAll('.category li').forEach(li => li.classList.remove('active'));
    document.getElementById(type === 'all' ? 'all' : type === 'general' ? 'general' : 'recycle').classList.add('active');
}

// 마커 체크마커 이미지 원래대로 돌리기
function resetMarkerImage() {
    clickedMarker.setImage(new kakao.maps.MarkerImage(clickedMarker.type === "general" ? 'general.png': 'recycle.png',
         new kakao.maps.Size(36, 36), { offset: new kakao.maps.Point(18, 36) }));
    clickedMarker = null;
}

		
// ================= 맵 초기화 ========================
document.addEventListener("DOMContentLoaded", initMap);

