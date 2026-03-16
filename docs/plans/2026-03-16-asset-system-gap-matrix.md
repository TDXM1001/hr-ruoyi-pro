# 资产系统全菜单差异矩阵

## 基线说明

- 盘点范围覆盖左侧资产系统 9 个业务菜单，不含流程中心和报表页。
- 主数据唯一内部主键冻结为 `assetId`，分类与属性管理继续使用 `categoryId`、`attrId` 作为局部主键。
- 状态口径统一拆分为 `assetStatus`、`status`、`wfStatus`、`archiveStatus`，本矩阵用来标识当前代码是否已经完全对齐。

## 现状矩阵

| 菜单 | 前端页面 | 前端 API | 后端 Controller | 主键口径 | 状态口径 | 已知缺口 |
| --- | --- | --- | --- | --- | --- | --- |
| 资产分类 | `art-design-pro/src/views/asset/category/index.vue` | `art-design-pro/src/api/asset/category.ts`、`art-design-pro/src/api/asset/category-attr.ts` | `AssetCategoryController`、`AssetCategoryAttrController` | 分类使用 `categoryId`，属性使用 `attrId`，与资产主档通过 `categoryId` 关联 | 分类启停沿用 `status`，未纳入统一业务单据状态体系 | 保留字段冲突校验、属性元数据返回和动态表单驱动能力仍需收口 |
| 资产台账 | `art-design-pro/src/views/asset/list/index.vue` | `art-design-pro/src/api/asset/info.ts`、`art-design-pro/src/api/asset/finance.ts` | `AssetInfoController`、`AssetFinanceController` | 已以 `assetId` 为主，`assetNo` 仍承担展示与业务编码 | 主档展示 `assetStatus`，归档能力与 `archiveStatus` 仍待显式收口 | 聚合详情、动态属性、附件、财务重算和折旧日志还需统一落地 |
| 领用归还 | `art-design-pro/src/views/asset/requisition/index.vue` | `art-design-pro/src/api/asset/requisition.ts` | `AssetRequisitionController` | 创建已要求 `assetId + assetNo`，单据主键为 `requisitionNo` | 已有 `status`、`wfStatus` 字段，但未沉淀统一单据基类 | 共享单据模型缺失，列表/详情响应与资产快照字段还需统一 |
| 维修管理 | `art-design-pro/src/views/asset/maintenance/index.vue` | `art-design-pro/src/api/asset/maintenance.ts` | `AssetMaintenanceController` | 当前仍允许仅传 `assetNo`，`assetId` 未完全强制 | 页面侧仅稳定暴露 `status`，`wfStatus` 缺失 | 创建契约、列表契约和状态回写规则尚未与统一模型对齐 |
| 报废处置 | `art-design-pro/src/views/asset/disposal/index.vue` | `art-design-pro/src/api/asset/disposal.ts` | `AssetDisposalController` | 当前仍允许仅传 `assetNo`，`assetId` 未完全强制 | 页面侧仅稳定暴露 `status`，`wfStatus` 缺失 | 固定资产处置单据的统一基类、终态回写和时间线同步仍待补齐 |
| 权属变更 | `art-design-pro/src/views/asset/real-estate/ownership/index.vue` | `art-design-pro/src/api/asset/real-estate-ownership.ts` | `AssetRealEstateOwnershipChangeController` | 当前 `assetId`、`assetNo` 仍为可选组合，未冻结为统一引用 | 仅暴露 `status`，未体现 `wfStatus` 语义 | 不动产单据未完全接入统一 `AssetRef`，审批态与主档回写需收口 |
| 用途变更 | `art-design-pro/src/views/asset/real-estate/usage/index.vue` | `art-design-pro/src/api/asset/real-estate-usage.ts` | `AssetRealEstateUsageChangeController` | 当前 `assetId`、`assetNo` 仍为可选组合，未冻结为统一引用 | 仅暴露 `status`，未体现 `wfStatus` 语义 | 目标用途字段已独立，但统一业务单据基础字段仍不完整 |
| 状态变更 | `art-design-pro/src/views/asset/real-estate/status/index.vue` | `art-design-pro/src/api/asset/real-estate-status.ts` | `AssetRealEstateStatusChangeController` | 当前 `assetId`、`assetNo` 仍为可选组合，未冻结为统一引用 | 使用 `status` 与 `targetAssetStatus`，未补足 `wfStatus` | 目标状态变更与资产主档状态回写规则还未统一建模 |
| 注销处置 | `art-design-pro/src/views/asset/real-estate/disposal/index.vue` | `art-design-pro/src/api/asset/real-estate-disposal.ts` | `AssetRealEstateDisposalController` | 当前 `assetId`、`assetNo` 仍为可选组合，未冻结为统一引用 | 使用 `status` 与 `targetAssetStatus`，未补足 `wfStatus` | 注销/处置动作的扩展字段、终态回写和统一单据模型仍待收口 |

## 回归结果

- 待批次 9 完成后回填全菜单联调与阻塞情况。
