# 资产模块不动产生命周期下一阶段 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 在现有资产聚合台账、固定资产审批闭环和生命周期入口分流已经稳定的基础上，补齐不动产专属生命周期 MVP，完成权属变更、用途变更、状态变更、注销/处置四类正式动作，并为这条主线补齐菜单权限、接口契约、测试矩阵和联调收口。

**Architecture:** 采用“一条业务主线 + 一条并行收口线”的推进方式。业务主线在 `ruoyi-asset` 中新增不动产动作域，保持 `asset_info` 与 `asset_real_estate` 只存当前事实，由不动产动作单负责过程留痕；其中权属变更和注销/处置接入现有最小审批闭环，用途变更和状态变更先按免审批直达实现。前端继续保留统一资产列表入口，但把不动产动作从占位按钮升级为正式列表页与申请页，并用菜单 SQL、契约文档和聚焦测试把新链路收稳。

**Tech Stack:** Java 17、Spring Boot 3、MyBatis、MySQL、Vue 3、TypeScript、Element Plus、Vitest、RuoYi

---

## 实施基线

- 已有聚合台账与不动产主数据：`asset_info` + `asset_real_estate`
- 已有最小审批闭环与工作流待办/已办接口
- 已有前端不动产生命周期占位入口：`art-design-pro/src/views/asset/list/asset-lifecycle.helper.ts`
- 已有固定资产生命周期服务测试、SQL 契约测试、前端 API/helper 测试可复用

## 本阶段非目标

- 不处理 `art-design-pro` 中 `monitor` / `system` 历史构建错误
- 不重做资产编辑抽屉整体结构
- 不扩展成可配置审批流平台
- 不进入楼栋/房间/分户层级建模

## 批次拆分

- 批次一：建立不动产生命周期 SQL 与领域基线
- 批次二：打通审批型动作后端主链路
- 批次三：打通免审批动作后端主链路
- 批次四：完成前端 API、入口分流与动作页面
- 批次五：补齐菜单权限、契约说明、联调验证与执行记录

### Task 1: 建立不动产生命周期 SQL 与领域基线

**Files:**

- Create: `RuoYi-Vue/sql/20260315_asset_real_estate_lifecycle.sql`
- Modify: `RuoYi-Vue/sql/sql执行.md`
- Create: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/sql/AssetRealEstateLifecycleSqlContractTest.java`

**Step 1: 先写 SQL 契约失败测试**

在 `AssetRealEstateLifecycleSqlContractTest.java` 中先锁定以下文本契约：

```java
assertAll(
    () -> assertTrue(sql.contains("CREATE TABLE `asset_real_estate_ownership_change`")),
    () -> assertTrue(sql.contains("CREATE TABLE `asset_real_estate_usage_change`")),
    () -> assertTrue(sql.contains("CREATE TABLE `asset_real_estate_status_change`")),
    () -> assertTrue(sql.contains("CREATE TABLE `asset_real_estate_disposal`"))
);
```

同时补充：

- 四张表都包含 `asset_id`
- 权属变更和注销/处置包含审批所需 `status`
- SQL 执行说明包含 `20260315_asset_real_estate_lifecycle.sql`

**Step 2: 运行测试确认失败**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetRealEstateLifecycleSqlContractTest test`

Expected: FAIL，原因应为脚本和测试文件尚不存在，或缺少目标建表语句。

**Step 3: 编写建表脚本和执行说明**

在 `20260315_asset_real_estate_lifecycle.sql` 中至少补齐：

- `asset_real_estate_ownership_change`
- `asset_real_estate_usage_change`
- `asset_real_estate_status_change`
- `asset_real_estate_disposal`

建议四张表统一包含：

```sql
`asset_id` bigint(20) NOT NULL COMMENT '资产ID',
`asset_no` varchar(64) DEFAULT NULL COMMENT '资产编号',
`status` varchar(32) NOT NULL COMMENT '单据状态',
`apply_user_id` bigint(20) DEFAULT NULL COMMENT '申请人',
`apply_dept_id` bigint(20) DEFAULT NULL COMMENT '申请部门'
```

