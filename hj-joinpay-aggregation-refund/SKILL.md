---
name: hj-joinpay-aggregation-refund
display_name: 汇聚支付聚合支付退款
description: "汇聚支付聚合支付退款 Skill：退款申请、退款查询、退款信息查询。当开发者需要发起退款或查询退款状态时使用。触发词：退款申请、退款查询、refund、queryRefund、queryRefundInfo。"
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
        - JOINPAY_NOTIFY_URL
---

# 聚合支付 - 退款

覆盖退款申请、退款查询、退款信息查询。

---

## 适配版本与定位

| 项目 | 内容 |
|------|------|
| Skill 版本 | `1.1.0` |
| 定位 | 聚合支付退款 |
| 适用范围 | 退款申请、退款查询、退款信息查询 |

## 前置依赖

| 项目 | 引用 |
|------|------|
| **凭据收集**（代码生成前必须完成） | [credential-collection.md](../hj-payment-integration/shared-rules/credential-collection.md) （退款需要 merchant_no + 密钥 + notify_url） |
| **签名硬约束** | [signing-constraints.md](../hj-payment-integration/shared-rules/signing-constraints.md) |
| **接口稳定性** | [interface-stability.md](../hj-payment-integration/shared-rules/interface-stability.md) |

> 以下场景**不需要收集参数**：仅咨询业务知识、仅查看示例代码、参数已收集过。

---

## 接口一：退款申请

### 接口概述

| 项目 | 内容 |
|------|------|
| 接口名称 | 退款申请 |
| 请求方式 | HTTP POST (form-urlencoded) |
| 接口路径 | `/tradeRt/refund` |
| 完整测试URL | `https://trade.joinpay.cc/tradeRt/refund` |
| p0_Version | `2.3` |
| **响应格式** | **JSON (application/json)** | ⚠️ 与请求格式不同！请求用form，响应收JSON |

### 请求参数

| 参数 | 必填 | 说明 | 示例 |
|------|------|------|------|
| p0_Version | 是 | 接口版本，固定 `2.3` | `2.3` |
| p1_MerchantNo | 是 | 商户号 | `888100500008456` |
| p2_OrderNo | 是 | 原支付订单号 | `ORDER20260413001` |
| p3_RefundOrderNo | 是 | 退款订单号（全局唯一） | `REF_ORDER20260413001` |
| p4_RefundAmount | 是 | 退款金额（元），精确到分 | `10.50` |
| p5_RefundReason | 是 | 退款原因 | `用户申请退款` |
| p6_NotifyUrl | **是** | 退款结果通知地址 | `https://your-domain.com/refund-notify` |
| hmac | 是 | 签名 | — |

### 响应参数

| 参数 | 说明 |
|------|------|
| ra_Code | 响应码（`100`=成功） |
| ra_Status | 退款状态（`100`=成功, `101`=失败, `102`=处理中） |
| rb_CodeMsg | 响应描述 |
| hmac | 签名 |

### 退款业务规则

#### 金额规则

```
原订单金额: 100.00 元
    │
    ├─ 第一次退款: 30.00 元  ✓  剩余可退: 70.00
    ├─ 第二次退款: 50.00 元  ✓  剩余可退: 20.00
    ├─ 第三次退款: 20.00 元  ✓  剩余可退: 0.00（全额已退完）
    └─ 第四次退款: 10.00 元  ✗  错误: 退款金额超出可退金额(20090004)
```

- **最小退款金额**: 0.01 元
- **退款金额为0**: 返回 `20090011`
- **退款金额 > 原订单金额**: 返回 `20090003`
- **累计退款 > 原订单金额**: 返回 `20090004`

#### 时间规则

| 场景 | 时效 |
|------|------|
| 部分退款 | 支付后 **365天内** 可发起 |
| 全额退款 | 支付后 **365天内** 可发起 |
| 退款到账 | 1~3 个工作日（信用卡可能更长） |

#### 状态规则

| 原订单状态 | 能否退款 | 说明 | 返回错误码 |
|-----------|---------|------|----------|
| SUCCESS（已支付） | ✅ 可以 | 正常情况 | — |
| NOTPAY（待支付） | ❌ 不行 | 订单未支付 | `20090002` |
| CLOSED（已关闭） | ❌ 不行 | 订单未支付 | `20090002` |
| FULLY_REFUNDED（已全额退） | ❌ 不行 | 已全额退款 | `20090012` |

#### 退款订单号规则

- 必须**全局唯一**
- 不能与已有支付订单号或退款订单号重复
- 重复会返回 `20090007` 或 `20090030`
- 建议格式：`REF_{原订单号}_{时间戳}` 或 `RF{yyyyMMddHHmmssSSS}{随机数}`

### 分账退款

如果原订单使用了**实时分账**功能，退款时需同步处理：

| 参数 | 必填 | 说明 |
|------|------|------|
| p7_AltRefInfo | 否(分账时必填) | 分账退款信息(JSON) |
| p9_AltOrderNo | 否 | 分账订单号 |
| pa_FundsAccount | 否 | 退款资金账户 |

