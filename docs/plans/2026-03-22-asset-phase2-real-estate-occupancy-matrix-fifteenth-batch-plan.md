# 不动产占用状态矩阵第十五批实施计划

## Task 1 落盘设计文档
- 新建第十五批设计文档
- 明确治理工具区收敛边界

## Task 2 按 TDD 补失败测试
- 在 `asset-real-estate-occupancy-panel.test.ts` 新增失败测试：
  - 保存并应用导入策略模板
  - 模板持久化恢复
  - 保存多个命名快照并切换恢复
  - 分级重置记录持久化恢复
- 先运行定向测试确认 RED

## Task 3 实现第十五批能力
- 在 `occupancy-panel.vue` 中新增：
  - 治理工具区入口与面板
  - 导入策略模板保存/应用/删除
  - 多快照保存/恢复/删除
  - 重置记录留痕
- 兼容第十三、十四批已有能力

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
- 保存并应用导入策略模板
- 保存两个命名快照并切换恢复
- 执行分级重置并查看记录摘要
- 刷新后核对模板、快照、记录仍可恢复

## Task 5 提交收口
- 仅提交第十五批相关前端文件与文档
- 不带入 disposal/approval 脏文件
- 中文提交信息保持“占用状态矩阵第十五批体验”口径
