# 资产管理系统设计方案

## 整体思路
本项目旨在设计并实现一个综合资产管理系统，管理范围包含不动产资产和固定资产。系统基于已有的 `art-design-pro` 前端（Vue 3 + Element Plus）和 `RuoYi-Vue` 后端构建。

### 核心功能
* **综合资产管理**：既包含不动产资产也包含固定资产，统一管理。
* **灵活的资产与自定义属性**：方案 3（分类 + 自定义属性）最灵活，能适应不动产和固定资产差异很大的属性需求。
* **审批流程**：全流程审批 — 采购、领用、调拨、维修、报废都走审批流程，不同金额/类型可以配置不同的审批链。预留接口，阶段用基于数据库的简单状态机审批（申请→部门审批→终审）。
* **折旧计算**：多种折旧法 — 直线法 + 双倍余额递减法 + 年数总和法，不同资产类别可选不同折旧方式。
* **统计报表/大屏**：两者都要 — 日常用报表页面，展示用独立大屏。

---

## 一、整体架构

```text
art-design-pro (Vue 3 + Element Plus)
├── views/asset/          # 资产管理页面模块
│   ├── category/         # 资产分类管理
│   ├── list/             # 资产台账（不动产/固定资产）
│   ├── requisition/      # 领用/归还
│   ├── transfer/         # 调拨
│   ├── maintenance/      # 维修保养
│   ├── inventory/        # 盘点
│   ├── disposal/         # 报废/处置
│   ├── procurement/      # 采购申请
│   ├── depreciation/     # 折旧管理
│   ├── workflow/         # 审批中心（待办/我发起）
│   ├── report/           # 统计报表
│   └── dashboard/        # 资产大屏
└── api/asset/            # 前端 API 层

RuoYi-Vue
├── ruoyi-asset/          # 新增资产管理模块（Maven 子模块）
│   ├── domain/           # 实体类
│   ├── mapper/           # MyBatis Mapper
│   ├── service/          # 业务逻辑
│   └── controller/       # REST API
└── ruoyi-workflow/       # 审批引擎模块（从零搭建轻量级）
    ├── domain/           # 审批单/审批节点/审批记录
    ├── service/          # 状态机核心逻辑
    └── controller/       # 审批 API
```

**技术决策**：新增独立 Maven 模块 `ruoyi-asset`，不污染 `ruoyi-system`，与已有模块风格一致。

## 二、数据库核心表设计

### 1. 资产分类体系（支持自定义属性）
```sql
asset_category          # 分类树（id, parent_id, name, code, level）
asset_category_attr     # 分类自定义属性定义（attr_name, attr_type[text/number/date/select], required）
```

### 2. 资产主表
```sql
asset_info              # 资产主表
  - asset_no            # 资产编号（自动生成，如 FD-2026-00001）
  - asset_name          # 资产名称
  - category_id         # 分类（关联 asset_category）
  - asset_type          # 类型：1=不动产 2=固定资产
  - dept_id             # 归属部门
  - user_id             # 责任人
  - location            # 存放位置
  - purchase_date       # 购置日期
  - purchase_price      # 原值
  - residual_rate       # 残值率（%）
  - depreciation_method # 折旧方式：1=直线法 2=双倍余额递减 3=年数总和
  - useful_life         # 使用年限（月）
  - status              # 状态：1=正常 2=领用中 3=维修中 4=盘点中 5=已报废

asset_attr_value        # 资产扩展属性值（asset_id, attr_id, attr_value）
```

### 3. 业务流水表
```sql
asset_procurement       # 采购申请
asset_requisition       # 领用/归还记录
asset_transfer          # 调拨记录
asset_maintenance       # 维修保养记录
asset_inventory         # 盘点任务 + 盘点明细
asset_disposal          # 报废/处置记录
asset_depreciation_log  # 每月折旧计算记录
```

### 4. 审批引擎表（ruoyi-workflow）
```sql
wf_approval_template    # 审批模板（业务类型, 审批链配置 JSON）
wf_approval_instance    # 审批实例（业务ID, 业务类型, 当前节点, 状态）
wf_approval_node        # 审批节点记录（审批人, 操作, 意见, 时间）
```

## 三、审批流程设计

**轻量级状态机**，基于数据库驱动，预留 Flowable 切换接口：

```text
[发起申请] → 状态: pending
    ↓
[部门主管审批] → 通过: dept_approved / 拒绝: rejected
    ↓
[分管领导审批（金额≥5万）] → 通过: leader_approved / 拒绝: rejected
    ↓
[资产管理员终审] → 通过: approved / 拒绝: rejected
    ↓
[业务执行 & 关闭]
```

- **模板配置**：不同业务类型（采购/报废/调拨等）可配置不同审批链
- **金额阈值**：可通过 `wf_approval_template` 的 JSON 配置控制节点是否触发
- **预留接口**：`ApprovalEngine` 接口，轻量级实现 `SimpleApprovalEngine`，后续可替换 `FlowableApprovalEngine`

## 四、折旧计算设计

```java
// 三种折旧方法（后端月度批处理，Quartz 定时任务）
interface DepreciationStrategy {
    BigDecimal calculate(AssetInfo asset, int currentMonth);
}

// 直线法：月折旧 = (原值 - 残值) / 使用月数
class StraightLineDepreciation implements DepreciationStrategy {}

// 双倍余额递减法：月折旧 = 账面净值 × (2 / 使用月数)
class DoubleDecliningDepreciation implements DepreciationStrategy {}

// 年数总和法：月折旧 = (原值 - 残值) × 年数比例
class SumOfYearsDepreciation implements DepreciationStrategy {}
```

- 每月 1 日 00:00 执行 Quartz 定时任务，批量生成 `asset_depreciation_log`
- 支持手动触发补算
- 累计折旧、账面净值实时从 log 表聚合计算

## 五、前端页面结构

| 功能模块 | 路由 | 说明 |
|---|---|---|
| 资产分类维护 | `/asset/category` | 树形分类 + 自定义属性配置 |
| 资产台账 | `/asset/list` | 主表，支持按类型/部门/状态过滤 |
| 资产详情 | `/asset/detail/:id` | 含扩展属性、折旧曲线、历史记录时间轴 |
| 采购申请 | `/asset/procurement` | 发起采购 + 审批列表 |
| 领用/归还 | `/asset/requisition` | 申请领用/归还确认 |
| 调拨管理 | `/asset/transfer` | 部门间资产转移 |
| 维修保养 | `/asset/maintenance` | 维修记录 + 申请维修 |
| 资产盘点 | `/asset/inventory` | 创建盘点任务、扫码盘点 |
| 报废处置 | `/asset/disposal` | 报废申请 + 处置记录 |
| 折旧管理 | `/asset/depreciation` | 折旧台账、月度汇总 |
| 审批中心 | `/asset/workflow` | 我的待审、我发起的单据 |
| 统计报表 | `/asset/report` | 多维度图表分析 |
| 资产大屏 | `/asset/screen` | 全屏可视化大屏 |

## 六、分阶段实施计划

| 阶段 | 内容 | 周期 |
|---|---|---|
| **Phase 1** | 数据库设计、`ruoyi-asset` 模块搭建、资产分类 + 台账 CRUD、前端路由和基础页面 | 2周 |
| **Phase 2** | 审批引擎（`ruoyi-workflow`）、采购/领用/调拨/维修/报废业务流程 | 3周 |
| **Phase 3** | 折旧计算（Quartz 定时任务 + 三种算法）、资产盘点模块 | 2周 |
| **Phase 4** | 统计报表页面 + 资产大屏（ECharts） | 1周 |
