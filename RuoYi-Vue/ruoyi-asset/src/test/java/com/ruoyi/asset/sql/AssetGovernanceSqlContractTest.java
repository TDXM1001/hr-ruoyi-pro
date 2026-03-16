package com.ruoyi.asset.sql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 资产治理口径 SQL 契约测试。
 *
 * 这组测试只锁定字典、模型脚本和执行说明里的最终口径，
 * 避免初始化脚本、升级补丁和前端约定再次漂移。
 */
class AssetGovernanceSqlContractTest {
    private static final Path DICT_SQL_PATH = Path.of("..", "sql", "20260311_asset_dicts.sql");
    private static final Path REFACTOR_SQL_PATH = Path.of("..", "sql", "20260314_asset_data_model_refactor.sql");
    private static final Path WORKFLOW_SQL_PATH = Path.of("..", "sql", "20260312_asset_workflow_business.sql");
    private static final Path GOVERNANCE_PATCH_SQL_PATH =
        Path.of("..", "sql", "20260316_asset_governance_patch.sql");
    private static final Path SQL_EXECUTION_DOC_PATH = Path.of("..", "sql", "sql执行.md");

    @Test
    void shouldLockAssetTypeAndStatusDictionaryBaseline() throws IOException {
        String dictSql = Files.readString(DICT_SQL_PATH, StandardCharsets.UTF_8);

        assertAll(
            () -> assertTrue(dictSql.contains("资产类型"), "缺少资产类型字典"),
            () -> assertTrue(
                dictSql.contains("VALUES (1, '固定资产', '1', 'asset_type'"),
                "资产类型字典必须收敛为 1=固定资产"
            ),
            () -> assertTrue(
                dictSql.contains("VALUES (2, '不动产', '2', 'asset_type'"),
                "资产类型字典必须收敛为 2=不动产"
            ),
            () -> assertTrue(
                dictSql.contains("VALUES (1, '在用', '1', 'asset_status'"),
                "资产状态字典必须使用“在用”口径"
            ),
            () -> assertTrue(
                dictSql.contains("VALUES (6, '已处置', '6', 'asset_status'"),
                "资产状态字典缺少“已处置”"
            ),
            () -> assertTrue(
                dictSql.contains("VALUES (7, '闲置', '7', 'asset_status'"),
                "资产状态字典缺少“闲置”"
            )
        );
    }

    @Test
    void shouldAlignGovernancePatchAndExecutionInstructions() throws IOException {
        String refactorSql = Files.readString(REFACTOR_SQL_PATH, StandardCharsets.UTF_8);
        String workflowSql = Files.readString(WORKFLOW_SQL_PATH, StandardCharsets.UTF_8);
        String executionDoc = Files.readString(SQL_EXECUTION_DOC_PATH, StandardCharsets.UTF_8);
        boolean governancePatchExists = Files.exists(GOVERNANCE_PATCH_SQL_PATH);
        String governancePatchSql = governancePatchExists
            ? Files.readString(GOVERNANCE_PATCH_SQL_PATH, StandardCharsets.UTF_8)
            : "";

        assertAll(
            () -> assertTrue(
                refactorSql.contains("资产类型：1=固定资产 2=不动产"),
                "重构脚本缺少资产类型最终口径"
            ),
            () -> assertTrue(
                refactorSql.contains("资产状态：1=在用 2=领用中 3=维修中 4=盘点中 5=已报废 6=已处置 7=闲置"),
                "重构脚本缺少资产状态最终口径"
            ),
            () -> assertTrue(
                workflowSql.contains("`disposal_type` varchar(20)"),
                "业务流水脚本缺少 disposal_type 字段"
            ),
            () -> assertTrue(
                governancePatchExists,
                "缺少治理补丁脚本 20260316_asset_governance_patch.sql"
            ),
            () -> assertTrue(
                governancePatchSql.contains("asset_type"),
                "治理补丁脚本缺少 asset_type 字典纠偏说明"
            ),
            () -> assertTrue(
                governancePatchSql.contains("asset_status"),
                "治理补丁脚本缺少 asset_status 补齐说明"
            ),
            () -> assertTrue(
                governancePatchSql.contains("disposal_type"),
                "治理补丁脚本缺少 disposal_type 历史数据说明"
            ),
            () -> assertTrue(
                executionDoc.contains("20260316_asset_governance_patch.sql"),
                "SQL 执行说明缺少治理补丁脚本"
            )
        );
    }
}
