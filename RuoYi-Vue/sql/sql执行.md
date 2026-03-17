# SQL 脚本执行顺序指引

为了确保系统功能正常运行，请按以下顺序在数据库中执行 SQL 脚本。

## 1. 基础环境初始化
首先执行若依框架的基础脚本，建立系统核心表结构和初始数据。

1.  **`ry_20250522.sql`**
    *   **用途**：初始化若依核心表（用户、角色、部门、菜单、字典、配置等）及数据。
    *   **重要性**：所有后续业务功能的基础。
2.  **`quartz.sql`**
    *   **用途**：初始化定时任务（Quartz）相关的表结构。
    *   **状态**：如果你需要使用定时任务功能，请务必执行。

## 2. 资产管理模块初始化（Phase 1）
建立资产管理相关的业务基础表及初始配置。

3.  **`20260311_asset_management_init.sql`**
    *   **用途**：创建资产分类表 (`asset_category`) 和资产台账主表 (`asset_info`)。
4.  **`20260314_asset_data_model_refactor.sql`**
    *   **用途**：
        *   将 `asset_info` 从业务编码主键升级为内部 `asset_id` 主键。
        *   增加资产财务表、不动产专表、扩展字段定义和值表、附件表、折旧日志表。
        *   回填旧资产主档的基础兼容数据。
    *   **依赖**：依赖 `20260311_asset_management_init.sql` 已先执行。
5.  **`20260311_asset_dicts.sql`**
    *   **用途**：初始化资产相关的字典数据（如：资产类型、资产状态）。
    *   **依赖**：依赖 `sys_dict_type` 和 `sys_dict_data` 表。
6.  **`20260311_asset_menu_init.sql`**
    *   **用途**：初始化资产管理系统的菜单和权限按钮。
    *   **依赖**：依赖 `sys_menu` 表。

## 3. 审批引擎与业务流程（Phase 2）
增加流程引擎相关表以及具体的业务申请单据。

7.  **`20260312_asset_workflow_business.sql`**
    *   **用途**：
        *   审批引擎核心表 (`wf_approval_template`, `wf_approval_instance`, `wf_approval_node`)。
        *   资产业务流水表（领用单、采购单、调拨单、维修单、处置单）。
    *   **说明**：
        *   当前版本已在领用、维修、处置单中直接包含 `asset_id` 字段。
        *   当前版本的 `wf_approval_template` 已升级为审批模板基线结构，包含 `template_name`、`approver_id`、`status`。
        *   当前版本的 `wf_approval_instance` 已包含 `approver_id`，用于把待办真正收敛到指定审批人。
        *   脚本会初始化几条默认审批模板，默认 `approver_id = 1`（通常是 `admin`）；如果你的环境要由其他人处理待办，执行后请立即改成真实审批人。
8.  **`20260312_asset_workflow_menu.sql`**
    *   **用途**：初始化审批中心的菜单（待办、已办）以及资产业务记录菜单。
    *   **依赖**：依赖 `sys_menu` 表，且会自动查找“资产系统”父菜单 ID。

## 4. 存量库增量修复（按需执行）
仅在数据库已经执行过旧版业务流水脚本时补充执行。

9.  **`20260314_asset_workflow_asset_id_patch.sql`**
    *   **适用场景**：数据库已经执行过旧版 `20260312_asset_workflow_business.sql`，当时领用、维修、处置单还没有 `asset_id` 字段。
    *   **依赖**：
        *   `20260314_asset_data_model_refactor.sql` 已执行，`asset_info.asset_id` 已生成。
        *   `20260312_asset_workflow_business.sql` 已执行，业务流水表已存在。
    *   **用途**：
        *   为 `asset_requisition`、`asset_maintenance`、`asset_disposal` 补充 `asset_id` 字段。
        *   根据 `asset_no` 回填历史数据。
        *   补充索引并收紧为非空约束。
10. **`20260315_asset_lifecycle_workflow_menu_patch.sql`**
    *   **适用场景**：数据库已经执行过旧版 `20260312_asset_workflow_menu.sql`，菜单仍保留 `repair/scrap` 命名或 workflow 权限标识尚未收敛到 `workflow:task:*`。
    *   **用途**：
        *   把维修、报废/处置菜单组件路径修正为 `asset/maintenance/index`、`asset/disposal/index`。
        *   把审批中心菜单权限修正为 `workflow:task:todo`、`workflow:task:done`，并补充 `workflow:task:approve` 按钮权限。
        *   补齐 `asset:requisition:return`、`asset:maintenance:*`、`asset:disposal:*` 等按钮权限。
    *   **说明**：脚本顶部提供 `@enable_workflow_center_menu` 开关；若当前环境暂不开放审批中心，可先改为 `0` 再执行。
