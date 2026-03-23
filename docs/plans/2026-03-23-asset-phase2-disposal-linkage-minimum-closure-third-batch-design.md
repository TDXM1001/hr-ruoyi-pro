# 不动产处置关联最小闭环第三批设计

## 1. 背景

第二批已经把处置闭环摘要接入总览正文，并补齐了生命周期中的处置节点回看。但从资产管理员视角看，总览和处置页签虽然已经能看到“最近处置动作”，仍然缺少一层更明确的责任归口提示。

这会带来两个问题：

1. 能看到最近动作，但不一定知道当前该谁继续推进。
2. 总览和处置页签虽然用的是同一套状态摘要，但对“当前责任归口”的表达还不够直接。

## 2. 目标

第三批只做一件事：

- 把处置页签摘要和总览里的最近处置动作，统一成更强的责任归口提示。

最终让资产管理员在总览和处置页签里都能直接回答：

1. 当前由谁负责推进。
2. 当前责任动作是什么。
3. 最近责任人是谁。
4. 当前责任提示是什么。

## 3. 设计原则

1. 继续保持不动产详情壳侧隔离实现。
2. 不修改统一处置模块脏文件。
3. 不新增后端接口，不新增 SQL。
4. 总览和处置页签必须复用同一套处置摘要，不允许各自维护分叉文案。

## 4. 处理方案

### 4.1 扩展处置摘要模型

在 `disposal-overview.ts` 中扩展处置摘要，补充以下字段：

1. `responsibilityOwnerLabel`
2. `responsibilityActionLabel`
3. `responsibilityHint`
4. `latestActionOwner`

这些字段由处置阶段统一推导，不让总览和处置页签各自组装文案。

### 4.2 责任归口映射

按阶段统一映射：

1. `NOT_STARTED / PENDING_SUBMIT / REJECTED_RESUBMIT`
   - 责任归口：资产管理员
2. `IN_REVIEW`
   - 责任归口：审批责任岗
3. `APPROVED_DISPOSING`
   - 责任归口：资产管理员 / 执行责任岗
4. `DISPOSED_CLOSED`
   - 责任归口：资产管理员

### 4.3 最近责任人来源

最近责任人优先按处置生命周期日志识别：

1. 读取最新处置节点日志的 `operateBy`
2. 若没有处置日志，再回退到处置记录中的 `confirmedBy`
3. 若仍为空，则显示 `-`

### 4.4 页面接入

#### 总览页

在“处置闭环摘要”卡片中新增三项：

1. 当前责任归口
2. 责任动作
3. 最近责任人

并补一条责任提示文案。

#### 处置页签

处置页签复用同一套字段，展示方式与总览保持一致。

## 5. 影响范围

1. `art-design-pro/src/views/asset/real-estate/detail/components/disposal-overview.ts`
2. `art-design-pro/src/views/asset/real-estate/detail/components/disposal-panel.vue`
3. `art-design-pro/src/views/asset/real-estate/detail/components/overview-panel.vue`
4. `art-design-pro/src/views/asset/real-estate/detail/index.vue`
5. `art-design-pro/tests/views/asset-real-estate-detail-page.test.ts`

## 6. 验证口径

1. 总览展示当前责任归口、责任动作、最近责任人。
2. 处置页签与总览展示同一套责任提示。
3. 处置审批中场景能正确显示“审批责任岗”。
4. 最近责任人优先取最新处置日志的操作人。
5. 详情壳与处置联动相关回归测试通过。

## 7. 结论

第三批不是扩处置流程，而是把“最近处置动作”提升成“责任归口视图”。这样资产管理员在总览或处置页签中都能立刻知道当前卡点归谁推进，减少只见状态不见责任的割裂感。