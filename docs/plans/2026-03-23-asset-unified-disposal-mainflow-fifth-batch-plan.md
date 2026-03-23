# 统一处置主流程第五批实施计划

## 1. 实施目标

让统一处置页的闭环入口卡从“来源解释卡”升级为“当前办理视图卡”，在不改后端的前提下保持首屏语义一致。

## 2. 实施步骤

1. 扩展 `disposal-source-context.ts`
   - 新增基于 `activeTab` 的入口卡上下文构建函数
2. 更新 `index.vue`
   - 使用响应式 `entryContext`
   - 让入口卡随当前视图变化
3. 先补失败测试，再做实现
4. 跑处置页和详情页回归

## 3. 验证命令

```bash
pnpm vitest run tests/views/asset-disposal-page.test.ts tests/views/asset-real-estate-detail-page.test.ts
```

## 4. 完成标准

1. `intent=view` 初始落在 `处置记录`
2. 切到 `待处置资产池` 后，入口卡同步更新锁定范围和办理说明
3. `intent=start` 初始落在 `待处置资产池`
4. 切到 `处置记录` 后，入口卡同步更新锁定范围和办理说明
