package com.ruoyi.asset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 资产模块冒烟测试。
 *
 * @author Codex
 */
class AssetModuleSmokeTest
{
    @Test
    @DisplayName("应该可以加载资产模块标记类")
    void shouldLoadAssetModuleMarker()
    {
        assertDoesNotThrow(() -> Class.forName("com.ruoyi.asset.AssetModuleMarker"));
    }
}
