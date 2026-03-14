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
