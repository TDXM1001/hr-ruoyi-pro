# 不动产占用状态矩阵第七批实施计划

## Task 1 落盘设计文档

- 新建第七批设计文档
- 明确三项增强能力边界
- 明确本批仍为纯前端增强

## Task 2 按 TDD 补失败测试

- 在 `asset-real-estate-occupancy-panel.test.ts` 新增失败测试：
  - 导出模板预设
  - 轨迹批注视图
  - 跨页签联动入口
- 在 `asset-real-estate-detail-page.test.ts` 新增失败测试：
  - 详情壳接收占用页签联动事件后切 Tab
- 先运行定向测试确认失败

## Task 3 实现占用页签第七批

- 在 `occupancy-panel.vue` 中新增：
  - 导出模板预设
  - 批注视图模式
  - 跨页签联动入口
- 在 `detail/index.vue` 中接入 `switch-tab` 事件

## Task 4 验证

### 4.1 定向验证

```bash
pnpm vitest run tests/views/asset-real-estate-occupancy-panel.test.ts
pnpm vitest run tests/views/asset-real-estate-detail-page.test.ts
```

### 4.2 回归验证

```bash
pnpm vitest run tests/views/asset-real-estate-detail-page.test.ts tests/views/asset-real-estate-occupancy-panel.test.ts tests/views/asset-real-estate-inspection-task-page.test.ts tests/views/asset-real-estate-rectification-form-page.test.ts tests/views/asset-real-estate-rectification-complete-page.test.ts tests/views/asset-real-estate-rectification-panel-approval.test.ts tests/api/asset-real-estate.test.ts
```

### 4.3 浏览器点测

- 打开不动产详情页占用页签
- 展开导出字段并点击预设
- 切换到批注视图验证批注卡片
- 点击跨页签联动入口验证总览/巡检/整改切换

## Task 5 提交收口

- 仅提交第七批相关前端文件与文档
- 不带入 disposal/approval 相关脏文件
- 中文提交信息保持“占用状态矩阵第七批体验”口径
