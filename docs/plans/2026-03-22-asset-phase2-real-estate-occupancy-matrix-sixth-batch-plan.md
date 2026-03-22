# 不动产占用状态矩阵第六批实施计划

## Task 1 落盘设计文档

- 新建第六批设计文档
- 明确三项增强能力边界
- 明确本批不改后端、不改 SQL

## Task 2 按 TDD 补失败测试

- 在 `asset-real-estate-occupancy-panel.test.ts` 新增失败测试：
  - 导出字段配置
  - 轨迹分组视图
  - 联动筛选快捷入口
- 先运行定向测试，确认新增用例失败

## Task 3 实现占用页签第六批

- 在 `occupancy-panel.vue` 中新增：
  - 导出字段配置面板
  - 导出字段持久化
  - 轨迹视图模式切换
  - 分组视图渲染
  - 状态矩阵快捷入口

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
- 切换列表/分组视图验证轨迹区展示
- 切换导出字段后执行导出
- 点击状态矩阵快捷入口验证联动筛选

## Task 5 提交收口

- 仅提交第六批相关前端文件与文档
- 不带入 disposal/approval 脏文件
- 中文提交信息保持“占用状态矩阵第六批体验”口径
