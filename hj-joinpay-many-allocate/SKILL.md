---
name: hj-joinpay-many-allocate
display_name: 汇聚延迟分账与多次分账
description: "汇聚支付延迟分账与多次分账 Skill：覆盖单次延迟分账请求、延迟分账查询、多次分账请求、完结分账、查询单笔分账、查询所有分账，以及金额规则、异步通知、状态与响应码。适用于 altHandle.singleLaterAllocate、altHandle.allocateQuery、altHandle.manyLaterAllocate、altHandle.finishAllocate、altHandle.altManyOrderQuery、altHandle.altManyTotalQuery 等 /allocFunds 接口。触发词：延迟分账、单次延迟分账、分账查询、多次分账、完结分账、altHandle、singleLaterAllocate、allocateQuery。"
version: 1.1.0
author: "hj-payment-skills"
homepage: https://www.joinpay.com
license: MIT
compatibility:
  - skillhub
dependencies:
  - hj-joinpay-pay-shared-base
metadata:
  skillhub:
    requires:
      config:
        - JOINPAY_MERCHANT_NO
        - JOINPAY_MERCHANT_KEY
        - JOINPAY_RSA_PRIVATE_KEY
        - JOINPAY_RSA_PUBLIC_KEY
        - JOINPAY_PLATFORM_PUBLIC_KEY
        - JOINPAY_NOTIFY_URL
---

# 汇聚延迟分账与多次分账

本 Skill 覆盖汇聚支付延迟分账与多次分账接口，使用独立的 JSON 协议：`POST /allocFunds` + `altHandle.*` method 分发。

## 关键边界

延迟分账/多次分账接口不是聚合支付 `/tradeRt/*`，也不是二级商户 `/altFunds`。它使用独立的 `method/version/data/rand_str/sign_type/mch_no/sign` 协议，签名串为 `key=value&key=value` 格式。

二级商户 `/altFunds` 常见 `sec_key` 敏感字段加密；延迟分账和多次分账文档当前未定义 `sec_key` 为必传字段，不要混用。

## 前置依赖

| 项目 | 引用 |
|------|------|
| 延迟分账/多次分账凭据收集 | [credential-collection-many-allocate.md](../hj-payment-integration/shared-rules/credential-collection-many-allocate.md) |
| 协议族索引 | [protocol-index.md](../hj-joinpay-pay-shared-base/protocol/protocol-index.md) |
| 延迟分账/多次分账签名规则 | [many-allocate-signing-rules.md](../hj-joinpay-pay-shared-base/protocol/many-allocate-signing-rules.md) |
| 接口稳定性 | [interface-stability.md](../hj-payment-integration/shared-rules/interface-stability.md) |
| 回调与幂等 | [callback-idempotency.md](../hj-payment-integration/shared-rules/callback-idempotency.md) |

## 适配版本与定位

| 项目 | 内容 |
|------|------|
| Skill 版本 | `1.1.0` |
| 定位 | 延迟分账 / 多次分账 / `/allocFunds` |
| 适用范围 | 单次延迟分账、延迟分账查询、多次分账、完结分账、单笔查询、全部查询 |
| 不承担 | 聚合支付下单、退款、二级商户入网 |

## 阅读顺序

| 你要解决什么 | 先看哪里 |
|-------------|---------|
| 判断接口和流程 | [接口总览.md](references/接口总览.md) |
| 判断单次延迟分账字段 | [延迟分账操作指南.md](references/延迟分账操作指南.md) |
| 计算本次分账总额、净额和手续费 | [金额规则与分账模式.md](references/金额规则与分账模式.md) |
| 处理同步受理、异步通知和状态流转 | [状态与通知.md](references/状态与通知.md) |
| 查看 Java/Python/Go/PHP 示例 | [示例代码接口索引](references/示例代码/接口索引.md) |
| 看响应码和排障建议 | [错误码与排障.md](references/错误码与排障.md) |

## 快速路由

| 用户意图 | method | 资料 |
|---------|--------|------|
| 发起单次延迟分账 | `altHandle.singleLaterAllocate` | 延迟分账操作指南 + 状态与通知 |
| 查询延迟分账结果 | `altHandle.allocateQuery` | 延迟分账操作指南 + 状态与通知 |
| 发起多次分账 | `altHandle.manyLaterAllocate` | 接口总览 + 金额规则与分账模式 |
| 完结分账 | `altHandle.finishAllocate` | 接口总览 + 状态与通知 |
| 查询单笔分账 | `altHandle.altManyOrderQuery` | 接口总览 + 状态与通知 |
| 查询订单所有分账 | `altHandle.altManyTotalQuery` | 接口总览 + 金额规则与分账模式 |

## 触发样例与反例

应该触发本 Skill：

- 多次分账
- 延迟分账
- 单次延迟分账
- 完结分账
- 查询所有分账
- `altHandle.singleLaterAllocate`
- `altHandle.allocateQuery`
- `altHandle.manyLaterAllocate`
- `altHandle.finishAllocate`
- `altHandle.altManyOrderQuery`
- `altHandle.altManyTotalQuery`

不应触发本 Skill：

- 微信扫码支付、公众号支付、小程序支付
- 聚合支付退款、关单、订单查询
- 二级商户入网、图片上传、签约
- `/tradeRt/*` 接口签名失败

## 实现原则

1. 先确认用户在做延迟分账/多次分账，而不是聚合支付交易或二级商户入网。
2. 涉及代码生成时，优先从 [many-allocate-signing-rules.md](../hj-joinpay-pay-shared-base/protocol/many-allocate-signing-rules.md) 复制协议流程。
3. 金额字段统一按**字符串两位小数**处理，不要用浮点直接累加。
4. `alt_order_no` 每次分账请求必须唯一；同一支付订单的多次分账要按业务顺序维护本地状态。
5. 异步通知必须做幂等，且不能让旧状态覆盖终态。
