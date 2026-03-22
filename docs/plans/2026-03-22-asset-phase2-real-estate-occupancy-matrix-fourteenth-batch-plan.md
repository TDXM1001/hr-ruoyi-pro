# 不动产占用状态矩阵第十四批实施计划

## Task 1 落盘设计文档
- 新建第十四批设计文档
- 明确纯前端治理收口边界

## Task 2 按 TDD 补失败测试
- 在 `asset-real-estate-occupancy-panel.test.ts` 新增失败测试：
  - 导出校验报告
  - 最近一次导入处理结果与策略复用
  - 趋势钻取快照保存/恢复/清空
  - 分级重置统计
- 先运行定向测试确认 RED

## Task 3 实现第十四批能力
- 在 `occupancy-panel.vue` 中新增：
  - 导入校验报告导出
  - 最近一次导入处理结果卡片
  - 复用上次逐条冲突策略
  - 趋势钻取筛选快照
  - 分级重置统计
- 兼容现有第十三批逻辑

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
- 导出导入校验报告
- 成功导入后查看最近一次导入处理结果
- 复用上次逐条策略
- 趋势钻取后保存、恢复、清空快照
- 分级重置统计并校验影响范围

## Task 5 提交收口
- 仅提交第十四批相关前端文件与文档
- 不带入 disposal/approval 脏文件
- 中文提交信息保持“占用状态矩阵第十四批体验”口径
