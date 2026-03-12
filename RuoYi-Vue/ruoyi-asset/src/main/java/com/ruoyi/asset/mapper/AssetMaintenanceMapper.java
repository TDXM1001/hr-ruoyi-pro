package com.ruoyi.asset.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.ruoyi.asset.domain.AssetMaintenance;

/**
 * 资产维修Mapper接口
 */
public interface AssetMaintenanceMapper {
    /** 查询维修单详情 */
    @Select("select maintenance_no as maintenanceNo, asset_no as assetNo, apply_user_id as applyUserId, apply_dept_id as applyDeptId, reason, status, create_time as createTime from asset_maintenance where maintenance_no = #{maintenanceNo}")
    public AssetMaintenance selectAssetMaintenanceByMaintenanceNo(String maintenanceNo);

    /** 查询列表 */
    @Select("<script>" +
            "select maintenance_no as maintenanceNo, asset_no as assetNo, apply_user_id as applyUserId, apply_dept_id as applyDeptId, reason, status, create_time as createTime from asset_maintenance " +
            "<where> " +
            "<if test=\"maintenanceNo != null and maintenanceNo != ''\"> and maintenance_no = #{maintenanceNo}</if> " +
            "<if test=\"assetNo != null and assetNo != ''\"> and asset_no = #{assetNo}</if> " +
            "</where>" +
            "</script>")
    public List<AssetMaintenance> selectAssetMaintenanceList(AssetMaintenance assetMaintenance);

    /** 新增 */
    @Insert("insert into asset_maintenance(maintenance_no, asset_no, apply_user_id, apply_dept_id, reason, status, create_time) " +
            "values(#{maintenanceNo}, #{assetNo}, #{applyUserId}, #{applyDeptId}, #{reason}, #{status}, #{createTime})")
    public int insertAssetMaintenance(AssetMaintenance assetMaintenance);

    /** 更新 */
    @Update("<script>" +
            "update asset_maintenance " +
            "<set> " +
            "<if test=\"status != null\">status = #{status},</if> " +
            "</set> " +
            "where maintenance_no = #{maintenanceNo}" +
            "</script>")
    public int updateAssetMaintenance(AssetMaintenance assetMaintenance);
}
