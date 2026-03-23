# 统一处置主流程第四批设计

## 1. 目标

第三批已经把来源视图和首屏操作引导收成了一张闭环入口卡。第四批继续只动统一处置页前端安全接入层，不进入后端主流程。

本批目标：

> 把闭环入口卡和首屏筛选/办理提示进一步统一成更强的办理视图。

## 2. 当前问题

第三批的入口卡已经能说明来源、锁定范围和下一步建议，但用户仍需要在卡片和 tab 头部之间来回理解“我现在是在办理还是回看”“如果想切到另一侧应该点哪里”。

## 3. 最终方案

采用“办理视图强化”方案：

1. 在闭环入口卡内新增 `当前办理视图` 区块。
2. 统一展示：
   - 当前办理视图名称
   - 当前办理视图解释
   - 主操作
   - 次操作
3. `intent=view` 时：
   - 当前办理视图：处置记录回看
   - 次操作：去待处置资产池
4. `intent=start` 时：
   - 当前办理视图：待处置资产池办理
   - 次操作：查看处置记录

## 4. 安全边界

### 4.1 可动文件

1. `art-design-pro/src/views/asset/disposal/disposal-source-context.ts`
2. `art-design-pro/src/views/asset/disposal/components/disposal-entry-card.vue`
3. `art-design-pro/src/views/asset/disposal/index.vue`
4. `art-design-pro/tests/views/asset-disposal-page.test.ts`

### 4.2 禁动文件

1. 所有处置后端文件
2. 所有 SQL
3. `RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml`

## 5. 验证口径

1. `intent=view` 首屏展示“当前办理视图：处置记录回看”，并支持切到待处置资产池。
2. `intent=start` 首屏展示“当前办理视图：待处置资产池办理”，并支持切回处置记录。
3. 第三批已有的来源横幅、来源卡、主操作入口不回退。
4. 真实浏览器首屏能看到办理视图与双操作按钮。
