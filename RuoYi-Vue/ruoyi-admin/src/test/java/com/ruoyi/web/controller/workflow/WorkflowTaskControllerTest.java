package com.ruoyi.web.controller.workflow;

import java.lang.reflect.Field;
import java.util.List;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.workflow.domain.dto.WorkflowTaskQueryReq;
import com.ruoyi.workflow.domain.vo.WorkflowTaskVo;
import com.ruoyi.workflow.service.IApprovalEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowTaskControllerTest {

    @Mock
    private IApprovalEngine approvalEngine;

    private TestableWorkflowTaskController controller;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        controller = new TestableWorkflowTaskController(2001L);
        injectField(controller, "approvalEngine", approvalEngine);
    }

    @Test
    void shouldFilterTodoTasksByUnifiedAliases() {
        when(approvalEngine.getTasks(2001L)).thenReturn(List.of(
            buildTask("REQ-20260317-001", "REQUISITION", "IN_PROGRESS"),
            buildTask("DIS-20260317-001", "SCRAP", "COMPLETED")
        ));

        WorkflowTaskQueryReq query = new WorkflowTaskQueryReq();
        query.setBizNo("REQ-20260317-001");
        query.setBizType("REQUISITION");
        query.setWfStatus("IN_PROGRESS");

        TableDataInfo result = controller.todo(query);

        assertEquals(1, result.getRows().size());
        WorkflowTaskVo task = (WorkflowTaskVo) result.getRows().get(0);
        assertEquals("REQ-20260317-001", task.getBizNo());
        assertEquals("REQUISITION", task.getBizType());
        assertEquals("IN_PROGRESS", task.getWfStatus());
    }

    @Test
    void shouldAllowLegacyQueryFieldsForDoneTasks() {
        when(approvalEngine.getDoneTasks(2001L)).thenReturn(List.of(
            buildLegacyTask("MNT-20260317-001", "REPAIR", "COMPLETED"),
            buildLegacyTask("RED-20260317-001", "REAL_ESTATE_DISPOSAL", "REJECTED")
        ));

        WorkflowTaskQueryReq query = new WorkflowTaskQueryReq();
        query.setBusinessId("MNT-20260317-001");
        query.setBusinessType("REPAIR");
        query.setStatus("COMPLETED");

        TableDataInfo result = controller.done(query);

        assertEquals(1, result.getRows().size());
        WorkflowTaskVo task = (WorkflowTaskVo) result.getRows().get(0);
        assertEquals("MNT-20260317-001", task.getBusinessId());
        assertEquals("REPAIR", task.getBusinessType());
        assertEquals("COMPLETED", task.getStatus());
    }

    private WorkflowTaskVo buildTask(String bizNo, String bizType, String wfStatus) {
        WorkflowTaskVo task = new WorkflowTaskVo();
        task.setInstanceId(1L);
        task.setBusinessId(bizNo);
        task.setBusinessType(bizType);
        task.setStatus(wfStatus);
        task.setBizNo(bizNo);
        task.setBizType(bizType);
        task.setWfStatus(wfStatus);
        return task;
    }

    private WorkflowTaskVo buildLegacyTask(String businessId, String businessType, String status) {
        WorkflowTaskVo task = new WorkflowTaskVo();
        task.setInstanceId(2L);
        task.setBusinessId(businessId);
        task.setBusinessType(businessType);
        task.setStatus(status);
        return task;
    }

    private void injectField(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        Field field = WorkflowTaskController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static final class TestableWorkflowTaskController extends WorkflowTaskController {
        private final Long userId;

        private TestableWorkflowTaskController(Long userId) {
            this.userId = userId;
        }

        @Override
        protected void startPage() {
            // 单元测试中不依赖分页上下文，只验证过滤口径。
        }

        @Override
        public Long getUserId() {
            return userId;
        }
    }
}
