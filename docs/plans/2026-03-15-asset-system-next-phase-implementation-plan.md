# 资产模块下一阶段系统化实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 在现有资产聚合模型、分类模板管理和台账边界已经收稳的基础上，完成“固定资产生命周期 + 最小审批闭环 + 菜单权限收口”的系统化落地，使资产模块进入可演示、可联调、可验收状态。

**Architecture:** 采用“审批最小可用闭环优先”的推进方式。先补齐 `ruoyi-workflow` 当前缺失的持久化和任务接口，再把领用、归还、维修、报废/处置这些固定资产关键动作挂到统一审批回调链路上，最后统一修正前端页面、菜单 SQL、权限标识和系统验收口径。当前阶段不继续扩张不动产权属变更等专属动作，也不把工作流扩展成重量级引擎。

**Tech Stack:** Java 17、Spring Boot 3、MyBatis、MySQL、Vue 3、TypeScript、Element Plus、Vitest、RuoYi

---

## 推进路线选择

### 方案 A：固定资产闭环 + 最小审批落地（推荐）

- 先补齐 `workflow/task` 后端接口和审批状态落库。
- 再打通领用归还、维修、报废/处置的前后端闭环。
- 同步修复菜单、权限、路由、SQL 命名漂移。

优点：

- 最符合当前代码现状。`ruoyi-asset` 里的领用、维修、处置服务已经依赖 `IApprovalEngine`，继续绕开审批只会制造二次返工。
- 可以直接复用现有 `art-design-pro/src/views/asset/workflow` 页面，而不是继续保留只有前端、没有后端的半成品。
- 能最快形成“台账 -> 发起动作 -> 审批 -> 业务回写 -> 台账回看”的完整演示链路。

缺点：

- 需要同时动 `ruoyi-workflow`、`ruoyi-asset`、`ruoyi-admin`、`art-design-pro` 和 SQL 脚本，改动面比只做前端页面更大。

### 方案 B：继续推迟审批，只做业务页面闭环

- 先补归还、维修、报废页面和接口。
- 维持当前工作流页面不上线，业务单据采用“默认通过”或“伪审批”。

优点：

- 短期代码量更小。

缺点：

- 与当前 `ruoyi-asset` 服务层已经接入 `IApprovalEngine` 的事实冲突。
- 业务单据状态会继续停留在“审批中”，归还、完成维修、正式报废都缺少统一状态出口。
- 后续再补工作流时会再次改动业务状态流转。

### 方案 C：不动产专属生命周期优先

- 先实现权属变更、用途变更、状态变更。
- 固定资产的审批和维修/报废闭环延后。

优点：

- 贴合长期业务蓝图。

缺点：

- 当前系统里最明显的缺口不在不动产，而在固定资产主链路和工作流断点。
- 会放大前期设计收益，却不能尽快解决现网级“入口可见但无法走通”的问题。

**推荐结论：** 采用方案 A。当前阶段最应该优先解决的是“系统里已经出现的半成品链路”，而不是继续平铺新能力。

## 当前系统判断

### 已完成基线

- 资产主档、财务、不动产、动态属性、附件、折旧日志已经完成聚合建模和前后端第一轮对接。
- 资产分类模板管理、台账实例填值边界、固定资产/不动产生命周期入口分流已经完成。
- 资产列表、编辑抽屉、财务弹窗、领用申请已经切到 `assetId` 主键口径。

### 当前关键缺口

- `art-design-pro/src/api/workflow/task.ts` 和 `art-design-pro/src/views/asset/workflow/*` 已存在，但后端缺少 `/workflow/task/todo`、`/workflow/task/done`、`/workflow/task/approve` 控制器。
- `AssetRequisitionServiceImpl`、`AssetMaintenanceServiceImpl`、`AssetDisposalServiceImpl` 已经调用 `IApprovalEngine`，但 `SimpleApprovalEngineImpl` 仍是占位实现，`approve`/`reject` 没有真正回写业务状态。
- `art-design-pro/src/api/asset/requisition.ts` 已有 `returnAsset`，但 `AssetRequisitionController` 还没有对应归还接口。
- `RuoYi-Vue/sql/20260312_asset_workflow_menu.sql` 仍使用 `asset/repair/index`、`asset/scrap/index` 和 `asset:repair:*`、`asset:scrap:*`，与当前后端控制器 `maintenance` / `disposal` 命名不一致。
- `art-design-pro/src/views/asset/list/index.vue` 的维修、报废、权属变更仍是占位入口。
- `AssetMaintenanceMapper.xml`、`AssetDisposalMapper.xml` 目前在资源目录下缺失，存在运行期找不到 SQL 映射的风险。
- `npm run build` 仍被 `monitor` / `system` 目录历史 TypeScript 问题阻塞，资产模块无法单独宣称“全仓构建绿”。

