# 系统配置与通知公告模块重构设计文档

## 1. 背景与目标
当前 `art-design-pro` 前端项目的“参数设置”和“通知公告”模块在对接 `RuoYi-Vue` 后端接口时，存在数据字段不匹配、操作逻辑不规范、权限控制失效以及渲染方式不稳定（使用 `render` 属性或不可重用的 `h()` 函数）等问题。

本设计旨在通过“极致规范化”方案，将这两个模块完全对齐若依（RuoYi）后端标准，并提升代码的可维护性。

## 2. 核心架构设计
### 2.1 渲染模式：模板插槽优先 (Slot-based Rendering)
- **改进点**：放弃在 `columnsFactory` 中直接使用 `h()` 函数或不受支持的 `render` 属性。
- **方案**：在 `columnsFactory` 中仅定义基础属性，配置 `useSlot: true`，并在 Vue 模板中声明对应插槽。
- **优点**：代码清晰度提升，组件（如 `DictTag`, `ArtButtonTable`）的传参更加直观。

### 2.2 数据流：先查后改 (Standard RuoYi Interaction)
- **改进点**：目前直接使用列表行数据填充编辑表单，可能导致大字段（如公告内容）丢失或展示数据陈旧。
- **方案**：
    1. 点击“修改”时，根据 ID 调用详情接口 (`getConfig` / `getNotice`)。
    2. 接口返回成功后，再将完整实体对象注入弹窗并打开。
- **API 对接**：
    - 参数设置：`GET /system/config/{configId}`
    - 通知公告：`GET /system/notice/{noticeId}`

### 2.3 权限控制：显式逻辑判断 (Manual Auth Logic)
- **方案**：统一在页面逻辑层使用 `hasAuth` 宏进行权限预判。
- **实现**：
    - 表格操作列按钮只有在通过权限检查后才会被渲染。
    - 针对 `ArtButtonTable` 组件，不再依赖其内部（尚不存在的）`auth` 属性。

## 3. 详细设计
### 3.1 参数设置 (SysConfig)
- **数据对齐**：
    - 搜索及提交字段：`configName`, `configKey`, `configType` (Y/N)。
    - 主键字段：`configId`。
- **渲染项**：
    - `configType`：使用 `sys_yes_no` 字典标签。
- **缓存管理**：保留“刷新缓存”功能，调用 `DELETE /system/config/refreshCache`。

### 3.2 通知公告 (SysNotice)
- **数据对齐**：
    - 搜索及提交字段：`noticeTitle`, `noticeType` (1/2), `status` (0/1), `noticeContent` (富文本)。
    - 主键字段：`noticeId`。
- **富文本集成**：确保 `ArtWangEditor` 获取的内容能够被正确识别并发送。
- **渲染项**：
    - `noticeType`：使用 `sys_notice_type` 字典。
    - `status`：使用 `sys_notice_status` 字典。

## 4. 提交规范
在提交数据至 API 之前，前端将进行一次“字段清洗”：
- **过滤冗余**：移除 `createTime`, `updateTime`, `params` 等由后端自动维护的审计字段。
- **格式校验**：确保 ID 在修改模式下存在，在新增模式下为 `undefined`。

## 5. 任务列表
1.  **API 增强**：确认 `src/api/system/` 下的接口方法支持 `get` 详情操作。
2.  **Config 页面重构**：
    - 改造 `handleUpdate` 为异步模式。
    - 列配置改为插槽模式，重写状态标签和操作按钮。
3.  **Notice 页面重构**：
    - 修复 `render` 属性无效问题。
    - 集成详情拉取逻辑。
    - 统一权限判断样式。
4.  **EditDialog 优化**：调整弹窗内的初始化逻辑，适配最新的详情数据注入方式。

## 6. 验收标准
- [ ] 列表分页、搜索功能完全匹配后端字段。
- [ ] “新增”功能表单校验通过且入库正确。
- [ ] “修改”功能先拉取详情再展示，修改后列表数据同步刷新。
- [ ] “删除”功能正常，且根据用户权限显示/隐藏对应按钮。
