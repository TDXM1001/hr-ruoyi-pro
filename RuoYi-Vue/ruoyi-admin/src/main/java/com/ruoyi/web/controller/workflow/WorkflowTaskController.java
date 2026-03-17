package com.ruoyi.web.controller.workflow;

import java.util.Collections;
import java.util.List;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.workflow.domain.dto.WorkflowApproveReq;
import com.ruoyi.workflow.domain.dto.WorkflowTaskQueryReq;
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
 * 流程任务控制器。
 *
 * 当前主要服务于资产系统的待办、已办和审批闭环，同时兼容新旧查询口径。
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
    public TableDataInfo todo(WorkflowTaskQueryReq queryReq) {
        List<WorkflowTaskVo> list = filterTasks(approvalEngine.getTasks(getUserId()), queryReq);
        return buildTaskTable(list, queryReq);
    }

    /**
     * 查询当前登录人的已办任务。
     */
    @PreAuthorize("@ss.hasPermi('workflow:task:done')")
    @GetMapping("/done")
    public TableDataInfo done(WorkflowTaskQueryReq queryReq) {
        List<WorkflowTaskVo> list = filterTasks(approvalEngine.getDoneTasks(getUserId()), queryReq);
        return buildTaskTable(list, queryReq);
    }

    /**
     * 审批任务。
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

    /**
     * 按统一别名过滤任务，同时兼容历史查询字段。
     */
    private List<WorkflowTaskVo> filterTasks(List<WorkflowTaskVo> list, WorkflowTaskQueryReq queryReq) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        if (queryReq == null) {
            return list;
        }
        String bizNo = firstNonBlank(queryReq.getBizNo(), queryReq.getBusinessId());
        String bizType = firstNonBlank(queryReq.getBizType(), queryReq.getBusinessType());
        String wfStatus = firstNonBlank(queryReq.getWfStatus(), queryReq.getStatus());
        return list.stream()
            .filter(task -> matchField(resolveBizNo(task), bizNo))
            .filter(task -> matchField(resolveBizType(task), bizType))
            .filter(task -> matchField(resolveWfStatus(task), wfStatus))
            .toList();
    }

    /**
     * 过滤后重新组装分页结果，避免前端拿到的总数和行数据不一致。
     */
    private TableDataInfo buildTaskTable(List<WorkflowTaskVo> list, WorkflowTaskQueryReq queryReq) {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setTotal(list == null ? 0 : list.size());
        rspData.setRows(paginateTasks(list, queryReq));
        return rspData;
    }

    private List<WorkflowTaskVo> paginateTasks(List<WorkflowTaskVo> list, WorkflowTaskQueryReq queryReq) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        if (queryReq == null || queryReq.getPageNum() == null || queryReq.getPageSize() == null) {
            return list;
        }
        if (queryReq.getPageNum() < 1 || queryReq.getPageSize() < 1) {
            return list;
        }
        int fromIndex = Math.min((queryReq.getPageNum() - 1) * queryReq.getPageSize(), list.size());
        int toIndex = Math.min(fromIndex + queryReq.getPageSize(), list.size());
        return list.subList(fromIndex, toIndex);
    }

    private boolean matchField(String actualValue, String expectedValue) {
        if (StringUtils.isBlank(expectedValue)) {
            return true;
        }
        return StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(actualValue), StringUtils.trimToEmpty(expectedValue));
    }

    private String resolveBizNo(WorkflowTaskVo task) {
        return firstNonBlank(task == null ? null : task.getBizNo(), task == null ? null : task.getBusinessId());
    }

    private String resolveBizType(WorkflowTaskVo task) {
        return firstNonBlank(task == null ? null : task.getBizType(), task == null ? null : task.getBusinessType());
    }

    private String resolveWfStatus(WorkflowTaskVo task) {
        return firstNonBlank(task == null ? null : task.getWfStatus(), task == null ? null : task.getStatus());
    }

    private String firstNonBlank(String primaryValue, String fallbackValue) {
        return StringUtils.isNotBlank(primaryValue) ? primaryValue : fallbackValue;
    }
}