## 本阶段非目标

- 不实现 Flowable 或其他重量级工作流引擎切换。
- 不实现不动产权属变更、用途变更、状态变更的正式页面和 API。
- 不在本阶段重做资产列表和编辑抽屉整体交互结构。
- 不把分类模板维护重新塞回资产台账抽屉。

## 批次拆分

- 批次一：补齐 `ruoyi-workflow` 最小可用审批链路。
- 批次二：打通固定资产领用归还、维修、报废/处置闭环。
- 批次三：修正菜单 SQL、权限标识、前端页面路由和占位入口。
- 批次四：做系统联调、回归验证和发布前收口。

### 当前完成状态

- [x] Task 1：`ruoyi-workflow` 最小可用审批链路已落地并通过 `SimpleApprovalEngineImplTest`
- [x] Task 2：审批结果到资产业务状态的回写桥接已落地并通过 `AssetWorkflowBusinessHandlerTest`
- [x] Task 3：固定资产领用归还、维修、报废/处置后端主链路已打通并通过相关服务测试
- [x] Task 4：前端领用归还、维修、报废/处置页面联动已完成，并通过资产相关 Vitest 与局部 lint
- [x] Task 5：菜单 SQL、权限标识补丁与契约测试已完成
- [x] Task 6：接口契约说明、聚焦回归验证和执行记录已完成

### 执行备注

- `ruoyi-asset` 聚焦测试在当前多模块工程下使用 reactor 方式验证：`mvn -pl ruoyi-asset,ruoyi-workflow -am ...`
- `art-design-pro` 的 `npm run build` 仍被 `monitor/system` 目录历史 TypeScript 问题阻塞，本次资产模块变更未新增这些报错
- 固定资产生命周期系统化基线完成后，不动产专属生命周期改由 `docs/plans/2026-03-15-asset-real-estate-lifecycle-next-phase-implementation-plan.md` 单独承接
- 因此本计划“本阶段非目标”里关于不动产正式页面与 API 的限制，只约束当前固定资产基线批次，不再约束后续不动产专项实现

### Task 1: 落地 `ruoyi-workflow` 最小可用任务链路

**Files:**

- Create: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/mapper/WfApprovalInstanceMapper.java`
- Create: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/mapper/WfApprovalNodeMapper.java`
- Create: `RuoYi-Vue/ruoyi-workflow/src/main/resources/mapper/workflow/WfApprovalInstanceMapper.xml`
- Create: `RuoYi-Vue/ruoyi-workflow/src/main/resources/mapper/workflow/WfApprovalNodeMapper.xml`
- Create: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/domain/vo/WorkflowTaskVo.java`
- Create: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/domain/dto/WorkflowApproveReq.java`
- Create: `RuoYi-Vue/ruoyi-workflow/src/test/java/com/ruoyi/workflow/service/impl/SimpleApprovalEngineImplTest.java`
- Create: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/workflow/WorkflowTaskController.java`
- Modify: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/service/IApprovalEngine.java`
- Modify: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/service/impl/SimpleApprovalEngineImpl.java`

**Step 1: 先写审批引擎失败测试**

在 `SimpleApprovalEngineImplTest.java` 中至少锁定以下行为：

- `startProcess` 会写入 `wf_approval_instance`
- `getTasks` 能返回待办列表
- `approve` 会把实例状态从 `pending` 更新为 `approved`
- `reject` 会把实例状态更新为 `rejected`
- 审批动作会追加 `wf_approval_node` 记录

**Step 2: 运行测试确认失败**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-workflow -Dtest=SimpleApprovalEngineImplTest test`

Expected: FAIL，原因应为当前审批引擎仍是内存占位实现，没有 Mapper 和落库逻辑。

**Step 3: 补齐 Mapper、XML 和引擎实现**

最小实现要求：

- `startProcess` 写入实例表，初始状态为 `pending`
- `getTasks(approverId)` 返回待办任务列表
- `approve` / `reject` 同时更新实例表和节点表
- 统一返回 `WorkflowTaskVo`，供前端待办/已办页面直接消费

**Step 4: 新增工作流任务控制器**

控制器至少提供：

- `GET /workflow/task/todo`
- `GET /workflow/task/done`
- `POST /workflow/task/approve`

权限标识本阶段统一收敛为：

- `workflow:task:todo`
- `workflow:task:done`
- `workflow:task:approve`

