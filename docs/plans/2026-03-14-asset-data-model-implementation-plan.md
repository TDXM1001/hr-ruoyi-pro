# 资产数据模型重构实施计划

> **For Claude:** 必须使用 `superpowers:executing-plans` 子技能按任务逐项执行本计划。

**目标：** 重构资产模块数据模型，使固定资产和不动产共享统一稳定的资产主档，同时将财务、不动产、动态属性、附件和折旧历史拆分到独立表中。

**架构：** 保持 `ruoyi-asset` 作为资产后端主模块，将 `asset_info` 升级为使用 `asset_id` 作为内部主键，并新增财务、不动产、动态属性、附件、折旧日志等聚合子表。通过 `ruoyi-admin` 暴露聚合新增、修改、详情接口，由 `ruoyi-asset` 服务负责多表事务协调和财务重算规则。

**技术栈：** Java 17、Spring Boot 3、MyBatis、MySQL、RuoYi 后端管理模块

---

### 任务 1：补充数据模型重构 SQL 脚本

**文件：**
- 创建：`RuoYi-Vue/sql/20260314_asset_data_model_refactor.sql`
- 修改：`RuoYi-Vue/sql/sql执行.md`
- 参考：`RuoYi-Vue/sql/20260311_asset_management_init.sql`

**步骤 1：编写迁移脚本骨架**

DDL 需要覆盖以下内容：

- 将 `asset_info` 主键从 `asset_no` 迁移为 `asset_id`
- 新增 `asset_finance`、`asset_real_estate`、`asset_category_attr`、`asset_attr_value`、`asset_attachment`、`asset_depreciation_log`
- 为 `asset_no`、`asset_id`、`property_cert_no` 和 `(asset_id, period)` 建立索引与唯一约束

**步骤 2：编写历史数据回填 SQL**

回填逻辑至少包括：

- 保留现有 `asset_no`
- 回填 `asset_id`
- 在条件允许时为现有资产补齐一条 `asset_finance` 记录
- 在迁移窗口期内临时保留旧字段，便于回滚

**步骤 3：校验 SQL 设计和约束**

执行：`cd RuoYi-Vue && mvn -q -pl ruoyi-asset -am validate`

预期：Maven 校验通过，SQL 脚本路径和命名没有明显问题。随后人工检查 DDL 中的主键、唯一键、外键关系和索引覆盖是否完整。

**步骤 4：提交**

```bash
git add RuoYi-Vue/sql/20260314_asset_data_model_refactor.sql RuoYi-Vue/sql/sql执行.md
git commit -m "docs: add asset data model refactor sql plan"
```

### 任务 2：将资产主档从 `asset_no` 主键重构为 `asset_id`

