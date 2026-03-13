# 资产管理系统实施计划（第二期）

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 建立轻量级审批引擎 `ruoyi-workflow` 模块，并完成资产管理核心业务流程（采购、领用、调拨、维修、报废）的后端接口与前端页面。

**Architecture:** 后端新增 `ruoyi-workflow` 独立模块提供基于数据库驱动的轻量级状态机审批能力；`ruoyi-asset` 接入审批引擎管理各业务流水表。前端基于 `art-design-pro` 开发“审批中心”及对应的各项业务台账与表单。

**Tech Stack:** Java, Spring Boot, MyBatis (后端); Vue 3, Element Plus, TypeScript (前端); MySQL

---

### 任务 1：创建审批引擎及业务流水表 SQL 脚本 (✅ 已完成)

**Files:**
- 创建: `RuoYi-Vue/sql/20260312_asset_workflow_business.sql`

**Step 1: 编写 SQL 脚本内容**

```sql
-- 1. 审批引擎核心表
CREATE TABLE `wf_approval_template` (
  `template_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `business_type` varchar(50) NOT NULL COMMENT '业务类型(如: asset_requisition)',
  `chain_config` json DEFAULT NULL COMMENT '审批链配置JSON',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批模板表';

CREATE TABLE `wf_approval_instance` (
  `instance_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '实例ID',
  `business_id` varchar(50) NOT NULL COMMENT '业务单据ID',
  `business_type` varchar(50) NOT NULL COMMENT '业务类型',
  `current_node` varchar(50) DEFAULT NULL COMMENT '当前审批节点',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态: pending/approved/rejected',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批实例表';

CREATE TABLE `wf_approval_node` (
  `node_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `instance_id` bigint(20) NOT NULL COMMENT '实例ID',
  `approver_id` bigint(20) DEFAULT NULL COMMENT '审批人ID',
  `action` varchar(20) DEFAULT NULL COMMENT '操作: approve/reject/transfer',
  `comment` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `process_time` datetime DEFAULT NULL,
  PRIMARY KEY (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流转记录表';

-- 2. 资产业务流水表 (以领用表为例，其他略简)
CREATE TABLE `asset_requisition` (
  `requisition_no` varchar(50) NOT NULL COMMENT '领用单号',
  `asset_no` varchar(50) NOT NULL COMMENT '资产编号',
  `apply_user_id` bigint(20) NOT NULL COMMENT '申请人',
  `apply_dept_id` bigint(20) NOT NULL COMMENT '申请部门',
  `reason` varchar(500) DEFAULT NULL COMMENT '领用原因',
  `status` tinyint(4) DEFAULT '0' COMMENT '单据状态：0=审批中 1=已通过 2=已驳回 3=已归还',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`requisition_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产领用单表';

-- (视实际情况补充 asset_procurement, asset_transfer, asset_maintenance, asset_disposal)
```

**Step 2: 验证 SQL 语法**
在本地环境或 CLI 中模拟验证 DDL 语法。

**Step 3: 提交代码（Commit）**
```bash
git add RuoYi-Vue/sql/20260312_asset_workflow_business.sql
git commit -m "feat: 添加审批引擎及业务流水表 SQL"
```

---

### 任务 2：创建 `ruoyi-workflow` 后端子模块 (✅ 已完成)

**Files:**
- 修改: `RuoYi-Vue/pom.xml`
- 修改: `RuoYi-Vue/ruoyi-admin/pom.xml`
- 创建: `RuoYi-Vue/ruoyi-workflow/pom.xml`

**Step 1: 添加子模块依赖和基础包**
在项目中注册 `ruoyi-workflow`，并在其 `pom.xml` 中引入 `ruoyi-common`。建立 `com.ruoyi.workflow` 包结构。

**Step 2: 验证项目编译**
Run: `cd RuoYi-Vue && mvn clean compile -pl ruoyi-workflow -am`
Expected: `BUILD SUCCESS`

**Step 3: 提交代码（Commit）**
```bash
git add RuoYi-Vue/pom.xml RuoYi-Vue/ruoyi-admin/pom.xml RuoYi-Vue/ruoyi-workflow/pom.xml
git commit -m "feat: 创建轻量级审批引擎 ruoyi-workflow 模块"
```

---

### 任务 3：实现 `ruoyi-workflow` 核心审批逻辑 (✅ 已完成)

**Files:**
- 创建: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/domain/WfApprovalInstance.java` 等实体类
- 创建: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/service/IApprovalEngine.java`
- 创建: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/service/impl/SimpleApprovalEngineImpl.java`

**Step 1: 编写轻量审批服务**
实现 `IApprovalEngine` 的 `startProcess` (发起审批), `approve` (同意), `reject` (驳回), `getTasks` (获取待办) 等方法。为未来切换 Flowable 预留抽象接口。

**Step 2: 执行模块编译**
Run: `cd RuoYi-Vue && mvn clean compile -pl ruoyi-workflow`
Expected: `BUILD SUCCESS`

**Step 3: 提交代码（Commit）**
```bash
git add RuoYi-Vue/ruoyi-workflow/
git commit -m "feat: 实现轻量级基于状态机的通用审批流程服务"
```

---

### 任务 4：在 `ruoyi-asset` 实现领用与归还业务 API (✅ 已完成)

**Files:**
- 创建: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetRequisition.java`
- 创建: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRequisitionServiceImpl.java`
- 创建: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRequisitionController.java`

**Step 1: 编写领用业务接口**
实现资产的领用申请功能。调用 `IApprovalEngine.startProcess` 发起审批。实现申请后的资产状态锁定（变为“领用中”）。

**Step 2: 编译测试**
Run: `cd RuoYi-Vue && mvn clean package -DskipTests=true`
Expected: `BUILD SUCCESS`

**Step 3: 提交代码（Commit）**
```bash
git add RuoYi-Vue/ruoyi-asset/ RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRequisitionController.java
git commit -m "feat: 实现资产领用与归还业务后台 API 并接入审批引擎"
```

---

### 任务 5：在 `ruoyi-asset` 实现维修与报废等流转业务 (简化批量版) (✅ 已完成)

**Files:**
- 创建: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/*` (维修保养、报废实体)
- 创建: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/*` (业务逻辑)
- 创建: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/*` (Controller)

**Step 1: 编写相关 API 逻辑**
提供维修申请、报废申请接口，逻辑同“领用”：创建业务单据 -> 触发流程引擎 -> 修改资产状态（维修中/盘点中/已报废）。

**Step 2: 编译测试**
Run: `cd RuoYi-Vue && mvn clean package -DskipTests=true`
Expected: `BUILD SUCCESS`

**Step 3: 提交代码（Commit）**
```bash
git add RuoYi-Vue/ruoyi-asset/ RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/
git commit -m "feat: 实现资产维修与报废处置的后台流转 API"
```

---

### 任务 6：配置前端审批流与业务菜单 SQL 脚本(✅ 已完成)

**Files:**
- 创建: `RuoYi-Vue/sql/20260312_asset_workflow_menu.sql`

**Step 1: 编写前端路由数据 SQL**
注入“审批中心(待办/已办)”、“领用归还”、“维保管理”、“报废处置”的系统菜单。
注入相关字典数据（如 `wf_status` / `business_type`）。

**Step 2: 提交代码（Commit）**
```bash
git add RuoYi-Vue/sql/20260312_asset_workflow_menu.sql
git commit -m "feat: 补充第二期审批及业务节点相关菜单 SQL"
```

---

### 任务 7：开发前端审批中心页面(✅ 已完成)

**Files:**
- 创建: `art-design-pro/src/api/workflow/task.ts`
- 创建: `art-design-pro/src/views/asset/workflow/todo/index.vue`
- 创建: `art-design-pro/src/views/asset/workflow/done/index.vue`

**Step 1: 编写审批中心界面**
使用 `ArtTable` 渲染统一的待办接口。提供弹窗：展示业务详情并进行“同意/驳回”操作。

**Step 2: 前端校验**
Run: `cd art-design-pro && npm run lint`

**Step 3: 提交代码（Commit）**
```bash
git add art-design-pro/src/api/workflow/ art-design-pro/src/views/asset/workflow/
git commit -m "feat: 完成前端统一审批中心页面（待办处理）"
```

---

### 任务 8：开发前端核心业务台账页面设计(✅ 已完成)

**Files:**
- 创建: `art-design-pro/src/api/asset/requisition.ts`
- 创建: `art-design-pro/src/views/asset/requisition/index.vue` 
- 修改: `art-design-pro/src/views/asset/list/index.vue` (增加操作列：领用/维修发起按钮)

**Step 1: 编写领用台账与申请表单**
实现在原资产台账点选资产发起领用/维修等流程；建立领用记录的展示列表界面。

**Step 2: 前端构建通过**
Run: `cd art-design-pro && npm run type-check` (或略过直接 run build)

**Step 3: 提交代码（Commit）**
```bash
git add art-design-pro/src/api/asset/ art-design-pro/src/views/asset/
git commit -m "feat: 完成资产相关核心业务（领用/维修）的前端界面与流转对接"
```
