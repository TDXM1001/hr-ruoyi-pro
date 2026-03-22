# 2026-03-22 不动产占用管理状态矩阵第二批实施计划

## Task 1: 设计文档落盘

- 新建设计文档并明确范围、状态矩阵、原因模板、轨迹筛选口径。

## Task 2: 失败测试

- 更新 `art-design-pro/tests/views/asset-real-estate-occupancy-panel.test.ts`
  - 覆盖状态矩阵展示
  - 覆盖状态筛选与关键字筛选
- 更新 `art-design-pro/tests/views/asset-real-estate-detail-page.test.ts`
  - 覆盖变更原因模板填充
  - 覆盖释放原因模板填充

## Task 3: 前端实现

- 更新 `art-design-pro/src/views/asset/real-estate/detail/components/occupancy-panel.vue`
  - 增加状态矩阵区
  - 增加历史筛选工具条
  - 增加筛选逻辑
- 更新 `art-design-pro/src/views/asset/real-estate/detail/index.vue`
  - 增加原因模板常量
  - 增加模板点击填充逻辑
  - 在 Drawer 中渲染模板按钮组

## Task 4: 验证

- 运行占用相关前端测试
- 运行详情壳相关回归测试
- 浏览器点测状态矩阵、原因模板、轨迹筛选