**Step 5: 运行模块测试和编译验证**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-workflow -Dtest=SimpleApprovalEngineImplTest test`

Expected: PASS

Run: `cd RuoYi-Vue && mvn -pl ruoyi-workflow -am -DskipTests=true compile`

Expected: `BUILD SUCCESS`

**Step 6: 提交**

```bash
git add RuoYi-Vue/ruoyi-workflow RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/workflow/WorkflowTaskController.java
git commit -m "feat: implement minimal workflow task persistence and apis"
```

### Task 2: 建立审批结果到资产业务状态的回写桥接

**Files:**

- Create: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/service/WorkflowBusinessHandler.java`
- Create: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/service/WorkflowBusinessHandlerRegistry.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetWorkflowBusinessHandler.java`
- Modify: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/service/impl/SimpleApprovalEngineImpl.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRequisitionServiceImpl.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetMaintenanceServiceImpl.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetDisposalServiceImpl.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetWorkflowBusinessHandlerTest.java`

**Step 1: 先写失败测试，锁定审批回写规则**

测试至少覆盖：

- 领用审批通过时，领用单状态变为 `1`，资产保持 `2=领用中`
- 领用审批驳回时，领用单状态变为 `2`，资产回退到 `1=在用`
- 维修审批通过时，维修单状态变为 `1`，资产保持 `3=维修中`
- 维修审批驳回时，维修单状态变为 `2`，资产回退到 `1=在用`
- 处置审批通过时，处置单状态变为 `1`，资产保持 `5=已报废` 或 `6=已处置`
- 处置审批驳回时，处置单状态变为 `2`，资产回退到 `1=在用`

**Step 2: 运行测试确认失败**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetWorkflowBusinessHandlerTest test`

Expected: FAIL，原因应为当前没有统一的审批回调桥接。

**Step 3: 实现业务回调注册机制**

要求：

- `ruoyi-workflow` 只定义回调接口和注册器，不直接依赖 `ruoyi-asset`
- `ruoyi-asset` 提供 `AssetWorkflowBusinessHandler`，按 `businessType` 处理 `asset_requisition`、`asset_maintenance`、`asset_disposal`
- 审批动作完成后由审批引擎统一触发业务回调

**Step 4: 验证编译与测试**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetWorkflowBusinessHandlerTest,AssetRequisitionServiceImplTest test`

Expected: PASS

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset,ruoyi-workflow -am -DskipTests=true compile`

Expected: `BUILD SUCCESS`

**Step 5: 提交**

```bash
git add RuoYi-Vue/ruoyi-workflow RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetWorkflowBusinessHandler.java RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetWorkflowBusinessHandlerTest.java
git commit -m "feat: bridge workflow approvals to asset lifecycle status"
```

### Task 3: 打通固定资产领用归还、维修、报废/处置主链路

**Files:**

- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRequisitionController.java`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetMaintenanceController.java`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetDisposalController.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetRequisitionService.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetMaintenanceService.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetDisposalService.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRequisitionServiceImpl.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetMaintenanceServiceImpl.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetDisposalServiceImpl.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetRequisitionMapper.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetMaintenanceMapper.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetDisposalMapper.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetRequisitionMapper.xml`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetMaintenanceMapper.xml`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetDisposalMapper.xml`
- Modify: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRequisitionServiceImplTest.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetMaintenanceServiceImplTest.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetDisposalServiceImplTest.java`

**Step 1: 先写失败测试**

测试至少锁定以下行为：

- `POST /asset/requisition/return/{requisitionNo}` 存在且归还后领用单状态变为 `3`，资产状态回到 `1=在用`
- 维修单存在“完成维修”动作，完成后资产状态回到 `1=在用`
- 报废/处置查询和详情接口可用于台账页展示
- `AssetMaintenanceMapper.xml`、`AssetDisposalMapper.xml` 在测试环境可正常加载

**Step 2: 运行测试确认失败**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetRequisitionServiceImplTest,AssetMaintenanceServiceImplTest,AssetDisposalServiceImplTest test`

Expected: FAIL，原因应包含归还接口缺失、维保/处置流程缺少完成动作或 Mapper XML 缺失。

**Step 3: 补齐后端动作和 Mapper**

本阶段最小交付要求：

- 领用：
  - 新增归还接口
  - 仅审批通过的领用单允许归还
- 维修：
  - 新增详情查询和“完成维修”接口
  - 完成维修后资产状态回到在用
- 报废/处置：
  - 新增详情查询接口
  - 状态口径与审批回写一致

**Step 4: 运行后端测试与编译**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetRequisitionServiceImplTest,AssetMaintenanceServiceImplTest,AssetDisposalServiceImplTest,AssetWorkflowBusinessHandlerTest test`

Expected: PASS

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -am -DskipTests=true compile`

