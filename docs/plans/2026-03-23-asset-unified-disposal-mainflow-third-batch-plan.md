# 统一处置主流程第三批实施计划

## 1. 实施目标

在不改后端、不加 SQL 的前提下，把统一处置页的来源视图和首屏操作引导收成一张闭环入口卡。

## 2. 实施步骤

1. 扩展 `DisposalSourceContext`
   - 新增入口卡标题、描述、主操作文案、主操作说明
2. 新增 `disposal-entry-card.vue`
   - 承接来源接入卡与主操作入口
3. 调整 `index.vue`
   - 用入口卡替换原来源上下文卡
   - 新增主操作处理函数
4. 更新页面测试
   - 先红灯验证入口卡缺失
   - 再转绿验证 `intent=view / intent=start`
5. 跑相关回归

## 3. 验证命令

```bash
pnpm vitest run tests/views/asset-disposal-page.test.ts
pnpm vitest run tests/views/asset-disposal-page.test.ts tests/views/asset-real-estate-detail-page.test.ts
```

## 4. 完成标准

1. 两种入口都能显示闭环入口卡
2. 两种主操作都能触发对应 tab 的刷新
3. 第二批来源视图语义不回退
4. 不引入新的 Vue error/warning
