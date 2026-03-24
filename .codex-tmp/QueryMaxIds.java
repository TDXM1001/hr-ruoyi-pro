import java.sql.*;
public class QueryMaxIds {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:mysql://localhost:3306/ruoyi-assets?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=false";
    try (Connection conn = DriverManager.getConnection(url, "root", "root")) {
      String[] queries = new String[] {
        "select max(task_id) as max_task_id from ast_asset_inventory_task",
        "select max(item_id) as max_item_id from ast_asset_inventory_item",
        "select task_id, task_no from ast_asset_inventory_task order by task_id desc limit 5",
        "select item_id, task_id, asset_id, follow_up_biz_id from ast_asset_inventory_item order by item_id desc limit 5"
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