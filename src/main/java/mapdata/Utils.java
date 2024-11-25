package mapdata;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    // tester
    public static void tester() {
        System.out.println("test log");
    }

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:src/main/java/database/seoulbinstamp.sqlite3";
        return DriverManager.getConnection(url);
    }

    /*
    name: allBinSelector
    Param: None
    desc: DB에서 데이터를 조회하고, 조회된 데이터를 반환
    return: List<Map<String, Object>> 형식의 데이터 목록 반환
    */
    public static List<Map<String, Object>> allBinSelector() {
        List<Map<String, Object>> binList = new ArrayList<>();
        String query = "SELECT bin_id, longitude, latitude, bin_type FROM binlist";

        try (Connection conn = Utils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Map<String, Object> binData = new HashMap<>();
                binData.put("bin_id", rs.getInt("bin_id"));
                binData.put("longitude", rs.getDouble("longitude"));
                binData.put("latitude", rs.getDouble("latitude"));
                binData.put("bin_type", rs.getString("bin_type"));
                binList.add(binData);
            }

            System.out.println("Connection success");
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
        String insertQuery = "INSERT INTO binlist_tmp (latitude, longitude, bin_type) values (?,?,?)";


        try (Connection conn = Utils.getConnection();
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
        String selectQuery = "SELECT AVG(review) as avg_review FROM binreview WHERE bin_id = ?";

        try (Connection conn = Utils.getConnection();
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
        String insertQuery = "INSERT INTO binreview (bin_id, review) values (?,?)";

        try (Connection conn = Utils.getConnection();
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

    // 스탬프 이미지 불러오기
    public static String getImagePath(int imageId) {
        String selectQuery = "SELECT image_path FROM images WHERE id = ?";
        String path = null;

        try (Connection conn = Utils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {

            pstmt.setInt(1, imageId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                path = rs.getString("image_path");
                System.out.println("Image Path: " + path);
            } else {
                System.out.println("No data found for imageId: " + imageId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return path;
    }

    // 스탬프 상태 저장
    public static void saveStamp(String date, boolean completed) {
        String insertQuery = "INSERT INTO stamps (date, completed) VALUES (?, ?)";
        try (Connection conn = Utils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            // PreparedStatement에 값 설정
            pstmt.setString(1, date);
            pstmt.setInt(2, completed ? 1 : 0);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 각 조각 퍼즐 진행 업데이트
    public static void updateProgress(int imageId, int pieceId, boolean completed) {
        String updateQuery = "UPDATE progress SET status = ? WHERE image_id = ? AND piece_id = ?";
        try (Connection conn = Utils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            // status : 1 = 열린 상태, status : 0 = 닫힌 상태
            /*
            만약, (3,2) (4,1) 이 저장되어있다면...
            INSERT INTO progress (image_id, piece_id, status) VALUES (1, 12, 1); -- (3,2)
            INSERT INTO progress (image_id, piece_id, status) VALUES (1, 16, 1); -- (4,1)

             */

            pstmt.setInt(1, completed ? 1 : 0);
            pstmt.setInt(2, imageId);
            pstmt.setInt(3, pieceId);
            pstmt.executeUpdate();

            System.out.println("Progress updated for imageId: " + imageId + ", pieceId: " + pieceId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 처음에 스탬프 판 받아오기
    public static List<Integer> getOpenedPieces(int imageId) {
        String sql = "SELECT piece_id FROM progress WHERE image_id = ? AND status = 1";
        List<Integer> openedPieces = new ArrayList<>();

        try (Connection conn = Utils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, imageId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                openedPieces.add(rs.getInt("piece_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return openedPieces;
    }

    // 조각을 클릭하면 스탬프 판에 업데이트 하기
    // picedId : 사용자가 클릭한 조각
    public void openPiece(int pieceId, int currentImageId, JButton[] puzzleButtons ) {
        String sql = "UPDATE progress SET status = 1 WHERE image_id = ? AND piece_id = ?";
        try (Connection conn = Utils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentImageId);
            pstmt.setInt(2, pieceId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
