let map;
let places;
let markers = [];
let searchMarkers = [];
let isMarkerClickEnable = true; // 마커 클릭 이벤트 활성화 여부
let isMapClickEnabel = false; // 맵 클릭 이벤트 활성화 여부
var clickedMarker = null; // 마커를 하나만 클릭하기 위함
let addMarker = null;
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
		const imageOption = { offset: new kakao.maps.Point(18, 36) }; // 마커 중심 좌표 설정
		const markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize, imageOption);


		const markerPosition = new kakao.maps.LatLng(bin.latitude, bin.longitude);
		var marker = new kakao.maps.Marker({
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
                window.java.callJavaMarkerEvent(parseInt(bin.bin_id));
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
        } else if(marker.type === type) {
            marker.setMap(map);
        } else {
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
			address = result[0].address.address_name; // 첫 번째 주소를 가져옴

			// 인포윈도우 내용 설정 (주소와 추가 버튼 포함)
			infowindowAdd = new kakao.maps.InfoWindow({
				content: `
                    <div style="padding:5px;">
                        <p>주소:${address}</p>
                    </div>`,
				//removable: true // removeable 속성을 ture 로 설정하면 인포윈도우를 닫을 수 있는 x버튼이 표시됩니다
			});

			// 클릭한 위치에 마커 추가
			if (addMarker) {
				/*if (infowindowAdd.getMap()) {
					infowindowAdd.close();  // 이미 열려 있으면 닫기
				}*/
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
			
			
			// 마우스를 올렸을 때 정보창 열기
			kakao.maps.event.addListener(addMarker, 'mouseover', () => {
				infowindowAdd.open(map, addMarker);
			});

			// 마우스를 뗐을 때 정보창 닫기
			kakao.maps.event.addListener(addMarker, 'mouseout', () => {
				infowindowAdd.close();
			});
			
			kakao.maps.event.addListener(addMarker, 'click', () => {
				window.java.showAddBinDialog(lat, lng, address);  // Java 메서드 호출
			})
			/*kakao.maps.event.addListener(addMarker, 'click', function() { // 마커를 클릭해서 실행하려고 했던 것
				// 인포윈도우가 이미 열려 있으면 닫기, 열려 있지 않으면 열기
				if (infowindowAdd.getMap()) {
					infowindowAdd.close();  // 이미 열려 있으면 닫기
				} else {
					infowindowAdd.open(map, addMarker);  // 열려 있지 않으면 열기
				}
			});*/
			// 마우스 포인터를 기본 상태로 변경
			document.body.style.cursor = "default";
			// Java로 주소 전달 (JxBrowser 사용)
			//window.java.showAddBinDialog(lat, lng, address);  // Java 메서드 호출
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
		/*if (infowindowAdd.getMap()) {
			infowindowAdd.close(); // 이전 인포윈도우 닫기
		}*/
		addMarker.setMap(null);  // 이전 마커 제거
	}
	// 핀 추가 모드 비활성화
	isPinModeActive = false;
}

/*function addTrashBin(lat, lng, address) {
	// Java에서 등록된 javaApp.showAddBinDialog 메서드를 호출
	if (window.java) {
		// 예시로 주소를 전달
		window.java.showAddBinDialog(lat, lng, address);  // Java 메서드 호출
	} else {
		alert("Java 객체를 찾을 수 없습니다.");
	}
}*/

// ================= 맵 초기화 ========================
document.addEventListener("DOMContentLoaded", initMap);

