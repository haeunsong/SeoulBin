package seoulbin.utils;


import seoulbin.model.Model;
import seoulbin.map.HomeLocation;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinUtils {
    /*
    name: allBinSelector
    Param: None
    desc: DB에서 데이터를 조회하고, 조회된 데이터를 반환
    return: List<Map<String, Object>> 형식의 데이터 목록 반환
    */
    public static List<Map<String, Object>> allBinSelector() {
        List<Map<String, Object>> binList = new ArrayList<>();
        String query = "SELECT bin_id, longitude, latitude, bin_type, detail, city FROM binlist";

        try (Connection conn = DatabaseUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // ResultSet에서 데이터를 가져와서 List에 저장
            while (rs.next()) {
                Map<String, Object> binData = new HashMap<>();
                binData.put("bin_id", rs.getInt("bin_id"));
                binData.put("longitude", rs.getDouble("longitude"));
                binData.put("latitude", rs.getDouble("latitude"));
                binData.put("bin_type", rs.getString("bin_type"));
                binData.put("detail", rs.getString("detail"));
                binData.put("city", rs.getString("city"));
                binList.add(binData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return binList;
    }

    /*
 name: addBinData
 Param: double latitude, double longitude, int binType, String detail, String city
 desc: binList_tmp(임시 리스트) 테이블에 추가
 return: 0 if success, -1 if failure
 */
    public static int addBinData(double latitude, double longitude, int binType, String detail, String city, String imagePath) {

        String insertQuery = "INSERT INTO binlist (latitude, longitude, bin_type, detail, city) values (?,?,?,?,?)";

        if (Model.isBin(imagePath) == 1) {
            try (Connection conn = DatabaseUtils.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

                // PreparedStatement에 값 설정
                pstmt.setDouble(1, latitude);
                pstmt.setDouble(2, longitude);
                pstmt.setInt(3, binType);
                pstmt.setString(4, detail);
                pstmt.setString(5, city);

                // 쿼리 실행
                int rowsAffected = pstmt.executeUpdate();

                // 데이터 추가 성공 여부 확인
                if (rowsAffected > 0) {
                    System.out.println("쓰레기통이 정상적으로 추가되었습니다.");
                    return 0; // 성공
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return -1; // 실패
        } else {
            return -1;
        }
    }

    /*
    name: selectBinReview
    Param: int bin_id
    desc: binReview 검색
    return: value if success, -1 if failure
    */

    public static double selectBinReview(int bin_id) {
        String selectQuery = "SELECT AVG(review) as avg_review FROM binreview WHERE bin_id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {

            // PreparedStatement에 값 설정
            pstmt.setInt(1, bin_id);

            // 쿼리 실행 및 결과 가져오기
            try (ResultSet rs = pstmt.executeQuery()) {
                // 결과가 있으면 평균 review 값을 가져옴
                if (rs.next()) {
                    double avgReview = rs.getDouble("avg_review");
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

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            // PreparedStatement에 값 설정
            pstmt.setInt(1, bin_id);
            pstmt.setInt(2, review);

            // 쿼리 실행
            int rowsAffected = pstmt.executeUpdate();

            // 데이터 추가 성공 여부 확인
            if (rowsAffected > 0) {
                System.out.println("쓰레기통 리뷰가 정상적으로 등록되었습니다.");
                return 0; // 성공
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // 실패
    }

    /*
    name: updateBinReview
    Param: int bin_id, int review
    desc: binReview 테이블에 리뷰 수정
    return: 0 if success, -1 if failure
    */
    public static int updateBinReview(int bin_id, int review) {
        String updateQuery = "UPDATE binreview SET review = ? WHERE bin_id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            // PreparedStatement에 값 설정
            pstmt.setInt(1, review);
            pstmt.setInt(2, bin_id);

            // 쿼리 실행
            int rowsAffected = pstmt.executeUpdate();

            // 데이터 업데이트 성공 여부 확인
            if (rowsAffected > 0) {
                System.out.println("쓰레기통 리뷰가 정상적으로 등록되었습니다.");
                return 0; // 성공
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // 실패
    }

    /*
   name: hasExistingReview
   Param: int bin_id
   desc: binReview 테이블에 리뷰가 존재하는지 확인
   return: 존재하면 true, 존재하지 않으면 false
   */
    public static boolean hasExistingReview(int bin_id) {
        String selectQuery = "SELECT COUNT(*) FROM binreview WHERE bin_id = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {

            pstmt.setInt(1, bin_id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // 리뷰가 존재하면 true 반환
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // 리뷰가 없거나 오류 발생 시 false
    }


    /*
       name: deleteBinData
       Param: int bin_id
       desc: binList에서 Bin 삭제
       return: 0 if success, -1 if failure
   */
    public static int deleteBinData(int bin_id) {

        String deleteQuery = "DELETE FROM binList WHERE bin_id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {

            // PreparedStatement에 값 설정
            pstmt.setInt(1, bin_id);

            // 쿼리 실행
            int rowsAffected = pstmt.executeUpdate();

            // 데이터 추가 성공 여부 확인
            if (rowsAffected > 0) {
                System.out.println("쓰레기통이 정상적으로 삭제되었습니다.");
                return 0; // 성공
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // 실패
    }

    /*
    name: updateHomeLocation
    Param: double latitude, double longitude, String address
    desc: 사용자가 선택한 위도, 경도, 주소를 받아 HOME 의 위치와 정보를 수정 (항상 한 행만 업데이트 되는 방식)
    return: 1 if success, -1 if failure
    */
    public static int updateHomeLocation(double latitude, double longitude, String address) {
        String query = "UPDATE home " +
                "SET latitude = ?, longitude = ?, address = ? " +
                "WHERE id = 1";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setDouble(1, latitude);
            pstmt.setDouble(2, longitude);
            pstmt.setString(3, address);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // 실패
    }

    /*
   name: getHomeLocation
   Param: 없음
   desc: 사용자의 HOME 위치를 조회
   return: HomeLocation 객체 반환
   */
    public static HomeLocation getHomeLocation() {
        String getQuery = "SELECT latitude, longitude, address FROM home LIMIT 1";

        try (Connection conn = DatabaseUtils.getConnection(); // SQLite 연결
             PreparedStatement pstmt = conn.prepareStatement(getQuery);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                // Home 위치가 존재하면 DTO로 반환
                double latitude = rs.getDouble("latitude");
                double longitude = rs.getDouble("longitude");
                String address = rs.getString("address");

                return new HomeLocation(latitude, longitude, address);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Home 위치가 없을 경우 null 반환
    }
}
