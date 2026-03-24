# 统一处置主流程第七批实施计划

## 1. 实施目标

把办理摘要条继续下沉到搜索区、空状态和刷新反馈口径，同时把中文注释要求写入总检查单，避免后续只能看实现看不出业务意图。

## 2. 实施步骤

1. 更新 `art-design-pro/src/views/asset/disposal/components/disposal-summary-bar.vue`
   - 支持 `showActions=false`
   - 搜索区复用同一组件，但不渲染操作按钮区
2. 更新 `art-design-pro/src/views/asset/disposal/index.vue`
   - 在 `ArtSearchBar` 上方渲染搜索区摘要条
   - 空状态文案跟随当前 `summaryContext.workflowLabel`
   - 刷新反馈按当前 `activeTab` 输出
   - 为关键逻辑补简短中文注释
3. 更新 `art-design-pro/src/views/asset/disposal/disposal-source-context.ts`
   - 没有来源时不展示意图标签，避免通用入口误写成联动场景
4. 更新 `art-design-pro/tests/views/asset-disposal-page.test.ts`
   - 覆盖搜索区摘要条
   - 覆盖空状态文案
   - 覆盖刷新成功提示
5. 更新总检查单
   - 把第七批目标和中文注释约定写进去

## 3. 验证命令

```bash
pnpm vitest run tests/views/asset-disposal-page.test.ts
pnpm vitest run tests/views/asset-disposal-page.test.ts tests/views/asset-real-estate-detail-page.test.ts
```

## 4. 完成标准

- 搜索区和 tab 头部都能看到同一套办理摘要条。
- 空状态文案会跟着当前办理视图变化。
- 刷新成功提示会随 `pool / record` 切换，且按当前 `workflowLabel` 输出。
- 总检查单已经记录中文注释要求。
