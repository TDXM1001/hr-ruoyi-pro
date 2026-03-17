# 资产台账生命周期逻辑统一认知稿

## 1. 文档目标与适用范围

本文档用于统一资产模块生命周期口径，帮助研发、测试、实施、产品在同一套事实基础上理解系统行为。  
文档覆盖固定资产与不动产两条主链，重点说明：

- 动作入口如何决定与渲染
- 资产状态、单据状态、流程状态如何并行
- 路由透传与不动产守卫如何限制可发起动作
- 固定资产与不动产动作如何分别落单与进入流程中心

本稿依据当前前端实现整理，不扩展尚未落地的流程设计。

---

## 2. 基础口径（必须统一）

### 2.1 资产类型

- `1`：固定资产
- `2`：不动产

### 2.2 资产状态（资产本体）

- `1`：在用
- `2`：领用中
- `3`：维修中
- `4`：盘点中
- `5`：已报废
- `6`：已处置
- `7`：闲置

### 2.3 固定资产终态

- 固定资产终态为 `{5, 6}`（已报废、已处置）。
- 进入终态后，固定资产业务动作（领用/维修/报废/处置）隐藏，仅保留通用动作。

### 2.4 三层状态并行模型

- `assetStatus`：资产本体当前状态
- `status`：业务单据状态（如待审批、已审批等）
- `wfStatus`：流程状态（如进行中、完成、驳回）

三层状态并行存在，不能互相替代。

---

## 3. 台账列表入口与动作编排

### 3.1 入口构成

资产列表动作由两段逻辑共同决定：

1. `buildLifecycleActions` 决定该资产“有哪些动作”
2. `buildOperationButtons` 决定这些动作“如何展示与触发”

### 3.2 已归档视图行为

- 当 `showArchived=true` 时，操作列仅显示“已归档，仅供查看”。
- 归档视图不开放业务动作发起。

### 3.3 动作到页面路由

- 领用：`/asset/requisition/index`
- 维修：`/asset/maintenance/index`
- 报废/处置：`/asset/disposal/index`（附 `disposalIntent=scrap|dispose`）
- 不动产四动作：
  - 权属变更：`/asset/real-estate/ownership/index`
  - 用途变更：`/asset/real-estate/usage/index`
  - 状态变更：`/asset/real-estate/status/index`
  - 注销/处置：`/asset/real-estate/disposal/index`

### 3.4 统一路由上下文

从台账页进入生命周期页面时，统一透传：

- `assetId`
- `assetNo`
- `assetName`
- `assetStatus`
- `openCreate=1`

不动产业务额外传最近动作快照字段：

- `latestActionType`
- `latestActionLabel`
- `latestDocStatus`
- `latestActionTime`
- `actionKey`

### 3.5 通用动作

- “变更”：当前为占位动作（提示待规划），不发起真实流程
- “归档”：走 `archiveInfo`，语义为移出日常台账
- “编辑”：主档编辑入口
- “财务”：财务摘要与折旧日志入口

---

## 4. 生命周期动作矩阵

| 资产类型 | 资产状态 | 入口动作 |
| --- | --- | --- |
| 固定资产（1） | 非终态（不在 5/6） | 领用、维修、报废、处置、变更（占位）、归档 |
| 固定资产（1） | 终态（5/6） | 变更（占位）、归档 |
| 不动产（2） | 任意（但受守卫限制） | 权属变更、用途变更、状态变更、注销/处置、变更（占位）、归档 |
| 任意 | 已归档视图 | 已归档，仅供查看（无动作） |

---

## 5. 固定资产生命周期主链

### 5.1 领用链路

- 发起：台账页提交 `applyRequisition`
- 状态映射：`0待审批 1已审批 2已驳回 3已完成`
- 归还条件：仅 `status=1` 且流程解析为 `approved` 才允许 `returnAsset`

### 5.2 维修链路

- 发起：台账页进入维修页，提交 `applyMaintenance`
- 完工条件：仅 `status=1` 且流程解析为 `approved` 才允许 `completeMaintenance`

### 5.3 处置链路

