# monitor/system 构建阻塞修复 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 修复 `art-design-pro` 中 `monitor/system` 页面导致的 TypeScript 构建阻塞，恢复 `npm run build` 全量通过。

**Architecture:** 先校正共享表格类型与日志页按钮调用口径，再定点修复 `monitor/job` 与 `monitor/server` 的具体类型问题，最后修正 `system/operlog` 的历史引用漂移并做全量构建验证。整个过程只修构建阻塞，不改业务行为，不引入额外重构。

**Tech Stack:** Vue 3、TypeScript、Element Plus、ECharts、Vite、vue-tsc

---

## 实施基线

- 当前分支：`main-assets`
- 当前全量构建失败命令：`cd art-design-pro && npm run build`
- 当前已确认失败文件：
  - `src/views/monitor/job/index.vue`
  - `src/views/monitor/logininfor/index.vue`
  - `src/views/monitor/operlog/index.vue`
  - `src/views/monitor/server/index.vue`
  - `src/views/system/log/logininfor/index.vue`
  - `src/views/system/log/operlog/index.vue`
  - `src/views/system/operlog/index.vue`
- 工作区中存在未跟踪临时文件：`art-design-pro/.tmp-asset-dev.log`
- 执行时不要把 `.tmp-asset-dev.log` 纳入提交

## 批次拆分

- 批次一：收敛共享表格类型与日志页按钮口径
- 批次二：修复 `monitor/job` 与 `monitor/server` 的类型问题
- 批次三：修复 `system/operlog` 引用路径并完成全量构建验证

### Task 1: 收敛共享表格类型与日志页按钮口径

**Files:**

- Modify: `art-design-pro/src/types/component/index.ts`
- Modify: `art-design-pro/src/views/monitor/logininfor/index.vue`
- Modify: `art-design-pro/src/views/monitor/operlog/index.vue`
- Modify: `art-design-pro/src/views/system/log/logininfor/index.vue`
- Modify: `art-design-pro/src/views/system/log/operlog/index.vue`

**Step 1: 运行构建，确认表格列与按钮类型错误仍然存在**

Run: `cd art-design-pro && npm run build`

Expected:

- `src/views/monitor/logininfor/index.vue` 和 `src/views/system/log/logininfor/index.vue` 报 `sortable: 'custom'` 与 `ColumnOption` 不兼容
- `src/views/monitor/operlog/index.vue` 和 `src/views/system/log/operlog/index.vue` 报 `sortable: 'custom'` 不兼容
- 两个操作日志页报 `ArtButtonTable` 的 `type: 'info'` 不符合类型约束

**Step 2: 写最小实现，校正共享类型并回收旧按钮枚举**

在 `art-design-pro/src/types/component/index.ts` 中，把：

```ts
sortable?: boolean
```

改为：

```ts
sortable?: boolean | 'custom'
```

在四个日志页中，把：

```ts
h(ArtButtonTable, {
  type: 'info',
  text: '详情',
  onClick: () => handleView(row)
})
```

统一改为当前组件已支持的查看语义：

```ts
h(ArtButtonTable, {
  type: 'view',
  onClick: () => handleView(row)
})
```

同时保留原有删除按钮与字典渲染逻辑，不改接口路径和业务行为。

**Step 3: 再次运行构建，确认这组错误消失**

Run: `cd art-design-pro && npm run build`

Expected:

- 上述四个日志页不再出现 `sortable` 与 `type: 'info'` 相关报错
- 构建若仍失败，应只剩 `monitor/job`、`monitor/server` 与 `system/operlog` 相关错误

**Step 4: 提交批次一**

```bash
git add art-design-pro/src/types/component/index.ts art-design-pro/src/views/monitor/logininfor/index.vue art-design-pro/src/views/monitor/operlog/index.vue art-design-pro/src/views/system/log/logininfor/index.vue art-design-pro/src/views/system/log/operlog/index.vue
git commit -m "fix: 收敛日志页表格类型与按钮口径"
```

### Task 2: 修复 monitor/job 与 monitor/server 的类型问题

**Files:**

- Modify: `art-design-pro/src/views/monitor/job/index.vue`
- Modify: `art-design-pro/src/views/monitor/server/index.vue`

**Step 1: 运行构建，确认监控页错误仍然可复现**

Run: `cd art-design-pro && npm run build`

Expected:

- `src/views/monitor/job/index.vue` 仍报 `ElSwitch onChange` 参数类型不兼容
- `src/views/monitor/server/index.vue` 仍报 `unknown`、`possibly null`、错误访问 `.value`、以及进度条数值类型不兼容

**Step 2: 写最小实现，修复定时任务页开关回调类型**

在 `art-design-pro/src/views/monitor/job/index.vue` 中，把：

```ts
onChange: (val: boolean) => handleStatusChange(row, val)
```

改成先接收组件真实签名，再在回调内收敛为布尔值：

```ts
onChange: (val: string | number | boolean) => handleStatusChange(row, Boolean(val))
```