---

## 接口二：退款查询

### 接口概述

| 项目 | 内容 |
|------|------|
| 接口名称 | 退款查询 |
| 请求方式 | HTTP POST (form-urlencoded) |
| 接口路径 | `/tradeRt/queryRefund` |
| 完整测试URL | `https://trade.joinpay.cc/tradeRt/queryRefund` |
| p0_Version | `2.3` |

### 请求参数

| 参数 | 必填 | 说明 | 示例 |
|------|------|------|------|
| p0_Version | 是 | 接口版本，固定 `2.3` | `2.3` |
| p1_MerchantNo | 是 | 商户号 | `888100500008456` |
| p2_RefundOrderNo | 是 | 退款订单号 | `REF_ORDER20260413001` |
| hmac | 是 | 签名 | — |

### 响应参数

> [P1] 此接口响应为 **JSON 格式**，不是 form 格式！
>
> 如果按 form 格式解析 JSON 响应会导致所有字段值为 `null`。
> 解析策略：检查响应正文首个非空白字符是否为 `{`，若是则用 JSON 解析器处理。

| 参数 | 说明 |
|------|------|
| ra_Code | 响应码 |
| ra_Status | 退款状态（`100`=成功, `101`=失败, `102`=处理中） |
| rb_CodeMsg | 响应描述 |
| hmac | 签名 |

### ra_Status 退款状态判断

| ra_Status | 含义 | rb_Code |
|-----------|------|---------|
| `100` | 退款成功 | `100` |
| `102` | 退款处理中 | `100` |
| `101` | 退款失败 | 具体业务错误码 |

```java
// 判断逻辑
String status = response.getString("ra_Status");
String code = response.getString("rb_Code");
if ("100".equals(status)) { /* 退款成功 */ }
else if ("102".equals(status)) { /* 退款处理中 */ }
else if ("101".equals(status)) { /* 退款失败，code 为具体错误码 */ }
```

---

## 接口三：退款信息查询

### 接口概述

| 项目 | 内容 |
|------|------|
| 接口名称 | 退款信息查询 |
| 请求方式 | HTTP POST (form-urlencoded) |
| 接口路径 | `/tradeRt/queryRefundInfo` |
| 完整测试URL | `https://trade.joinpay.cc/tradeRt/queryRefundInfo` |
| p0_Version | `2.3` |

### 请求参数

| 参数 | 必填 | 说明 | 示例 |
|------|------|------|------|
| p0_Version | 是 | 接口版本，固定 `2.3` | `2.3` |
| p1_MerchantNo | 是 | 商户号 | `888100500008456` |
| p2_OrderNo | 是 | 原支付订单号 | `ORDER20260413001` |
| hmac | 是 | 签名 | — |

### 与退款查询的区别

| 方式 | 接口 | 输入 | 输出 |
|------|------|------|------|
| 按退款单号查 | queryRefund | p2_RefundOrderNo | 单笔退款详情（金额/状态/时间） |
| 按支付单号查 | queryRefundInfo | p2_OrderNo | 该订单所有退款记录列表 |

**推荐**：日常用 `queryRefund` 追踪单笔退款；对账时用 `queryRefundInfo` 检查某订单全部退款。

## 常见退款失败错误码

| 响应码 | 含义 | 解决方案 |
|--------|------|---------|
| 20090001 | 支付成功记录不存在 | 检查原支付订单号 p2_OrderNo |
| 20090012 | 退款订单号已存在 | 更换退款订单号 |
| 20090020 | 支付订单剩余可退金额不足 | 检查累计退款金额 |
| 20090023 | 可用余额不足 | 商户账户余额问题 |
| 10080002 | 验证签名失败 | 检查签名算法和密钥 |
| JS999999 | 其他原因导致交易失败 | 联系汇聚客服 |
| JS002009 | 请求通道退款异常 | 渠道问题，稍后重试 |
| CR000007 | 该订单已完成退款 | 无需重复退款 |
| CR000009 | 用户账户异常或已注销 | 不能原路退回 |

> 完整响应码 → [错误码.md](../hj-joinpay-aggregation-base/references/错误码.md)

## 参考资料索引

| 文件 | 内容 |
|-----|------|
| [订单状态流转.md](../hj-joinpay-aggregation-base/references/订单状态流转.md) | 订单状态定义、退款规则 |
| [错误码.md](../hj-joinpay-aggregation-base/references/错误码.md) | TOP 20 错误码速查 |
| [响应码完整列表.md](../hj-joinpay-aggregation-base/references/响应码完整列表.md) | 完整响应码（含退款专用） |
| [基础支付常见问题.md](../hj-joinpay-aggregation-order/references/基础支付常见问题.md) | 20 个 FAQ（含退款相关） |
| [signing-rules.md](../hj-joinpay-pay-shared-base/protocol/signing-rules.md) | 签名规则 |
