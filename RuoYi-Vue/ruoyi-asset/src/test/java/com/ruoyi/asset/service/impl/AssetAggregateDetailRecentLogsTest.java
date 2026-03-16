package com.ruoyi.asset.service.impl;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.asset.domain.AssetAttachment;
import com.ruoyi.asset.domain.AssetAttrValue;
import com.ruoyi.asset.domain.AssetDepreciationLog;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.mapper.AssetAttachmentMapper;
import com.ruoyi.asset.mapper.AssetAttrValueMapper;
import com.ruoyi.asset.mapper.AssetDepreciationLogMapper;
import com.ruoyi.asset.mapper.AssetFinanceMapper;
import com.ruoyi.asset.mapper.AssetRealEstateMapper;
import com.ruoyi.asset.mapper.AssetTimelineMapper;
import com.ruoyi.asset.service.IAssetFinanceService;
import com.ruoyi.asset.service.IAssetInfoService;
import com.ruoyi.asset.domain.vo.AssetDetailVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * 资产详情最近折旧日志测试。
 */
@ExtendWith(MockitoExtension.class)
class AssetAggregateDetailRecentLogsTest {
    @Mock
    private IAssetInfoService assetInfoService;

    @Mock
    private AssetFinanceMapper assetFinanceMapper;

    @Mock
    private AssetRealEstateMapper assetRealEstateMapper;

    @Mock
    private AssetAttrValueMapper assetAttrValueMapper;

    @Mock
    private AssetAttachmentMapper assetAttachmentMapper;

    @Mock
    private AssetDepreciationLogMapper assetDepreciationLogMapper;

    @Mock
    private AssetTimelineMapper assetTimelineMapper;

    @Mock
    private IAssetFinanceService assetFinanceService;

    @InjectMocks
    private AssetAggregateServiceImpl assetAggregateService;

    @Test
    void shouldOnlyReturnLatestTwelveDepreciationLogs() {
        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setAssetId(1001L);
        assetInfo.setAssetNo("A-1001");
        when(assetInfoService.selectAssetInfoByAssetId(1001L)).thenReturn(assetInfo);
        when(assetAttrValueMapper.selectAssetAttrValueByAssetId(1001L)).thenReturn(new ArrayList<AssetAttrValue>());
        when(assetAttachmentMapper.selectAssetAttachmentByAssetId(1001L)).thenReturn(new ArrayList<AssetAttachment>());
        when(assetDepreciationLogMapper.selectAssetDepreciationLogByAssetId(1001L)).thenReturn(buildLogs(15));
        when(assetTimelineMapper.selectAssetTimelineByAssetId(1001L)).thenReturn(new ArrayList<>());

        AssetDetailVo result = assetAggregateService.selectAssetDetailByAssetId(1001L);

        assertEquals(12, result.getDepreciationLogs().size());
        assertEquals("2026-15", result.getDepreciationLogs().get(0).getPeriod());
        assertEquals("2026-04", result.getDepreciationLogs().get(11).getPeriod());
    }

    private List<AssetDepreciationLog> buildLogs(int count) {
        List<AssetDepreciationLog> logs = new ArrayList<>();
        for (int index = count; index >= 1; index--) {
            AssetDepreciationLog log = new AssetDepreciationLog();
            log.setAssetId(1001L);
            log.setPeriod(String.format("2026-%02d", index));
            logs.add(log);
        }
        return logs;
    }
}
