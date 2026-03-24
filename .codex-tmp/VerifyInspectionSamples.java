import java.sql.*;
public class VerifyInspectionSamples {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:mysql://localhost:3306/ruoyi-assets?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=false";
    try (Connection conn = DriverManager.getConnection(url, "root", "root")) {
      String[] queries = new String[] {
        "select task_id, task_no, task_name from ast_asset_inventory_task where task_no like 'IV-2026-900%' order by task_id",
        "select item_id, task_id, result_desc, follow_up_biz_id, process_status from ast_asset_inventory_item where item_id in (6,7,8) order by item_id"
      };
      for (String sql : queries) {
        System.out.println("=== SQL ===");
        System.out.println(sql);
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
          ResultSetMetaData md = rs.getMetaData();
          int cols = md.getColumnCount();
          while (rs.next()) {
            for (int i = 1; i <= cols; i++) {
              System.out.print(md.getColumnLabel(i) + "=" + rs.getString(i) + (i == cols ? "" : " | "));
            }
            System.out.println();
          }
        }
      }
    }
  }
}