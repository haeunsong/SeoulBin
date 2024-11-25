let map;
let places;
let markers = [];
let searchMarkers = [];
let addMarker=null;
let infowindowAdd = null;
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

// 2. 쓰레기통 데이터 로드 및 마커 표시
// {"bin_id":3437,"bin_type":"1","latitude":37.48328556,"longitude":126.8789442}
// {"bin_id":3642,"bin_type":"0","latitude":37.4507049,"longitude":126.9085555}
function loadTrashBins(data) {
    if (!map) {
        console.error("Map is not initialized.");
        return;
    }
    // 기존 마커 초기화
    markers.forEach(marker => marker.setMap(null));
    markers = [];

    // // 새로운 마커 추가
    data.forEach(bin => {
        let type = bin.bin_type;

        // data 가 배열이라서 무조건 순회하면서 값 추출해야함!
        const imageSrc = type === "0"
            ? 'general.png' // 일반 쓰레기통
            : 'recycle.png'; // 재활용 쓰레기통
        const imageSize = new kakao.maps.Size(36, 36); // 마커 이미지 크기
        const imageOption = { offset: new kakao.maps.Point(18, 36) }; // 마커 중심 좌표 설정
        const markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize, imageOption);

        const markerPosition = new kakao.maps.LatLng(bin.latitude, bin.longitude);
        const marker = new kakao.maps.Marker({
            map: map,
            position: markerPosition,
            title: bin.title,
            image: markerImage
        });
        // maker 에 type 추가 - 필터링 시 이용
        marker.type = type === "0" ? "general" : "recycle";
        const infowindow = new kakao.maps.InfoWindow({
            content: `<div style="padding:5px;">${(type === "0" ? '일반쓰레기' : '재활용쓰레기')}</div>`
        });

        // 마우스를 올렸을 때 정보창 열기
        kakao.maps.event.addListener(marker, 'mouseover', () => {
            infowindow.open(map, marker);
        });

        // 마우스를 뗐을 때 정보창 닫기
        kakao.maps.event.addListener(marker, 'mouseout', () => {
            infowindow.close();
        });
        markers.push(marker);
    });

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
                position: new kakao.maps.LatLng(lat,lng),
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
        } else if(marker.type === type) {
            marker.setMap(map);
        }else {
            marker.setMap(null);
        }
    });

    // 카테고리 버튼 스타일 업데이트
    document.querySelectorAll('.category li').forEach(li => li.classList.remove('active'));
    document.getElementById(type === 'all' ? 'all' : type === 'general' ? 'general' : 'recycle').classList.add('active');
}

// 맵 사이즈 변경
function resizeMap(width, height) {
        const div = document.getElementById('map');
        mapWidth = width - 19;
        mapHeight = height - 19;
        div.style.width = mapWidth + 'px';
        div.style.height = mapHeight + 'px';
}

// 새로운 쓰레기통 마커 추가 모드 활성화
function addNewBin() {
    if (isPinModeActive) return;  // 이미 핀 추가 모드가 활성화 되어 있으면 함수 종료

    // 마우스 포인터를 핀 모양으로 변경
    document.body.style.cursor = "url('pin-pointer.png'), auto";  // 핀 포인터 이미지 경로 설정

    // 지도 클릭 이벤트 처리: 마커 추가 모드 활성화
    kakao.maps.event.addListener(map, 'click', onMapClick);

    // 핀 추가 모드 활성화
    isPinModeActive = true;
}

function onMapClick(event) {
    const lat = event.latLng.getLat();
    const lng = event.latLng.getLng();

    // 카카오맵 Geocoder 인스턴스 생성
    const geocoder = new kakao.maps.services.Geocoder();

    // 위도와 경도로 주소를 가져오는 함수
    geocoder.coord2Address(lng, lat, (result, status) => {
        if (status === kakao.maps.services.Status.OK) {
            const address = result[0].address.address_name; // 첫 번째 주소를 가져옴

            // 인포윈도우 내용 설정 (주소와 추가 버튼 포함)
            infowindowAdd = new kakao.maps.InfoWindow({
                content: `
                    <div style="padding:5px;">
                        <p>${address}
                        <button style=""type="button" onclick="addTrashBin()">추가</button>
						</p>
                    </div>`,
				removable : true // removeable 속성을 ture 로 설정하면 인포윈도우를 닫을 수 있는 x버튼이 표시됩니다
			});

            // 클릭한 위치에 마커 추가
			if (addMarker) {
				if(infowindowAdd.getMap()){
					infowindowAdd.close();  // 이미 열려 있으면 닫기
				}
				addMarker.setMap(null);  // 이전 마커 제거
			}

            addMarker = new kakao.maps.Marker({
                position: new kakao.maps.LatLng(lat, lng),
                map: map,
                icon: {
                    url: 'pin-pointer.png',  // 핀 이미지 URL
                    size: new kakao.maps.Size(30, 30),
                    offset: new kakao.maps.Point(15, 30)  // 마커 기준점
                }
            });

            // 마커 위치로 지도 중심 이동
            map.setCenter(new kakao.maps.LatLng(lat, lng));

			kakao.maps.event.addListener(addMarker, 'click', function () {
			// 인포윈도우가 이미 열려 있으면 닫기, 열려 있지 않으면 열기
				if (infowindowAdd.getMap()) {
					infowindowAdd.close();  // 이미 열려 있으면 닫기
				} else {
					infowindowAdd.open(map, addMarker);  // 열려 있지 않으면 열기
				}
			});
            // 마우스 포인터를 기본 상태로 변경
            document.body.style.cursor = "default";
        } else {
            console.log("주소 변환 실패");
        }
    });

    // 핀 추가 모드 비활성화
    isPinModeActive = false;
}

// 핀 찍기 모드 종료
function removeBinAddingMode() {
    // 마우스 포인터를 기본 상태로 변경
    document.body.style.cursor = "default";

    // 지도 클릭 이벤트 리스너 제거
    kakao.maps.event.removeListener(map, 'click', onMapClick);

    // 추가된 마커를 숨길 수 있다면, 추가한 마커를 제거하는 로직을 추가할 수 있음
	if (addMarker) {
		if (infowindowAdd.getMap()) {
			infowindowAdd.close(); // 이전 인포윈도우 닫기
		}
		addMarker.setMap(null);  // 이전 마커 제거
		}

    // 핀 추가 모드 비활성화
    isPinModeActive = false;
}

// ================= 맵 초기화 ========================
document.addEventListener("DOMContentLoaded", initMap);