**Step 4: 重新运行测试确认通过**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetRealEstateLifecycleSqlContractTest test`

Expected: PASS

**Step 5: Commit**

```bash
git add RuoYi-Vue/sql/20260315_asset_real_estate_lifecycle.sql RuoYi-Vue/sql/sql执行.md RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/sql/AssetRealEstateLifecycleSqlContractTest.java
git commit -m "test: lock real estate lifecycle sql contract"
```

### Task 2: 落地审批型不动产动作后端主链路

**Files:**

- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetRealEstateOwnershipChange.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetRealEstateDisposal.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetRealEstateOwnershipChangeMapper.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetRealEstateDisposalMapper.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetRealEstateOwnershipChangeMapper.xml`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetRealEstateDisposalMapper.xml`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetRealEstateOwnershipChangeService.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetRealEstateDisposalService.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRealEstateOwnershipChangeServiceImpl.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRealEstateDisposalServiceImpl.java`
- Create: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateOwnershipChangeController.java`
- Create: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateDisposalController.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetWorkflowBusinessHandler.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetWorkflowBusinessHandlerTest.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateOwnershipChangeServiceImplTest.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateDisposalServiceImplTest.java`

**Step 1: 先写失败测试，锁定审批型动作的最小行为**

在两个服务测试中先锁定：

- `assetType != 2` 时拒绝创建单据
- 创建权属变更单后会调用 `approvalEngine.startProcess(...)`
- 创建注销/处置单后会调用 `approvalEngine.startProcess(...)`
- Mapper XML 能从 classpath 正常加载

示例：

```java
@Test
void shouldRejectOwnershipChangeForFixedAsset() {
    when(assetInfoMapper.selectAssetInfoByAssetId(9001L)).thenReturn(fixedAsset());
    assertThrows(ServiceException.class, () -> service.insertOwnershipChange(buildReq()));
}
```

**Step 2: 运行测试确认失败**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset,ruoyi-workflow -am "-Dtest=AssetRealEstateOwnershipChangeServiceImplTest,AssetRealEstateDisposalServiceImplTest" test`

Expected: FAIL，原因应为领域对象、Mapper、Service 和控制器尚不存在。

**Step 3: 实现权属变更与注销/处置单据域**

最小实现要求：

- 两类单据都支持列表、详情、新增
- 新增时校验 `assetType=2`
- 新增时记录前值与目标值
- 新增后触发 `approvalEngine.startProcess`
- 单据初始状态统一收敛为 `pending`

**Step 4: 扩展工作流业务回写**

修改 `AssetWorkflowBusinessHandler.java`，补充：

```java
return List.of(
    "asset_requisition",
    "asset_maintenance",
    "asset_disposal",
    "asset_real_estate_ownership_change",
    "asset_real_estate_disposal"
);
```

并增加：

- 权属变更审批通过后回写 `asset_real_estate.rights_holder`、`property_cert_no`、`registration_date`
- 不动产注销/处置审批通过后回写资产主档终态与单据状态
- 驳回时只更新单据状态，不污染主档

**Step 5: 运行测试确认通过**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset,ruoyi-workflow -am "-Dtest=AssetRealEstateOwnershipChangeServiceImplTest,AssetRealEstateDisposalServiceImplTest,AssetWorkflowBusinessHandlerTest" test`

Expected: PASS

**Step 6: Commit**

```bash
git add RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetRealEstateOwnershipChange.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetRealEstateDisposal.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetRealEstateOwnershipChangeMapper.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetRealEstateDisposalMapper.java RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetRealEstateOwnershipChangeMapper.xml RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetRealEstateDisposalMapper.xml RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetRealEstateOwnershipChangeService.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetRealEstateDisposalService.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRealEstateOwnershipChangeServiceImpl.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRealEstateDisposalServiceImpl.java RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateOwnershipChangeController.java RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateDisposalController.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetWorkflowBusinessHandler.java RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetWorkflowBusinessHandlerTest.java RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateOwnershipChangeServiceImplTest.java RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateDisposalServiceImplTest.java
git commit -m "feat: add real estate approval lifecycle"
```

### Task 3: 落地免审批不动产动作后端主链路

**Files:**

- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetRealEstateUsageChange.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetRealEstateStatusChange.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetRealEstateUsageChangeMapper.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetRealEstateStatusChangeMapper.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetRealEstateUsageChangeMapper.xml`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetRealEstateStatusChangeMapper.xml`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetRealEstateUsageChangeService.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetRealEstateStatusChangeService.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRealEstateUsageChangeServiceImpl.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRealEstateStatusChangeServiceImpl.java`
- Create: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateUsageChangeController.java`
- Create: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateStatusChangeController.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetRealEstateMapper.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetRealEstateMapper.xml`
- Create: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateUsageChangeServiceImplTest.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateStatusChangeServiceImplTest.java`

**Step 1: 先写失败测试，锁定免审批动作的直达语义**

在两个测试中先锁定：

