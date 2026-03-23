# 统一处置主流程第四批实施计划

## 1. 实施目标

在不改后端、不加 SQL 的前提下，把统一处置页闭环入口卡继续强化为可直接指导办理的首屏视图。

## 2. 实施步骤

1. 扩展 `DisposalSourceContext`
   - 新增办理视图标题、名称、描述
   - 新增次操作文案与说明
2. 更新 `disposal-entry-card.vue`
   - 增加办理视图区块
   - 增加主/次操作按钮区
3. 更新 `index.vue`
   - 新增次操作处理函数
4. 先写失败测试，再实现
5. 跑处置页和详情页相关回归

## 3. 验证命令

```bash
pnpm vitest run tests/views/asset-disposal-page.test.ts
pnpm vitest run tests/views/asset-disposal-page.test.ts tests/views/asset-real-estate-detail-page.test.ts
```

## 4. 完成标准

1. 两种意图都能看到当前办理视图提示
2. 两种意图都能从入口卡切到另一侧视图
3. 不引入新的业务相关告警
4. 不越过当前前端安全接入边界
