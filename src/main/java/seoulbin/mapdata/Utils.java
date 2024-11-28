package seoulbin.mapdata;

import seoulbin.model.Model;
import seoulbin.HomeLocation;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:src/main/java/seoulbin.database/seoulbin.sqlite3";
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
        String query = "SELECT bin_id, longitude, latitude, bin_type, detail, city FROM binlist";

        try (Connection conn = Utils.getConnection();
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
            try (Connection conn = Utils.getConnection();
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
        }else{
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

        try (Connection conn = Utils.getConnection();
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

        try (Connection conn = Utils.getConnection();
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

    // 리뷰 업데이트
    public static int updateBinReview(int bin_id, int review) {
        String updateQuery = "UPDATE binreview SET review = ? WHERE bin_id = ?";

        try (Connection conn = Utils.getConnection();
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

    // 리뷰 있는지 조회
    public static boolean hasExistingReview(int bin_id) {
        String selectQuery = "SELECT COUNT(*) FROM binreview WHERE bin_id = ?";
        try (Connection conn = getConnection();
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
        String url = "jdbc:sqlite:src/main/java/seoulbin.database/seoulbin.sqlite3";
        String deleteQuery = "DELETE FROM binList WHERE bin_id = ?";

        try (Connection conn = DriverManager.getConnection(url);
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

    // home 위치 수정
    public static int updateHomeLocation(double latitude, double longitude, String address) {
        String query = "UPDATE home " +
                "SET latitude = ?, longitude = ?, address = ? " +
                "WHERE id = 1";

        try (Connection conn = Utils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setDouble(1, latitude);
            pstmt.setDouble(2, longitude);
            pstmt.setString(3, address);

            return pstmt.executeUpdate(); // 성공 시 업데이트된 행 수 반환
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // 실패
    }

    // home 위치 가져오기
    public static HomeLocation getHomeLocation() {
        String getQuery = "SELECT latitude, longitude, address FROM home LIMIT 1";

        try (Connection conn = getConnection(); // SQLite 연결
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
