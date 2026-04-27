---
name: hj-payment-skills
display_name: 汇聚支付 Skill 集合
description: "汇聚支付 Skill 集合入口。用于一次性导入整个 hj-payment-skills 目录，并在聚合支付接入、下单、查询、退款、签名规则、异步通知等场景中路由到对应子 Skill。"
version: 1.1.0
author: "hj-payment-skills"
homepage: https://www.joinpay.com
license: MIT
compatibility:
  - skillhub
dependencies:
  - hj-payment-integration
  - hj-joinpay-pay-shared-base
  - hj-joinpay-aggregation-base
  - hj-joinpay-aggregation-order
  - hj-joinpay-aggregation-query
  - hj-joinpay-aggregation-refund
metadata:
  skillhub:
    requires:
      config: []
---

# 汇聚支付 Skill 集合

这是 `hj-payment-skills` 仓库的根入口，用于让导入器在选择父目录时也能识别到 `SKILL.md`。

优先使用 [hj-payment-integration](./hj-payment-integration/SKILL.md) 作为总入口，再根据用户意图进入对应子 Skill。

## 子 Skill 路由

| 用户意图 | 推荐入口 |
|---------|---------|
| 第一次接入汇聚支付、不知道从哪里开始 | [hj-payment-integration](./hj-payment-integration/SKILL.md) |
| 签名规则、异步通知、多语言 SDK、发布检查 | [hj-joinpay-pay-shared-base](./hj-joinpay-pay-shared-base/SKILL.md) |
| 聚合支付初始化、公共参数、支付类型、FrpCode | [hj-joinpay-aggregation-base](./hj-joinpay-aggregation-base/SKILL.md) |
| 微信、支付宝、银联下单，二维码支付，付款码支付 | [hj-joinpay-aggregation-order](./hj-joinpay-aggregation-order/SKILL.md) |
| 订单查询、关闭订单、资金管控订单查询 | [hj-joinpay-aggregation-query](./hj-joinpay-aggregation-query/SKILL.md) |
| 退款申请、退款查询、退款信息查询 | [hj-joinpay-aggregation-refund](./hj-joinpay-aggregation-refund/SKILL.md) |

## 使用规则

1. 用户意图不明确时，先进入 `hj-payment-integration` 判断场景。
2. 涉及实际 API 调用或代码生成前，必须先确认签名方式：MD5 或 RSA。
3. 涉及代码生成、接口调用或线上环境操作前，先完成必要参数收集与用户确认。
4. 业务字段、参数命名、签名规则和接口地址以对应子 Skill 的 `SKILL.md` 与 `references/` 为准。

