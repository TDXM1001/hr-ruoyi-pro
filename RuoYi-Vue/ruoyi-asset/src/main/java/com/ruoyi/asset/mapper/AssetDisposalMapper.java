package com.ruoyi.asset.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.ruoyi.asset.domain.AssetDisposal;

/**
 * 资产报废Mapper接口
 */
public interface AssetDisposalMapper {
    @Select("select disposal_no as disposalNo, asset_id as assetId, asset_no as assetNo, apply_user_id as applyUserId, apply_dept_id as applyDeptId, reason, status, create_time as createTime from asset_disposal where disposal_no = #{disposalNo}")
    public AssetDisposal selectAssetDisposalByDisposalNo(String disposalNo);

    @Select("<script>" +
            "select disposal_no as disposalNo, asset_id as assetId, asset_no as assetNo, apply_user_id as applyUserId, apply_dept_id as applyDeptId, reason, status, create_time as createTime from asset_disposal " +
            "<where> " +
            "<if test=\"disposalNo != null and disposalNo != ''\"> and disposal_no = #{disposalNo}</if> " +
            "<if test=\"assetId != null\"> and asset_id = #{assetId}</if> " +
            "<if test=\"assetNo != null and assetNo != ''\"> and asset_no = #{assetNo}</if> " +
            "</where>" +
            "</script>")
    public List<AssetDisposal> selectAssetDisposalList(AssetDisposal assetDisposal);

    @Insert("insert into asset_disposal(disposal_no, asset_id, asset_no, apply_user_id, apply_dept_id, reason, status, create_time) " +
            "values(#{disposalNo}, #{assetId}, #{assetNo}, #{applyUserId}, #{applyDeptId}, #{reason}, #{status}, #{createTime})")
    public int insertAssetDisposal(AssetDisposal assetDisposal);

    @Update("<script>" +
            "update asset_disposal " +
            "<set> " +
            "<if test=\"status != null\">status = #{status},</if> " +
            "</set> " +
            "where disposal_no = #{disposalNo}" +
            "</script>")
    public int updateAssetDisposal(AssetDisposal assetDisposal);
}
