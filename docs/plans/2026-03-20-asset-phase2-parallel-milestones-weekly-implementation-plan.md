# Asset Phase 2 Parallel Milestones Weekly Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 在不破坏一期固定资产闭环的前提下，以三线并行+分层门禁方式完成二期能力落地与上线准备。  

**Architecture:** 先完成 M0 公共底座（模型、审批回写矩阵、集成契约），再并行推进不动产/审批/集成三线 MVP，随后做跨线联调与试运行收口。坚持 DRY、YAGNI、TDD 与小步快提交流程。  

**Tech Stack:** Spring Boot 3、MyBatis XML、MySQL、Vue 3、TypeScript、Element Plus、JUnit 5、Mockito、Vitest  

---

## 周次映射（12周）

1. W1-W2：M0 基线冻结与公共底座  
2. W3-W6：M1 三线 MVP 并行  
3. W7-W9：M2 跨线联调  
4. W10-W12：M3 试运行收口  

---

### Task 1: M0-不动产模型与数据基线（W1）

**Files:**
- Create: `RuoYi-Vue/sql/asset/09-asset-real-estate-schema-upgrade-20260320.sql`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetRealEstateProfile.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetRealEstateMapper.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetRealEstateMapper.xml`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateServiceImplTest.java`

**Step 1: Write the failing test**

```java
@Test
@DisplayName("不动产扩展档案应按资产ID正确查询")
void shouldQueryRealEstateProfileByAssetId() {
    when(assetRealEstateMapper.selectByAssetId(1001L)).thenReturn(buildProfile(1001L));
    AssetRealEstateProfile profile = service.selectByAssetId(1001L);
    assertEquals("CERT-001", profile.getOwnershipCertNo());
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -pl ruoyi-asset -am test -Dtest=AssetRealEstateServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false"`  
Expected: FAIL（类/Mapper/Service 尚不存在）

**Step 3: Write minimal implementation**

```java
public AssetRealEstateProfile selectByAssetId(Long assetId) {
    return assetRealEstateMapper.selectByAssetId(assetId);
}
```

**Step 4: Run test to verify it passes**

Run: `mvn -pl ruoyi-asset -am test -Dtest=AssetRealEstateServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false"`  
Expected: PASS

**Step 5: Commit**

```bash
git add RuoYi-Vue/sql/asset/09-asset-real-estate-schema-upgrade-20260320.sql \
        RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetRealEstateProfile.java \
        RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetRealEstateMapper.java \
        RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetRealEstateMapper.xml \
        RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateServiceImplTest.java
git commit -m "feat: add real estate profile schema and query baseline"
```

### Task 2: M0-审批回写矩阵基线（W2）

