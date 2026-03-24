import java.sql.*;
public class QueryRealEstateAssets {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:mysql://127.0.0.1:3306/ruoyi-assets?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowMultiQueries=true";
    try (Connection conn = DriverManager.getConnection(url, "root", "root");
         PreparedStatement ps = conn.prepareStatement("select asset_id, asset_code, asset_name, asset_status from ast_asset_ledger where asset_type = 'REAL_ESTATE' and del_flag = '0' order by asset_id");
         ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        System.out.println(rs.getLong(1) + "|" + rs.getString(2) + "|" + rs.getString(3) + "|" + rs.getString(4));
      }
    }
  }
}