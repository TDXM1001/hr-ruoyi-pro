# 资产一期收口整改实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 交付一份清晰的资产一期收口设计文档，并补齐“资产生命周期聚合详情”能力，让资产管理员可以在一个详情页里同时查看台账当前态、交接、盘点、处置和变更轨迹，而不需要在多个页面之间来回切换。

**Architecture:** 保持现有固定资产一期边界不变，不新增不动产流程。后端在台账模块下新增只读生命周期聚合接口，尽量复用现有业务对象，仅为生命周期展示补充最小 VO；前端把资产详情页从“主档字段展示页”升级为“生命周期聚合页”。二期扩展位通过接口形态和文档约束预留，而不是在本批次直接开放新业务链。

**Tech Stack:** Spring Boot 3、MyBatis XML、RuoYi Admin Controller/Service 规范、Vue 3、TypeScript、Element Plus、Vitest、JUnit 5、Mockito

---

### 任务 1：落统一收口设计文档

**Files:**
- Create: `docs/plans/2026-03-20-asset-phase1-closure-remediation-design.md`

**Step 1: 编写设计文档内容**

文档必须包含：
- 双基线评审框架
- 一期固定资产闭环原则
- Mermaid 流程图
- P0/P1/P2 差距分层
- 第一批整改范围
- 二期扩展位边界

**Step 2: 校对文档表达**

检查点：
- 是否清楚区分“一期完成度”和“系统总设计缺口”
- Mermaid 图是否能在支持 Mermaid 的 Markdown 查看器中渲染
- 是否存在范围外承诺，误导为“本次直接上线不动产流程”

**Step 3: Commit**

```bash
git add docs/plans/2026-03-20-asset-phase1-closure-remediation-design.md
git commit -m "docs: add asset phase1 closure remediation design"
```

### 任务 2：先写生命周期聚合后端失败测试

**Files:**
- Modify: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetLedgerServiceImplTest.java`

**Step 1: 先写失败测试**

测试目标：
- 按资产 ID 查询时，服务层能一次性加载台账详情
- 同时聚合该资产的交接、盘点、处置、变更轨迹
- 当没有业务留痕时，返回空列表而不是 `null`

示例结构：

```java
@Test
@DisplayName("查询资产生命周期详情时应聚合业务留痕记录")
void shouldAssembleLifecycleDetail()
{
    when(assetLedgerMapper.selectAssetLedgerById(8L)).thenReturn(buildLedgerVo(8L));
    when(assetHandoverItemMapper.selectAssetHandoverItemsByAssetId(8L)).thenReturn(List.of(buildHandoverItemVo()));
    when(assetInventoryMapper.selectAssetInventoryRecordsByAssetId(8L)).thenReturn(List.of(buildInventoryRecordVo()));
    when(assetDisposalMapper.selectAssetDisposalsByAssetId(8L)).thenReturn(List.of(buildDisposalVo()));
    when(assetChangeLogMapper.selectAssetChangeLogListByAssetId(8L)).thenReturn(List.of(buildChangeLog()));

    AssetLedgerLifecycleVo detail = service.selectAssetLifecycleById(8L);

    assertEquals(1, detail.getHandoverRecords().size());
    assertEquals(1, detail.getInventoryRecords().size());
    assertEquals(1, detail.getDisposalRecords().size());
    assertEquals(1, detail.getChangeLogs().size());
}
```

**Step 2: 运行测试并确认失败**

Run:

```bash
mvn -pl ruoyi-asset -am test -Dtest=AssetLedgerServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false"
```

Expected: FAIL，原因应为生命周期 VO、Mapper 方法、Service 方法尚不存在

**Step 3: Commit**

```bash
git add RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetLedgerServiceImplTest.java
git commit -m "test: cover asset lifecycle detail aggregation"
```

### 任务 3：实现后端生命周期聚合能力

**Files:**
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/vo/AssetLedgerLifecycleVo.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/vo/AssetInventoryRecordVo.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetChangeLogMapper.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetChangeLogMapper.xml`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetHandoverItemMapper.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetHandoverItemMapper.xml`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetInventoryMapper.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetInventoryMapper.xml`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetDisposalMapper.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetDisposalMapper.xml`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetLedgerService.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetLedgerServiceImpl.java`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetLedgerController.java`

**Step 1: 编写最小实现**

需要实现：
- 一个只读生命周期聚合 VO，承接台账当前态和四类记录列表
- Mapper 新增按资产 ID 查询：
  - 交接记录
  - 盘点记录
  - 处置记录
  - 变更轨迹
- Service 新增 `selectAssetLifecycleById(Long assetId)`
- Controller 新增 `GET /asset/ledger/{assetId}/lifecycle`

行为要求保持最小化：
- 资产不存在时抛 `ServiceException`
- 没有业务记录时返回空列表
- 第一版不做分页

**Step 2: 运行后端测试并确认转绿**

Run:

```bash
mvn -pl ruoyi-asset -am test -Dtest=AssetLedgerServiceImplTest,AssetInventoryServiceImplTest,AssetDisposalServiceImplTest,AssetHandoverServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false"
```

Expected: PASS

**Step 3: Commit**

```bash
git add RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/vo \
        RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper \
        RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset \
        RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service \
        RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetLedgerServiceImpl.java \
        RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetLedgerController.java
