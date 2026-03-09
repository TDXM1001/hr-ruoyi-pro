# 设计方案：日志管理、通知公告、参数设置功能实现

## 1. 项目背景
在 `art-design-pro` 前端项目中实现与 `RuoYi-Vue` 后端对接的三个核心管理模块：通知公告、参数设置、日志管理（操作日志与登录日志）。确保 UI 风格与现有系统保持一致，并充分利用项目已有的核心组件。

## 2. 核心设计原则
- **一致性**：使用 `ArtTable`、`ArtSearchBar`、`ArtTableHeader` 和 `ArtButtonTable` 核心组件。
- **标准化**：严格遵循若依（RuoYi）后端的 API 协议、数据结构和权限标识。
- **现代化**：采用 Vue 3 Composition API 和 `useTable` 核心 Hook。
- **解耦性**：API 定义与视图层分离，弹窗组件独立化。

## 3. 目录与路径规划

### 3.1 前端视图 (Views)
| 功能模块 | 目录路径 | 权限标识前缀 |
| :--- | :--- | :--- |
| **通知公告** | `src/views/system/notice` | `system:notice` |
| **参数设置** | `src/views/system/config` | `system:config` |
| **操作日志** | `src/views/monitor/operlog` | `monitor:operlog` |
| **登录日志** | `src/views/monitor/logininfor` | `monitor:logininfor` |

### 3.2 API 定义 (API)
| 功能模块 | 接口文件路径 | 后端 Base URL |
| :--- | :--- | :--- |
| **通知公告** | `src/api/system/notice.ts` | `/system/notice` |
| **参数设置** | `src/api/system/config.ts` | `/system/config` |
| **操作日志** | `src/api/monitor/operlog.ts` | `/monitor/operlog` |
| **登录日志** | `src/api/monitor/logininfor.ts` | `/monitor/logininfor` |

## 4. 详细模块设计

### 4.1 通知公告 (Notice)
- **列表显示**：公告标题、类型（`sys_notice_type`）、状态（`sys_notice_status`）、公告内容（省略）、创建者、创建时间。
- **核心组件**：
  - `ArtSearchBar`: 后端支持按标题、操作人员、类型搜索。
  - `ArtWangEditor`: 用于编辑公告内容，支持 HTML 格式。
- **操作**：新增、编辑、删除、批量删除。

### 4.2 参数设置 (Config)
- **列表显示**：参数名称、键名、键值、系统内置（`sys_yes_no`）、备注、创建时间。
- **核心组件**：使用 `useTable` 自动处理分页。
- **操作**：新增、编辑、删除、导出、刷新缓存。

### 4.3 操作日志 (OperLog) - 只读
- **列表显示**：系统模块、操作类型（`sys_oper_type`）、操作人员、操作地点、操作状态（`sys_common_status`）、操作时间、耗时。
- **核心组件**：使用 `ArtTable` 的 `expand` 属性或独立弹窗展示详情。
- **详情弹窗**：展示请求方法、请求地址、请求参数、返回参数、异常内容。
- **操作**：批量删除、清空、导出。

### 4.4 登录日志 (LoginInfor) - 只读
- **列表显示**：登录账号、登录地址、登录地点、浏览器、操作系统、登录状态（`sys_common_status`）、提示消息、访问时间。
- **操作**：批量删除、清空、导出、解锁用户（对应 API `/monitor/logininfor/unlock/{userName}`）。

## 5. 技术实现要点

### 5.1 数据流管理
- 统一使用 `useTable` Hook：
  ```ts
  const { data, loading, pagination, searchParams, refreshData } = useTable({
    core: {
      apiFn: listNotice,
      columnsFactory: () => [ ... ]
    }
  })
  ```
- 字典接入：统一使用 `useDict` 辅助函数。

### 5.2 UI 细节
- 状态展示：使用 `DictTag` 组件确保颜色与后端逻辑一致。
- 按钮权限：使用 `v-auth` 指令严格控制按钮显示。
- 表格按钮：使用 `ArtButtonTable` 定义常见的 `edit`, `delete`, `info` 类型。

## 6. 后续计划
1. 定义 API 接口。
2. 创建页面骨架与搜索栏。
3. 实现表格展示与分页。
4. 开发新增/编辑/详情弹窗（包含富文本集成）。
5. 验证与测试。
