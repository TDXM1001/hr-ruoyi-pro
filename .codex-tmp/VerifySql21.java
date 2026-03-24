import java.sql.*;
public class VerifySql21 {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:mysql://127.0.0.1:3306/ruoyi-assets?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowMultiQueries=true";
    try (Connection conn = DriverManager.getConnection(url, "root", "root")) {
      try (PreparedStatement ps = conn.prepareStatement("select disposal_no, disposal_status, confirmed_by, confirmed_time from ast_asset_disposal where asset_id = 20002 and disposal_no = 'DIS-2026-9001'")) {
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            System.out.println("DISPOSAL_SAMPLE=" + rs.getString(1) + "|" + rs.getString(2) + "|" + rs.getString(3) + "|" + rs.getString(4));
          }
        }
      }
      try (PreparedStatement ps = conn.prepareStatement("select asset_status from ast_asset_ledger where asset_id = 20002")) {
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            System.out.println("ASSET_STATUS=" + rs.getString(1));
          }
        }
      }
      try (PreparedStatement ps = conn.prepareStatement("select count(*) from ast_asset_change_log where asset_id = 20002 and log_id between 21001 and 21004")) {
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            System.out.println("DISPOSAL_LOG_COUNT=" + rs.getInt(1));
          }
        }
      }
    }
  }
}