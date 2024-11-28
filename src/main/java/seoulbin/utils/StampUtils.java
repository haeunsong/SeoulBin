package seoulbin.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StampUtils {

    // 하루에 열 수 있는 최대 조각 수
    private static final int MAX_DAILY_PIECES = 5;

    /*
   name: getImagePath
   Param: int imageId
   desc: imageId 에 해당하는 이미지 경로 조회
   return: String 타입의 이미지 경로 반환
   */
    public static String getImagePath(int imageId) {
        String selectQuery = "SELECT image_path FROM images WHERE id = ?";
        String path = null;

        try (Connection conn = DatabaseUtils.getConnection();
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

    /*
   name: updateProgress
   Param: int imageId, int pieceId, boolean completed
   desc: 퍼즐 조각의 상태 업데이트
   return: 없음
   */
    public static void updateProgress(int imageId, int pieceId, boolean completed) {
        String updateQuery = "UPDATE progress SET status = ? WHERE image_id = ? AND piece_id = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            // status : 1 = 열린 상태, status : 0 = 닫힌 상태
            pstmt.setInt(1, completed ? 1 : 0);
            pstmt.setInt(2, imageId);
            pstmt.setInt(3, pieceId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
      name: getOpenedPieces
    Param: int imageId
    desc: 처음에 스탬프 판 받아와서 버튼의 visible 처리
    return: List<Integer> 형식의 piece_id 목록
    */
    public static List<Integer> getOpenedPieces(int imageId) {
        String sql = "SELECT piece_id FROM progress WHERE image_id = ? AND status = 1";
        List<Integer> openedPieces = new ArrayList<>();

        try (Connection conn = DatabaseUtils.getConnection();
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

    /*
      name: initializeDailyProgress
      Param: 없음
      desc: 날짜가 변경되면 데이터 초기화, 데이터가 하나도 없을 경우 삽입
      return: 없음
    */
    public static void initializeDailyProgress() {
        String query =
                "UPDATE daily_progress " +
                        "SET date = date('now'), opened_count = 0 " +
                        "WHERE date != date('now'); " +
                        "INSERT INTO daily_progress (date, opened_count) " +
                        "SELECT date('now'), 0 WHERE NOT EXISTS (SELECT 1 FROM daily_progress);";

        try (Connection conn = DatabaseUtils.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query); // 업데이트 및 삽입 쿼리 실행
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
      name: incrementOpenedCount
      Param: 없음
      desc: 날짜가 현재 날짜와 동일하면 opended_count 1씩 증가
      return: 없음
    */
    public static void incrementOpenedCount() {
        String query = "UPDATE daily_progress SET opened_count = opened_count + 1 WHERE date = date('now')";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.executeUpdate(); // opened_count 값 증가
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
      name: canOpenMorePieces
      Param: 없음
      desc: 현재 조각을 더 열 수 있는지 확인
      return: 다섯 조각(하루에 최대 열 수 있는 조각 개수)보다 적게 열었으면 true, 이미 다섯 조각을 모두 열었으면 false 반환
    */
    public static boolean canOpenMorePieces() {
        String query = "SELECT opened_count FROM daily_progress WHERE date = date('now')";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("opened_count") < MAX_DAILY_PIECES;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
