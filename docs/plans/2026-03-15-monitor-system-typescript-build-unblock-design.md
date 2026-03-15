# monitor/system 构建阻塞修复设计

## 背景

当前 `art-design-pro` 执行 `npm run build` 时，会在 `monitor` 与 `system` 相关页面上触发一组 TypeScript 构建错误，导致前端无法形成“全仓构建通过”的基线。结合现有代码和错误日志，这批问题并不是某一个页面的单点失误，而是几类历史写法与当前共享类型、组件约束之间逐步漂移后集中暴露出来的结果。

这批阻塞已经影响到资产模块之外的系统页面交付口径。如果继续只在资产目录内做局部验证，虽然不影响资产功能本身，但会长期保留“项目构建失败”的技术债。因此本轮需要先把 `monitor/system` 的 TypeScript 构建阻塞清掉，再继续后续业务演进。

## 目标

- 修复当前 `npm run build` 中由 `monitor` / `system` 页面引起的 TypeScript 错误。
- 保持页面现有业务行为与接口路径不变，不引入与本轮无关的 UI 重构。
- 收敛共享类型与页面写法之间的漂移，避免同类页面反复出现同样报错。

## 非目标

- 不改造资产模块逻辑。
- 不重做 `monitor` / `system` 页面交互结构和视觉样式。
- 不顺手合并重复页面，不把本轮问题扩展成“系统页全面重构”。

## 方案选择

### 方案 A：仅做页面级补丁

只在 7 个报错页面上做最小改动，哪里报错就在哪里强转或替换。

优点：

- 改动最少，见效最快。

缺点：

- 共享类型仍然失真，比如表格列 `sortable: 'custom'` 实际能工作，但类型定义不允许。
- 未来旧页面继续启用时，还会重复暴露相同问题。

### 方案 B：共享类型校正 + 页面定点修复

先修正已经和真实组件能力不一致的共享类型，再对不符合当前组件约束的旧页面写法做定点收敛。

优点：

- 能一次性解决这批构建错误背后的主要根因。
- 改动范围仍然可控，不会扩大成系统页重构。

缺点：

- 需要同时修改共享类型文件和若干页面，不是单文件补丁。

### 方案 C：顺手做 monitor/system 页面去重重构

把 `monitor/*`、`system/log/*`、`system/operlog` 的重复实现统一抽象或合并。

优点：

- 长期结构更整洁。

缺点：

- 明显超出“解除构建阻塞”的范围。
- 风险和验证成本都更高。

## 推荐方案

采用方案 B。

理由是这批错误已经呈现出明显的“组件契约漂移”特征，如果只做页面级绕过，会继续保留根因。另一方面，这批问题的共享根因又没有大到需要立刻做结构性重构，因此“校正共享类型 + 页面定点修复”是当前性价比最高的做法。

## 根因分组

### 1. 表格列定义与共享类型漂移

`useTable` / `ArtTable` 实际透传给 `ElTableColumn` 的能力比当前 `ColumnOption` 类型更宽，尤其是 `sortable: 'custom'` 这类旧页面常用写法已经被多个系统页使用，但类型定义仍只允许 `boolean`。这会导致登录日志、操作日志等页面虽然运行逻辑正确，却在构建阶段失败。

### 2. 表格操作按钮枚举与旧页面写法漂移

`ArtButtonTable` 当前只声明了 `add`、`edit`、`delete`、`more`、`view` 这几种 `type`，但部分旧页面仍使用 `type: 'info'`。这属于组件对外契约已经收紧，而调用方仍保留旧枚举的问题。

### 3. 组件事件与服务监控类型缺失

`monitor/job` 中 `ElSwitch` 的 `onChange` 参数写成了 `boolean`，但组件签名是 `string | number | boolean`。`monitor/server` 则同时存在接口返回值未显式收窄、ECharts 实例和 DOM ref 混用、模板上数字类型不匹配等问题。这些错误都属于“当前代码依赖运行时隐式推断，但严格类型检查不再接受”。

