import java.sql.*;
public class VerifySql18 {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:mysql://localhost:3306/ruoyi-assets?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=false";
    try (Connection conn = DriverManager.getConnection(url, "root", "root")) {
      try (PreparedStatement ps = conn.prepareStatement(
          "select column_name from information_schema.columns where table_schema = 'ruoyi-assets' and table_name = 'ast_asset_rectification_order' and column_name in ('approval_status','approval_submitted_time','approval_finished_time') order by ordinal_position")) {
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            System.out.println("COLUMN=" + rs.getString(1));
          }
        }
      }
      try (PreparedStatement ps = conn.prepareStatement(
          "select count(1) from information_schema.tables where table_schema = 'ruoyi-assets' and table_name = 'ast_asset_rectification_approval_record'")) {
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            System.out.println("APPROVAL_TABLE_COUNT=" + rs.getInt(1));
          }
        }
      }
    }
  }
}