Expected: `BUILD SUCCESS`

**Step 5: 提交**

```bash
git add RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl
git commit -m "feat: complete fixed asset lifecycle backend actions"
```

### Task 4: 完成前端审批、领用归还、维修、报废/处置页面联动

**Files:**

- Modify: `art-design-pro/src/api/workflow/task.ts`
- Modify: `art-design-pro/src/api/asset/requisition.ts`
- Create: `art-design-pro/src/api/asset/maintenance.ts`
- Create: `art-design-pro/src/api/asset/disposal.ts`
- Modify: `art-design-pro/src/views/asset/workflow/todo/index.vue`
- Modify: `art-design-pro/src/views/asset/workflow/done/index.vue`
- Modify: `art-design-pro/src/views/asset/requisition/index.vue`
- Create: `art-design-pro/src/views/asset/maintenance/index.vue`
- Create: `art-design-pro/src/views/asset/disposal/index.vue`
- Modify: `art-design-pro/src/views/asset/list/index.vue`
- Create: `art-design-pro/tests/api/asset-maintenance.test.ts`
- Create: `art-design-pro/tests/api/asset-disposal.test.ts`
- Modify: `art-design-pro/tests/api/asset-requisition.test.ts`
- Modify: `art-design-pro/tests/views/asset/requisition.helper.test.ts`

**Step 1: 先写失败的 API 与页面契约测试**

至少锁定以下事实：

- `returnAsset` 走 `/asset/requisition/return/{requisitionNo}`
- `maintenance.ts` 走 `/asset/maintenance/list`、`/asset/maintenance/{maintenanceNo}`、`/asset/maintenance`
- `disposal.ts` 走 `/asset/disposal/list`、`/asset/disposal/{disposalNo}`、`/asset/disposal`
- workflow 页面消费的待办/已办/审批接口与后端控制器一致

**Step 2: 运行测试确认失败**

Run: `cd art-design-pro && npx vitest run tests/api/asset-requisition.test.ts tests/api/asset-maintenance.test.ts tests/api/asset-disposal.test.ts`

Expected: FAIL，因为维修、处置 API 文件尚不存在，工作流接口契约也未被后端真正承接。

**Step 3: 补齐前端 API 和页面**

要求：

- 领用台账页恢复“归还”按钮，但只在审批通过后显示
- 资产列表页中的“维修”“报废/处置”入口不再只是占位提示，而是跳转到实际台账页或弹出真实表单
- `workflow/todo` 和 `workflow/done` 直接消费新后端接口，不再依赖虚构数据结构
- 维修、报废/处置页至少提供：
  - 列表查询
  - 从资产列表带入选中资产上下文
  - 新增申请表单
  - 状态列展示

**Step 4: 运行前端测试与局部 lint**

Run: `cd art-design-pro && npx vitest run tests/api/asset-requisition.test.ts tests/api/asset-maintenance.test.ts tests/api/asset-disposal.test.ts tests/views/asset/requisition.helper.test.ts`

Expected: PASS

Run: `cd art-design-pro && npm run lint -- src/api/asset src/api/workflow src/views/asset`

Expected: PASS

**Step 5: 提交**

```bash
git add art-design-pro/src/api/asset art-design-pro/src/api/workflow art-design-pro/src/views/asset art-design-pro/tests/api art-design-pro/tests/views/asset
git commit -m "feat: connect asset lifecycle pages with workflow and business apis"
```

### Task 5: 修正菜单 SQL、权限标识和系统暴露面

**Files:**

- Create: `RuoYi-Vue/sql/20260315_asset_lifecycle_workflow_menu_patch.sql`
- Modify: `RuoYi-Vue/sql/sql执行.md`
- Create: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/sql/AssetMenuSqlContractTest.java`

**Step 1: 先写失败的 SQL 契约测试**

测试至少锁定以下事实：

- 菜单组件路径使用 `asset/maintenance/index`、`asset/disposal/index`
- 权限标识使用 `asset:maintenance:*`、`asset:disposal:*`
- workflow 菜单权限与 `WorkflowTaskController` 保持一致
- 不再保留 `asset/repair/index`、`asset/scrap/index` 这些无实际页面的路径

**Step 2: 运行测试确认失败**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetMenuSqlContractTest test`

Expected: FAIL，因为现有脚本仍使用 `repair` / `scrap` 口径。

**Step 3: 编写增量补丁脚本**

要求：

