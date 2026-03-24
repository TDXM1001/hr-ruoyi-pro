import java.sql.*;

public class VerifySql20 {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3306/ruoyi-assets?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowMultiQueries=true";
        try (Connection conn = DriverManager.getConnection(url, "root", "root")) {
            try (PreparedStatement ps = conn.prepareStatement(
                "select occupancy_no, occupancy_status, use_dept_id, responsible_user_id, location_name from ast_asset_real_estate_occupancy_order where asset_id = ? and del_flag = '0' order by occupancy_id desc limit 1")) {
                ps.setLong(1, 20001L);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("LATEST_OCCUPANCY=" + rs.getString(1) + "," + rs.getString(2) + "," + rs.getLong(3) + "," + rs.getLong(4) + "," + rs.getString(5));
                    }
                }
            }
        }
    }
}