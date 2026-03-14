package com.ruoyi.asset.sql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 业务流水数据模型迁移脚本契约测试。
 */
class AssetWorkflowSqlMigrationContractTest {
    private static final Path SQL_PATH = Path.of("..", "sql", "20260314_asset_data_model_refactor.sql");

    @Test
    void shouldAddAssetIdColumnsForWorkflowTables() throws IOException {
        String sql = Files.readString(SQL_PATH, StandardCharsets.UTF_8);

        assertAll(
            () -> assertTrue(sql.contains("ALTER TABLE `asset_requisition`"), "迁移脚本缺少 asset_requisition 变更"),
            () -> assertTrue(sql.contains("ADD COLUMN `asset_id` bigint(20)"), "迁移脚本缺少业务流水 asset_id 字段"),
            () -> assertTrue(sql.contains("UPDATE `asset_requisition` req"), "迁移脚本缺少领用单 asset_id 回填"),
            () -> assertTrue(sql.contains("ALTER TABLE `asset_maintenance`"), "迁移脚本缺少 asset_maintenance 变更"),
            () -> assertTrue(sql.contains("UPDATE `asset_maintenance` maintenance"), "迁移脚本缺少维修单 asset_id 回填"),
            () -> assertTrue(sql.contains("ALTER TABLE `asset_disposal`"), "迁移脚本缺少 asset_disposal 变更"),
            () -> assertTrue(sql.contains("UPDATE `asset_disposal` disposal"), "迁移脚本缺少处置单 asset_id 回填")
        );
    }
}
