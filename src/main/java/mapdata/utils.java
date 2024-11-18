package mapdata;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class utils {
    private static Connection connection;
    // tester
    public static void tester() {
        System.out.println("test log");
    }

    /*
    name: allBinSelector
    Param: None
    desc: DB에서 데이터를 조회하고, 조회된 데이터를 반환
    return: List<Map<String, Object>> 형식의 데이터 목록 반환
    */

    public static List<Map<String, Object>> allBinSelector() {
        List<Map<String, Object>> binList = new ArrayList<>();
        try {
            String url = "jdbc:sqlite:src/main/java/database/seoulbin.sqlite3";
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT bin_id, longitude, latitude, bin_type FROM binlist");

            // ResultSet에서 데이터를 가져와서 List에 저장
            while (rs.next()) {
                Map<String, Object> binData = new HashMap<>();
                binData.put("bin_id", rs.getInt("bin_id"));
                binData.put("longitude", rs.getDouble("longitude"));
                binData.put("latitude", rs.getDouble("latitude"));
                binData.put("bin_type", rs.getString("bin_type"));
                binList.add(binData);
            }

            System.out.println("connection success"); //connection success log

            // 자원 해제
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return binList;
    }

    /*
    name: allBinReturn
    Param: None
    desc: allBinSelector 메서드 호출 후 데이터를 확인하고 반환
    return: 0 if success, -1 if failure
    */

    public static int allBinReturn() {
        List<Map<String, Object>> binDataList = allBinSelector();

        if (binDataList.isEmpty()) {
            System.out.println("No data found or failed to retrieve data");
            return -1; // 실패
        }

        // 데이터 출력 (테스트 용도)
        for (Map<String, Object> binData : binDataList) {
            System.out.println("Bin_Id: " + binData.get("bin_id") +
                    "Longitude: " + binData.get("longitude") +
                    ", Latitude: " + binData.get("latitude") +
                    ", Bin Type: " + binData.get("bin_type"));
        }
        return 0; // 성공
    }

    /*
    name: addBinData
    Param: double latitude, double longitude, int binType
    desc: binList_tmp(임시 리스트) 테이블에 추가
    return: 0 if success, -1 if failure
    */

    public static int addBinData(double latitude, double longitude, int binType) {
        String url = "jdbc:sqlite:src/main/java/database/seoulbin.sqlite3";
        String insertQuery = "INSERT INTO binlist_tmp (latitude, longitude, bin_type) values (?,?,?)";


        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            // PreparedStatement에 값 설정
            pstmt.setDouble(1, latitude);
            pstmt.setDouble(2, longitude);
            pstmt.setInt(3, binType);

            // 쿼리 실행
            int rowsAffected = pstmt.executeUpdate();

            // 데이터 추가 성공 여부 확인
            if (rowsAffected > 0) {
                System.out.println("Data inserted successfully.");
                return 0; // 성공
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // 실패
    }

    /*
    name: selectBinReview
    Param: int bin_id
    desc: binReview 검색
    return: value if success, -1 if failure
    */

    public static double selectBinReview(int bin_id) {
        String url = "jdbc:sqlite:src/main/java/database/seoulbin.sqlite3";
        String selectQuery = "SELECT AVG(review) as avg_review FROM binreview WHERE bin_id = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {

            // PreparedStatement에 값 설정
            pstmt.setInt(1, bin_id);

            // 쿼리 실행 및 결과 가져오기
            try (ResultSet rs = pstmt.executeQuery()) {
                // 결과가 있으면 평균 review 값을 가져옴
                if (rs.next()) {
                    double avgReview = rs.getDouble("avg_review");
                    System.out.println("Average review: " + avgReview);
                    return avgReview; // 평균값 반환
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // 실패 시 -1 반환
    }


    /*
    name: addBinReview
    Param: int bin_id, int review
    desc: binReview 테이블에 리뷰 추가
    return: 0 if success, -1 if failure
    */

    public static int addBinReview(int bin_id, int review) {
        String url = "jdbc:sqlite:src/main/java/database/seoulbin.sqlite3";
        String insertQuery = "INSERT INTO binreview (bin_id, review) values (?,?)";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            // PreparedStatement에 값 설정
            pstmt.setInt(1, bin_id);
            pstmt.setInt(2, review);

            // 쿼리 실행
            int rowsAffected = pstmt.executeUpdate();

            // 데이터 추가 성공 여부 확인
            if (rowsAffected > 0) {
                System.out.println("Data inserted successfully.");
                return 0; // 성공
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // 실패
    }
}
