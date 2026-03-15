package com.ruoyi.asset.sql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 不动产生命周期 SQL 契约测试。
 *
 * 这组测试只锁定脚本文本和执行说明，确保后续建表与交付文档不会漂移。
 */
class AssetRealEstateLifecycleSqlContractTest {
    private static final Path REAL_ESTATE_LIFECYCLE_SQL_PATH =
        Path.of("..", "sql", "20260315_asset_real_estate_lifecycle.sql");
    private static final Path SQL_EXECUTION_DOC_PATH = Path.of("..", "sql", "sql执行.md");

    @Test
    void shouldContainAllLifecycleTablesAndApprovalColumns() throws IOException {
        String sql = Files.readString(REAL_ESTATE_LIFECYCLE_SQL_PATH, StandardCharsets.UTF_8);

        assertAll(
            () -> assertTrue(sql.contains("CREATE TABLE `asset_real_estate_ownership_change`"), "缺少权属变更表"),
            () -> assertTrue(sql.contains("CREATE TABLE `asset_real_estate_usage_change`"), "缺少用途变更表"),
            () -> assertTrue(sql.contains("CREATE TABLE `asset_real_estate_status_change`"), "缺少状态变更表"),
            () -> assertTrue(sql.contains("CREATE TABLE `asset_real_estate_disposal`"), "缺少注销/处置表"),
            () -> assertTrue(sql.contains("`asset_id` bigint(20) NOT NULL"), "缺少统一资产 ID 字段"),
            () -> assertTrue(sql.contains("`status` varchar(32) NOT NULL"), "缺少审批状态字段"),
            () -> assertTrue(sql.contains("`apply_user_id` bigint(20) DEFAULT NULL"), "缺少申请人字段"),
            () -> assertTrue(sql.contains("`apply_dept_id` bigint(20) DEFAULT NULL"), "缺少申请部门字段")
        );
    }

    @Test
    void shouldDocumentLifecycleSqlExecutionOrder() throws IOException {
        String executionDoc = Files.readString(SQL_EXECUTION_DOC_PATH, StandardCharsets.UTF_8);

        assertTrue(
            executionDoc.contains("20260315_asset_real_estate_lifecycle.sql"),
            "SQL 执行说明缺少不动产生命周期脚本"
        );
    }
}
