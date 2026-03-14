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
    private static final Path WORKFLOW_SQL_PATH = Path.of("..", "sql", "20260312_asset_workflow_business.sql");
    private static final Path PATCH_SQL_PATH = Path.of("..", "sql", "20260314_asset_workflow_asset_id_patch.sql");
    private static final Path REFACTOR_SQL_PATH = Path.of("..", "sql", "20260314_asset_data_model_refactor.sql");

    @Test
    void shouldDefineAssetIdColumnsInWorkflowBusinessScript() throws IOException {
        String sql = Files.readString(WORKFLOW_SQL_PATH, StandardCharsets.UTF_8);

        assertAll(
            () -> assertTrue(sql.contains("CREATE TABLE `asset_requisition`"), "业务流水基线脚本缺少领用单建表"),
            () -> assertTrue(sql.contains("CREATE TABLE `asset_maintenance`"), "业务流水基线脚本缺少维修单建表"),
            () -> assertTrue(sql.contains("CREATE TABLE `asset_disposal`"), "业务流水基线脚本缺少处置单建表"),
            () -> assertTrue(sql.contains("`asset_id` bigint(20) NOT NULL COMMENT '资产ID'"), "业务流水基线脚本缺少 asset_id 字段"),
            () -> assertTrue(sql.contains("KEY `idx_asset_requisition_asset_id` (`asset_id`)"), "业务流水基线脚本缺少领用单 asset_id 索引"),
            () -> assertTrue(sql.contains("KEY `idx_asset_maintenance_asset_id` (`asset_id`)"), "业务流水基线脚本缺少维修单 asset_id 索引"),
            () -> assertTrue(sql.contains("KEY `idx_asset_disposal_asset_id` (`asset_id`)"), "业务流水基线脚本缺少处置单 asset_id 索引")
        );
    }

    @Test
    void shouldProvidePatchScriptForExistingWorkflowTables() throws IOException {
        String patchSql = Files.readString(PATCH_SQL_PATH, StandardCharsets.UTF_8);
        String refactorSql = Files.readString(REFACTOR_SQL_PATH, StandardCharsets.UTF_8);

        assertAll(
            () -> assertTrue(refactorSql.contains("20260314_asset_workflow_asset_id_patch.sql"), "重构脚本缺少增量补丁脚本提示"),
            () -> assertTrue(patchSql.contains("ALTER TABLE `asset_requisition` ADD COLUMN `asset_id`"), "增量补丁缺少领用单 asset_id 加列"),
            () -> assertTrue(patchSql.contains("UPDATE `asset_requisition` req"), "增量补丁缺少领用单 asset_id 回填"),
            () -> assertTrue(patchSql.contains("ALTER TABLE `asset_maintenance` ADD COLUMN `asset_id`"), "增量补丁缺少维修单 asset_id 加列"),
            () -> assertTrue(patchSql.contains("UPDATE `asset_maintenance` maintenance"), "增量补丁缺少维修单 asset_id 回填"),
            () -> assertTrue(patchSql.contains("ALTER TABLE `asset_disposal` ADD COLUMN `asset_id`"), "增量补丁缺少处置单 asset_id 加列"),
            () -> assertTrue(patchSql.contains("UPDATE `asset_disposal` disposal"), "增量补丁缺少处置单 asset_id 回填")
        );
    }
}
