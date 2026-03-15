# 资产后端接口契约变更说明

## 背景

随着资产数据模型从单表模式切换到聚合模式，`/asset/info` 相关接口已经不再直接暴露旧版 `AssetInfo` 单表结构。前端联调需要同步切换到以 `assetId` 为内部主键、以聚合对象为载荷的新契约。

## 一、资产新增与修改请求结构

### 旧结构

旧版请求体为平铺 `AssetInfo`，典型字段包括：

- `assetNo`
- `assetName`
- `categoryId`
- `assetType`
- `deptId`
- `userId`
- `status`
- `remark`

### 新结构

新增和修改均使用聚合请求体：

```json
{
  "basicInfo": {
    "assetId": 1001,
    "assetNo": "FA-2026-0001",
    "assetName": "测试资产",
    "categoryId": 10,
    "assetType": "1",
    "manageDeptId": 200,
    "useDeptId": 201,
    "responsibleUserId": 3001,
    "userId": 3002,
    "assetStatus": "1",
    "remark": "备注"
  },
  "financeInfo": {
    "bookType": "1",
    "currencyCode": "CNY",
    "originalValue": 10000.00,
    "salvageRate": 0.03,
    "depreciationMethod": "1",
    "usefulLifeMonth": 60,
    "depreciationStartDate": "2026-03-01"
  },
  "realEstateInfo": null,
  "dynamicAttrs": [
    {
      "attrId": 501,
      "attrCode": "manufacturer",
      "attrValueText": "示例厂商"
    }
  ],
  "attachments": [
    {
      "bizType": "invoice",
      "fileName": "invoice.pdf",
      "fileUrl": "/profile/upload/2026/03/invoice.pdf"
    }
  ]
}
```

### 约束说明

- `basicInfo.assetId` 仅修改接口必填，新增接口不传
- `basicInfo.assetNo` 继续保留为业务编码，但后端内部关联统一依赖 `assetId`
- `financeInfo` 为必填
- `assetType=2` 时必须提交 `realEstateInfo`
- 已开始折旧的资产，不允许通过聚合修改直接变更 `originalValue`、`salvageRate`、`depreciationMethod`、`usefulLifeMonth`、`depreciationStartDate`

## 二、资产详情响应结构

### 旧结构

旧版详情接口直接返回 `AssetInfo` 单表对象。

### 新结构

详情接口 `GET /asset/info/{assetId}` 返回聚合对象：

```json
{
  "basicInfo": {
    "assetId": 1001,
    "assetNo": "FA-2026-0001",
    "assetName": "测试资产",
    "assetType": "1",
    "categoryId": 10,
    "useDeptId": 201,
    "assetStatus": "1"
  },
  "financeInfo": {
    "assetId": 1001,
    "originalValue": 10000.00,
    "salvageRate": 0.03,
    "salvageValue": 300.00,
    "depreciableValue": 9700.00,
    "depreciationMethod": "1",
    "usefulLifeMonth": 60,
    "monthlyDepreciationAmount": 161.67,
    "accumulatedDepreciation": 323.34,
    "netBookValue": 9676.66,
    "bookValue": 9676.66,
    "lastDepreciationPeriod": "2026-03"
  },
  "realEstateInfo": null,
  "dynamicAttrs": [],
  "attachments": [],
  "depreciationLogs": [
    {
      "assetId": 1001,
      "period": "2026-03",
      "depreciationAmount": 161.67
    }
  ]
}
```

### 约束说明

- `depreciationLogs` 当前仅返回最近 12 条折旧日志，用于详情页快速展示
- 如需完整折旧历史，应调用独立财务接口查询
- 列表接口仍返回轻量视图 `AssetListVo`，不返回完整聚合结构

## 三、动态扩展属性载荷结构

动态属性使用数组载荷，后端支持以下字段组合：

- `attrId`
- `categoryId`
- `attrCode`
- `attrValueText`
- `attrValueNumber`
- `attrValueDate`
- `attrValueJson`

说明：

