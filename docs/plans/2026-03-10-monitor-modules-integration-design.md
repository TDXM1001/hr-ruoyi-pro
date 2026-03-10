# 监控模块集成设计方案 (Monitor Modules Integration Design)

本项目旨在基于 `art-design-pro` 前端框架，深度集成 `RuoYi-Vue` 后端的监控系列模块。通过引入数据可视化、标准化表格和高效的交互逻辑，提升系统运维的直观性和便利性。

## 1. 总体目标
- **增强视觉体验**：利用 ECharts 和大屏看板风格升级服务器及缓存监控。
- **保持操作一致性**：管理类模块遵循项目现有的 `ArtTable` 和 `ArtSearchBar` 规范。
- **提升性能**：采用异步渐进式加载方案，确保复杂监控页面的流畅度。

## 2. 目录结构
```text
src/
├── api/monitor/               # API 接口
│   ├── online.ts              # 在线用户
│   ├── job.ts                 # 定时任务
│   ├── server.ts              # 服务监控
│   └── cache.ts               # 缓存监控 & 列表
├── views/monitor/             # 页面组件
│   ├── online/                # 在线用户 (ArtTable)
│   ├── job/                   # 定时任务 (ArtTable + Switch)
│   ├── druid/                 # 数据监控 (Iframe 嵌入)
│   ├── server/                # 服务监控 (ECharts 看板)
│   └── cache/                 # 缓存监控
│       ├── index.vue          # 监控仪表盘 (ECharts)
│       └── list.vue           # 缓存列表 (三栏联动)
```

## 3. 页面详细规格

### 3.1 在线用户 (monitor/online)
- **展示**：用户名、IP、地点、浏览器、操作系统、登录时间。
- **核心操作**：一键“强退” (Force Logout)。
- **技术点**：基于实时 Session 管理，支持批量处理。

### 3.2 定时任务 (monitor/job)
- **展示**：任务名称、表达式、状态、执行记录。
- **功能**：增删改查、状态快捷切换、立即执行一次、查看详细日志。
- **交互**：任务日志通过抽屉 (Drawer) 或弹窗 (Dialog) 展示历史执行详情。

### 3.3 数据监控 (monitor/druid)
- **实现方式**：Iframe 全屏嵌入。
- **地址**：后端基地址 + `/druid/index.html`。
- **优化**：处理高度自适应和加载状态占位。

### 3.4 服务监控 (monitor/server)
- **风格**：数据大屏 / 科技感看板。
- **组件**：
    - **CPU/内存/JVM/负载**：ECharts 仪表盘 (Gauge)。
    - **磁盘占用**：百分比环形图或带状态色彩的进度条列表。
    - **系统信息**：排版整洁的文本参数墙。
- **机制**：进入页面触发异步并加载，支持手动刷新按钮。

### 3.5 缓存监控 & 列表 (monitor/cache)
- **监控视图 (Index)**：
    - 命令统计：饼图 (Pie Chart)。
    - 内存趋势：面积折线图 (Area Chart)。
- **管理列表 (List)**：
    - **三栏布局**：
        1. 左栏：缓存名称类别。
        2. 中栏：具体 Key 列表（支持搜索）。
        3. 右栏：键值详情展示（JSON 代码高亮）。
    - **清理操作**：支持按 Key 删除、按组清理、全量清理。

## 4. 技术栈
- **核心框架**：Vue 3 + TypeScript
- **组件库**：Element-Plus + 项目核心 UI 组件 (`ArtTable`, `ArtSearchBar`)
- **图表库**：ECharts 5.x
- **动画/样式**：Tailwind CSS + SCSS

## 5. 开发建议
- **性能监控**：由于涉及到定时刷新（如 Server 监控），需在页面销毁时正确清除定时器。
- **字典管理**：充分利用 `useDict` Hook 处理任务状态、任务分组等选项。
- **错误处理**：对后端监控 API 的偶发性超时或权限不足进行优雅降级（展示 Empty 状态而非崩溃）。
