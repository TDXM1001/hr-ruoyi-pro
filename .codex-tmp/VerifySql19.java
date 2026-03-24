import java.sql.*;

public class VerifySql19 {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3306/ruoyi-assets?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowMultiQueries=true";
        try (Connection conn = DriverManager.getConnection(url, "root", "root")) {
            try (PreparedStatement ps = conn.prepareStatement(
                "select count(*) from information_schema.tables where table_schema = ? and table_name = ?")) {
                ps.setString(1, "ruoyi-assets");
                ps.setString(2, "ast_asset_real_estate_occupancy_order");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("OCCUPANCY_TABLE_COUNT=" + rs.getInt(1));
                    }
                }
            }
        }
    }
}