- 不修改历史初始化脚本的既有含义，新增补丁脚本做幂等修正
- 若 workflow 控制器尚未随版本发布，则补丁脚本应允许先不启用审批中心菜单
- 同步补齐按钮权限，如：
  - `asset:requisition:return`
  - `asset:maintenance:query`
  - `asset:maintenance:add`
  - `asset:maintenance:complete`
  - `asset:disposal:query`
  - `asset:disposal:add`

**Step 4: 运行测试并更新执行说明**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetMenuSqlContractTest,AssetWorkflowSqlMigrationContractTest test`

Expected: PASS

**Step 5: 提交**

```bash
git add RuoYi-Vue/sql/20260315_asset_lifecycle_workflow_menu_patch.sql RuoYi-Vue/sql/sql执行.md RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/sql/AssetMenuSqlContractTest.java
git commit -m "docs: patch asset lifecycle and workflow menu contracts"
```

### Task 6: 做系统联调、回归验证和发布前收口

**Files:**

- Modify: `docs/plans/2026-03-14-asset-api-contract-notes.md`
- Verify only

**Step 1: 更新接口契约说明**

补充以下内容：

- workflow 待办、已办、审批接口
- 领用归还接口
- 维修详情/完成接口
- 报废处置详情接口
- 菜单权限和前端页面路径的最终口径

**Step 2: 运行后端聚焦测试**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-workflow -Dtest=SimpleApprovalEngineImplTest test`

Expected: PASS

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetRequisitionServiceImplTest,AssetMaintenanceServiceImplTest,AssetDisposalServiceImplTest,AssetWorkflowBusinessHandlerTest,AssetWorkflowSqlMigrationContractTest,AssetMenuSqlContractTest test`

Expected: PASS

**Step 3: 运行前端聚焦测试**

Run: `cd art-design-pro && npx vitest run tests/api/asset-requisition.test.ts tests/api/asset-maintenance.test.ts tests/api/asset-disposal.test.ts tests/api/asset-finance.test.ts tests/api/asset-category-attr.test.ts tests/views/asset/requisition.helper.test.ts tests/views/asset/asset-lifecycle.helper.test.ts`

Expected: PASS

**Step 4: 运行 lint 与构建**

Run: `cd art-design-pro && npm run lint -- src/api/asset src/api/workflow src/views/asset`

Expected: PASS

Run: `cd art-design-pro && npm run build`

Expected: 若本阶段仍被 `monitor` / `system` 历史问题阻塞，必须把阻塞文件和原因写入执行记录，不能归因到资产模块。

**Step 5: 手工验收闭环**

检查点：

- 资产列表可以发起领用、维修、报废/处置
- workflow 待办页能看到新建业务单据
- 审批通过后业务单据状态和资产状态同步变化
- 审批驳回后资产状态正确回退
- 领用审批通过后可以归还
- 维修审批通过后可以完成维修
- 菜单点击不会出现 404 或空白页

**Step 6: 检查最终工作区并提交**

Run: `git status --short`

Expected: 仅包含本阶段资产生命周期、workflow、菜单 SQL 和契约说明相关变更

```bash
git add docs/plans/2026-03-14-asset-api-contract-notes.md art-design-pro RuoYi-Vue
git commit -m "feat: complete systemized asset lifecycle and workflow baseline"
```

## 风险与对策

- 风险：`SimpleApprovalEngineImpl` 当前是占位实现，若只补控制器不补落库，workflow 页面仍然只是空壳。
  对策：先做 Task 1，不允许跳过。

- 风险：业务服务当前在发起申请时就改资产状态，若审批驳回没有回退逻辑，会导致台账脏状态。
  对策：Task 2 必须实现统一审批回调桥接。

- 风险：菜单脚本里的 `repair` / `scrap` 命名漂移会让系统菜单指向不存在页面。
  对策：Task 5 必须使用增量补丁修正，不直接依赖旧脚本。

- 风险：全仓 `npm run build` 仍被 `monitor` / `system` 历史类型问题阻塞，影响“项目整体构建绿”口径。
  对策：本阶段以资产聚焦验证为主，同时在执行记录中显式登记阻塞项；如要进入发布窗口，再单列“全仓构建绿化”并行任务。

## 完成定义

- `ruoyi-workflow` 已提供最小可用的待办、已办、审批接口和数据落库能力。
- 领用、归还、维修、报废/处置已形成“申请 -> 审批 -> 状态回写 -> 台账可见”的固定资产闭环。
- 前端不再保留“看起来能点、实际上必失败”的 lifecycle 或 workflow 入口。
- 菜单 SQL、权限标识、控制器路径、前端组件路径已统一。
- 系统已经具备继续进入“不动产专属生命周期”或“工作流能力增强”的稳定基线。
