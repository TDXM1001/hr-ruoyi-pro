import java.sql.*;
public class QueryEncoding {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:mysql://localhost:3306/ruoyi-assets?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=false";
    try (Connection conn = DriverManager.getConnection(url, "root", "root")) {
      String[] queries = new String[] {
        "select task_id, task_no, task_name, hex(task_name) as task_hex from ast_asset_inventory_task where task_no like 'IV-2026-900%' order by task_id",
        "select item_id, task_id, result_desc, hex(result_desc) as desc_hex, follow_up_biz_id from ast_asset_inventory_item where task_id in (select task_id from ast_asset_inventory_task where task_no like 'IV-2026-900%') order by item_id",
        "select rectification_id, rectification_no, issue_desc, completion_desc, acceptance_remark from ast_asset_rectification_order where rectification_no in ('RC-2026-0001','RC-2026-0002') order by rectification_id"
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