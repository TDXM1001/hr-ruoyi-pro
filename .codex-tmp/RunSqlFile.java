import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
public class RunSqlFile {
  public static void main(String[] args) throws Exception {
    String file = args[0];
    String url = "jdbc:mysql://localhost:3306/ruoyi-assets?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=false";
    String content = Files.readString(Path.of(file), StandardCharsets.UTF_8);
    List<String> statements = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    for (String line : content.split("\\r?\\n")) {
      String trimmed = line.trim();
      if (trimmed.startsWith("--") || trimmed.isEmpty()) {
        continue;
      }
      current.append(line).append('\n');
      if (trimmed.endsWith(";")) {
        statements.add(current.toString());
        current.setLength(0);
      }
    }
    try (Connection conn = DriverManager.getConnection(url, "root", "root")) {
      for (String statement : statements) {
        String sql = statement.trim();
        if (sql.endsWith(";")) {
          sql = sql.substring(0, sql.length() - 1);
        }
        try (Statement st = conn.createStatement()) {
          st.execute(sql);
          System.out.println("OK: " + sql.replaceAll("\\s+", " ").substring(0, Math.min(80, sql.replaceAll("\\s+", " ").length())));
        }
      }
    }
  }
}