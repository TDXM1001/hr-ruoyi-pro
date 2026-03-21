# 不动产占用管理最小闭环 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为不动产档案补齐资产级占用管理最小闭环，支持发起占用、变更占用、释放占用，并回写资产主档与生命周期。

**Architecture:** 采用独立占用单表 `ast_asset_real_estate_occupancy_order` 记录当前与历史占用，资产主档 `ast_asset_ledger` 只保留当前占用快照。第一批只做后端 SQL、服务、接口和单测；第二批再补前端占用页签交互。

**Tech Stack:** Spring Boot, MyBatis XML, JUnit5, Mockito, MySQL, Vue3（第二批）

---

### Task 1: 落库不动产占用单表

**Files:**
- Create: `RuoYi-Vue/sql/asset/19-asset-real-estate-occupancy-minimum-closure-upgrade-20260322.sql`

**Step 1: 写表结构与索引**

创建：
1. `ast_asset_real_estate_occupancy_order`
2. 索引：`asset_id`、`occupancy_status`、`occupancy_no`

**Step 2: 写增量兼容逻辑**

保证脚本可重复执行：
1. `create table if not exists`
2. 不直接依赖旧环境清库

**Step 3: 预留本地执行验证**

验证点：
1. 表已存在
2. 字段完整

### Task 2: 写占用闭环后端失败测试

**Files:**
- Modify: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateServiceImplTest.java`

**Step 1: 新增失败测试：发起占用成功**

校验：
1. 插入占用单
2. 回写资产主档使用信息
3. 写入变更日志

**Step 2: 新增失败测试：重复发起占用被拒绝**

校验：
1. 已存在 `ACTIVE` 占用时抛出异常

**Step 3: 新增失败测试：变更占用关闭旧单并创建新单**

校验：
1. 旧单更新为 `RELEASED`
2. 新单插入为 `ACTIVE`
3. 资产主档回写为新占用

**Step 4: 新增失败测试：释放占用回写资产为闲置**

校验：
1. 占用单改为 `RELEASED`
2. 资产状态回写 `IDLE`
3. 使用部门、责任人、位置清空

**Step 5: 运行单测确认失败**

Run:
`mvn -pl ruoyi-asset -am test "-Dtest=AssetRealEstateServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false"`

Expected:
- 因接口或实现缺失而失败

### Task 3: 实现占用 Domain / Mapper / Service / Controller

**Files:**
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetRealEstateOccupancyOrder.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/bo/AssetRealEstateOccupancyBo.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/bo/AssetRealEstateOccupancyReleaseBo.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/vo/AssetRealEstateOccupancyVo.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetRealEstateOccupancyMapper.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetRealEstateOccupancyMapper.xml`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetRealEstateService.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRealEstateServiceImpl.java`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateController.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/vo/AssetLedgerLifecycleVo.java`

**Step 1: 增加占用查询接口**

实现：
1. 查询资产占用列表
2. 生命周期聚合带出占用记录

**Step 2: 增加发起占用接口**

实现：
1. 校验资产存在且为不动产
2. 校验无 `ACTIVE` 占用
3. 插入占用单
4. 回写资产主档
5. 写变更日志

**Step 3: 增加变更占用接口**

实现：
1. 校验当前占用为 `ACTIVE`
2. 关闭旧单
3. 插入新单
4. 回写资产主档
5. 写变更日志

**Step 4: 增加释放占用接口**

实现：
1. 校验当前占用为 `ACTIVE`
2. 更新占用单为 `RELEASED`
3. 回写资产主档为 `IDLE`
4. 清空使用快照
5. 写变更日志

### Task 4: 运行测试并修正到全绿

**Files:**
- Verify: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateServiceImplTest.java`

**Step 1: 跑目标单测**

Run:
`mvn -pl ruoyi-asset -am test "-Dtest=AssetRealEstateServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false"`

Expected:
- PASS

**Step 2: 跑后端编译**

Run:
`mvn -pl ruoyi-admin -am -DskipTests compile`

Expected:
- BUILD SUCCESS

### Task 5: 执行 SQL 并核对本地库

**Files:**
- Verify: `RuoYi-Vue/sql/asset/19-asset-real-estate-occupancy-minimum-closure-upgrade-20260322.sql`

**Step 1: 执行 SQL 到本地库 `ruoyi-assets`**

**Step 2: 校验表与字段存在**

校验点：
1. `ast_asset_real_estate_occupancy_order` 已存在
2. 必填字段完整

### Task 6: 提交第一批占用闭环

**Files:**
- Stage: 本批新增与修改文件

**Step 1: git add 仅添加本批安全文件**

**Step 2: 使用中文提交信息**

```bash
git commit -m "feat: 完成不动产占用管理最小闭环后端第一批"
```
