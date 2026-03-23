# 不动产处置关联最小闭环第四批实施计划

## 1. 实施目标

本批围绕两个子目标实施：

1. 清理统一处置页 4 条 Vue 警告。
2. 把生命周期里的处置节点和处置页签记录卡片统一成最近责任归口视图。

## 2. 实施范围

### 2.1 代码文件

- `art-design-pro/src/views/asset/disposal/index.vue`
- `art-design-pro/src/views/asset/real-estate/detail/components/disposal-overview.ts`
- `art-design-pro/src/views/asset/real-estate/detail/components/disposal-panel.vue`
- `art-design-pro/src/views/asset/real-estate/detail/components/overview-panel.vue`
- `art-design-pro/tests/views/asset-disposal-page.test.ts`
- `art-design-pro/tests/views/asset-real-estate-detail-page.test.ts`

### 2.2 明确不在范围内

- 统一处置模块后端接口与服务
- 不动产处置主流程扩展
- SQL 变更
- 处置审批主线后端实现

## 3. 实施步骤

### 步骤 1：先写失败测试

- 为统一处置页补“挂载时不应出现审批弹窗相关未定义属性警告”测试。
- 为不动产详情壳补两条失败测试：
  - 生命周期处置节点展示责任归口
  - 处置记录卡片展示责任归口

### 步骤 2：修复统一处置页告警

在 `asset/disposal/index.vue` 中补齐：

- 审批弹窗状态
- 审批抽屉状态
- 标题计算
- 关闭回调
- 审批轨迹加载
- 审批提交处理

目标是让页面在不进入审批动作时也能稳定挂载。

### 步骤 3：抽取处置责任归口解释器

在 `disposal-overview.ts` 中新增责任归口复用能力，并让已有摘要继续共享这一层解释器。

### 步骤 4：接入总览生命周期与处置记录卡片

- 总览生命周期中的处置节点增加责任归口视图。
- 处置页签记录卡片增加责任归口视图。

### 步骤 5：跑相关回归

执行：

- 统一处置页定向测试
- 不动产详情壳定向测试
- 处置页、详情壳、整改页签相关回归

## 4. 完成标准

满足以下条件即可认为第四批完成：

1. 统一处置页挂载时不再出现 4 条已知 Vue 警告。
2. 总览生命周期里的处置节点可以看到责任归口视图。
3. 处置页签记录卡片可以看到责任归口视图。
4. 相关前端回归测试通过。

## 5. 后续衔接

第四批完成后，可以继续考虑两条方向：

1. 继续做处置关联最小闭环后续批次，把摘要、责任归口、联动入口进一步统一。
2. 如果要正式进入处置主流程实现，再单独隔离当前工作区已有的处置后端脏文件，避免实现阶段发生碰撞。