- 单一字段通常只需提交一个值列
- 后端会在写入时按 `assetId` 整体覆盖当前资产的动态属性
- 动态属性不能覆盖系统保留字段，例如 `asset_no`、`original_value`、`property_cert_no`

## 四、财务相关接口

新增管理端财务接口如下：

- `POST /asset/finance/{assetId}/recalculate`
  用途：按资产手动重算财务结果
- `GET /asset/finance/{assetId}/depreciation-logs`
  用途：查询指定资产的全部折旧日志
- `POST /asset/finance/accrue/{period}`
  用途：按期间批量执行月度折旧计提，`period` 格式为 `yyyy-MM`

补充说明：

- 财务重算接口返回最新 `AssetFinance` 快照
- 批量计提接口返回本次新生成的折旧日志集合
- 已处置、财务数据不完整或本期已执行过计提的资产会在批量计提时被跳过

## 五、前端最小改造触点

以下前端文件需要优先同步：

- `art-design-pro/src/api/asset/info.ts`
  需要将详情、删除接口的路径参数从 `assetNo` 切换为 `assetId`
- `art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue`
  需要将新增、修改请求体改为聚合结构，不再直接提交平铺 `AssetInfo`
- 资产列表页与详情页
  需要兼容 `assetStatus`、`useDeptId`、`manageDeptId`、`responsibleUserId` 等新字段
- 动态表单区域
  需要支持 `dynamicAttrs` 数组结构，而不是散落在固定表单字段中
- 财务信息页签或详情区块
  需要接入财务重算、折旧日志和月度计提入口

## 六、已知兼容差异

- 旧前端类型定义中仍存在 `deptId`、`status`、`assetType: 1=不动产/2=固定资产` 的旧口径
- 新后端内部主键统一为 `assetId`，`assetNo` 只保留为业务编码和展示字段
- 若前端继续使用旧请求体，将无法满足新接口的必填校验和聚合写入逻辑

## 七、固定资产生命周期与审批中心接口

### 审批中心

- `GET /workflow/task/todo`
  用途：查询当前登录人的待办任务
- `GET /workflow/task/done`
  用途：查询当前登录人的已办任务
- `POST /workflow/task/approve`
  用途：提交审批动作，当前只支持 `approve`、`reject`

审批请求体示例：

```json
{
  "instanceId": 1003,
  "action": "approve",
  "comment": "同意"
}
```

补充说明：

- `businessType` 已统一映射为前端字典口径：`REQUISITION`、`RETURN`、`REPAIR`、`SCRAP`
- `status` 已统一映射为 `wf_status` 字典值：`IN_PROGRESS`、`COMPLETED`、`REJECTED`

### 领用归还

- `POST /asset/requisition/return/{requisitionNo}`
  用途：归还已审批通过的领用单

补充说明：

- 仅 `status=1` 的领用单允许归还
- 归还成功后领用单状态更新为 `3`，资产状态回退为 `1=在用`

### 维修台账

- `GET /asset/maintenance/list`
  用途：分页查询维修记录
- `GET /asset/maintenance/{maintenanceNo}`
  用途：查询维修单详情
- `POST /asset/maintenance`
  用途：发起维修申请
- `POST /asset/maintenance/complete/{maintenanceNo}`
  用途：完成已审批通过的维修单

补充说明：

- 维修申请支持前端优先传 `assetId`，也允许仅传 `assetNo` 由后端解析主档
- 完成维修后维修单状态更新为 `3`，资产状态回退为 `1=在用`

### 报废/处置台账

- `GET /asset/disposal/list`
  用途：分页查询报废/处置记录
- `GET /asset/disposal/{disposalNo}`
  用途：查询报废/处置单详情
- `POST /asset/disposal`
  用途：发起报废/处置申请

补充说明：

- 当前阶段审批通过后统一把资产状态收敛为 `5=已报废`
- 后续如需区分“已报废 / 已处置”，会在 `disposalType` 正式入模后单独扩展

## 八、菜单路径与权限最终口径

- `asset/requisition/index`
  页面权限：`asset:requisition:list`
  按钮权限：`asset:requisition:return`
