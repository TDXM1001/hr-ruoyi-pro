# HR System 第一期核心基建 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 基于若依前端 (art-design-pro) 和后端构建大型企业 HR 系统第一期核心基建，包括独立的 HR 组织架构、员工档案体系以及基于 Flowable 的精简版可视化审批流程（钉钉风格）。

**Architecture:** 
- 后端：在若依框架基础上扩展独立的 `ruoyi-hr`（HR业务模块）和 `ruoyi-workflow`（流程引擎模块）Maven 子模块。HR 业务数据表（如 `hr_employee`, `hr_organization`）与若依系统表（如 `sys_user`）松耦合关联。Flowable 流程引擎采用嵌入式部署。
- 前端：在 `art-design-pro` 项目中增加 HR 模块相关页面及流程设计器（支持发起人、审批人、条件分支等基础节点，并通过 JSON 与后端的 BPMN 格式互转）。

**Tech Stack:**
- 前端：Vue 3 + Vite + Element Plus + Tailwind CSS + Pinia + Vue Router
- 后端：Spring Boot + MyBatis + MySQL + Flowable (BPMN 2.0)

---

### Task 1: 建立独立的数据模型基础 (SQL)

**Files:**
- Create: `e:\my-project\hr-ruoyi-pro\RuoYi-Vue\sql\hr_core_init.sql`

**Step 1: 编写第一期核心数据表 SQL**
在这个初始文件中准备表结构。表结构来自设计文档 `hr-design/02-数据模型.md`。包含 `hr_organization`, `hr_org_change`, `hr_position_category`, `hr_position_level`, `hr_position`, `hr_employee`, `hr_employee_education`, `hr_employee_work_history`, `hr_employee_contact`, `hr_contract`, `hr_employee_change`。

