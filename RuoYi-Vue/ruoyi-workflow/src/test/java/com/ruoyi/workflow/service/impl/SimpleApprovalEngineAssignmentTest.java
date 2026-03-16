package com.ruoyi.workflow.service.impl;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import com.ruoyi.common.exception.ServiceException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 审批模板分派基线测试。
 */
@ExtendWith(MockitoExtension.class)
class SimpleApprovalEngineAssignmentTest {

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
    void shouldAssignTemplateApproverWhenStartProcess() {
        when(approvalTemplateMapper.selectEnabledTemplateByBusinessType("asset_requisition"))
            .thenReturn(buildTemplate("asset_requisition", "资产领用审批", 1L));
        when(approvalInstanceMapper.insertWfApprovalInstance(any(WfApprovalInstance.class))).thenAnswer(invocation -> {
            WfApprovalInstance instance = invocation.getArgument(0);
            instance.setInstanceId(1001L);
            return 1;
        });

        approvalEngine.startProcess("REQ-20260316-001", "asset_requisition");

        ArgumentCaptor<WfApprovalInstance> captor = ArgumentCaptor.forClass(WfApprovalInstance.class);
        verify(approvalInstanceMapper).insertWfApprovalInstance(captor.capture());
        assertEquals("REQ-20260316-001", captor.getValue().getBusinessId());
        assertEquals("asset_requisition", captor.getValue().getBusinessType());
        assertEquals("1", readField(captor.getValue(), "approverId"));
    }

    @Test
    void shouldReturnTodoOnlyForAssignedApprover() {
        WorkflowTaskVo task = new WorkflowTaskVo();
        task.setInstanceId(1001L);
        task.setBusinessId("REQ-20260316-001");
        task.setBusinessType("asset_requisition");
        task.setStatus("pending");
        task.setApproverId(1L);
        when(approvalInstanceMapper.selectTodoTaskList(1L)).thenReturn(List.of(task));
        when(approvalInstanceMapper.selectTodoTaskList(2L)).thenReturn(Collections.emptyList());

        List<WorkflowTaskVo> adminTasks = approvalEngine.getTasks(1L);
        List<WorkflowTaskVo> otherTasks = approvalEngine.getTasks(2L);

        assertEquals(1, adminTasks.size());
        assertTrue(otherTasks.isEmpty());
    }

    @Test
    void shouldRejectStartProcessWhenTemplateMissing() {
        when(approvalTemplateMapper.selectEnabledTemplateByBusinessType("asset_requisition")).thenReturn(null);

        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> approvalEngine.startProcess("REQ-20260316-002", "asset_requisition")
        );

        assertTrue(exception.getMessage().contains("审批模板"));
        verify(approvalInstanceMapper, never()).insertWfApprovalInstance(any(WfApprovalInstance.class));
    }

    @Test
    void shouldRejectApproveWhenCurrentUserIsNotAssignedApprover() {
        WfApprovalInstance instance = new WfApprovalInstance();
        instance.setInstanceId(1002L);
        instance.setBusinessId("REQ-20260316-003");
        instance.setBusinessType("asset_requisition");
        instance.setStatus("pending");
        when(approvalInstanceMapper.selectWfApprovalInstanceByInstanceId(1002L)).thenReturn(instance);

        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> approvalEngine.approve(1002L, 2L, "越权审批")
        );

        assertTrue(exception.getMessage().contains("审批人"));
        verify(approvalNodeMapper, never()).insertWfApprovalNode(any());
    }

    private WfApprovalTemplate buildTemplate(String businessType, String templateName, Long approverId) {
        WfApprovalTemplate template = new WfApprovalTemplate();
        template.setBusinessType(businessType);
        template.setTemplateName(templateName);
        template.setApproverId(approverId);
        template.setStatus("0");
        return template;
    }

    /**
     * 红灯阶段通过反射读取待新增字段，保证测试先表达预期。
     */
    private String readField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(target);
            return value == null ? null : String.valueOf(value);
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }
}