- 发起：台账页进入处置页，提交 `applyDisposal`
- 处置类型：固定资产处置类型枚举（报废/出售/划转/捐赠）
- 台账入口可预置 `disposalIntent=scrap|dispose`

### 5.4 固定资产业务建单约束

固定资产业务建单统一通过共享 helper 构造 payload，强制携带：

- `assetId`
- `assetNo`

缺失时直接报错，禁止仅凭资产编号反查发单。

---

## 6. 不动产生命周期主链

### 6.1 四类动作

- 权属变更
- 用途变更
- 状态变更
- 注销/处置

### 6.2 统一守卫（getRealEstateActionGuard）

以下任一命中将禁用发起：

1. 缺少 `assetId` 或 `assetNo`
2. 资产已在终态 `{5, 6}`
3. 最近动作为“注销/处置”且 `latestDocStatus=pending`，禁止再发起其他不动产业务
4. 同类型动作已存在 `pending` 单据，禁止重复发起

### 6.3 不动产业务状态口径

- 权属变更、注销/处置页面：审批型状态（pending/approved/rejected）
- 用途变更、状态变更页面：直达完成口径（completed）

### 6.4 注销/处置默认值

- `disposalType=cancel`
- `targetAssetStatus=6`

### 6.5 自动开单弹窗约束

- `openCreate=1` 只在守卫通过时生效
- 守卫不通过则不自动开单，并给出禁用原因

---

## 7. 流程中心与时间线关联

### 7.1 流程中心接口

- 待办：`/workflow/task/todo`
- 已办：`/workflow/task/done`
- 审批提交：`/workflow/task/approve`

### 7.2 业务类型兜底字典

流程任务本地兜底包含：

- 资产领用
- 资产归还
- 资产维修
- 资产处置
- 不动产权属变更
- 不动产用途变更
- 不动产状态变更
- 不动产处置

### 7.3 时间线统一口径

时间线动作与文案统一维护在 `types/asset.ts`，包含：

- 固定资产：`REQUISITION / REPAIR / SCRAP / DISPOSAL`
- 不动产：`REAL_ESTATE_OWNERSHIP_CHANGE / REAL_ESTATE_USAGE_CHANGE / REAL_ESTATE_STATUS_CHANGE / REAL_ESTATE_DISPOSAL`

时间线单据状态统一使用：`pending / approved / rejected / completed`。

---

## 8. 验证场景（回归清单）

1. 固定资产 `assetStatus=1` 时，台账动作包含：领用/维修/报废/处置 + 通用动作。
2. 固定资产 `assetStatus in {5,6}` 时，固定资产业务动作隐藏。
3. 不动产存在待审批“注销/处置”单时，权属/用途/状态变更均被守卫阻断。
4. 领用台账“归还资产”仅在 `status=1 && wfStatus=approved` 时可见。
5. 维修台账“维修完工”仅在 `status=1 && wfStatus=approved` 时可见。
6. 带 `openCreate=1` 路由进入时，仅守卫通过才自动弹出建单。

---

## 9. 关键假设与边界

- 本文默认输出为研发可落地视角，不做产品简版叙述。
- 后端是资产状态流转的最终事实源，前端承担入口守卫、上下文透传与状态展示。
- 当前“主按钮 + 更多”仅是操作列展示优化，不改变生命周期语义。
- “变更”动作仍为占位入口，后续需补齐业务单据与流程策略后转正式动作。

---

## 10. 快速定位代码（阅读索引）

- 台账动作入口：`art-design-pro/src/views/asset/list/index.vue`
- 动作矩阵规则：`art-design-pro/src/views/asset/list/asset-lifecycle.helper.ts`
- 不动产守卫与 payload：`art-design-pro/src/views/asset/real-estate/real-estate-lifecycle.helper.ts`
- 固定资产业务共享 helper：`art-design-pro/src/views/asset/requisition/requisition.helper.ts`
- 统一类型与时间线口径：`art-design-pro/src/types/asset.ts`
- 流程中心 API：`art-design-pro/src/api/workflow/task.ts`
