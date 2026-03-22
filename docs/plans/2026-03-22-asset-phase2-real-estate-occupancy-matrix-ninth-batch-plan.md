# 不动产占用状态矩阵第九批实施计划

## Task 1 落盘设计文档

- 新建第九批设计文档
- 明确三项增强能力边界
- 明确本批仍为纯前端增强

## Task 2 按 TDD 补失败测试

- 在 `asset-real-estate-occupancy-panel.test.ts` 新增失败测试：
  - 复制系统预设生成自定义预设
  - 切换批注模板
  - 批注导出跟随模板
- 在 `asset-real-estate-detail-page.test.ts` 新增失败测试：
  - 跨页签联动后展示回跳来源提示
- 先运行定向测试确认失败

## Task 3 实现占用页签第九批

- 在 `occupancy-panel.vue` 中新增：
  - 自定义预设复制新建
  - 批注模板切换
  - 批注导出模板联动
- 在 `tab-state.ts` 中扩展：
  - 回跳来源记忆结构
- 在 `detail/index.vue` 中接入：
  - 来源提示展示
  - 刷新恢复与回跳清理

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
- 复制系统预设为新预设并验证列表更新
- 切换批注模板并验证批注卡片文案变化
- 导出批注并确认导出内容跟随模板
- 从占用联动到巡检/整改后验证来源提示
- 刷新页面后再次验证来源提示存在

## Task 5 提交收口

- 仅提交第九批相关前端文件与文档
- 不带入 disposal/approval 相关脏文件
- 中文提交信息保持“占用状态矩阵第九批体验”口径