- 只允许 `assetType=2`
- 创建单据后不调用 `approvalEngine`
- 创建完成后单据直接进入 `completed`
- `AssetRealEstateMapper` 被调用以回写当前用途或当前状态

示例：

```java
verify(assetRealEstateMapper).updateAssetRealEstate(argThat((estate) ->
    "办公".equals(estate.getBuildingUse())
));
```

**Step 2: 运行测试确认失败**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetRealEstateUsageChangeServiceImplTest,AssetRealEstateStatusChangeServiceImplTest test`

Expected: FAIL，原因应为两个服务域尚未实现。

**Step 3: 实现用途变更与状态变更服务**

最小实现要求：

- 新增、列表、详情
- 创建时读取当前 `asset_real_estate` 或主档状态作为前值
- 写入目标值后直接完成单据
- 同步更新 `asset_real_estate` 和必要的 `asset_info.asset_status`

**Step 4: 运行测试确认通过**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetRealEstateUsageChangeServiceImplTest,AssetRealEstateStatusChangeServiceImplTest test`

Expected: PASS

**Step 5: Commit**

```bash
git add RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetRealEstateUsageChange.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetRealEstateStatusChange.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetRealEstateUsageChangeMapper.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetRealEstateStatusChangeMapper.java RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetRealEstateUsageChangeMapper.xml RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetRealEstateStatusChangeMapper.xml RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetRealEstateUsageChangeService.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetRealEstateStatusChangeService.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRealEstateUsageChangeServiceImpl.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRealEstateStatusChangeServiceImpl.java RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateUsageChangeController.java RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateStatusChangeController.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetRealEstateMapper.java RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetRealEstateMapper.xml RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateUsageChangeServiceImplTest.java RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateStatusChangeServiceImplTest.java
git commit -m "feat: add direct real estate lifecycle changes"
```

### Task 4: 收敛前端共享类型、API 契约与生命周期入口

**Files:**

- Modify: `art-design-pro/src/types/asset.ts`
- Create: `art-design-pro/src/api/asset/real-estate-ownership.ts`
- Create: `art-design-pro/src/api/asset/real-estate-usage.ts`
- Create: `art-design-pro/src/api/asset/real-estate-status.ts`
- Create: `art-design-pro/src/api/asset/real-estate-disposal.ts`
- Modify: `art-design-pro/src/views/asset/list/asset-lifecycle.helper.ts`
- Modify: `art-design-pro/src/views/asset/list/index.vue`
- Create: `art-design-pro/tests/api/asset-real-estate-ownership.test.ts`
- Create: `art-design-pro/tests/api/asset-real-estate-usage.test.ts`
- Create: `art-design-pro/tests/api/asset-real-estate-status.test.ts`
- Create: `art-design-pro/tests/api/asset-real-estate-disposal.test.ts`
- Create: `art-design-pro/tests/views/asset/real-estate-lifecycle.helper.test.ts`

**Step 1: 先写失败测试，锁定前端入口分流与 API 路径**

先锁定：

- 不动产动作不再只返回 `realEstateChange`
- 动作键拆成 `realEstateOwnership`、`realEstateUsage`、`realEstateStatus`、`realEstateDisposal`
- 四个 API 文件分别请求正确路径

示例：

```ts
expect(buildLifecycleActions({ assetType: '2', assetStatus: '1' }).map((item) => item.key)).toEqual(
  expect.arrayContaining(['realEstateOwnership', 'realEstateUsage', 'realEstateStatus', 'realEstateDisposal'])
)
```

**Step 2: 运行测试确认失败**

Run: `cd art-design-pro && npx vitest run tests/api/asset-real-estate-ownership.test.ts tests/api/asset-real-estate-usage.test.ts tests/api/asset-real-estate-status.test.ts tests/api/asset-real-estate-disposal.test.ts tests/views/asset/real-estate-lifecycle.helper.test.ts`

Expected: FAIL，原因应为 API 文件和动作键尚未实现。

**Step 3: 修改共享类型和入口 helper**

在 `src/types/asset.ts` 中至少新增：

```ts
key:
  | 'change'
  | 'delete'
  | 'requisition'
  | 'repair'
  | 'disposal'
  | 'realEstateOwnership'
  | 'realEstateUsage'
  | 'realEstateStatus'
  | 'realEstateDisposal'
```

在 `asset-lifecycle.helper.ts` 中把不动产动作改为正式入口定义，不再只给占位文案。

**Step 4: 补齐四个 API 封装**

四个 API 文件最小要提供：

