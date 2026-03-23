# 统一处置主流程前两批接入检查单

## 1. 当前阶段定位

这份检查单只覆盖统一处置主流程的前两批前端安全接入工作，不包含处置后端主流程改造。

当前目标已经完成两件事：

1. 接住不动产详情壳传来的来源上下文
2. 把统一处置页首屏来源视图收成一套稳定入口

一句话判断：

> 统一处置页已经能安全承接不动产来源，并区分“查看进展”和“发起处置”两种入口。

## 2. 第一批完成项

### 2.1 已完成

1. 统一处置页能识别 `source=real-estate-disposal-tab`
2. 能识别 `intent=view` 与 `intent=start`
3. 能承接：
   - `assetId`
   - `assetCode`
   - `assetName`
4. 首屏可展示：
   - 来源横幅
   - 资产上下文卡
   - 返回不动产详情入口
5. 两种入口都能落到正确首屏：
   - `intent=view -> tab=record`
   - `intent=start -> tab=pool`

### 2.2 已验证

1. 自动化测试通过
2. 真实浏览器点测通过
3. 控制台没有新增业务相关 Vue 警告

## 3. 第二批完成项

### 3.1 已完成

1. 把来源横幅、资产上下文卡、筛选说明收成更统一的来源视图
2. 来源接入卡新增：
   - `当前锁定范围`
   - `下一步建议`
3. 有来源参数时，tab 内重复来源提示已收回
4. 无来源参数时，统一处置页保持原有行为

### 3.2 已验证

1. `intent=view` 首屏显示统一来源视图，且落在 `处置记录`
2. `intent=start` 首屏显示统一来源视图，且落在 `待处置资产池`
3. 两种入口都已做真实浏览器点测
4. 没有新增业务相关控制台错误或警告

## 4. 当前页面口径

### 4.1 intent=view

首屏应该看到：

1. 来源横幅：来自不动产档案处置联动
2. 当前意图：查看进展
3. 当前锁定范围：处置记录
4. 下一步建议：先核对该资产的处置记录、审批进展和责任归口
5. 返回不动产详情入口

### 4.2 intent=start

首屏应该看到：

1. 来源横幅：来自不动产档案处置联动
2. 当前意图：发起处置
3. 当前锁定范围：待处置资产池
4. 下一步建议：确认该资产是否出现在待处置资产池，再继续办理
5. 返回不动产详情入口

## 5. 当前可动文件

前两批安全接入层只建议继续动这些文件：

1. `art-design-pro/src/views/asset/disposal/index.vue`
2. `art-design-pro/src/views/asset/disposal/disposal-source-context.ts`
3. `art-design-pro/tests/views/asset-disposal-page.test.ts`
4. 后续如果只做来源入口组件化，可以新增 `art-design-pro/src/views/asset/disposal/components/*`

## 6. 当前禁动文件

继续保持不动：

1. `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetDisposalController.java`
2. `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetDisposalServiceImpl.java`
3. `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetDisposalMapper.xml`
4. `RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml`
5. 所有 SQL

## 7. 自动化验证命令

### 7.1 页面定向测试

```bash
pnpm vitest run tests/views/asset-disposal-page.test.ts
```

### 7.2 相关回归

```bash
pnpm vitest run tests/views/asset-disposal-page.test.ts tests/views/asset-real-estate-detail-page.test.ts
```

## 8. 浏览器点测路径

### 8.1 查看进展入口

```text
#/asset/disposal?tab=record&assetId=20002&assetCode=RE-2026-0002&assetName=深圳测试不动产B座&source=real-estate-disposal-tab&intent=view
```

### 8.2 发起处置入口

```text
#/asset/disposal?tab=pool&assetId=20001&assetCode=RE-2026-0001&assetName=深圳研发办公楼A座&source=real-estate-disposal-tab&intent=start
```

## 9. 当前遗留项

这两批已经收住，但还有两类非阻塞遗留项：

1. 项目里仍存在 3 条无关组件缺失报错：
   - `/tool/build/index`
   - `/tool/gen/index`
   - `/tool/swagger/index`
2. 统一处置后端主流程还没有进入安全实现阶段

## 10. 下一步建议

建议继续按这个顺序推进：

1. 统一处置主流程第三批：把来源视图和首屏操作引导收成一张更完整的闭环入口卡
2. 仍然只动前端安全接入层
3. 等入口层稳定后，再考虑处置主流程后端改造
