# 统一处置主流程 1~5 批短检查单

## 1. 当前定位

- [x] 当前已完成的是统一处置页前端安全接入层的 1~5 批收口，不是处置后端主流程改造。
- [x] 当前稳定链路是“不动产详情壳 -> 统一处置页”的来源承接、回看和继续办理入口。
- [x] 后续第六批建议继续只动前端安全层，不碰处置后端脏文件、`application-druid.yml` 和 SQL。

## 2. 1~5 批完成项

### 第 1 批：来源安全接入

- [x] 统一处置页可识别 `source=real-estate-disposal-tab`。
- [x] 统一处置页可识别 `intent=view` / `intent=start`。
- [x] 可承接 `assetId`、`assetCode`、`assetName`。
- [x] 可根据来源意图决定首屏落点：
  - `intent=view -> record`
  - `intent=start -> pool`
- [x] 页面提供返回不动产详情入口。

### 第 2 批：来源视图统一

- [x] 顶部来源横幅继续保留。
- [x] 资产上下文、锁定范围、下一步建议被收进统一来源视图。
- [x] 有来源参数时，tab 内重复的来源过滤提示被收回。
- [x] 无来源参数时，统一处置页原有行为保持不变。

### 第 3 批：闭环入口卡

- [x] 来源视图升级为闭环入口卡。
- [x] 入口卡统一展示资产、意图、首屏落点、锁定范围、下一步建议。
- [x] 新增主操作入口：
  - `record -> 查看处置记录`
  - `pool -> 进入待处置资产池`

### 第 4 批：办理视图强化

- [x] 入口卡新增“当前办理视图”区块。
- [x] 新增次操作按钮，可在 `pool` / `record` 之间切换。
- [x] `intent=view` 与 `intent=start` 共用同一张卡，只切换办理语义。

### 第 5 批：入口卡跟随当前视图

- [x] 入口卡已不再固定绑定来源意图。
- [x] `activeTab` 切换后，下列内容会同步更新：
  - 当前锁定范围
  - 下一步建议
  - 当前办理视图
  - 主操作
  - 次操作

## 3. 当前页面应有状态

- [x] 顶部有来源横幅，只在有来源参数时展示。
- [x] 横幅下方有一张闭环入口卡。
- [x] 入口卡已经能统一表达：
  - 资产编码 / 资产名称
  - 当前意图
  - 首屏落点
  - 当前锁定范围
  - 下一步建议
  - 当前办理视图
  - 主操作 / 次操作
- [x] 入口卡已跟随当前 `activeTab` 实时变化。
- [ ] tab 内表格头部提示还没有和入口卡完全统一。
- [ ] 当前 tab 头部仍是两套写法：
  - `pool` 头部：`处置入口：待处置资产池` + `刷新资产池`
  - `record` 头部：`刷新记录`

## 4. 第六批直接切入点

- [ ] 把“入口卡”和“tab 内表格头部提示”收成一套统一的“办理摘要条”。
- [ ] 办理摘要条语义应跟随当前 `activeTab`，而不是在入口卡和 tab 头部分别各写一套。
- [ ] 第六批至少应统一这些信息：
  - 当前办理视图名称
  - 当前办理说明
  - 下一步建议
  - 主操作
  - 次操作或辅助动作
  - 刷新动作
- [ ] 第六批完成后，首屏和 tab 内头部应该表达同一套办理语义，不再让用户在两处拼接理解。

## 5. 第六批建议可动文件

- [x] `art-design-pro/src/views/asset/disposal/index.vue`
- [x] `art-design-pro/src/views/asset/disposal/disposal-source-context.ts`
- [x] `art-design-pro/src/views/asset/disposal/components/disposal-entry-card.vue`
- [x] 可按需新增 `art-design-pro/src/views/asset/disposal/components/disposal-summary-bar.vue`
- [x] `art-design-pro/tests/views/asset-disposal-page.test.ts`
- [x] `art-design-pro/tests/views/asset-real-estate-detail-page.test.ts`

## 6. 第六批继续禁动

- [x] `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetDisposalController.java`
- [x] `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetDisposalServiceImpl.java`
- [x] `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetDisposalMapper.xml`
- [x] `RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml`
- [x] 所有 SQL

## 7. 后续回看时的最短判断句

> 1~5 批已经把统一处置页做成“会跟随当前 tab 变化的闭环入口卡”，第六批不用再重做来源接入，只需要把入口卡和 tab 头部提示统一成一套办理摘要条。

## 8. 回归命令

```bash
pnpm vitest run tests/views/asset-disposal-page.test.ts
pnpm vitest run tests/views/asset-disposal-page.test.ts tests/views/asset-real-estate-detail-page.test.ts
```

## 9. 第七批代码注释约定

- [x] 关键业务逻辑必须保留简短中文注释，重点解释“为什么这么写”和“这段口径从哪里来”。
- [x] 优先覆盖当前办理视图、空状态文案和刷新反馈这三类容易丢上下文的逻辑。
