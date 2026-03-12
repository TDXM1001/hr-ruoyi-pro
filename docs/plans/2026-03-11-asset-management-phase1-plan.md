# 资产管理系统实施计划（第一期）

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**目标:** 建立资产管理后端的核心架构 `ruoyi-asset` 模块，完成资产分类与台账的数据库表建立及前后端的基础 CRUD 和路由页面配置。

**架构:** 在 `RuoYi-Vue` 后端新建独立的 `ruoyi-asset` Maven 子模块进行隔离开发；在前端 `art-design-pro` 项目中配置相应的资产管理字典、菜单，并开发基础的列表、树形分类和表单组件。

**技术栈:** Java, Spring Boot, MyBatis(后端); Vue 3, Element Plus, TypeScript(前端); MySQL

---

### [x] 任务 1：创建资产核心数据表 SQL 脚本

**文件:**
- 创建: `RuoYi-Vue/sql/20260311_asset_management_init.sql`

**步骤 1：编写 SQL 脚本内容**

```sql
CREATE TABLE `asset_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父节点ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `code` varchar(50) DEFAULT NULL COMMENT '分类编码',
  `level` int(11) DEFAULT NULL COMMENT '层级',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产分类表';

CREATE TABLE `asset_info` (
  `asset_no` varchar(50) NOT NULL COMMENT '资产编号',
  `asset_name` varchar(100) NOT NULL COMMENT '资产名称',
  `category_id` bigint(20) NOT NULL COMMENT '分类ID',
  `asset_type` tinyint(4) NOT NULL COMMENT '类型：1=不动产 2=固定资产',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '归属部门',
  `user_id` bigint(20) DEFAULT NULL COMMENT '责任人',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态：1=正常 2=领用中 3=维修中 4=盘点中 5=已报废',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`asset_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产主表';

-- 预留自定义属性等表创建结构，保持最简骨架。
```

**步骤 2：在测试库或本地执行 SQL 验证无语法异常**
由于仅涉及 DDL 语句，只需确认在数据库或 CLI 执行不报错。

**步骤 3：提交代码（Commit）**

```bash
git add RuoYi-Vue/sql/20260311_asset_management_init.sql
git commit -m "feat: 添加基础资产数据表 SQL"
```

---

### [x] 任务 2：创建后端 `ruoyi-asset` 子模块

**文件:**
- 修改: `RuoYi-Vue/pom.xml`
- 修改: `RuoYi-Vue/ruoyi-admin/pom.xml`
- 创建: `RuoYi-Vue/ruoyi-asset/pom.xml`

**步骤 1：添加子模块依赖和基础结构**

完善在顶级和 `ruoyi-admin` 项目下的依赖导入，并建立基础包结构 `com.ruoyi.asset`。

**步骤 2：验证项目是否能正常编译**

Run: `cd RuoYi-Vue && mvn clean compile -pl ruoyi-asset -am`
Expected: `BUILD SUCCESS`

**步骤 3：提交代码（Commit）**

```bash
git add RuoYi-Vue/pom.xml RuoYi-Vue/ruoyi-admin/pom.xml RuoYi-Vue/ruoyi-asset/pom.xml
git commit -m "feat: 创建 ruoyi-asset 后端子模块"
```

---

### [x] 任务 3：实现后端资产分类接口 (CRUD)

**文件:**
- 创建: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetCategory.java`
- 创建: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetCategoryMapper.java`
- 创建: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetCategoryMapper.xml`
- 创建: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetCategoryService.java`
- 创建: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetCategoryServiceImpl.java`
- 创建: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetCategoryController.java`

**步骤 1：编写针对分类业务的 CRUD 逻辑**

实现基础的数据层、服务层以及暴露出控制器接口供前端调用。

**步骤 2：执行整体编译确保代码结构合法**

Run: `cd RuoYi-Vue && mvn compile`
Expected: `BUILD SUCCESS`且代码无红误。

**步骤 3：提交代码（Commit）**

```bash
git add RuoYi-Vue/ruoyi-asset/ RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetCategoryController.java
git commit -m "feat: 实现后端的资产分类管理 CRUD API"
```

---

### [x] 任务 4：实现后端资产台账主表接口 (CRUD)

**文件:**
- 创建: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetInfo.java`
... （及 Mapper, XML, Service, Controller 相关文件类同上）

**步骤 1：编写资产台账主要逻辑**
包括查询过滤（以分类、资产编号和状态等），新增资产，修改资产等逻辑。

**步骤 2：编译测试模块功能**

Run: `cd RuoYi-Vue && mvn clean package -DskipTests=true`
Expected: `BUILD SUCCESS`

**步骤 3：提交代码（Commit）**

```bash
git add RuoYi-Vue/ruoyi-asset/ RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetInfoController.java
git commit -m "feat: 实现后端的资产信息台账的基础 CRUD API"
```

---

### 任务 5：配置前端资产管理路由 (含初始化菜单数据)

**文件:**
- 创建: `RuoYi-Vue/sql/20260311_asset_menu_init.sql`

**步骤 1：准备菜单数据库建置 SQL 脚本**

```sql
-- 这里假设是生成对应 asset/category 和 asset/list 的菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('资产系统', 0, 4, 'asset', NULL, 1, 0, 'M', '0', '0', '', 'dict', 'admin', sysdate(), '', null, '资产管理父菜单');
```

**步骤 2：提交代码（Commit）**

```bash
git add RuoYi-Vue/sql/20260311_asset_menu_init.sql
git commit -m "feat: 添加菜单基础配置 SQL"
```

---

### 任务 6：开发前端资产分类页面

**文件:**
- 创建: `art-design-pro/src/api/asset/category.ts`
- 创建: `art-design-pro/src/views/asset/category/index.vue`

**步骤 1：按规范编写基于 `el-tree` 等组件的分类体系维护界面**
其中涉及对应 Vue 相关逻辑、API 请求函数绑定。

**步骤 2：前端本地验证结构正确**

Run: `cd art-design-pro && npm run type-check` （若没有配置跳过即可）或者直接通过 `npm run build` 预验证。
Expected: 不出现阻断型错误

**步骤 3：提交代码（Commit）**

```bash
git add art-design-pro/src/api/asset/category.ts art-design-pro/src/views/asset/category/index.vue
git commit -m "feat: 新增前端资产分类页面架构"
```

---

### 任务 7：开发前端资产台账信息页面

**文件:**
- 创建: `art-design-pro/src/api/asset/info.ts`
- 创建: `art-design-pro/src/views/asset/list/index.vue`

**步骤 1：构建资产台账列表与信息表单抽屉组件**
包括上部的基础条件搜索、主体的列表配置以及基于弹框的属性录入交互（表单）。

**步骤 2：前端构建校验**

Run: `cd art-design-pro && npm run lint`

**步骤 3：提交代码（Commit）**

```bash
git add art-design-pro/src/api/asset/info.ts art-design-pro/src/views/asset/list/index.vue
git commit -m "feat: 完成资产信息基础主界面的骨架搭建"
```
