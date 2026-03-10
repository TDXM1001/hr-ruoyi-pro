# 监控模块集成实现计划 (Monitor Modules Implementation Plan)

本计划旨在分阶段实现 `2026-03-10-monitor-modules-integration-design.md` 中定义的监控模块。

## 状态说明
- ⏳ 待处理 (Pending)
- 🚧 进行中 (In Progress)
- ✅ 已完成 (Completed)

## 任务列表

### 第一阶段：API 基础建设与在线用户/Druid 基础页面 (Batch 1)
- [x] **任务 1: 创建监控模块所有 API 接口** ✅
    - `src/api/monitor/online.ts`: 在线用户接口（列表、强退）
    - `src/api/monitor/job.ts`: 定时任务接口（增删改查、状态切换、立即执行）
    - `src/api/monitor/server.ts`: 服务监控接口（获取监控数据）
    - `src/api/monitor/cache.ts`: 缓存监控接口（监控信息、清理缓存）
- [x] **任务 2: 实现在线用户页面 (monitor/online)** ✅
    - 路径: `src/views/monitor/online/index.vue`
    - 使用 `ArtTable` 和 `ArtSearchBar`
    - 功能：用户搜索、分页列表、单条/批量强退
- [x] **任务 3: 实现 Druid 数据监控页面 (monitor/druid)** ✅
    - 路径: `src/views/monitor/druid/index.vue`
    - 功能：Iframe 全屏嵌入后端 Druid 监控页面
    - 优化：处理加载占位和高度自适应

### 第二阶段：服务监控与缓存监控大屏 (Batch 2)
- [x] **任务 4: 实现服务监控页面 (monitor/server)** ✅
    - 路径: `src/views/monitor/server/index.vue`
    - 使用 ECharts 仪表盘展示 CPU、内存、JVM 状态
    - 展示磁盘占用及服务器系统信息
- [x] **任务 5: 实现缓存监控仪表盘 (monitor/cache/index)** ✅
    - 路径: `src/views/monitor/cache/index.vue`
    - ECharts 饼图：命令统计
    - ECharts 面积图：内存趋势

### 第三阶段：定时任务管理与缓存列表联动 (Batch 3)
- [x] **任务 6: 实现定时任务管理页面 (monitor/job)** ✅
    - 路径: `src/views/monitor/job/index.vue`
    - 功能：CRUD、Cron 表达式、状态切换
    - 实现：任务详情弹窗、执行日志抽屉
- [x] **任务 7: 实现缓存列表管理页面 (monitor/cache/list)** ✅
    - 路径: `src/views/monitor/cache/list.vue`
    - 布局：左栏（缓存名）、中栏（Key 列表）、右栏（文本/JSON 详情）
    - 功能：精准清理、按组清理、全量清理

## 验证项
- [ ] 所有 API 请求均走 `Request` 工具类，符合 RuoYi 响应格式。
- [ ] 页面操作（搜索、刷新、删除）逻辑正确。
- [ ] ECharts 图表自适应窗口缩放。
- [ ] 代码包含中文注释，Git 提交使用中文。
