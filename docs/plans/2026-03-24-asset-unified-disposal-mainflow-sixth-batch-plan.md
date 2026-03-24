# 统一处置主流程第六批实施计划

## 1. 实施目标

把统一处置页的入口卡和 tab 内表格头部提示收成一套统一的办理摘要条，并修正次操作按当前视图互切的行为。

## 2. 实施步骤

1. 扩展 `disposal-source-context.ts`
   - 新增 `DisposalSummaryBarContext`
   - 新增摘要条上下文构建函数
   - 为无来源场景补齐通用办理文案
2. 新增 `disposal-summary-bar.vue`
   - 统一承接办理视图、下一步建议、主操作、次操作、刷新动作
   - 支持入口卡和 tab 头部两种复用场景
3. 更新 `disposal-entry-card.vue`
   - 用办理摘要条替换原“办理视图 + 操作区”
   - 保留现有来源信息和测试定位点
4. 更新 `index.vue`
   - 在当前激活 tab 的头部接入办理摘要条
   - 统一接入摘要条上下文
   - 修正次操作按当前 `activeTab` 互切
5. 更新 `asset-disposal-page.test.ts`
   - 先补失败测试，覆盖“入口卡与 tab 头部复用同一套摘要条”
   - 覆盖切换后摘要条同步更新与刷新动作切换
6. 跑处置页与不动产详情页相关回归

## 3. 验证命令

```bash
pnpm vitest run tests/views/asset-disposal-page.test.ts
pnpm vitest run tests/views/asset-disposal-page.test.ts tests/views/asset-real-estate-detail-page.test.ts
```

## 4. 完成标准

1. 入口卡和 tab 头部都复用同一套办理摘要条。
2. 当前办理视图、下一步建议和刷新动作会随 `activeTab` 同步更新。
3. 次操作按钮能够真正按当前视图在 `pool / record` 间双向切换。
4. 无来源参数时，不回退已有行为。
5. 处置页和不动产详情页相关测试通过。