**Files:**
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/enums/AssetApprovalType.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/enums/AssetApprovalStatus.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetApprovalRecord.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/AssetApprovalStatusMatrix.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/AssetApprovalStatusMatrixTest.java`

**Step 1: Write the failing test**

```java
@Test
@DisplayName("处置审批通过后资产应回写为已处置")
void shouldMapDisposalApprovedToDisposed() {
    AssetStatus result = matrix.mapToAssetStatus(AssetApprovalType.DISPOSAL, AssetApprovalStatus.APPROVED);
    assertEquals(AssetStatus.DISPOSED, result);
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -pl ruoyi-asset -am test -Dtest=AssetApprovalStatusMatrixTest "-Dsurefire.failIfNoSpecifiedTests=false"`  
Expected: FAIL（映射类和枚举尚不存在）

**Step 3: Write minimal implementation**

```java
if (type == AssetApprovalType.DISPOSAL && status == AssetApprovalStatus.APPROVED) {
    return AssetStatus.DISPOSED;
}
```

**Step 4: Run test to verify it passes**

Run: `mvn -pl ruoyi-asset -am test -Dtest=AssetApprovalStatusMatrixTest "-Dsurefire.failIfNoSpecifiedTests=false"`  
Expected: PASS

**Step 5: Commit**

```bash
git add RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/enums/AssetApprovalType.java \
        RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/enums/AssetApprovalStatus.java \
        RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetApprovalRecord.java \
        RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/AssetApprovalStatusMatrix.java \
        RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/AssetApprovalStatusMatrixTest.java
git commit -m "feat: add approval status matrix baseline"
```

### Task 3: M1-不动产主线 MVP（W3-W4）

**Files:**
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetRealEstateService.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRealEstateServiceImpl.java`
- Create: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateController.java`
- Create: `art-design-pro/src/api/asset/real-estate.ts`
- Create: `art-design-pro/src/views/asset/real-estate/index.vue`
- Test: `art-design-pro/tests/api/asset-real-estate.test.ts`

**Step 1: Write the failing test**

```ts
it("should request /asset/real-estate/list", async () => {
  await getRealEstateList({});
  expect(mockRequest).toHaveBeenCalledWith(expect.objectContaining({ url: "/asset/real-estate/list" }));
});
```

**Step 2: Run test to verify it fails**

Run: `pnpm vitest run tests/api/asset-real-estate.test.ts`  
Expected: FAIL（API helper 尚不存在）

**Step 3: Write minimal implementation**

```ts
export function getRealEstateList(params: any) {
  return request({ url: "/asset/real-estate/list", method: "get", params });
}
```

**Step 4: Run test to verify it passes**

Run: `pnpm vitest run tests/api/asset-real-estate.test.ts`  
Expected: PASS

**Step 5: Commit**

```bash
git add RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetRealEstateService.java \
        RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRealEstateServiceImpl.java \
        RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateController.java \
        art-design-pro/src/api/asset/real-estate.ts \
        art-design-pro/src/views/asset/real-estate/index.vue \
        art-design-pro/tests/api/asset-real-estate.test.ts
git commit -m "feat: add real estate mvp flow"
```

### Task 4: M1-审批主线 MVP（W3-W5）

**Files:**
- Create: `RuoYi-Vue/sql/asset/10-asset-approval-schema-upgrade-20260320.sql`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetApprovalMapper.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetApprovalMapper.xml`
- Create: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetApprovalController.java`
- Create: `art-design-pro/src/api/asset/approval.ts`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetApprovalServiceImplTest.java`

**Step 1: Write the failing test**

```java
@Test
@DisplayName("审批通过应触发资产状态回写")
void shouldWriteBackStatusOnApproval() {
    service.approve(2001L, "pass");
    verify(assetLedgerMapper).updateAssetStatus(eq(2001L), eq(AssetStatus.DISPOSED));
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -pl ruoyi-asset -am test -Dtest=AssetApprovalServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false"`  
Expected: FAIL

**Step 3: Write minimal implementation**

```java
public void approve(Long assetId, String opinion) {
    assetLedgerMapper.updateAssetStatus(assetId, AssetStatus.DISPOSED.name(), SecurityUtils.getUsername());
}
```

**Step 4: Run test to verify it passes**

Run: `mvn -pl ruoyi-asset -am test -Dtest=AssetApprovalServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false"`  
Expected: PASS

**Step 5: Commit**

```bash
git add RuoYi-Vue/sql/asset/10-asset-approval-schema-upgrade-20260320.sql \
        RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetApprovalMapper.java \
        RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetApprovalMapper.xml \
        RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetApprovalController.java \
        RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetApprovalServiceImplTest.java \
        art-design-pro/src/api/asset/approval.ts
git commit -m "feat: add approval mvp and status write-back"
```

### Task 5: M1-集成主线 MVP（W4-W6）

**Files:**
- Create: `RuoYi-Vue/sql/asset/11-asset-integration-schema-upgrade-20260320.sql`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetIntegrationEvent.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetIntegrationService.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetIntegrationServiceImpl.java`
- Create: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetIntegrationController.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetIntegrationServiceImplTest.java`

**Step 1: Write the failing test**

```java
@Test
@DisplayName("同一幂等键重复回写应只处理一次")
void shouldBeIdempotentForFinanceWriteBack() {
    service.writeBackFinanceVoucher(buildEvent("idem-001"));
    service.writeBackFinanceVoucher(buildEvent("idem-001"));
    verify(assetIntegrationMapper, times(1)).insertEvent(any());
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -pl ruoyi-asset -am test -Dtest=AssetIntegrationServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false"`  
Expected: FAIL

**Step 3: Write minimal implementation**

```java
if (assetIntegrationMapper.existsByIdempotencyKey(event.getIdempotencyKey())) {
    return;
}
assetIntegrationMapper.insertEvent(event);
```

**Step 4: Run test to verify it passes**

Run: `mvn -pl ruoyi-asset -am test -Dtest=AssetIntegrationServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false"`  
Expected: PASS

**Step 5: Commit**

```bash
git add RuoYi-Vue/sql/asset/11-asset-integration-schema-upgrade-20260320.sql \
        RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetIntegrationEvent.java \
        RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetIntegrationService.java \
        RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetIntegrationServiceImpl.java \
        RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetIntegrationController.java \
        RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetIntegrationServiceImplTest.java
git commit -m "feat: add integration mvp with idempotency"
```

### Task 6: M2-跨线场景1联调（W7）

**Files:**
- Create: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/AssetPhase2CrossFlowSmokeTest.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetInventoryServiceImpl.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetDisposalServiceImpl.java`

**Step 1: Write the failing test**

```java
@Test
@DisplayName("巡检异常到整改审批通过后应回写为在用")
void shouldCloseRectificationFlow() {
    // arrange + act
    assertEquals(AssetStatus.IN_USE, ledger.getStatus());
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -pl ruoyi-asset -am test -Dtest=AssetPhase2CrossFlowSmokeTest "-Dsurefire.failIfNoSpecifiedTests=false"`  
Expected: FAIL

**Step 3: Write minimal implementation**

```java
// 在整改审批通过分支执行统一回写
assetLedgerMapper.updateAssetStatus(assetId, AssetStatus.IN_USE.name(), SecurityUtils.getUsername());
```

**Step 4: Run test to verify it passes**

Run: `mvn -pl ruoyi-asset -am test -Dtest=AssetPhase2CrossFlowSmokeTest "-Dsurefire.failIfNoSpecifiedTests=false"`  
Expected: PASS

**Step 5: Commit**

```bash
git add RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/AssetPhase2CrossFlowSmokeTest.java \
        RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetInventoryServiceImpl.java \
        RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetDisposalServiceImpl.java
git commit -m "test: add cross-flow smoke for rectification approval"
```

### Task 7: M2-跨线场景2联调（W8-W9）

**Files:**
- Modify: `art-design-pro/src/views/asset/ledger/detail/index.vue`
- Create: `art-design-pro/src/views/asset/approval/index.vue`
- Create: `art-design-pro/tests/views/asset-phase2-cross-flow-page.test.ts`
- Modify: `art-design-pro/src/api/asset/ledger.ts`
- Modify: `art-design-pro/src/api/asset/approval.ts`

**Step 1: Write the failing test**

```ts
it("should render approval timeline and finance writeback status", async () => {
  // mount detail page
  expect(screen.getByText("审批轨迹")).toBeInTheDocument();
  expect(screen.getByText("财务回写状态")).toBeInTheDocument();
});
```

**Step 2: Run test to verify it fails**

Run: `pnpm vitest run tests/views/asset-phase2-cross-flow-page.test.ts`  
Expected: FAIL

**Step 3: Write minimal implementation**

```ts
// 增加详情页审批轨迹与集成状态区块渲染
```

**Step 4: Run test to verify it passes**

Run: `pnpm vitest run tests/views/asset-phase2-cross-flow-page.test.ts tests/views/asset-ledger-detail-page.test.ts`  
Expected: PASS

**Step 5: Commit**

```bash
git add art-design-pro/src/views/asset/ledger/detail/index.vue \
        art-design-pro/src/views/asset/approval/index.vue \
        art-design-pro/tests/views/asset-phase2-cross-flow-page.test.ts \
        art-design-pro/src/api/asset/ledger.ts \
        art-design-pro/src/api/asset/approval.ts
git commit -m "feat: add phase2 cross-flow timeline ui"
```

### Task 8: M3-UAT收口与发布门禁（W10-W12）

**Files:**
- Create: `docs/plans/2026-03-20-asset-management-system-phase2-uat-checklist.md`
- Create: `docs/plans/2026-03-20-asset-management-system-phase2-completion-review.md`
- Modify: `RuoYi-Vue/sql/asset/README.md`

**Step 1: Write the failing test**

```text
新增二期UAT条目：不动产主线、审批回写、集成幂等、跨线闭环
```

**Step 2: Run test to verify it fails**

Run: `mvn -pl ruoyi-asset -am test "-Dtest=Asset*Test" "-Dsurefire.failIfNoSpecifiedTests=false"`  
Run: `pnpm vitest run tests/api/asset-ledger.test.ts tests/views/asset-ledger-detail-page.test.ts`  
Expected: 若未完成二期能力则存在失败用例/缺失用例

**Step 3: Write minimal implementation**

```text
补全二期收口文档、更新脚本执行顺序、补齐回归用例清单
```

**Step 4: Run test to verify it passes**

Run: `mvn -pl ruoyi-asset -am test "-Dtest=Asset*Test" "-Dsurefire.failIfNoSpecifiedTests=false"`  
Run: `pnpm vitest run tests/api/asset-ledger.test.ts tests/api/asset-real-estate.test.ts tests/views/asset-ledger-detail-page.test.ts tests/views/asset-phase2-cross-flow-page.test.ts`  
Expected: PASS

**Step 5: Commit**

```bash
git add docs/plans/2026-03-20-asset-management-system-phase2-uat-checklist.md \
        docs/plans/2026-03-20-asset-management-system-phase2-completion-review.md \
        RuoYi-Vue/sql/asset/README.md
git commit -m "docs: add phase2 uat and completion review"
```

---

## 执行说明

1. 每个 Task 必须按“先红后绿”执行，不得跳过失败测试阶段。  
2. 每个 Task 结束必须单独提交，避免大提交难以回滚。  
3. 若 M0 任何项未达标，禁止进入 M1。  
4. 若 M2 跨线场景未全绿，禁止进入 M3 发布评审。  