```sql
-- e:\my-project\hr-ruoyi-pro\RuoYi-Vue\sql\hr_core_init.sql
-- 组织架构表
CREATE TABLE `hr_organization` (
  `org_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '组织ID',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '上级组织ID',
  `ancestors` varchar(500) DEFAULT '' COMMENT '祖级列表',
  `org_name` varchar(100) DEFAULT '' COMMENT '组织名称',
  `org_code` varchar(50) DEFAULT NULL COMMENT '组织编码',
  `org_type` char(1) DEFAULT NULL COMMENT '组织类型（1集团 2公司 3事业部 4中心 5部门 6团队）',
  `org_level` int(4) DEFAULT NULL COMMENT '组织层级',
  `leader_id` bigint(20) DEFAULT NULL COMMENT '负责人ID',
  `phone` varchar(20) DEFAULT '' COMMENT '联系电话',
  `address` varchar(200) DEFAULT '' COMMENT '办公地址',
  `establish_date` date DEFAULT NULL COMMENT '成立日期',
  `order_num` int(4) DEFAULT '0' COMMENT '排序号',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `sys_dept_id` bigint(20) DEFAULT NULL COMMENT '关联若依 sys_dept_id',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`org_id`),
  UNIQUE KEY `uk_org_code` (`org_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织架构表';

-- 此处省略后续表...详细参考 hr-design/02-数据模型.md 需要完整补齐
```

**Step 2: 执行测试**
不进行独立测试步，但开发在本地数据库运行此 SQL 确认无语法错误且表生成成功。

**Step 3: Commit**
```bash
git add RuoYi-Vue/sql/hr_core_init.sql
git commit -m "feat: 新增 hr 系统第一期核心业务表 sql 脚本"
```

### Task 2: 配置并在后端创建 Maven 模块

**Files:**
- Modify: `e:\my-project\hr-ruoyi-pro\RuoYi-Vue\pom.xml`
- Create: `e:\my-project\hr-ruoyi-pro\RuoYi-Vue\ruoyi-hr\pom.xml`
- Create: `e:\my-project\hr-ruoyi-pro\RuoYi-Vue\ruoyi-workflow\pom.xml`

**Step 1: 在根目录下维护 pom 增加依赖和子模块**
更新总 pom.xml，`<modules>` 内追加 `<module>ruoyi-hr</module>` 和 `<module>ruoyi-workflow</module>`。并在 `dependencyManagement` 中管理版本。

**Step 2: 初始化 ruoyi-hr 子模块配置**
创建 `ruoyi-hr/pom.xml`，依赖 `ruoyi-common` 及 `ruoyi-system`。

**Step 3: 初始化 ruoyi-workflow 子模块配置**
创建 `ruoyi-workflow/pom.xml`，依赖 Flowable-spring-boot-starter (版本推荐选择匹配现有 Spring Boot 架构的版本，如 6.8.0 等)、JSON 转换相关依赖及 `ruoyi-hr`（按需或让 controller 层去组装）。

**Step 4: 测试编译通过**
Run: `mvn clean package -DskipTests` (或类似的构建指令)
Expected: BUILD SUCCESS

**Step 5: Commit**
```bash
git add RuoYi-Vue/pom.xml RuoYi-Vue/ruoyi-hr/pom.xml RuoYi-Vue/ruoyi-workflow/pom.xml
git commit -m "build: 增加 ruoyi-hr 与 ruoyi-workflow 独立模块"
```

### Task 3: 员工档案后端接口核心功能开发

**Files:**
- Create: `e:\my-project\hr-ruoyi-pro\RuoYi-Vue\ruoyi-hr\src\main\java\com\ruoyi\hr\domain\HrEmployee.java`
- Create: `e:\my-project\hr-ruoyi-pro\RuoYi-Vue\ruoyi-hr\src\main\java\com\ruoyi\hr\mapper\HrEmployeeMapper.java`
- Create: `e:\my-project\hr-ruoyi-pro\RuoYi-Vue\ruoyi-hr\src\main\resources\mapper\hr\HrEmployeeMapper.xml`
- Create: `e:\my-project\hr-ruoyi-pro\RuoYi-Vue\ruoyi-hr\src\main\java\com\ruoyi\hr\service\IHrEmployeeService.java`
- Create: `e:\my-project\hr-ruoyi-pro\RuoYi-Vue\ruoyi-hr\src\main\java\com\ruoyi\hr\service\impl\HrEmployeeServiceImpl.java`
- Create: `e:\my-project\hr-ruoyi-pro\RuoYi-Vue\ruoyi-admin\src\main\java\com\ruoyi\web\controller\hr\HrEmployeeController.java`

**Step 1: 编写基础业务框架**
实现依据 `hr_employee` 表的基础 CRUD 操作。
- `HrEmployeeServiceImpl` 中，员工新增/入职的逻辑必须包含在 `sys_user` 表的联动插入，也就是桥接机制的落实，这里需要引入 `ISysUserService` 进行处理，确保事务 (`@Transactional`)。

**Step 2: 编写查询和测试**
Run: 编译运行后端项目
Expected: 没有编译问题或启动闪退，可以在 Swagger 中看到对应 HR 模块的新接口。

**Step 3: Commit**
```bash
git add .
git commit -m "feat(hr): 提供员工档案基础 CRUD 及用户表桥接联动接口"
```

### Task 4: 前端路由体系与页面框架构建

**Files:**
- Modify: (若存在菜单初始化脚本，也可以直接提供 sql 文件，否则就在现有菜单配置添加) `e:\my-project\hr-ruoyi-pro\RuoYi-Vue\sql\hr_menu_init.sql`
- Create: `e:\my-project\hr-ruoyi-pro\art-design-pro\src\api\hr\employee.ts`
- Create: `e:\my-project\hr-ruoyi-pro\art-design-pro\src\views\hr\employee\index.vue`
- Create: `e:\my-project\hr-ruoyi-pro\art-design-pro\src\views\hr\employee\detail\index.vue`
- Create: `e:\my-project\hr-ruoyi-pro\art-design-pro\src\views\hr\organization\index.vue`

**Step 1: 提供菜单 SQL 及前端 API 封装**
向 `hr_menu_init.sql` 编写 `sys_menu` 表插入语句以映射我们在设计稿中设定的菜单 (人事管理下的组织、岗位、员工分类等)。创建基础 Axios 的前后端连接文件 `employee.ts`。

**Step 2: 开发员工花名册页面**
依靠 Element Plus (如 `el-table` `el-pagination` `el-form`) 构建花名册主体页面骨架结构 (`index.vue`)，并展示模拟或初级的检索及表格列表。

**Step 3: 开发 360 度档案长分页结构**
创建 `detail/index.vue`。基于 `el-tabs` 提供基础页签支持：`[基本信息] [教育经历] [工作经历] ...`。

**Step 4: 测试启动前端**
Run: `npm run dev`
Expected: 页面启动正常，导航正确渲染，能看到花名册布局不报错。

**Step 5: Commit**
```bash
git add art-design-pro/
git commit -m "feat(front): 提供人事管理与员工花名册前端基础骨架"
```

### Task 5: 组织架构可视化配置功能开发

**Files:**
- Create: `e:\my-project\hr-ruoyi-pro\art-design-pro\src\views\hr\organization\components\OrgTree.vue`
- Create: `e:\my-project\hr-ruoyi-pro\art-design-pro\src\views\hr\organization\components\OrgList.vue`

**Step 1: 开发树形与列表切换主容器**
在 `src\views\hr\organization\index.vue` 引入上述两组件实现切换。

**Step 2: 实现组织架构树**
使用 Element Plus 的 `el-tree` 组件，展现集团-中心-部门结构的加载样式及拖拽功能开关预留。

**Step 3: 运行检查并 Commit**
```bash
git commit -am "feat(front): 实现组织架构双视图框架结构与树形组件骨架"
```

### Task 6: 前端钉钉风格可视化流程设计器搭建

**Files:**
- Create: `e:\my-project\hr-ruoyi-pro\art-design-pro\src\views\workflow\process\design\index.vue`
- Create: `e:\my-project\hr-ruoyi-pro\art-design-pro\src\views\workflow\process\design\NodeConfig.vue`
- Create: 安装设计器相关必需的前端依赖和状态库文件等（如果在框架内自行使用纯Vue/G2编写则不需要，此处假设自研组件树并集成在相应子目录下）。

**Step 1: 开发流程树形画布**
实现类似钉钉的自顶向下的节点结构，定义核心数据结构 JSON (例如 `{"type":"start", "childNode":{"type":"approver", ...}}`)。
包含 🔵 蓝色(起始)、🟠 橙色(审批)、🟢 绿色(抄送)、🟡 黄色(分支) 的卡片和 `+` 号按钮。

**Step 2: 实现右侧节点配置抽屉**
在 `NodeConfig.vue` 建立审批人指定策略的表单选项：指定角色，直属上级等项配置以及或签/会签逻辑。

**Step 3: 测试操作体验**
Run: `npm run dev` 调起网页检查在 `/workflow/process/design` 下的显示并能在节点间点击操作右侧抽屉弹开。
Expected: 树形数据正常增减变化。

**Step 4: Commit**
```bash
git add art-design-pro/src/views/workflow
git commit -m "feat(front-workflow): 搭建精简版钉钉风格审批流可视化设计器框架"
```

### Task 7: 审批 JSON 到 Flowable BPMN 互转引擎 (后端集成)

**Files:**
- Create: `e:\my-project\hr-ruoyi-pro\RuoYi-Vue\ruoyi-workflow\src\main\java\com\ruoyi\workflow\utils\BpmnModelTransfer.java`
- Create: `e:\my-project\hr-ruoyi-pro\RuoYi-Vue\ruoyi-workflow\src\main\java\com\ruoyi\workflow\service\IWorkflowProcessService.java`
- Create: `e:\my-project\hr-ruoyi-pro\RuoYi-Vue\ruoyi-workflow\src\main\java\com\ruoyi\workflow\service\impl\WorkflowProcessServiceImpl.java`

**Step 1: BPMN 转换工具类实现**
解析前端传来的类似 JSON，翻译成 Flowable 的 `BpmnModel` 模型结构。具体过程：
- 组装 `StartEvent`
- 当遭遇指定人员审批节点，转化为 `UserTask` 及相应的指派表达式（如 `${approver}`）。
- 当遭遇到并行/互斥路由时，生成 `ExclusiveGateway`。
- 最终关联为完整的 `SequenceFlow` 并通过 `repositoryService.createDeployment()` 发布。

**Step 2: 提供集成与部署接口**
实现 `deploy` 以及启动流程 `startProcessInstanceByKey` 相应的核心 API，并将它们暴露出去以便为前端以及 `hr_employee_change` 能够提供审批的发起能力。

**Step 3: 测试相关封装**
确保未抛出 ClassNotFound 错误且项目正常构建启动加载 Flowable。

**Step 4: Commit**
```bash
git add .
git commit -m "feat(workflow): 实现 Flowable 嵌入式部署及审批 JSON 可视化至 BPMN XML 解析逻辑的对接层"
```

### Task 8: 预设审批场景整合及应用

**Files:**
- Modify: `e:\my-project\hr-ruoyi-pro\RuoYi-Vue\ruoyi-hr\src\main\java\com\ruoyi\hr\service\impl\HrEmployeeChangeServiceImpl.java` (假设前置步骤生成了此异动服务)

**Step 1: 整合异动表及审批流程引擎联动**
开发员工入职/转正等逻辑。例如，触发 `HrEmployeeChangeService` 创建一条新状态为"审批中" (status=0)的记录，而后通过 `WorkflowProcessService` 启动"入职审批"工作流，把 `change_id` 当做业务键 (Business Key) 存入。

**Step 2: 确认审批完成后置拦截器**
当 Flowable 节点全部结束触发流程完毕事件，调用相应的业务回调，对 `HrEmployeeChangeService` 数据进行落池：如转正情况完成，就把员工业绩状态重置为"已转正"。

**Step 3: Commit**
```bash
git commit -am "feat(hr): 融合业务层异动审批的生命周期联动操作管理"
```