**文件：**
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetInfo.java`
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetInfoMapper.java`
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetInfoMapper.xml`
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetInfoService.java`
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetInfoServiceImpl.java`
- 修改：`RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetInfoController.java`

**步骤 1：识别所有仍将 `asset_no` 当作主键使用的位置**

梳理所有仍以 `asset_no` 作为表主键或强关联字段的位置，统一替换为 `asset_id`，同时保留 `asset_no` 作为唯一业务编码。

**步骤 2：扩展 `AssetInfo` 字段**

至少补充以下字段：

- `assetId`
- `assetNo`
- `assetName`
- `assetType`
- `categoryId`
- `specModel`
- `unit`
- `ownershipOrgId`
- `manageDeptId`
- `useDeptId`
- `responsibleUserId`
- `userId`
- `locationId`
- `locationText`
- `acquireMethod`
- `purchaseDate`
- `capitalizationDate`
- `enableDate`
- `assetStatus`

**步骤 3：修改 Mapper 查询和映射**

确保 XML 和 Mapper 方法支持：

- 按 `assetId` 查询
- 按唯一业务编码 `assetNo` 查询
- 支持按 `assetType`、`assetStatus`、`categoryId`、`useDeptId` 进行列表筛选

**步骤 4：编译验证**

执行：`cd RuoYi-Vue && mvn clean compile -pl ruoyi-asset -am`

预期：`BUILD SUCCESS`

**步骤 5：提交**

```bash
git add RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetInfo.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetInfoMapper.java RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetInfoMapper.xml RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetInfoService.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetInfoServiceImpl.java RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetInfoController.java
git commit -m "feat: refactor asset master to asset id based model"
```

### 任务 3：新增财务、不动产、扩展属性、附件和折旧日志领域对象

**文件：**
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetFinance.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetRealEstate.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetCategoryAttr.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetAttrValue.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetAttachment.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetDepreciationLog.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetFinanceMapper.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetRealEstateMapper.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetCategoryAttrMapper.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetAttrValueMapper.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetAttachmentMapper.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetDepreciationLogMapper.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetFinanceMapper.xml`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetRealEstateMapper.xml`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetCategoryAttrMapper.xml`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetAttrValueMapper.xml`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetAttachmentMapper.xml`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetDepreciationLogMapper.xml`

**步骤 1：新增领域对象**

每个 Domain 只对应一张表，不要在 Domain 中混入页面聚合字段。

**步骤 2：新增 Mapper 接口与 XML**

除基础 CRUD 外，至少支持以下查询：

- 按 `assetId` 查询财务信息
- 按 `assetId` 查询不动产信息
- 按 `assetId` 查询附件
- 按 `categoryId` 查询扩展字段定义
- 按 `assetId` 查询扩展字段值
- 按 `assetId` 和 `(assetId, period)` 查询折旧日志

**步骤 3：编译验证**

执行：`cd RuoYi-Vue && mvn clean compile -pl ruoyi-asset -am`

预期：`BUILD SUCCESS`

**步骤 4：提交**

```bash
git add RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset
git commit -m "feat: add asset aggregate subtable domains and mappers"
```

### 任务 4：引入聚合 DTO、VO 和编排服务

**文件：**
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/dto/AssetCreateReq.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/dto/AssetUpdateReq.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/vo/AssetListVo.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/vo/AssetDetailVo.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetAggregateService.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetAggregateServiceImpl.java`
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetInfoServiceImpl.java`
- 修改：`RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetInfoController.java`

**步骤 1：定义请求和响应对象**

`AssetCreateReq` 和 `AssetUpdateReq` 应按以下结构组织：

- `basicInfo`
- `financeInfo`
- `realEstateInfo`
- `dynamicAttrs`
- `attachments`

`AssetListVo` 应保持列表场景轻量化，`AssetDetailVo` 负责聚合完整详情。

**步骤 2：实现聚合编排服务**

`AssetAggregateServiceImpl` 至少负责：

- 在一个事务中写入主表和子表
- 按资产类型校验必填子记录
- 按 `assetId` 加载聚合详情
- 事务性更新主表和子表数据

**步骤 3：调整 Controller 接口入参与出参**

将资产新增、修改、详情接口切换为聚合 DTO/VO，不再直接暴露单表 Domain。

**步骤 4：编译验证**

执行：`cd RuoYi-Vue && mvn clean compile -pl ruoyi-asset -am`

预期：`BUILD SUCCESS`

**步骤 5：提交**

```bash
git add RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/dto RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/vo RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetAggregateService.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetAggregateServiceImpl.java RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetInfoController.java
git commit -m "feat: add aggregate asset create update and detail service"
```

### 任务 5：实现财务计算规则和折旧日志

**文件：**
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetFinanceService.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetDepreciationService.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetFinanceServiceImpl.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetDepreciationServiceImpl.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/finance/DepreciationStrategy.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/finance/StraightLineDepreciation.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/finance/DoubleDecliningDepreciation.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/finance/SumOfYearsDepreciation.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/finance/StraightLineDepreciationTest.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/finance/DoubleDecliningDepreciationTest.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/finance/SumOfYearsDepreciationTest.java`
- 修改：`RuoYi-Vue/ruoyi-asset/pom.xml`

**步骤 1：补充测试依赖**

如果 `ruoyi-asset/pom.xml` 中缺少 `spring-boot-starter-test` 或等价测试依赖，先补充测试作用域依赖。

**步骤 2：先写折旧策略失败用例**

测试至少覆盖：

- 残值计算
- 各折旧策略的月折旧额计算
- 累计折旧上限控制
- 账面价值保留两位小数的舍入规则

**步骤 3：运行测试并确认失败**

执行：`cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=StraightLineDepreciationTest,DoubleDecliningDepreciationTest,SumOfYearsDepreciationTest test`

预期：测试因折旧策略尚未实现而失败。

**步骤 4：实现最小可用财务计算服务**

由财务服务统一维护以下重算逻辑：

- `salvageValue`
- `depreciableValue`
- `monthlyDepreciationAmount`
- `netBookValue`
- `bookValue`

由折旧服务按资产和期间写入 `asset_depreciation_log`。

**步骤 5：再次运行测试**

执行：`cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=StraightLineDepreciationTest,DoubleDecliningDepreciationTest,SumOfYearsDepreciationTest test`

预期：`BUILD SUCCESS`

**步骤 6：提交**

```bash
git add RuoYi-Vue/ruoyi-asset/pom.xml RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/finance
git commit -m "feat: add asset finance calculation and depreciation logs"
```

### 任务 6：补充分类扩展字段配置接口

