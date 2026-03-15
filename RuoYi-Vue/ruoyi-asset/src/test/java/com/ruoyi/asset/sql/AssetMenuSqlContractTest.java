package com.ruoyi.asset.sql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 菜单与权限补丁脚本契约测试。
 *
 * 这里不连数据库，只校验脚本文本是否已经收敛到当前控制器和页面的最终口径，
 * 避免后续再把 repair/scrap 这样的历史命名重新写回脚本。
 */
class AssetMenuSqlContractTest {
    private static final Path LEGACY_MENU_SQL_PATH = Path.of("..", "sql", "20260312_asset_workflow_menu.sql");
    private static final Path PATCH_MENU_SQL_PATH = Path.of("..", "sql", "20260315_asset_lifecycle_workflow_menu_patch.sql");
    private static final Path REAL_ESTATE_MENU_SQL_PATH = Path.of("..", "sql", "20260315_asset_real_estate_lifecycle_menu.sql");
    private static final Path SQL_EXECUTION_DOC_PATH = Path.of("..", "sql", "sql执行.md");

    @Test
    void shouldKeepLegacyScriptAsPatchInputAndExposeNewLifecycleTargets() throws IOException {
        String legacySql = Files.readString(LEGACY_MENU_SQL_PATH, StandardCharsets.UTF_8);
        String patchSql = Files.readString(PATCH_MENU_SQL_PATH, StandardCharsets.UTF_8);

        assertAll(
            () -> assertTrue(legacySql.contains("asset/repair/index"), "基线脚本缺少历史维修页面路径，补丁无法说明修正来源"),
            () -> assertTrue(legacySql.contains("asset/scrap/index"), "基线脚本缺少历史报废页面路径，补丁无法说明修正来源"),
            () -> assertTrue(patchSql.contains("asset/maintenance/index"), "补丁脚本缺少维修台账页面路径"),
            () -> assertTrue(patchSql.contains("asset/disposal/index"), "补丁脚本缺少处置台账页面路径"),
            () -> assertTrue(patchSql.contains("asset:maintenance:list"), "补丁脚本缺少维修菜单权限"),
            () -> assertTrue(patchSql.contains("asset:disposal:list"), "补丁脚本缺少处置菜单权限")
        );
    }

    @Test
    void shouldAlignWorkflowAndButtonPermissionsWithCurrentControllers() throws IOException {
        String patchSql = Files.readString(PATCH_MENU_SQL_PATH, StandardCharsets.UTF_8);

        assertAll(
            () -> assertTrue(patchSql.contains("workflow:task:todo"), "补丁脚本缺少 workflow 待办权限"),
            () -> assertTrue(patchSql.contains("workflow:task:done"), "补丁脚本缺少 workflow 已办权限"),
            () -> assertTrue(patchSql.contains("workflow:task:approve"), "补丁脚本缺少 workflow 审批按钮权限"),
            () -> assertTrue(patchSql.contains("asset:requisition:return"), "补丁脚本缺少领用归还按钮权限"),
            () -> assertTrue(patchSql.contains("asset:maintenance:query"), "补丁脚本缺少维修详情权限"),
            () -> assertTrue(patchSql.contains("asset:maintenance:add"), "补丁脚本缺少维修新增权限"),
            () -> assertTrue(patchSql.contains("asset:maintenance:complete"), "补丁脚本缺少维修完成权限"),
            () -> assertTrue(patchSql.contains("asset:disposal:query"), "补丁脚本缺少处置详情权限"),
            () -> assertTrue(patchSql.contains("asset:disposal:add"), "补丁脚本缺少处置新增权限")
        );
    }

    @Test
    void shouldExposeRealEstateLifecycleMenusAndPermissions() throws IOException {
        String realEstateMenuSql = Files.readString(REAL_ESTATE_MENU_SQL_PATH, StandardCharsets.UTF_8);

        assertAll(
            () -> assertTrue(realEstateMenuSql.contains("asset/real-estate/ownership/index"), "菜单脚本缺少权属变更页面路径"),
            () -> assertTrue(realEstateMenuSql.contains("asset/real-estate/usage/index"), "菜单脚本缺少用途变更页面路径"),
            () -> assertTrue(realEstateMenuSql.contains("asset/real-estate/status/index"), "菜单脚本缺少状态变更页面路径"),
            () -> assertTrue(realEstateMenuSql.contains("asset/real-estate/disposal/index"), "菜单脚本缺少注销/处置页面路径"),
            () -> assertTrue(realEstateMenuSql.contains("asset:realEstateOwnership:list"), "菜单脚本缺少权属变更菜单权限"),
            () -> assertTrue(realEstateMenuSql.contains("asset:realEstateOwnership:add"), "菜单脚本缺少权属变更新增权限"),
            () -> assertTrue(realEstateMenuSql.contains("asset:realEstateUsage:list"), "菜单脚本缺少用途变更菜单权限"),
            () -> assertTrue(realEstateMenuSql.contains("asset:realEstateUsage:add"), "菜单脚本缺少用途变更新增权限"),
            () -> assertTrue(realEstateMenuSql.contains("asset:realEstateStatus:list"), "菜单脚本缺少状态变更菜单权限"),
            () -> assertTrue(realEstateMenuSql.contains("asset:realEstateStatus:add"), "菜单脚本缺少状态变更新增权限"),
            () -> assertTrue(realEstateMenuSql.contains("asset:realEstateDisposal:list"), "菜单脚本缺少注销/处置菜单权限"),
            () -> assertTrue(realEstateMenuSql.contains("asset:realEstateDisposal:add"), "菜单脚本缺少注销/处置新增权限")
        );
    }

    @Test
    void shouldDocumentMenuPatchExecutionOrder() throws IOException {
        String executionDoc = Files.readString(SQL_EXECUTION_DOC_PATH, StandardCharsets.UTF_8);

        assertAll(
            () -> assertTrue(
                executionDoc.contains("20260315_asset_lifecycle_workflow_menu_patch.sql"),
                "SQL 执行说明缺少菜单权限补丁脚本"
            ),
            () -> assertTrue(
                executionDoc.contains("20260315_asset_real_estate_lifecycle_menu.sql"),
                "SQL 执行说明缺少不动产生命周期菜单脚本"
            )
        );
    }
}