### 4. 系统操作日志页面引用路径漂移

`system/operlog` 页面引用了仓库里并不存在的 `@/api/system/operlog` 与错误的弹窗模块路径，而实际可用实现已经存在于 `@/api/system/log/operlog` 和 `src/views/system/log/operlog/modules/operlog-detail-dialog.vue`。这是典型的历史移动或复制后未同步引用的问题。

## 实施设计

### 1. 共享类型只修正真实失真点

优先修正 [`src/types/component/index.ts`](/e:/my-project/hr-ruoyi-pro/art-design-pro/src/types/component/index.ts) 中 `ColumnOption` 的 `sortable` 类型，使其与 `ArtTable -> ElTableColumn` 实际透传能力一致，兼容当前项目真实在用的 `'custom'` 写法。

这里不扩大其他类型面，只修复已经被现有页面稳定使用、且可以确认是合法能力的字段。

### 2. 页面层按组件当前约束回收旧写法

对 `ArtButtonTable` 仍使用 `type: 'info'` 的页面，统一切换到当前组件已经支持的 `view` 语义，不扩张按钮枚举。这样可以保持组件公共契约稳定，避免为了兼容少量旧页面而重新放宽组件边界。

### 3. 服务监控页补本地类型，不引入额外抽象

`monitor/server` 只补当前页面使用到的最小响应类型，包括 CPU、内存、JVM、系统信息和磁盘列表。页面内部统一：

- 明确 `serverData` 的结构。
- 分离 DOM `ref` 和 ECharts 实例。
- 用局部工具函数安全更新仪表盘。

这样能把报错修掉，又不会把这轮任务扩展成“监控接口全量建模”。

### 4. 系统日志页优先复用现有实现

`system/operlog` 不重新造 API 或弹窗，而是改为指向仓库中已经存在、且已经被 `system/log/operlog` 使用的实现路径。这样能最大限度降低风险，也符合“修构建阻塞，不做业务重写”的目标。

## 风险与对策

### 风险 1：共享类型放宽过度

如果在 `ColumnOption` 上一次性放宽太多字段，会掩盖真正不该进入公共契约的能力。

对策：

- 只调整已被现有页面稳定使用、且能被 `ElTableColumn` 正确消费的字段。
- 不借这次机会扩大无关类型面。

### 风险 2：页面修复后引入行为变化

尤其是日志页和定时任务页，若把类型修复写成行为改造，可能影响用户操作。

对策：

- 所有改动以“保持现有交互语义”为前提。
- 优先替换类型口径，不改接口路径和业务按钮逻辑。

### 风险 3：构建通过后仍有隐藏问题

局部页面虽然通过类型检查，但若没有重新跑全量构建，就无法确认是否还有残余错误。

对策：

- 以 `npm run build` 为最终完成标准。
- 如有必要，补跑与日志/监控页面相关的局部 lint。

## 验证策略

### 基线验证

先以当前 `npm run build` 的失败输出作为红灯基线，确认错误仍集中在：

- `src/views/monitor/job/index.vue`
- `src/views/monitor/logininfor/index.vue`
- `src/views/monitor/operlog/index.vue`
- `src/views/monitor/server/index.vue`
- `src/views/system/log/logininfor/index.vue`
- `src/views/system/log/operlog/index.vue`
- `src/views/system/operlog/index.vue`

### 过程验证

- 每完成一个根因分组，优先跑对应的类型检查或全量构建，防止多处叠加后难以定位。
- 对共享类型改动，重点复核系统日志页和已有正常页面是否仍能通过。

### 完成验证

以 `art-design-pro` 下执行 `npm run build` 成功作为最终交付标准。

## 预期结果

完成后，`monitor/system` 相关历史 TypeScript 构建阻塞应被清除，前端项目恢复到可全量构建的状态。此次修复同时会把一批共享类型与页面写法的历史漂移收回来，为后续继续推进系统页或业务页改造提供更稳定的基线。
