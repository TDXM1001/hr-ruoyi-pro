# 不动产占用状态矩阵第十三批实施计划

## Task 1 落盘设计文档
- 新建第十三批设计文档
- 明确纯前端治理增强边界

## Task 2 按 TDD 补失败测试
- 在 `asset-real-estate-occupancy-panel.test.ts` 新增失败测试：
  - 导入校验报告汇总
  - 单条冲突覆盖全局策略
  - 趋势钻取联动轨迹筛选
  - 重置统计确认弹层
- 先运行定向测试确认 RED

## Task 3 实现第十三批能力
- 在 `occupancy-panel.vue` 中新增：
  - 导入校验报告与单条冲突策略
  - 趋势钻取联动轨迹区
  - 重置统计确认
- 兼容现有第十二批逻辑

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
- 导入包含无效项和冲突项的 JSON
- 单条处理覆盖全局策略
- 点击趋势柱后联动轨迹区
- 重置统计并确认二次确认生效

## Task 5 提交收口
- 仅提交第十三批相关前端文件与文档
- 不带入 disposal/approval 脏文件
- 中文提交信息保持“占用状态矩阵第十三批体验”口径
