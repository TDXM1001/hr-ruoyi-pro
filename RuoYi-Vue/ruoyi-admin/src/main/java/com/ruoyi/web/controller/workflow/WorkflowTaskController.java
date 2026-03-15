package com.ruoyi.web.controller.workflow;

import java.util.List;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.workflow.domain.dto.WorkflowApproveReq;
import com.ruoyi.workflow.domain.vo.WorkflowTaskVo;
import com.ruoyi.workflow.service.IApprovalEngine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作流任务控制器。
 *
 * 当前阶段只暴露待办、已办、审批三个最小接口，
 * 先把资产生命周期需要的审批闭环跑通，不提前扩成完整 BPM 控制台。
 */
@RestController
@RequestMapping("/workflow/task")
public class WorkflowTaskController extends BaseController {

    @Autowired
    private IApprovalEngine approvalEngine;

    /**
     * 查询当前登录人的待办任务。
     */
    @PreAuthorize("@ss.hasPermi('workflow:task:todo')")
    @GetMapping("/todo")
    public TableDataInfo todo() {
        startPage();
        List<WorkflowTaskVo> list = approvalEngine.getTasks(getUserId());
        return getDataTable(list);
    }

    /**
     * 查询当前登录人的已办任务。
     */
    @PreAuthorize("@ss.hasPermi('workflow:task:done')")
    @GetMapping("/done")
    public TableDataInfo done() {
        startPage();
        List<WorkflowTaskVo> list = approvalEngine.getDoneTasks(getUserId());
        return getDataTable(list);
    }

    /**
     * 审批任务。
     *
     * 前端把同意和驳回统一提交到一个入口，因此这里按 action 分流，
     * 可以避免前端维护两套接口地址，也便于后续扩展转办。
     */
    @PreAuthorize("@ss.hasPermi('workflow:task:approve')")
    @PostMapping("/approve")
    public AjaxResult approve(@RequestBody WorkflowApproveReq approveReq) {
        if (approveReq.getInstanceId() == null) {
            return error("审批实例ID不能为空");
        }
        if (StringUtils.isBlank(approveReq.getAction())) {
            return error("审批动作不能为空");
        }

        if ("approve".equalsIgnoreCase(approveReq.getAction())) {
            approvalEngine.approve(approveReq.getInstanceId(), getUserId(), approveReq.getComment());
            return success("审批通过");
        }
        if ("reject".equalsIgnoreCase(approveReq.getAction())) {
            approvalEngine.reject(approveReq.getInstanceId(), getUserId(), approveReq.getComment());
            return success("审批驳回");
        }
        return error("暂不支持的审批动作: " + approveReq.getAction());
    }
}
