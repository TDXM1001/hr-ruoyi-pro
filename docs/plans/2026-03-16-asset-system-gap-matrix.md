# 资产系统联调差异矩阵

## 执行拆分与进度

- [ ] 批次 1A：基线矩阵与冻结项
- [ ] 批次 1B：前端共享契约基础收口（固定资产业务）
- [ ] 批次 1C：后端固定资产业务单据口径收口
- [ ] 批次 2A：前端共享契约扩展到不动产业务
- [ ] 批次 2B：后端不动产业务单据口径收口
- [ ] 批次 3A：资产分类与动态属性建模收口
- [ ] 批次 3B：资产台账聚合表单、附件、财务收口
- [ ] 批次 4A：固定资产业务页面联调
- [ ] 批次 4B：不动产业务页面联调
- [ ] 批次 5A：流程中心、报表预警与治理收口
- [ ] 批次 5B：全菜单联调回归与上线前验证

## 现状差异矩阵

| 菜单 | 前端页面 | 前端 API | 后端 Controller | 主键口径 | 状态口径 | 已知缺口 |
| --- | --- | --- | --- | --- | --- | --- |
| 资产分类 | `art-design-pro/src/views/asset/category/index.vue` | `art-design-pro/src/api/asset/category.ts` `art-design-pro/src/api/asset/category-attr.ts` | `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetCategoryController.java` `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetCategoryAttrController.java` | 分类主键与属性主键已分离 | 以分类状态与属性状态为主 | 需要补齐动态表单元数据、保留字段冲突和建模规则统一 |
| 资产台账 | `art-design-pro/src/views/asset/list/index.vue` | `art-design-pro/src/api/asset/info.ts` `art-design-pro/src/api/asset/finance.ts` | `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetInfoController.java` `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetFinanceController.java` | 已切到 `assetId` | `assetStatus` 已存在，`archiveStatus` 未完全显式化 | 需要继续统一聚合表单、附件、财务和归档口径 |
| 领用归还 | `art-design-pro/src/views/asset/requisition/index.vue` | `art-design-pro/src/api/asset/requisition.ts` | `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRequisitionController.java` | 已支持 `assetId + assetNo` | `status` 已有，`wfStatus` 未统一导出 | 需要收敛共享单据模型与流程状态导出 |
| 维修管理 | `art-design-pro/src/views/asset/maintenance/index.vue` | `art-design-pro/src/api/asset/maintenance.ts` | `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetMaintenanceController.java` | 已支持 `assetId/assetNo` 双口径 | `status` 已有，`wfStatus` 未统一导出 | 需要收敛共享单据模型与流程状态导出 |
| 报废处置 | `art-design-pro/src/views/asset/disposal/index.vue` | `art-design-pro/src/api/asset/disposal.ts` | `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetDisposalController.java` | 已支持 `assetId/assetNo` 双口径 | `status` 已有，`wfStatus` 未统一导出 | 需要收敛共享单据模型与流程状态导出 |
| 权属变更 | `art-design-pro/src/views/asset/real-estate/ownership/index.vue` | `art-design-pro/src/api/asset/real-estate-ownership.ts` | `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateOwnershipChangeController.java` | 已支持 `assetId/assetNo` | `status` 已有，`wfStatus` 未统一导出 | 需要进入第二批收敛共享单据模型 |
| 用途变更 | `art-design-pro/src/views/asset/real-estate/usage/index.vue` | `art-design-pro/src/api/asset/real-estate-usage.ts` | `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateUsageChangeController.java` | 已支持 `assetId/assetNo` | 以 `status` 为主 | 需要进入第二批收敛共享单据模型 |
| 状态变更 | `art-design-pro/src/views/asset/real-estate/status/index.vue` | `art-design-pro/src/api/asset/real-estate-status.ts` | `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateStatusChangeController.java` | 已支持 `assetId/assetNo` | 以 `status` 为主 | 需要进入第二批收敛共享单据模型 |
| 注销处置 | `art-design-pro/src/views/asset/real-estate/disposal/index.vue` | `art-design-pro/src/api/asset/real-estate-disposal.ts` | `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateDisposalController.java` | 已支持 `assetId/assetNo` | `status` 已有，`wfStatus` 未统一导出 | 需要进入第二批收敛共享单据模型 |

## 冻结项

- 主键冻结：资产主档及所有业务单据统一以 `assetId` 作为内部关联主键，`assetNo` 只承担展示与业务编码职责。
- 状态冻结：本轮联调明确区分 `assetStatus`、`status`、`wfStatus`、`archiveStatus` 四套语义，不再接受混用。
- 菜单冻结：左侧资产系统菜单名称与层级本轮不再新增变体，只做联调收口。
- 权限冻结：现有权限前缀保持稳定，优先在返回口径和页面联调层收口，不同步改造权限体系命名。

## 回归结果

- 待第一批实现后补充。