11. **`20260315_asset_real_estate_lifecycle.sql`**
    *   **适用场景**：需要启用不动产权属变更、用途变更、状态变更、注销/处置四类生命周期动作。
    *   **依赖**：
        *   `20260314_asset_data_model_refactor.sql` 已执行，`asset_real_estate` 主表已存在。
        *   如需审批型动作流转，`20260312_asset_workflow_business.sql` 已执行，审批引擎表已存在。
    *   **用途**：
        *   创建 `asset_real_estate_ownership_change`、`asset_real_estate_usage_change`、`asset_real_estate_status_change`、`asset_real_estate_disposal` 四张动作单据表。
        *   为不动产动作保留前值、目标值、申请信息和单据状态，避免过程字段直接写回主档。
12. **`20260315_asset_real_estate_lifecycle_menu.sql`**
    *   **适用场景**：需要为不动产权属变更、用途变更、状态变更、注销/处置四类页面补齐菜单与按钮权限。
    *   **依赖**：
        *   `20260311_asset_menu_init.sql` 已执行，`资产系统` 父菜单已存在。
        *   前端已提供 `asset/real-estate/*/index` 四个页面组件。
    *   **用途**：
        *   创建 `asset/real-estate/ownership/index`、`asset/real-estate/usage/index`、`asset/real-estate/status/index`、`asset/real-estate/disposal/index` 四个台账菜单。
        *   补齐 `asset:realEstateOwnership:*`、`asset:realEstateUsage:*`、`asset:realEstateStatus:*`、`asset:realEstateDisposal:*` 的最小按钮权限。
13. **`20260316_asset_governance_patch.sql`**
    *   **适用场景**：数据库已执行过旧版字典或生命周期脚本，仍存在资产类型、资产状态、处置类型口径漂移；或者数据库已执行过旧版 `20260312_asset_workflow_business.sql`，审批模板表还没有 `template_name / approver_id / status` 等字段。
    *   **依赖**：
        *   建议 `20260314_asset_data_model_refactor.sql` 已执行，主档字段已统一为 `asset_type`、`asset_status`。
        *   `20260312_asset_workflow_business.sql` 已执行，`asset_disposal.disposal_type` 字段已存在。
    *   **用途**：
        *   把 `asset_type` 字典统一为 `1=固定资产 2=不动产`。
        *   把 `asset_status` 字典统一为 `1=在用 2=领用中 3=维修中 4=盘点中 5=已报废 6=已处置 7=闲置`。
        *   为历史 `asset_disposal.disposal_type` 空值回填默认 `scrap`，并给出 sell/transfer/donate 的治理说明。
        *   为旧版 `wf_approval_template` 自动补齐 `template_name`、`approver_id`、`status` 字段。
        *   为旧版 `wf_approval_instance` 自动补齐 `approver_id` 字段，并按模板回填历史实例的审批人。
        *   自动补齐最小审批模板数据，确保待办不会继续以“所有人可见”的旧方式运行。
        *   明确旧菜单旧命名需继续配合 `20260315_asset_lifecycle_workflow_menu_patch.sql` 收口。
14. **`20260317_asset_menu_permission_patch.sql`**
    *   **适用场景**：页面操作依赖 `asset:requisition:add/query` 或 `asset:finance:query`，但数据库未补齐对应按钮权限菜单。
    *   **依赖**：
        *   `20260311_asset_menu_init.sql` 已执行（需要 `asset:info:list` 菜单作为父节点）。
        *   `20260312_asset_workflow_menu.sql` 已执行（需要 `asset/requisition/index` 菜单作为父节点）。
    *   **用途**：
        *   补齐领用台账按钮权限：`asset:requisition:query`、`asset:requisition:add`。
        *   补齐资产台账财务查看权限：`asset:finance:query`。
        *   解决“前端按钮存在但因权限缺失被禁用”的问题。

## 执行建议

