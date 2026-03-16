package com.ruoyi.asset.service.impl;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.asset.domain.AssetAttachment;
import com.ruoyi.asset.domain.AssetAttrValue;
import com.ruoyi.asset.domain.AssetDepreciationLog;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetRealEstate;
import com.ruoyi.asset.domain.vo.AssetDetailVo;
import com.ruoyi.asset.domain.vo.AssetTimelineVo;
import com.ruoyi.asset.mapper.AssetAttachmentMapper;
import com.ruoyi.asset.mapper.AssetAttrValueMapper;
import com.ruoyi.asset.mapper.AssetDepreciationLogMapper;
import com.ruoyi.asset.mapper.AssetFinanceMapper;
import com.ruoyi.asset.mapper.AssetRealEstateMapper;
import com.ruoyi.asset.mapper.AssetTimelineMapper;
import com.ruoyi.asset.service.IAssetFinanceService;
import com.ruoyi.asset.service.IAssetInfoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

/**
 * 资产统一动作时间线测试。
 */
@ExtendWith(MockitoExtension.class)
class AssetTimelineServiceImplTest {
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
    void shouldExposeUnifiedActionTimelineInAssetDetail() {
        AssetInfo basicInfo = new AssetInfo();
        basicInfo.setAssetId(2001L);
        basicInfo.setAssetNo("RE-2001");
        basicInfo.setAssetStatus("1");

        AssetRealEstate realEstate = new AssetRealEstate();
        realEstate.setAssetId(2001L);
        realEstate.setRightsHolder("旧权利人");

        AssetTimelineVo approvedOwnership = new AssetTimelineVo();
        approvedOwnership.setActionType("REAL_ESTATE_OWNERSHIP_CHANGE");
        approvedOwnership.setActionLabel("权属变更");
        approvedOwnership.setBusinessNo("OC-20260315-01");
        approvedOwnership.setDocStatus("approved");

        AssetTimelineVo rejectedDisposal = new AssetTimelineVo();
        rejectedDisposal.setActionType("REAL_ESTATE_DISPOSAL");
        rejectedDisposal.setActionLabel("注销/处置");
        rejectedDisposal.setBusinessNo("RD-20260314-01");
        rejectedDisposal.setDocStatus("rejected");

        when(assetInfoService.selectAssetInfoByAssetId(2001L)).thenReturn(basicInfo);
        when(assetRealEstateMapper.selectAssetRealEstateByAssetId(2001L)).thenReturn(realEstate);
        when(assetAttrValueMapper.selectAssetAttrValueByAssetId(2001L)).thenReturn(new ArrayList<AssetAttrValue>());
        when(assetAttachmentMapper.selectAssetAttachmentByAssetId(2001L)).thenReturn(new ArrayList<AssetAttachment>());
        when(assetDepreciationLogMapper.selectAssetDepreciationLogByAssetId(2001L))
            .thenReturn(new ArrayList<AssetDepreciationLog>());
        when(assetTimelineMapper.selectAssetTimelineByAssetId(2001L))
            .thenReturn(List.of(approvedOwnership, rejectedDisposal));

        AssetDetailVo result = assetAggregateService.selectAssetDetailByAssetId(2001L);

        assertFalse(result.getTimeline().isEmpty());
        assertEquals("REAL_ESTATE_OWNERSHIP_CHANGE", result.getTimeline().get(0).getActionType());
        assertEquals("approved", result.getTimeline().get(0).getDocStatus());
        assertEquals("rejected", result.getTimeline().get(1).getDocStatus());
        assertEquals("1", result.getBasicInfo().getAssetStatus());
        assertEquals("旧权利人", result.getRealEstateInfo().getRightsHolder());
    }
}
