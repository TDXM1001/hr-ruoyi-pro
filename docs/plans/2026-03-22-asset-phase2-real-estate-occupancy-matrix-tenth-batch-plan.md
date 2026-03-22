# 不动产占用状态矩阵第十批实施计划

## Task 1 落盘设计文档

- 新建第十批设计文档
- 明确三项增强能力边界
- 明确本批仍为纯前端增强

## Task 2 按 TDD 补失败测试

- 在 `asset-real-estate-occupancy-panel.test.ts` 新增失败测试：
  - 自定义预设编辑
  - 自定义预设删除
  - 模板预览
  - 来源链路统计持久化
- 先运行定向测试确认失败

## Task 3 实现占用页签第十批

- 在 `occupancy-panel.vue` 中新增：
  - 自定义预设编辑删除
  - 模板预览区
  - 来源链路统计与持久化

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
- 编辑一个自定义预设并验证字段覆盖
- 删除一个自定义预设并确认界面移除
- 切换批注模板并检查预览区
- 点击跨页签联动并检查统计变化
- 刷新页面后再次检查统计仍存在

## Task 5 提交收口

- 仅提交第十批相关前端文件与文档
- 不带入 disposal/approval 相关脏文件
- 中文提交信息保持“占用状态矩阵第十批体验”口径
