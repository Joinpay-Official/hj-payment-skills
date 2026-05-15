---
name: hj-joinpay-aggregation-query
display_name: 汇聚支付聚合支付查询
description: "汇聚支付聚合支付查询 Skill：订单查询、关闭订单、资金管控订单查询。当开发者需要查询支付结果、关闭未支付订单、查询资金管控状态时使用。触发词：订单查询、关单、资金查询、queryOrder、closeOrder、queryFundsControlOrder。"
version: 1.1.0
author: "hj-payment-skills"
homepage: https://www.joinpay.com
license: MIT
compatibility:
  - skillhub
dependencies:
  - hj-joinpay-aggregation-base
metadata:
  skillhub:
    requires:
      config:
        - JOINPAY_MERCHANT_NO
        - JOINPAY_MERCHANT_KEY
        - JOINPAY_RSA_PRIVATE_KEY
        - JOINPAY_RSA_PUBLIC_KEY
---

# 聚合支付 - 查询与关单

覆盖订单查询、关闭订单、资金管控订单查询。

---

## 适配版本与定位

| 项目 | 内容 |
|------|------|
| Skill 版本 | `1.1.0` |
| 定位 | 聚合支付查询与关单 |
| 适用范围 | 订单查询、关闭订单、资金管控订单查询 |

## 前置依赖

| 项目 | 引用 |
|------|------|
| **凭据收集**（代码生成前必须完成） | [credential-collection.md](../hj-payment-integration/shared-rules/credential-collection.md) （查询/关单只需 merchant_no + 密钥，无需 notify_url） |
| **签名硬约束** | [signing-constraints.md](../hj-payment-integration/shared-rules/signing-constraints.md) |
| **接口稳定性** | [interface-stability.md](../hj-payment-integration/shared-rules/interface-stability.md) |

> 以下场景**不需要收集参数**：仅咨询业务知识、仅查看示例代码、参数已收集过。

---

## 接口一：订单查询

### 接口概述

| 项目 | 内容 |
|------|------|
| 接口名称 | 订单查询 |
| 请求方式 | HTTP POST (form-urlencoded) |
| 接口路径 | `/tradeRt/queryOrder` |
| 完整测试URL | `https://trade.joinpay.cc/tradeRt/queryOrder` |
| p0_Version | `2.6` |

### 请求参数

| 参数 | 必填 | 说明 | 示例 |
|------|------|------|------|
| p0_Version | 是 | 接口版本，固定 `2.6` | `2.6` |
| p1_MerchantNo | 是 | 商户号 | `888100500008456` |
| p2_OrderNo | 是 | 商户订单号 | `ORDER20260413001` |
| hmac | 是 | 签名 | — |

### 响应参数

| 参数 | 说明 |
|------|------|
| ra_Code | 响应码（`100`=成功） |
| ra_Status | 交易状态（`100`=成功） |
| rb_CodeMsg | 响应描述 |
| r2_OrderNo | 商户订单号 |
| r7_TrxNo | 平台交易流水号 |
| hmac | 签名 |

### 使用建议

- 下单后建议轮询查询（间隔 5~10 秒），而非依赖前端回调
- 最终订单状态以查询结果或异步通知为准
- 支付成功后 ra_Code=`100` 且 ra_Status=`100`

---

## 接口二：关闭订单

### 接口概述

| 项目 | 内容 |
|------|------|
| 接口名称 | 关闭订单 |
| 请求方式 | HTTP POST (form-urlencoded) |
| 接口路径 | `/tradeRt/closeOrder` |
| 完整测试URL | `https://trade.joinpay.cc/tradeRt/closeOrder` |
| p0_Version | `1.0` |

### 请求参数

| 参数 | 必填 | 说明 | 示例 |
|------|------|------|------|
| p0_Version | 是 | 接口版本，固定 `1.0` | `1.0` |
| p1_MerchantNo | 是 | 商户号 | `888100500008456` |
| p2_OrderNo | 是 | 商户订单号 | `ORDER20260413001` |
| p3_FrpCode | 是 | 原支付交易类型 | `WEIXIN_NATIVE` |
| hmac | 是 | 签名 | — |

### 关单规则

| 条件 | 能否关单 | 说明 |
|------|---------|------|
| 订单状态=NOTPAY | ✅ 可以 | 未支付的超时订单可以关单 |
| 订单状态=SUCCESS | ❌ 不行 | 已支付的订单不能关单，只能退款 |
| 订单状态=CLOSED | ❌ 不行 | 已关闭的订单重复关单会返回错误 |
| 订单状态=FAILED | ❌ 不行 | 失败的订单无需关单 |

> **最佳实践**：建议在下单后 **30分钟** 内轮询订单状态，若仍为 NOTPAY 则调用关单接口释放库存。

### 响应参数

> [P1] 此接口响应为 **JSON 格式**，不是 form 格式！
>
> 如果按 form 格式解析 JSON 响应会导致所有字段值为 `null`。
> 解析策略：检查响应正文首个非空白字符是否为 `{`，若是则用 JSON 解析器处理。

| 参数 | 说明 |
|------|------|
| ra_Code | 响应码（`100`=成功） |
| rb_CodeMsg | 响应描述 |
| hmac | 签名 |

---

## 接口三：资金管控订单查询

### 接口概述

| 项目 | 内容 |
|------|------|
| 接口名称 | 资金管控订单查询 |
| 请求方式 | HTTP POST (form-urlencoded) |
| 接口路径 | `/tradeRt/queryFundsControlOrder` |
| 完整测试URL | `https://trade.joinpay.cc/tradeRt/queryFundsControlOrder` |
| p0_Version | `1.0` |

### 请求参数

| 参数 | 必填 | 说明 | 示例 |
|------|------|------|------|
| p0_Version | 是 | 接口版本，固定 `1.0` | `1.0` |
| p1_MerchantNo | 是 | 商户号 | `888100500008456` |
| p2_OrderNo | 是 | 商户订单号 | `ORDER20260413001` |
| hmac | 是 | 签名 | — |

### 响应参数

| 参数 | 说明 |
|------|------|
| ra_Code | 响应码（`100`=成功） |
| rb_CodeMsg | 响应描述 |
| rc_Result | 资金管控详情 |
| hmac | 签名 |

### 使用场景

- 发货管理：确认支付成功后确认是否可以发货
- 分账查询：查询分账资金状态
- 担保交易：查询担保交易的解冻状态

## 参考资料索引

| 文件 | 内容 |
|-----|------|
| [订单状态流转.md](../hj-joinpay-aggregation-base/references/订单状态流转.md) | 订单状态定义、关单规则 |
| [错误码.md](../hj-joinpay-aggregation-base/references/错误码.md) | TOP 20 错误码速查 |
| [响应码完整列表.md](../hj-joinpay-aggregation-base/references/响应码完整列表.md) | 完整响应码（含退款专用） |
| [基础支付常见问题.md](../hj-joinpay-aggregation-order/references/基础支付常见问题.md) | 20 个 FAQ |
| [signing-rules.md](../hj-joinpay-pay-shared-base/protocol/signing-rules.md) | 签名规则 |
