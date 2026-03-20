# 2026-03-18 资产管理系统设计

## 设计结论

本次设计面向一个从 0 开始建设的资产管理系统，复用现有用户体系，其余资产业务能力从统一台账开始建设。系统纳管范围覆盖 `固定资产 + 不动产`，目标不是只建设一个账务登记工具，而是建设一套覆盖两类资产全生命周期的统一管理系统。

推荐方案为：

- 一个统一资产台账底座
- 两条业务主线：固定资产主线、不动产主线
- 一套统一状态框架
- 一套横向财务协同口径
- 一期先覆盖闭环主能力，二期再深化合同、改造、移动盘点和集成

## 系统定位

- 建设目标：建立统一、可信、可追溯的资产台账与生命周期闭环
- 核心角色：资产管理员、部门领用人、财务
- 第一阶段起点：统一资产台账
- 生命周期范围：建账、使用归属、盘点核查、整改维护、财务协同、处置退出

## 业务面

- 统一资产台账
- 资产取得与建账
- 资产归属与使用
- 盘点、巡检与维护
- 价值与财务协同
- 处置与退出

## 业务主线

### 固定资产主线

取得或接收 -> 建账 -> 入库或登记 -> 领用或分配 -> 调拨/借用/退还 -> 盘点 -> 维修/保养 -> 处置/报废/出售

### 不动产主线

取得/购置/接管 -> 建账 -> 权属登记 -> 使用分配/占用 -> 巡检/整改 -> 价值变动协同 -> 处置/转让/注销

## 关键跨业务流程

- 取得结果进入统一台账
- 台账进入使用归属
- 使用结果进入财务协同
- 使用结果进入盘点与巡检
- 盘点与巡检结果回流台账或触发处置
- 处置结果先进入财务协同，再回写台账终态

## 统一状态框架

- 草稿
- 待建账
- 待核查
- 在册
- 使用中
- 闲置中
- 盘点中
- 整改中
- 待处置
- 已处置
- 已注销

## 一期 MVP

- 统一资产台账
- 固定资产建账、领用、调拨、退还、盘点、处置
- 不动产建账、权属基础登记、使用分配、占用管理、巡检或核查、处置
- 财务基础口径维护与导出
- 基础预警和基础统计

## 待确认重点

- 不动产是否一期纳入租赁和合同能力
- 资产处置是否需要完整审批链
- 不动产空间粒度管理到什么层级
- 财务协同的最小字段与对接方式

## 前端体验设计补充

为避免资产模块页面出现“说明区过厚、首屏表格过小、任务页操作空间不足、同类页面骨架不统一”的问题，前端页面设计补充已独立整理为：

- `docs/plans/2026-03-19-asset-frontend-ux-guidelines.md`

后续资产台账、资产使用、资产盘点、资产处置及相关详情/表单页面，均应按该补充文档中的页面分型和骨架规范实现。

## 文档落盘位置

详细设计文档已拆分写入：

- `management-system-blueprinting/2026-03-18-asset-management-system/00-system-goal.md`
- `management-system-blueprinting/2026-03-18-asset-management-system/01-business-surfaces.md`
- `management-system-blueprinting/2026-03-18-asset-management-system/02-cross-business-flows.md`
- `management-system-blueprinting/2026-03-18-asset-management-system/03-business-points.md`
- `management-system-blueprinting/2026-03-18-asset-management-system/04-relationship-matrix.md`
- `management-system-blueprinting/2026-03-18-asset-management-system/05-lifecycle-state.md`
- `management-system-blueprinting/2026-03-18-asset-management-system/06-mvp-scope.md`
- `management-system-blueprinting/2026-03-18-asset-management-system/99-open-questions.md`
