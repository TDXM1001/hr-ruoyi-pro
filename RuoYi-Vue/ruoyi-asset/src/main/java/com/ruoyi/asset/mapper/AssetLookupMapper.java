package com.ruoyi.asset.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.asset.domain.vo.AssetTreeNodeVo;
import com.ruoyi.asset.domain.vo.AssetUserOptionVo;

/**
 * 资产页面联调用轻量查询。
 *
 * <p>当前 Mapper 专门服务于资产台账页面的选择器数据，
 * 包括分类树、部门树和责任人远程搜索。</p>
 *
 * @author Codex
 */
public interface AssetLookupMapper
{
    /**
     * 查询资产分类树节点。
     *
     * @param assetType 资产类型
     * @return 分类平铺节点
     */
    List<AssetTreeNodeVo> selectAssetCategoryNodes(@Param("assetType") String assetType);

    /**
     * 查询可用部门树节点。
     *
     * @return 部门平铺节点
     */
    List<AssetTreeNodeVo> selectDeptNodes();

    /**
     * 查询责任人下拉选项。
     *
     * @param keyword 搜索关键字
     * @return 责任人选项
     */
    List<AssetUserOptionVo> selectResponsibleUserOptions(@Param("keyword") String keyword);
}