- `listXxx`
- `getXxxDetail`
- `createXxx`

审批型动作不需要前端单独实现审批 API，继续走现有工作流待办页。

**Step 5: 重新运行测试确认通过**

Run: `cd art-design-pro && npx vitest run tests/api/asset-real-estate-ownership.test.ts tests/api/asset-real-estate-usage.test.ts tests/api/asset-real-estate-status.test.ts tests/api/asset-real-estate-disposal.test.ts tests/views/asset/real-estate-lifecycle.helper.test.ts`

Expected: PASS

**Step 6: Commit**

```bash
git add art-design-pro/src/types/asset.ts art-design-pro/src/api/asset/real-estate-ownership.ts art-design-pro/src/api/asset/real-estate-usage.ts art-design-pro/src/api/asset/real-estate-status.ts art-design-pro/src/api/asset/real-estate-disposal.ts art-design-pro/src/views/asset/list/asset-lifecycle.helper.ts art-design-pro/src/views/asset/list/index.vue art-design-pro/tests/api/asset-real-estate-ownership.test.ts art-design-pro/tests/api/asset-real-estate-usage.test.ts art-design-pro/tests/api/asset-real-estate-status.test.ts art-design-pro/tests/api/asset-real-estate-disposal.test.ts art-design-pro/tests/views/asset/real-estate-lifecycle.helper.test.ts
git commit -m "feat: expose real estate lifecycle actions in frontend"
```

### Task 5: 落地不动产动作页面并接入统一台账入口

**Files:**

- Create: `art-design-pro/src/views/asset/real-estate/ownership/index.vue`
- Create: `art-design-pro/src/views/asset/real-estate/usage/index.vue`
- Create: `art-design-pro/src/views/asset/real-estate/status/index.vue`
- Create: `art-design-pro/src/views/asset/real-estate/disposal/index.vue`
- Create: `art-design-pro/src/views/asset/real-estate/real-estate-lifecycle.helper.ts`
- Create: `art-design-pro/tests/views/asset/real-estate-ledger.helper.test.ts`
- Modify: `art-design-pro/src/views/asset/list/index.vue`

**Step 1: 先写失败测试，锁定资产上下文透传和页面回显**

测试至少锁定：

- 从资产列表进入不动产动作页时会带上 `assetId`、`assetNo`、`assetName`
- 新建成功后会刷新动作列表
- 审批型动作页能正确展示 `pending` / `approved` / `rejected`
- 免审批动作页能正确展示 `completed`

示例：

```ts
expect(parseAssetRouteQuery({ assetId: '1001', assetNo: 'RE-001' })).toMatchObject({
  assetId: 1001,
  assetNo: 'RE-001'
})
```

**Step 2: 运行测试确认失败**

Run: `cd art-design-pro && npx vitest run tests/views/asset/real-estate-ledger.helper.test.ts`

Expected: FAIL，原因应为页面 helper 和相关视图尚未存在。

**Step 3: 实现四个页面**

每个页面最小要求：

- 查询条件
- 列表表格
- 新建申请抽屉或弹窗
- 查看详情能力
- 从路由 query 预填资产上下文

推荐保持与现有页面风格一致：

- 参考 `src/views/asset/requisition/index.vue`
- 参考 `src/views/asset/maintenance/index.vue`
- 参考 `src/views/asset/disposal/index.vue`

**Step 4: 修改资产列表跳转逻辑**

在 `src/views/asset/list/index.vue` 中：

- 固定资产维持现有跳转
- 不动产新增四个正式跳转入口
- 删除占位消息提示

**Step 5: 运行测试确认通过**

Run: `cd art-design-pro && npx vitest run tests/views/asset/real-estate-ledger.helper.test.ts tests/views/asset/real-estate-lifecycle.helper.test.ts`

Expected: PASS

**Step 6: Commit**

```bash
git add art-design-pro/src/views/asset/real-estate/ownership/index.vue art-design-pro/src/views/asset/real-estate/usage/index.vue art-design-pro/src/views/asset/real-estate/status/index.vue art-design-pro/src/views/asset/real-estate/disposal/index.vue art-design-pro/src/views/asset/real-estate/real-estate-lifecycle.helper.ts art-design-pro/tests/views/asset/real-estate-ledger.helper.test.ts art-design-pro/src/views/asset/list/index.vue
git commit -m "feat: add real estate lifecycle ledger pages"
```

### Task 6: 补齐菜单权限、契约说明、联调验证与执行记录

**Files:**

