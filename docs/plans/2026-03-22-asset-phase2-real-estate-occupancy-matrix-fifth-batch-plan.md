# 不动产占用状态矩阵第五批实施计划

## Task 1 落盘设计文档

- 新建第五批设计文档
- 明确三项增强能力边界
- 明确不改后端、不改 SQL

## Task 2 按 TDD 补失败测试

- 在 `asset-real-estate-occupancy-panel.test.ts` 新增失败测试：
  - 摘要卡片联动轨迹定位
  - 筛选条件持久化
  - 导出当前筛选轨迹
- 先运行定向测试，确认新增用例失败

## Task 3 实现占用页签第五批

- 在 `occupancy-panel.vue` 中新增：
  - 卡片点击定位逻辑
  - 轨迹聚焦高亮
  - `localStorage` 持久化读写
  - CSV 导出按钮和导出逻辑

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
- 点击摘要卡片验证轨迹定位
- 修改筛选后刷新验证持久化
- 点击导出按钮验证文件下载

