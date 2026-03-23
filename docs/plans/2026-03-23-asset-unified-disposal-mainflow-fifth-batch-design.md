# 统一处置主流程第五批设计

## 1. 目标

第四批已经把来源视图和首屏操作引导收成了闭环入口卡，但入口卡里的办理提示仍然固定绑定来源意图，而不是用户当前所在的办理视图。

本批目标：

> 让闭环入口卡跟随当前 tab 实时更新，真正收成“当前办理视图”。

## 2. 当前问题

当用户从 `intent=view` 进入统一处置页后，再切到 `待处置资产池`，入口卡仍然保留“处置记录回看”的锁定范围、办理说明和主操作文案。

这会造成两层语义不一致：

1. tab 已经切到了新视图
2. 入口卡仍然在讲旧视图

## 3. 最终方案

采用“入口卡实时跟随当前视图”方案：

1. 保留来源意图本身
   - 当前意图继续展示用户是从“不动产处置联动”里以 `view/start` 进入
2. 把下列字段改为跟随 `activeTab`
   - 当前锁定范围
   - 下一步建议
   - 当前办理视图
   - 主操作
   - 次操作
3. 当用户切换 tab 或点击入口卡次操作时，入口卡同步更新

## 4. 安全边界

### 4.1 可动文件

1. `art-design-pro/src/views/asset/disposal/disposal-source-context.ts`
2. `art-design-pro/src/views/asset/disposal/index.vue`
3. `art-design-pro/tests/views/asset-disposal-page.test.ts`

### 4.2 不动文件

1. 所有处置后端文件
2. 所有 SQL
3. `RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml`
