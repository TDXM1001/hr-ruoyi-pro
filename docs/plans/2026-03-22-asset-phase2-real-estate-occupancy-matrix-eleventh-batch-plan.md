# 不动产占用状态矩阵第十一批实施计划

## Task 1 落盘设计文档

- 新建第十一批设计文档
- 明确三项增强边界
- 明确仍为纯前端增强

## Task 2 按 TDD 补失败测试

- 在 `asset-real-estate-occupancy-panel.test.ts` 新增失败测试：
  - 预设导出 JSON
  - 预设导入 JSON
  - 模板差异对比
  - 来源链路趋势图
- 先运行定向测试确认 RED

## Task 3 实现占用页签第十一批

- 在 `occupancy-panel.vue` 中新增：
  - 预设导入导出面板
  - 模板差异对比区
  - 来源链路趋势图
- 兼容旧版来源链路统计存储结构

## Task 4 验证

### 4.1 定向验证

```bash
pnpm vitest run tests/views/asset-real-estate-occupancy-panel.test.ts
```

### 4.2 回归验证

```bash
pnpm vitest run tests/views/asset-real-estate-detail-page.test.ts tests/views/asset-real-estate-occupancy-panel.test.ts tests/views/asset-real-estate-inspection-task-page.test.ts tests/views/asset-real-estate-rectification-form-page.test.ts tests/views/asset-real-estate-rectification-complete-page.test.ts tests/views/asset-real-estate-rectification-panel-approval.test.ts tests/api/asset-real-estate.test.ts
```

### 4.3 浏览器点测

- 打开不动产详情页占用页签
- 导出一条自定义预设
- 清空并重新导入
- 切换批注模板并查看差异对比
- 执行跨页签联动并检查最近 7 天趋势条

## Task 5 提交收口

- 仅提交第十一批相关前端文件与文档
- 不带入 disposal/approval 相关脏文件
- 中文提交信息保持“占用状态矩阵第十一批体验”口径
