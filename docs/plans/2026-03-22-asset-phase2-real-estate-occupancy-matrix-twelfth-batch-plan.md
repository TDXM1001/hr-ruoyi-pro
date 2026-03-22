# 不动产占用状态矩阵第十二批实施计划

## Task 1 落盘设计文档
- 新建第十二批设计文档
- 明确“治理收口型增强”边界
- 明确仍为纯前端增强

## Task 2 按 TDD 补失败测试
- 在 `asset-real-estate-occupancy-panel.test.ts` 新增失败测试：
  - 导入预览清单
  - 同名预设自动改名导入
  - 近 30 天时间窗趋势重算
  - 趋势图点击钻取与取消
  - 重置统计
- 先运行定向测试确认 RED

## Task 3 实现第十二批能力
- 在 `occupancy-panel.vue` 中新增：
  - 导入预览与冲突处理区
  - 趋势图时间窗切换与重置
  - 趋势柱点击钻取
- 兼容现有导出、模板差异、来源链路统计逻辑

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
- 导入同名预设并选择自动改名
- 生成来源链路事件
- 切换时间窗到近 30 天
- 点击趋势柱进入钻取态
- 重置统计并确认清空

## Task 5 提交收口
- 仅提交第十二批相关前端文件与文档
- 不带入 disposal/approval 脏文件
- 中文提交信息保持“占用状态矩阵第十二批体验”口径