- `asset/maintenance/index`
  页面权限：`asset:maintenance:list`
  按钮权限：`asset:maintenance:query`、`asset:maintenance:add`、`asset:maintenance:complete`
- `asset/disposal/index`
  页面权限：`asset:disposal:list`
  按钮权限：`asset:disposal:query`、`asset:disposal:add`
- `asset/workflow/todo/index`
  页面权限：`workflow:task:todo`
  按钮权限：`workflow:task:approve`
- `asset/workflow/done/index`
  页面权限：`workflow:task:done`
- `asset/real-estate/ownership/index`
  页面权限：`asset:realEstateOwnership:list`
  按钮权限：`asset:realEstateOwnership:query`、`asset:realEstateOwnership:add`
- `asset/real-estate/usage/index`
  页面权限：`asset:realEstateUsage:list`
  按钮权限：`asset:realEstateUsage:query`、`asset:realEstateUsage:add`
- `asset/real-estate/status/index`
  页面权限：`asset:realEstateStatus:list`
  按钮权限：`asset:realEstateStatus:query`、`asset:realEstateStatus:add`
- `asset/real-estate/disposal/index`
  页面权限：`asset:realEstateDisposal:list`
  按钮权限：`asset:realEstateDisposal:query`、`asset:realEstateDisposal:add`

## 九、不动产生命周期接口

### 权属变更

- `GET /asset/real-estate/ownership/list`
  用途：分页查询权属变更记录
- `GET /asset/real-estate/ownership/{ownershipChangeNo}`
  用途：查询权属变更详情
- `POST /asset/real-estate/ownership`
  用途：发起权属变更申请

补充说明：

- 权属变更属于审批型动作，单据状态按 `pending -> approved/rejected` 流转
- 前端创建请求体最小字段为 `assetId/assetNo`、`targetRightsHolder`、`targetPropertyCertNo`、`targetRegistrationDate`、`reason`

### 用途变更

- `GET /asset/real-estate/usage/list`
  用途：分页查询用途变更记录
- `GET /asset/real-estate/usage/{usageChangeNo}`
  用途：查询用途变更详情
- `POST /asset/real-estate/usage`
  用途：发起用途变更

补充说明：

- 用途变更属于免审批动作，创建后单据直接进入 `completed`
- 后端在写入单据后会同步回写 `asset_real_estate.land_use` 与 `asset_real_estate.building_use`

### 状态变更

- `GET /asset/real-estate/status/list`
  用途：分页查询状态变更记录
- `GET /asset/real-estate/status/{statusChangeNo}`
  用途：查询状态变更详情
- `POST /asset/real-estate/status`
  用途：发起状态变更

补充说明：

- 状态变更属于免审批动作，创建后单据直接进入 `completed`
- 后端在写入单据后会同步回写 `asset_info.asset_status`

### 注销/处置

- `GET /asset/real-estate/disposal/list`
  用途：分页查询注销/处置记录
- `GET /asset/real-estate/disposal/{disposalNo}`
  用途：查询注销/处置详情
- `POST /asset/real-estate/disposal`
  用途：发起注销/处置申请

补充说明：

- 注销/处置属于审批型动作，单据状态按 `pending -> approved/rejected` 流转
- 当前阶段前端最小请求体字段为 `assetId/assetNo`、`disposalType`、`targetAssetStatus`、`reason`

### 动作分流口径

- 审批型动作：权属变更、注销/处置
- 免审批动作：用途变更、状态变更
- 资产列表页不再暴露 `realEstateChange` 占位键，而是拆分为 `realEstateOwnership`、`realEstateUsage`、`realEstateStatus`、`realEstateDisposal`

## 十、当前构建阻塞说明

`art-design-pro` 的 `npm run build` 仍被以下历史 TypeScript 问题阻塞，本次资产生命周期改动未新增这些报错：

- `src/views/monitor/job/index.vue`
- `src/views/monitor/logininfor/index.vue`
- `src/views/monitor/operlog/index.vue`
- `src/views/monitor/server/index.vue`
- `src/views/system/log/logininfor/index.vue`
- `src/views/system/log/operlog/index.vue`
- `src/views/system/operlog/index.vue`
