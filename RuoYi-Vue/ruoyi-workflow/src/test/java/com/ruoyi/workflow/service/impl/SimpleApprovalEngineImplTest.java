package com.ruoyi.workflow.service.impl;

import java.util.List;
import com.ruoyi.workflow.domain.WfApprovalInstance;
import com.ruoyi.workflow.domain.WfApprovalTemplate;
import com.ruoyi.workflow.domain.vo.WorkflowTaskVo;
import com.ruoyi.workflow.mapper.WfApprovalInstanceMapper;
import com.ruoyi.workflow.mapper.WfApprovalNodeMapper;
import com.ruoyi.workflow.mapper.WfApprovalTemplateMapper;
import com.ruoyi.workflow.service.WorkflowBusinessHandlerRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SimpleApprovalEngine 的最小闭环测试。
 *
 * 这组测试先锁住“实例落库、任务可见、审批状态可回写”这三件事，
 * 避免后续在接资产业务回调时还要回头补基础审批语义。
 */
@ExtendWith(MockitoExtension.class)
class SimpleApprovalEngineImplTest {

    @Mock
    private WfApprovalInstanceMapper approvalInstanceMapper;

    @Mock
    private WfApprovalNodeMapper approvalNodeMapper;

    @Mock
    private WfApprovalTemplateMapper approvalTemplateMapper;

    @Mock
    private WorkflowBusinessHandlerRegistry businessHandlerRegistry;

    @InjectMocks
    private SimpleApprovalEngineImpl approvalEngine;

    @Test
    void shouldPersistPendingInstanceWhenStartProcess() {
        when(approvalTemplateMapper.selectEnabledTemplateByBusinessType("asset_requisition"))
            .thenReturn(buildTemplate("asset_requisition", 4001L));
        when(approvalInstanceMapper.insertWfApprovalInstance(any(WfApprovalInstance.class))).thenAnswer(invocation -> {
            WfApprovalInstance instance = invocation.getArgument(0);
            instance.setInstanceId(1001L);
            return 1;
        });

        Long instanceId = approvalEngine.startProcess("REQ-20260315-001", "asset_requisition");

        assertEquals(1001L, instanceId);

        ArgumentCaptor<WfApprovalInstance> captor = ArgumentCaptor.forClass(WfApprovalInstance.class);
        verify(approvalInstanceMapper).insertWfApprovalInstance(captor.capture());
        assertEquals("REQ-20260315-001", captor.getValue().getBusinessId());
        assertEquals("asset_requisition", captor.getValue().getBusinessType());
        assertEquals(4001L, captor.getValue().getApproverId());
        assertEquals("pending", captor.getValue().getStatus());
        assertNotNull(captor.getValue().getCreateTime());
    }

    @Test
    void shouldReturnNormalizedTodoTasks() {
        WorkflowTaskVo task = new WorkflowTaskVo();
        task.setInstanceId(1001L);
        task.setBusinessId("REQ-20260315-001");
        task.setBusinessType("asset_requisition");
        task.setStatus("pending");
        task.setCurrentNode("待审批");
        when(approvalInstanceMapper.selectTodoTaskList(2001L)).thenReturn(List.of(task));

        List<WorkflowTaskVo> tasks = approvalEngine.getTasks(2001L);

        assertEquals(1, tasks.size());
        assertEquals("REQUISITION", tasks.get(0).getBusinessType());
        assertEquals("IN_PROGRESS", tasks.get(0).getStatus());
    }

    @Test
    void shouldReturnNormalizedDoneTasks() {
        WorkflowTaskVo task = new WorkflowTaskVo();
        task.setInstanceId(1002L);
        task.setBusinessId("DSP-20260315-001");
        task.setBusinessType("asset_disposal");
        task.setStatus("approved");
        task.setCurrentNode("审批完成");
        task.setAction("approve");
        when(approvalInstanceMapper.selectDoneTaskList(2001L)).thenReturn(List.of(task));

        List<WorkflowTaskVo> tasks = approvalEngine.getDoneTasks(2001L);

        assertEquals(1, tasks.size());
        assertEquals("SCRAP", tasks.get(0).getBusinessType());
        assertEquals("COMPLETED", tasks.get(0).getStatus());
    }

    @Test
    void shouldAppendNodeAndMarkInstanceApprovedWhenApprove() {
        WfApprovalInstance instance = new WfApprovalInstance();
        instance.setInstanceId(1003L);
        instance.setBusinessId("REQ-20260315-002");
        instance.setBusinessType("asset_requisition");
        instance.setApproverId(3001L);
        instance.setStatus("pending");
        when(approvalInstanceMapper.selectWfApprovalInstanceByInstanceId(1003L)).thenReturn(instance);

        approvalEngine.approve(1003L, 3001L, "同意");

        verify(approvalNodeMapper).insertWfApprovalNode(any());

        ArgumentCaptor<WfApprovalInstance> captor = ArgumentCaptor.forClass(WfApprovalInstance.class);
        verify(approvalInstanceMapper).updateWfApprovalInstance(captor.capture());
        assertEquals(1003L, captor.getValue().getInstanceId());
        assertEquals("approved", captor.getValue().getStatus());
        verify(businessHandlerRegistry).handleApproved("asset_requisition", "REQ-20260315-002");
    }

    @Test
    void shouldAppendNodeAndMarkInstanceRejectedWhenReject() {
        WfApprovalInstance instance = new WfApprovalInstance();
        instance.setInstanceId(1004L);
        instance.setBusinessId("MNT-20260315-001");
        instance.setBusinessType("asset_maintenance");
        instance.setApproverId(3002L);
        instance.setStatus("pending");
        when(approvalInstanceMapper.selectWfApprovalInstanceByInstanceId(1004L)).thenReturn(instance);

        approvalEngine.reject(1004L, 3002L, "驳回");

        verify(approvalNodeMapper).insertWfApprovalNode(any());

        ArgumentCaptor<WfApprovalInstance> captor = ArgumentCaptor.forClass(WfApprovalInstance.class);
        verify(approvalInstanceMapper).updateWfApprovalInstance(captor.capture());
        assertEquals(1004L, captor.getValue().getInstanceId());
        assertEquals("rejected", captor.getValue().getStatus());
        verify(businessHandlerRegistry).handleRejected("asset_maintenance", "MNT-20260315-001");
    }

    /**
     * 当前引擎按业务类型绑定单一审批人，测试里复用这个最小模板即可。
     */
    private WfApprovalTemplate buildTemplate(String businessType, Long approverId) {
        WfApprovalTemplate template = new WfApprovalTemplate();
        template.setBusinessType(businessType);
        template.setTemplateName("默认审批模板");
        template.setApproverId(approverId);
        template.setStatus("0");
        return template;
    }
}