**文件：**
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetCategoryAttrService.java`
- 创建：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetCategoryAttrServiceImpl.java`
- 创建：`RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetCategoryAttrController.java`
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetCategoryServiceImpl.java`
- 修改：`RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetCategoryController.java`

**步骤 1：实现扩展字段定义 CRUD**

至少支持：

- 新增字段定义
- 修改字段定义
- 按分类查询字段定义
- 禁用字段定义

**步骤 2：加入保留字段校验**

拒绝与系统保留字段冲突的 `attr_code`，例如：

- `asset_no`
- `asset_name`
- `original_value`
- `property_cert_no`

**步骤 3：编译验证**

执行：`cd RuoYi-Vue && mvn clean compile -pl ruoyi-asset -am`

预期：`BUILD SUCCESS`

**步骤 4：提交**

```bash
git add RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetCategoryAttrService.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetCategoryAttrServiceImpl.java RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetCategoryAttrController.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetCategoryServiceImpl.java RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetCategoryController.java
git commit -m "feat: add configurable asset category attribute management"
```

### 任务 7：更新业务流水服务，统一改用 `asset_id` 并增加财务约束

**文件：**
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetRequisition.java`
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetMaintenance.java`
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetDisposal.java`
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetRequisitionMapper.java`
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetMaintenanceMapper.java`
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetDisposalMapper.java`
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetRequisitionMapper.xml`
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRequisitionServiceImpl.java`
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetMaintenanceServiceImpl.java`
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetDisposalServiceImpl.java`
- 修改：`RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRequisitionController.java`
- 修改：`RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetMaintenanceController.java`
- 修改：`RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetDisposalController.java`

**步骤 1：替换对 `asset_no` 的强耦合**

在业务单据中引入 `assetId` 作为内部关联字段，同时保留 `assetNo` 用于展示和查询。

**步骤 2：增加资产生命周期校验**

至少校验：

- 已处置资产不能领用
- 维修中的资产不能领用
- 已进入折旧的资产不能静默修改财务基础数据

**步骤 3：编译验证**

执行：`cd RuoYi-Vue && mvn clean package -DskipTests=true`

预期：`BUILD SUCCESS`

**步骤 4：提交**

```bash
git add RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset
git commit -m "feat: migrate asset workflows to asset id based relations"
```

### 任务 8：补充管理端财务接口和聚合详情接口

**文件：**
- 创建：`RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetFinanceController.java`
- 修改：`RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetInfoController.java`
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetFinanceService.java`
- 修改：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetDepreciationService.java`

**步骤 1：暴露财务相关接口**

至少增加以下接口：

- 按 `assetId` 重算财务结果
- 按 `assetId` 查询折旧日志
- 按期间执行月度折旧计提

**步骤 2：输出聚合详情结构**

详情接口应返回：

- 主档基础信息
- 财务信息
- 不动产信息
- 动态扩展属性
- 附件信息
- 最近折旧日志

**步骤 3：编译验证**

执行：`cd RuoYi-Vue && mvn clean package -DskipTests=true`

预期：`BUILD SUCCESS`

**步骤 4：提交**

```bash
git add RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetFinanceController.java RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetInfoController.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetFinanceService.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetDepreciationService.java
git commit -m "feat: expose asset finance and aggregate detail apis"
```

### 任务 9：补充前后端接口契约说明

**文件：**
- 修改：`docs/plans/2026-03-12-asset-management-phase2-plan.md`
- 创建：`docs/plans/2026-03-14-asset-api-contract-notes.md`
- 参考：`art-design-pro/src/api/asset/info.ts`
- 参考：`art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue`

**步骤 1：记录接口契约变更**

记录以下请求和响应变化：

- 资产聚合新增、修改请求结构
- 资产详情返回结构
- 动态扩展属性载荷结构
- 财务重算相关接口

**步骤 2：评估对现有前端入口的影响**

列出后端交付后前端必须同步调整的最小触点。

**步骤 3：提交**

```bash
git add docs/plans/2026-03-12-asset-management-phase2-plan.md docs/plans/2026-03-14-asset-api-contract-notes.md
git commit -m "docs: record asset backend contract changes for frontend sync"
```

### 任务 10：最终验证

**文件：**
- 仅验证，无新增文件

**步骤 1：运行后端测试**

执行：`cd RuoYi-Vue && mvn test -pl ruoyi-asset -am`

预期：`BUILD SUCCESS`

**步骤 2：运行打包验证**

执行：`cd RuoYi-Vue && mvn clean package -DskipTests=true`

预期：`BUILD SUCCESS`

**步骤 3：检查最终变更集**

执行：`git status --short`

预期：在进入最终合并流程前，仅保留预期的资产后端相关文件变更。

**步骤 4：提交**

```bash
git add .
git commit -m "feat: complete asset data model refactor backend delivery"
```