保留 `handleStatusChange(row, val: boolean)` 现有业务逻辑不变。

**Step 3: 写最小实现，补齐服务监控页本地类型并修正实例使用方式**

在 `art-design-pro/src/views/monitor/server/index.vue` 中完成以下收敛：

- 为页面使用到的数据结构补充本地类型，如 `ServerCpuInfo`、`ServerMemInfo`、`ServerJvmInfo`、`ServerSysInfo`、`ServerSysFile`、`ServerInfo`。
- 让 `serverData` 使用显式类型，而不是 `ref<any>({})`。
- 给 `getServer()` 返回结果做最小收窄，例如：

```ts
type ServerResponse = { data: ServerInfo }
const res = (await getServer()) as ServerResponse
serverData.value = res.data
```

- 分离 DOM `ref` 与 ECharts 实例：

```ts
const cpuChartRef = ref<HTMLElement | null>(null)
let cpuChart: echarts.ECharts | null = null
```

- 初始化实例时不要写 `cpuChart.value = ...`，统一使用：

```ts
if (cpuChartRef.value) {
  cpuChart = echarts.init(cpuChartRef.value)
}
```

- 更新图表时增加空值保护，不直接对可能为 `null` 的实例调用。
- 进度条百分比改为明确数字，例如：

```ts
:percentage="Number(scope.row.usage)"
```

**Step 4: 再次运行构建，确认监控页错误消失**

Run: `cd art-design-pro && npm run build`

Expected:

- `monitor/job` 与 `monitor/server` 不再报类型错误
- 若构建仍失败，应只剩 `system/operlog` 的引用路径问题

**Step 5: 提交批次二**

```bash
git add art-design-pro/src/views/monitor/job/index.vue art-design-pro/src/views/monitor/server/index.vue
git commit -m "fix: 修复监控页面类型构建错误"
```

### Task 3: 修复 system/operlog 引用路径漂移

**Files:**

- Modify: `art-design-pro/src/views/system/operlog/index.vue`

**Step 1: 运行构建，确认系统操作日志页仍报模块找不到**

Run: `cd art-design-pro && npm run build`

Expected:

- `src/views/system/operlog/index.vue` 报 `@/api/system/operlog` 模块不存在
- `./modules/operlog-detail-dialog.vue` 模块路径不正确
- 页面中的 `ArtButtonTable` 若仍使用 `type: 'info'`，也会一起报错

**Step 2: 写最小实现，改为引用仓库中真实存在的实现**

把：

```ts
import { list, delOperlog, cleanOperlog } from '@/api/system/operlog'
import OperlogDetailDialog from './modules/operlog-detail-dialog.vue'
```

改为：

```ts
import { list, delOperlog, cleanOperlog } from '@/api/system/log/operlog'
import OperlogDetailDialog from '../log/operlog/modules/operlog-detail-dialog.vue'
```

同时把查看按钮统一改为：

```ts
h(ArtButtonTable, { type: 'view', onClick: () => handleView(row) })
```

其余表格与删除逻辑保持不变。

**Step 3: 运行全量构建，确认前端恢复通过**

Run: `cd art-design-pro && npm run build`

Expected: PASS

**Step 4: 如有必要，补跑 monitor/system 相关局部 lint**

Run: `cd art-design-pro && npm run lint -- src/views/monitor src/views/system src/types/component`

Expected: PASS

**Step 5: 提交批次三**

```bash
git add art-design-pro/src/views/system/operlog/index.vue
git commit -m "fix: 修正系统操作日志页面引用路径"
```

### Task 4: 收口验证与最终提交

**Files:**

- Verify only

**Step 1: 复核工作区，确认没有误带临时文件**

Run: `git status --short`

Expected:

- 不包含 `art-design-pro/.tmp-asset-dev.log`
- 只包含本轮构建阻塞修复相关文件，或工作区已干净

**Step 2: 运行最终验证**

Run: `cd art-design-pro && npm run build`

Expected: PASS

Run: `cd art-design-pro && npm run lint -- src/views/monitor src/views/system src/types/component`

Expected: PASS

**Step 3: 如前面未分批提交，则统一提交**

```bash
git add art-design-pro/src/types/component/index.ts art-design-pro/src/views/monitor/job/index.vue art-design-pro/src/views/monitor/logininfor/index.vue art-design-pro/src/views/monitor/operlog/index.vue art-design-pro/src/views/monitor/server/index.vue art-design-pro/src/views/system/log/logininfor/index.vue art-design-pro/src/views/system/log/operlog/index.vue art-design-pro/src/views/system/operlog/index.vue
git commit -m "fix: 清理monitor-system类型构建阻塞"
```

## 完成定义

- `art-design-pro` 下执行 `npm run build` 成功
- `monitor` / `system` 相关页面不再出现当前这批 TypeScript 报错
- 未把 `art-design-pro/.tmp-asset-dev.log` 提交到仓库
- 所有本轮提交信息使用中文