- Create: `RuoYi-Vue/sql/20260315_asset_real_estate_lifecycle_menu.sql`
- Modify: `RuoYi-Vue/sql/sql执行.md`
- Modify: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/sql/AssetMenuSqlContractTest.java`
- Modify: `docs/plans/2026-03-14-asset-api-contract-notes.md`
- Modify: `docs/plans/2026-03-15-asset-system-next-phase-implementation-plan.md`

**Step 1: 先写失败测试，锁定菜单路径与权限口径**

在 `AssetMenuSqlContractTest.java` 中补充以下文本契约：

```java
assertAll(
    () -> assertTrue(patchSql.contains("asset/real-estate/ownership/index")),
    () -> assertTrue(patchSql.contains("asset/real-estate/disposal/index")),
    () -> assertTrue(patchSql.contains("asset:realEstateOwnership:list")),
    () -> assertTrue(patchSql.contains("asset:realEstateDisposal:add"))
);
```

同时补充：

- 用途变更、状态变更路径和权限
- `sql执行.md` 中记录新菜单脚本

**Step 2: 运行测试确认失败**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetMenuSqlContractTest test`

Expected: FAIL，原因应为新菜单 SQL 与契约文档尚未补齐。

**Step 3: 补齐菜单 SQL 与接口契约说明**

`2026-03-14-asset-api-contract-notes.md` 至少新增：

- `GET /asset/real-estate/ownership/list`
- `POST /asset/real-estate/ownership`
- `GET /asset/real-estate/usage/list`
- `POST /asset/real-estate/usage`
- `GET /asset/real-estate/status/list`
- `POST /asset/real-estate/status`
- `GET /asset/real-estate/disposal/list`
- `POST /asset/real-estate/disposal`

并写明：

- 哪些动作进审批
- 哪些动作免审批
- 菜单路径与权限标识最终口径
- `npm run build` 若仍被历史目录阻塞，需要明确说明“非本次新增”

**Step 4: 做聚焦验证并回填执行记录**

后端聚焦验证：

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset,ruoyi-workflow -am "-Dtest=AssetRealEstateLifecycleSqlContractTest,AssetMenuSqlContractTest,AssetRealEstateOwnershipChangeServiceImplTest,AssetRealEstateDisposalServiceImplTest,AssetRealEstateUsageChangeServiceImplTest,AssetRealEstateStatusChangeServiceImplTest,AssetWorkflowBusinessHandlerTest" test`

Expected: PASS

前端聚焦验证：

Run: `cd art-design-pro && npx vitest run tests/api/asset-real-estate-ownership.test.ts tests/api/asset-real-estate-usage.test.ts tests/api/asset-real-estate-status.test.ts tests/api/asset-real-estate-disposal.test.ts tests/views/asset/real-estate-lifecycle.helper.test.ts tests/views/asset/real-estate-ledger.helper.test.ts`

Expected: PASS

全仓构建备注：

Run: `cd art-design-pro && npm run build`

Expected: 若仍失败，应只剩历史 `monitor/system` 目录问题，且本次新增页面不引入新的 TypeScript 报错。

**Step 5: Commit**

```bash
git add RuoYi-Vue/sql/20260315_asset_real_estate_lifecycle_menu.sql RuoYi-Vue/sql/sql执行.md RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/sql/AssetMenuSqlContractTest.java docs/plans/2026-03-14-asset-api-contract-notes.md docs/plans/2026-03-15-asset-system-next-phase-implementation-plan.md
git commit -m "docs: close out real estate lifecycle delivery plan"
```

## 风险与对策

- 风险：不动产动作字段直接堆回 `asset_real_estate`
  对策：只有审批通过或动作完成后才回写当前事实，过程字段全部保留在动作单中

- 风险：前端动作键和菜单路径再次漂移
  对策：同时用 `AssetMenuSqlContractTest`、Vitest helper 测试和契约文档锁定最终口径

- 风险：全仓构建历史问题干扰本阶段验收
  对策：聚焦资产模块测试与局部联调，并在执行记录中单独标注历史阻塞

## 完成定义

- 四类不动产动作均有正式建表、Mapper、Service、Controller 与前端页面
- 权属变更与注销/处置接入最小审批闭环并能回写主档
- 用途变更与状态变更免审批直达并能留痕、回写主档
- 资产列表中的不动产动作从占位按钮升级为正式页面入口
- 菜单 SQL、权限标识、接口契约、测试矩阵和联调说明全部补齐
- 可以明确说明资产模块局部验证结果，以及全仓构建是否仍只受历史目录阻塞