*   **全新库初始化**：按 1 到 8 的顺序执行；如果需要不动产生命周期动作，再继续执行第 11、12 个脚本。全新库直接执行最新版 `20260312_asset_workflow_business.sql` 时，审批模板相关字段已经在脚本里，不一定需要第 13 个脚本；但如果你希望统一历史字典/处置口径，或者想让脚本可重复执行，也可以继续执行第 13 个脚本。若要使用资产台账里的“领用申请/财务查看”动作，请再执行第 14 个脚本补齐按钮权限。
*   **已执行旧版脚本的升级库**：在完成现有初始化脚本后，额外执行第 9 个补丁脚本；如菜单仍是旧命名，再执行第 10 个补丁脚本；如需不动产生命周期，再执行第 11、12 个脚本；如字典、状态、`disposal_type` 或审批模板结构仍是旧口径，必须再执行第 13 个治理补丁脚本；如果前端按钮被权限判断禁用，再执行第 14 个菜单权限补丁脚本。
*   **审批模板执行后的必要动作**：无论是新库还是升级库，执行完第 7 或第 13 个脚本后，都建议立刻检查 `wf_approval_template.approver_id`。当前初始化默认写入 `1`，如果实际审批人不是 `admin`，请手工改成真实用户 ID，否则待办都会落到用户 `1`。
*   **推荐核对 SQL**：执行完第 7、13、14 个脚本后，建议至少核对下面几条：
    *   `select template_id, business_type, template_name, approver_id, status from wf_approval_template order by template_id;`
    *   `select instance_id, business_id, business_type, approver_id, status from wf_approval_instance order by instance_id desc limit 20;`
    *   `select dict_label, dict_value from sys_dict_data where dict_type = 'asset_status' order by dict_sort;`
    *   `select disposal_no, disposal_type from asset_disposal where disposal_type is null or disposal_type = '';`
*   **补丁执行失败时**：如果第 9 个脚本在收紧非空约束时失败，通常说明某些业务单据的 `asset_no` 无法匹配到 `asset_info`，需要先修正历史数据后再重跑补丁。

*   **报表预警 MVP（批次六）**：本批次不新增业务表结构 SQL，直接复用 `asset_info`、`asset_finance`、`asset_real_estate`、`asset_maintenance`、`wf_approval_instance` 等现有表；只要前述第 3 至第 14 个脚本按顺序完成，`/asset/report/summary` 与 `/asset/report/warnings` 即可工作。

## 典型执行方式

### 场景 A：全新数据库

按下面顺序执行：

1. `ry_20250522.sql`
2. `quartz.sql`（如果需要定时任务）
3. `20260311_asset_management_init.sql`
4. `20260314_asset_data_model_refactor.sql`
5. `20260311_asset_dicts.sql`
6. `20260311_asset_menu_init.sql`
7. `20260312_asset_workflow_business.sql`
8. `20260312_asset_workflow_menu.sql`
9. `20260315_asset_lifecycle_workflow_menu_patch.sql`（如果要使用当前最新菜单路径与审批权限）
10. `20260315_asset_real_estate_lifecycle.sql`（如果需要不动产动作）
11. `20260315_asset_real_estate_lifecycle_menu.sql`（如果需要不动产菜单）
12. `20260316_asset_governance_patch.sql`（建议执行一次，做字典与审批模板口径兜底）
13. `20260317_asset_menu_permission_patch.sql`（建议执行一次，补齐领用申请与财务查看按钮权限）

执行完成后，如果审批人不是 `admin`，再执行类似：

```sql
UPDATE wf_approval_template
SET approver_id = 100
WHERE business_type IN ('asset_requisition', 'asset_maintenance', 'asset_disposal');
```

### 场景 B：数据库已经执行过旧版 workflow 脚本

至少补执行下面顺序：

1. `20260314_asset_workflow_asset_id_patch.sql`（如果业务单据表还没有 `asset_id`）
2. `20260315_asset_lifecycle_workflow_menu_patch.sql`（如果菜单还是 `repair/scrap` 老命名）
3. `20260316_asset_governance_patch.sql`
4. `20260317_asset_menu_permission_patch.sql`（如果资产台账中的领用/财务操作被权限禁用）

这个场景下，第 13 个脚本会额外帮你做三件事：

*   补齐 `wf_approval_template.template_name / approver_id / status`
*   补齐 `wf_approval_instance.approver_id`
*   把历史实例按模板回填审批人

第 14 个脚本会额外补齐两类常见缺口权限：

*   `asset:requisition:add`、`asset:requisition:query`
*   `asset:finance:query`

执行完成后，同样要检查并修正 `wf_approval_template.approver_id` 是否为你的真实审批人。

---

### 注意事项
*   **数据库环境**：建议使用 MySQL 5.7+ 或 MySQL 8.0。
*   **字符集**：推荐使用 `utf8mb4` 字符集以支持更多字符。
*   **执行前备份**：存量库执行第 9 个补丁脚本前，建议先做一次数据库备份。
*   **执行方式**：可以使用 Navicat、MySQL Workbench 或命令行工具按上述顺序依次导入。
