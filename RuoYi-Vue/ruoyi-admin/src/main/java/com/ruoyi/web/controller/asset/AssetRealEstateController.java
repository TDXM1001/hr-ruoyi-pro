package com.ruoyi.web.controller.asset;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.asset.domain.AssetRealEstateProfile;
import com.ruoyi.asset.service.IAssetRealEstateService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 不动产权属档案控制器。
 *
 * <p>用于支撑资产管理员按台账维度查询不动产扩展信息。</p>
 *
 * @author Codex
 */
@RestController
@RequestMapping("/asset/real-estate")
public class AssetRealEstateController extends BaseController
{
    @Autowired
    private IAssetRealEstateService assetRealEstateService;

    /**
     * 查询不动产权属档案列表。
     *
     * @param query 查询条件
     * @return 分页数据
     */
    @PreAuthorize("@ss.hasPermi('asset:realEstate:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetRealEstateProfile query)
    {
        startPage();
        List<AssetRealEstateProfile> list = assetRealEstateService.selectList(query);
        return getDataTable(list);
    }

    /**
     * 查询不动产权属档案详情。
     *
     * @param assetId 资产ID
     * @return 详情
     */
    @PreAuthorize("@ss.hasPermi('asset:realEstate:query')")
    @GetMapping("/{assetId}")
    public AjaxResult getInfo(@PathVariable Long assetId)
    {
        return success(assetRealEstateService.selectByAssetId(assetId));
    }
}

