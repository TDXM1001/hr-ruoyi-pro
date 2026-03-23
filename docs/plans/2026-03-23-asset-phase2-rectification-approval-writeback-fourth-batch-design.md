# 不动产整改审批回写第四批设计

## 1. 背景

第三批已经把不动产详情壳总览中的“整改闭环摘要”做了出来，但整改页签仍保留着一套独立的概览文案和推进提示。当前问题不是功能缺失，而是同一条资产在“总览”和“整改页签”里看到的闭环解释层级还不完全一致。

这会带来两个问题：

1. 资产管理员在总览页看到的是“闭环状态 + 最近动作 + 下一步建议”，切到整改页签后却回到了另一套提示话术。
2. 生命周期轨迹在总览页已经能识别整改关键节点，但整改页签里的轨迹仍然偏原始日志，闭环阶段感不够强。

第四批的目标是继续收口，不再增加新的审批动作，而是让详情壳顶部摘要、总览摘要和整改页签使用同一套闭环解释口径。

## 2. 目标

### 2.1 业务目标

让资产管理员在同一条资产详情中，不论停留在顶部摘要、总览页还是整改页签，都能快速回答下面三个问题：

1. 当前整改闭环卡在哪个阶段。
2. 最近一次整改或审批动作是什么。
3. 下一步应该先推进哪件事。

### 2.2 技术目标

1. 继续复用现有 `rectification-overview.ts` 作为共享解释层。
2. 避免在整改页签中再维护第二套闭环状态判断逻辑。
3. 让整改页签轨迹和总览轨迹对整改关键节点的展示保持一致。

## 3. 方案对比

### 方案 A：只调整整改页签文案

- 做法：保留现有整改页签统计，只把提示文案改得更像总览摘要。
- 优点：改动最小。
- 缺点：仍然会保留两套状态判断来源，后面继续演进时容易再次分叉。

### 方案 B：共享闭环摘要层复用到整改页签（推荐）

- 做法：继续使用 `rectification-overview.ts` 计算闭环状态、最近动作、下一步建议；整改页签只负责展示，不再自己定义另一套闭环说明。
- 优点：总览、顶部摘要、整改页签都吃同一套解释层，口径最稳。
- 缺点：需要同步整理整改页签轨迹展示和测试用例。

### 方案 C：新增整改闭环聚合接口

- 做法：再做一条专用接口，专门返回详情壳需要的闭环摘要。
- 优点：聚合边界更干净。
- 缺点：当前收益不足，会把这一批从“最小返工前端收口”扩展成后端接口设计。

## 4. 最终方案

采用方案 B：共享闭环摘要层复用到整改页签。

### 4.1 数据来源

继续复用现有共享解释器：

- `buildRectificationOverviewSummary`
- `decorateOverviewLifecycleRecords`

父层 `detail/index.vue` 负责：

1. 基于同一批整改记录计算 `rectificationOverviewSummary`。
2. 基于同一批生命周期日志产出带整改阶段标识的轨迹记录。
3. 把整改页签所需的闭环摘要和整改轨迹一起传给 `RectificationPanel`。

### 4.2 整改页签展示收口

整改页签顶部保留两块内容，但第二块“推进提示”改为真正复用共享闭环摘要：

1. 左侧“整改概览”继续展示数量型指标。
2. 右侧改成“闭环提示”，展示：
   - 当前闭环状态
   - 最近整改动作
   - 下一步建议
   - 固定的操作边界说明

这样整改页签不再依赖本地的分支文案，而是直接展示共享摘要层给出的统一结论。

### 4.3 整改轨迹展示收口

整改页签中的“整改轨迹”继续保留，但对整改相关节点补齐阶段标签和提示说明，效果与总览中的生命周期轨迹一致：

- 发起整改
- 完成整改
- 提交审批
- 审批通过
- 审批驳回

这样用户在总览看轨迹和在整改页签看轨迹时，不会再出现一边是阶段表达、一边是纯原始日志的割裂感。

## 5. 影响范围

### 5.1 前端

- `art-design-pro/src/views/asset/real-estate/detail/index.vue`
- `art-design-pro/src/views/asset/real-estate/detail/components/rectification-panel.vue`
- `art-design-pro/src/views/asset/real-estate/detail/components/rectification-overview.ts`
- `art-design-pro/tests/views/asset-real-estate-detail-page.test.ts`
- `art-design-pro/tests/views/asset-real-estate-rectification-panel-approval.test.ts`

### 5.2 不涉及

1. 不新增 SQL。
2. 不改后端接口。
3. 不触碰处置相关脏文件。
4. 不触碰 `application-druid.yml`。

## 6. 验证口径

### 6.1 定向验证

1. 详情壳总览和整改页签对同一批整改数据给出相同的闭环状态判断。
2. 整改页签展示最近整改动作和下一步建议。
3. 整改轨迹中对整改关键节点展示阶段标签和提示。

### 6.2 回归验证

继续跑：

- `tests/views/asset-real-estate-detail-page.test.ts`
- `tests/views/asset-real-estate-rectification-panel-approval.test.ts`
- `tests/views/asset-real-estate-rectification-form-page.test.ts`
- `tests/views/asset-real-estate-rectification-complete-page.test.ts`
- `tests/api/asset-real-estate.test.ts`

## 7. 完成定义

满足以下条件，第四批视为完成：

1. 详情壳顶部摘要、总览摘要、整改页签闭环提示使用同一套状态口径。
2. 整改页签可以直接展示最近整改动作和下一步建议。
3. 整改轨迹对整改和审批节点的阶段识别与总览一致。
4. 定向测试先失败后通过，相关回归保持绿色。
