import java.sql.*;
public class QueryInspectionRows {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:mysql://localhost:3306/ruoyi-assets?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=false";
    try (Connection conn = DriverManager.getConnection(url, "root", "root")) {
      String[] queries = new String[] {
        "select * from ast_asset_inventory_task where task_id in (7,8) order by task_id",
        "select * from ast_asset_inventory_item where item_id in (6,7) order by item_id"
      };
      for (String sql : queries) {
        System.out.println(\"=== SQL ===\");
        System.out.println(sql);
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
          ResultSetMetaData md = rs.getMetaData();
          int cols = md.getColumnCount();
          while (rs.next()) {
            for (int i = 1; i <= cols; i++) {
              System.out.println(md.getColumnLabel(i) + \"=\" + rs.getString(i));
            }
            System.out.println("---");
          }
        }
      }
    }
  }
}