let map;
let places;
let markers = [];
let searchMarkers = [];

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
    console.log("loadTrashBins() 호출됨");

    // 기존 마커 초기화
    markers.forEach(marker => marker.setMap(null));
    markers = [];

    // 마커 이미지 설정
    // const imageSrc = bin.type === 0
    //     ? 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_red.png' // 일반 쓰레기통
    //     : 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_blue.png'; // 재활용 쓰레기통
    // const imageSize = new kakao.maps.Size(36, 36); // 마커 이미지 크기
    // const imageOption = { offset: new kakao.maps.Point(18, 36) }; // 마커 중심 좌표 설정
    //
    // const markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize, imageOption);

    // 새로운 마커 추가
    data.forEach(bin => {
        let type = bin.bin_type;
        const markerPosition = new kakao.maps.LatLng(bin.latitude, bin.longitude);
        const marker = new kakao.maps.Marker({
            map: map,
            position: markerPosition,
            title: bin.title,
           // image: markerImage
        });
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

// // 3. 마커 추가 함수
// function addMarker(title, lat, lng, type) {
//     // let imgSrc = 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_red.png';
//     // let imgSize = new kakao.maps.Size(64, 69);
//     // let imgOption = {offset: new kakao.maps.Point(27, 69)};
//
//   //  let markerImage = new kakao.maps.MarkerImage(imgSrc, imgSize, imgOption);
//     let markerPosition = new kakao.maps.LatLng(lat, lng);
//
//     console.log(map);
//     let marker = new kakao.maps.Marker({
//         position: markerPosition,
//         map: map,
//         title: title,
//         //  image: markerImage,
//         type: type
//     });
//
//     let infowindow = new kakao.maps.InfoWindow({
//         content: `<div style="padding:5px;">${type === 0 ? '일반 쓰레기통' : '재활용 쓰레기통'}</div>`,
//         removable: true
//     });
//
//     kakao.maps.event.addListener(marker, 'click', function () {
//         infowindow.open(map, marker);
//     });
//
//     markers.push(marker);
// }

// 3. 장소 검색
function searchPlaces(keyword) {
    // places.keywordSearch(keyword, function (result, status) {
    //     if (status === kakao.maps.services.Status.OK) {
    //         let place = result[0];
    //         let lat = place.y;
    //         let lng = place.x;
    //         let name = place.place_name;
    //
    //         console.log("검색 결과:", name, lat, lng);
    //
    //         let position = new kakao.maps.LatLng(lat, lng);
    //         map.setCenter(position);
    //         addMarker(name, lat, lng, null); // 검색된 장소는 type을 설정하지 않음
    //     } else if (status === kakao.maps.services.Status.ZERO_RESULT) {
    //         alert("검색 결과가 없습니다.");
    //     } else if (status === kakao.maps.services.Status.ERROR) {
    //         alert("검색 중 오류가 발생했습니다.");
    //     }
    // });

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
            console.log("검색 결과:", name, lat, lng);

            // let position = new kakao.maps.LatLng(lat, lng);
            // map.setCenter(position);
            // addMarker(name, lat, lng, null); // 검색된 장소는 type을 설정하지 않음
            // if (status === kakao.maps.services.Status.OK) {
            //     console.log("Search results:", result);
            //     // 검색 결과를 지도에 표시하는 로직 추가
            //     result.forEach(place => {
            //         const marker = new kakao.maps.Marker({
            //             position: new kakao.maps.LatLng(place.y, place.x),
            //             map: map
            //         });
            //         // 검색 마커 클릭 이벤트 추가
            //         kakao.maps.event.addListener(marker, 'click', () => {
            //             const info = `<div style="padding:5px;">${place.place_name}</div>`;
            //             const infowindow = new kakao.maps.InfoWindow({ content: info });
            //             infowindow.open(map, marker);
            //         });
            //
            //         searchMarkers.push(marker);
            //     });
            //     // 첫 번째 검색 결과로 지도 중심 이동
            //     if (result.length > 0) {
            //         const firstPlace = result[0];
            //         map.setCenter(new kakao.maps.LatLng(firstPlace.y, firstPlace.x));
            //     }
        } else {
            console.log("Search failed with status:", status);
        }
    });
}

function clearMarkers() {
    searchMarkers.forEach(marker => marker.setMap(null)); // 지도에서 제거
    searchMarkers = []; // 배열 초기화
}

document.addEventListener("DOMContentLoaded", initMap);


// ================= 맵 초기화 ========================


// 마커 필터링 함수
function filterMarkers(type) {
    markers.forEach(marker => {
        if (type === 'all' || marker.type === type) {
            marker.setMap(map); // 지도에 표시
        } else {
            marker.setMap(null); // 지도에서 제거
        }
    });

    // 카테고리 버튼 스타일 업데이트
    document.querySelectorAll('.category li').forEach(li => li.classList.remove('active'));
    document.getElementById(type === 'all' ? 'all' : type === 0 ? 'general' : 'recycle').classList.add('active');
}


// // 지도 클릭 이벤트
// kakao.maps.event.addListener(map, 'click', function (mouseEvent) {
//     if (isBinAddingMode) {
//         let lat = mouseEvent.latLng.getLat();
//         let lng = mouseEvent.latLng.getLng();
//         console.log("Clicked location:", lat, lng);
//
//         // Java 객체와의 통신
//         window.java.addBin(lat, lng);
//
//         isBinAddingMode = false; // 모드 종료
//     }
// });



/// / 페이지 로드 후 초기화
// window.onload = init;