git commit -m "feat: add asset lifecycle detail backend"
```

### 任务 4：先写前端生命周期详情失败测试

**Files:**
- Modify: `art-design-pro/tests/api/asset-ledger.test.ts`
- Create: `art-design-pro/tests/views/asset-ledger-detail-page.test.ts`

**Step 1: 先写 API 失败测试**

覆盖点：
- `getAssetLedgerLifecycle` 已导出
- 该方法请求 `/asset/ledger/{assetId}/lifecycle`

**Step 2: 先写页面失败测试**

覆盖点：
- 详情页在 API 返回后能渲染生命周期区域
- 页面展示“生命周期轨迹”“交接记录”“盘点记录”“处置记录”等区块

**Step 3: 运行测试并确认红灯**

Run:

```bash
pnpm vitest run tests/api/asset-ledger.test.ts tests/views/asset-ledger-detail-page.test.ts
```

Expected: FAIL，因为前端还没有生命周期 API helper 和生命周期详情 UI

**Step 4: Commit**

```bash
git add art-design-pro/tests/api/asset-ledger.test.ts art-design-pro/tests/views/asset-ledger-detail-page.test.ts
git commit -m "test: cover asset lifecycle detail page"
```

### 任务 5：实现前端生命周期详情页

**Files:**
- Modify: `art-design-pro/src/api/asset/ledger.ts`
- Modify: `art-design-pro/src/views/asset/ledger/detail/index.vue`

**Step 1: 编写最小实现**

需要补充：
- 生命周期 API 类型和请求方法
- 详情页加载逻辑：台账当前态 + 生命周期聚合数据
- 生命周期轨迹时间线
- 交接/盘点/处置记录区块
- 业务记录为空时的空态处理

需要保留：
- 现有固定资产一期的视觉语言
- 返回/编辑等现有导航操作
- 已有主档详情区块

**Step 2: 运行前端测试并确认转绿**

Run:

```bash
pnpm vitest run tests/api/asset-ledger.test.ts tests/views/asset-ledger-detail-page.test.ts tests/views/asset-use-form-page.test.ts
```

Expected: PASS

**Step 3: Commit**

```bash
git add art-design-pro/src/api/asset/ledger.ts art-design-pro/src/views/asset/ledger/detail/index.vue
git commit -m "feat: add asset lifecycle detail page"
```

### 任务 6：补收口证据与回归验证

**Files:**
- Modify: `docs/plans/2026-03-20-asset-management-system-phase1-completion-review.md`

**Step 1: 更新完成度评审文档**

补充说明第一批整改已新增：
- 统一收口设计文档
- 生命周期详情聚合能力

**Step 2: 运行回归验证命令**

Run:

```bash
mvn -pl ruoyi-asset -am test -Dtest=AssetLedgerServiceImplTest,AssetInventoryServiceImplTest,AssetDisposalServiceImplTest,AssetHandoverServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false"
pnpm vitest run tests/api/asset-ledger.test.ts tests/views/asset-ledger-detail-page.test.ts tests/views/asset-use-form-page.test.ts
```

Expected: PASS

**Step 3: Commit**

```bash
git add docs/plans/2026-03-20-asset-management-system-phase1-completion-review.md
git commit -m "docs: record asset lifecycle remediation"
```
