package com.ruoyi.asset.service.impl;

import com.ruoyi.asset.domain.AssetDisposal;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.mapper.AssetDisposalMapper;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.workflow.service.IApprovalEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 固定资产报废/处置语义测试。
 */
@ExtendWith(MockitoExtension.class)
class AssetDisposalSemanticsServiceImplTest {

    @Mock
    private AssetDisposalMapper assetDisposalMapper;

    @Mock
    private AssetInfoMapper assetInfoMapper;

    @Mock
    private IApprovalEngine approvalEngine;

    @InjectMocks
    private AssetDisposalServiceImpl assetDisposalService;

    @Test
    void shouldRejectBlankDisposalTypeWhenSubmittingDisposal() {
        AssetDisposal disposal = buildDisposal(null);

        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> assetDisposalService.insertAssetDisposal(disposal)
        );

        assertEquals("处置类型不能为空", exception.getMessage());
        verifyNoInteractions(assetDisposalMapper, assetInfoMapper, approvalEngine);
    }

    @Test
    void shouldMarkAssetDisposedWhenSubmittingSellDisposal() {
        AssetDisposal disposal = buildDisposal("sell");
        AssetInfo assetInfo = buildAssetInfo(3001L, "FA-2026-0301", "1");
        when(assetInfoMapper.selectAssetInfoByAssetId(3001L)).thenReturn(assetInfo);

        assetDisposalService.insertAssetDisposal(disposal);

        ArgumentCaptor<AssetDisposal> disposalCaptor = ArgumentCaptor.forClass(AssetDisposal.class);
        verify(assetDisposalMapper).insertAssetDisposal(disposalCaptor.capture());
        assertEquals("sell", disposalCaptor.getValue().getDisposalType());
        assertEquals("FA-2026-0301", disposalCaptor.getValue().getAssetNo());

        ArgumentCaptor<AssetInfo> assetCaptor = ArgumentCaptor.forClass(AssetInfo.class);
        verify(assetInfoMapper).updateAssetInfo(assetCaptor.capture());
        assertEquals(3001L, assetCaptor.getValue().getAssetId());
        assertEquals("6", assetCaptor.getValue().getAssetStatus());

        verify(approvalEngine).startProcess("DIS-20260316-0301", "asset_disposal");
    }

    private AssetDisposal buildDisposal(String disposalType) {
        AssetDisposal disposal = new AssetDisposal();
        disposal.setDisposalNo("DIS-20260316-0301");
        disposal.setAssetId(3001L);
        disposal.setReason("固定资产处置语义测试");
        disposal.setDisposalType(disposalType);
        return disposal;
    }

    private AssetInfo buildAssetInfo(Long assetId, String assetNo, String assetStatus) {
        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setAssetId(assetId);
        assetInfo.setAssetNo(assetNo);
        assetInfo.setAssetStatus(assetStatus);
        return assetInfo;
    }
}
