# 不动产占用状态矩阵第八批实施计划

## Task 1 落盘设计文档

- 新建第八批设计文档
- 明确三项增强能力边界
- 明确本批仍为纯前端增强

## Task 2 按 TDD 补失败测试

- 在 `asset-real-estate-occupancy-panel.test.ts` 新增失败测试：
  - 预设命名自定义
  - 批注导出
- 在 `asset-real-estate-detail-page.test.ts` 新增失败测试：
  - 占用联动后的回跳记忆
- 先运行定向测试确认失败

## Task 3 实现占用页签第八批

- 在 `occupancy-panel.vue` 中新增：
  - 预设命名设置与持久化
  - 批注导出按钮与导出逻辑
- 在 `tab-state.ts` 中新增：
  - 回跳记忆存取工具
- 在 `detail/index.vue` 中接入：
  - 返回占用联动入口
  - 回跳清理逻辑

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
- 修改预设名称并确认显示更新
- 切到批注视图并执行批注导出
- 从占用联动到巡检/整改后验证回跳按钮
- 刷新页面后再次验证回跳记忆存在

## Task 5 提交收口

- 仅提交第八批相关前端文件与文档
- 不带入 disposal/approval 相关脏文件
- 中文提交信息保持“占用状态矩阵第八批体验”口径
