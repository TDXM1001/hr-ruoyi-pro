package com.ruoyi.asset.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 不动产占用释放入参。
 *
 * @author Codex
 */
public class AssetRealEstateOccupancyReleaseBo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "释放日期不能为空")
    private String endDate;

    @NotBlank(message = "释放原因不能为空")
    @Size(max = 500, message = "释放原因长度不能超过500个字符")
    private String releaseReason;

    public String getEndDate()
    {
        return endDate;
    }

    public void setEndDate(String endDate)
    {
        this.endDate = endDate;
    }

    public String getReleaseReason()
    {
        return releaseReason;
    }

    public void setReleaseReason(String releaseReason)
    {
        this.releaseReason = releaseReason;
    }
}
