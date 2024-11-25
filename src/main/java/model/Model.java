package model;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Model {

    private static final String API_URL = "https://detect.roboflow.com/bin-kvnls/3";
    private static final String API_KEY = "BaU7pMgNrCLqNMa76tDs";

    public static int isBin(String imagePath) {

        try {
            // 이미지 업로드 및 예측 요청
            String response = uploadAndPredict(imagePath);

            // 응답에서 결과 출력 (1 또는 0)
            int result = parseResponse(response);
            return result;

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Roboflow API에 이미지를 업로드하고 예측 결과를 가져옵니다.
     */
    private static String uploadAndPredict(String imagePath) throws IOException {
        // API URL 설정
        String apiUrl = API_URL + "?api_key=" + API_KEY;

        // HTTP 클라이언트 생성
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // POST 요청 생성
            HttpPost postRequest = new HttpPost(apiUrl);

            // 이미지 파일 추가
            File imageFile = new File(imagePath);
            FileBody fileBody = new FileBody(imageFile);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addPart("file", fileBody);
            postRequest.setEntity(builder.build());

            // 요청 실행
            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                // 응답 처리
                return new String(response.getEntity().getContent().readAllBytes());
            }
        }
    }

    /**
     * Roboflow API의 JSON 응답에서 class가 'bin'인지 확인하여 1 또는 0을 반환합니다.
     */
    private static int parseResponse(String response) {
        // 간단한 JSON 파싱
        if (response.contains("\"class\":\"bin\"")) {
            return 1; // class가 'bin'인 경우
        } else {
            return 0; // class가 'bin'이 아닌 경우
        }
    }
}
