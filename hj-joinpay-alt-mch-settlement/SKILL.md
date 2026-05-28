---
name: hj-joinpay-alt-mch-settlement
display_name: 汇聚分账方入网与结算
description: "汇聚支付分账方入网与结算 Skill：覆盖分账方添加、修改、查询、资质图片上传、协议签约、手工结算、结算查询、自动结算查询和账户余额查询。适用于 altmch.create、altmch.modify、altmch.query、altMchPics.*、altMchSign.*、altSettle.launch、altSettle.get、altSettle.getAutoSettle、altAccount.get 等 /allocFunds 接口。触发词：分账方入网、分账方结算、分账方签约、分账方图片、altmch、altSettle、altAccount。"
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

# 汇聚分账方入网与结算

本 Skill 覆盖汇聚支付分账方从入网到可结算的接口链路，使用 `/allocFunds` JSON 协议和 `altmch.*`、`altMchPics.*`、`altMchSign.*`、`altSettle.*`、`altAccount.*` method 分发。

## 关键边界

分账方入网与结算不是聚合支付 `/tradeRt/*`，也不是二级商户 `/altFunds`。它与延迟分账/多次分账同属 `/allocFunds` JSON 协议，但 method 族不同；公共参数为 `method/version/data/rand_str/sign_type/mch_no/sign`，签名串为 `key=value&key=value` 格式。

本 Skill 处理“分账方资料、认证、图片、签约、结算、账户余额”。订单维度的延迟分账/多次分账执行、完结分账和分账查询仍进入 [hj-joinpay-many-allocate](../hj-joinpay-many-allocate/)。

## 前置依赖

| 项目 | 引用 |
|------|------|
| 分账方 `/allocFunds` 签名规则 | [签名规则.md](references/签名规则.md) |
| 协议族索引 | [protocol-index.md](../hj-joinpay-pay-shared-base/protocol/protocol-index.md) |
| 协议与版本矩阵 | [protocol-version-matrix.md](../hj-payment-integration/shared-rules/protocol-version-matrix.md) |
| 回调与幂等 | [callback-idempotency.md](../hj-payment-integration/shared-rules/callback-idempotency.md) |

## 适配版本与定位

| 项目 | 内容 |
|------|------|
| Skill 版本 | `1.1.0` |
| 定位 | 分账方入网 / 资质图片 / 协议签约 / 分账方结算 |
| 适用范围 | 分账方添加、修改、查询、图片上传/修改/查询、协议内容签约、页面签约、结算、结算查询、账户查询 |
| 不承担 | 聚合支付下单、退款、二级商户入网、订单延迟分账/多次分账执行 |

## 阅读顺序

| 你要解决什么 | 先看哪里 |
|-------------|---------|
| 判断接口、method 和公共参数 | [接口总览.md](references/接口总览.md) |
| 写加签、验签、排查签名错误 | [签名规则.md](references/签名规则.md) |
| 梳理从入网到可结算全流程 | [入网到可结算流程.md](references/入网到可结算流程.md) |
| 对接结算、结算查询或账户余额查询 | [结算与账户查询.md](references/结算与账户查询.md) |
| 处理认证、图片审核、签约、结算通知 | [状态与通知.md](references/状态与通知.md) |
| 查响应码、限制和排障建议 | [错误码与排障.md](references/错误码与排障.md) |
| 查看 Java/PHP/Python/Go 示例 | [示例代码索引.md](references/示例代码/接口索引.md) |

## 快速路由

| 用户意图 | method | 资料 |
|---------|--------|------|
| 添加分账方 | `altmch.create` | 接口总览 + 入网到可结算流程 |
| 修改分账方资料 | `altmch.modify` | 入网到可结算流程 + 状态与通知 |
| 查询分账方 | `altmch.query` | 接口总览 + 状态与通知 |
| 上传、修改、查询资质图片 | `altMchPics.uploadPic`、`altMchPics.modifyPic`、`altMchPics.queryPicsInfo` | 接口总览 + 入网到可结算流程 |
| 内容签约 | `altMchSign.querySignContent`、`altMchSign.sign` | 接口总览 + 入网到可结算流程 |
| 页面签约 | `altMchSign.getSignUrl` | 接口总览 + 状态与通知 |
| 查询签约状态 | `altMchSign.querySignRecord` | 接口总览 + 入网到可结算流程 |
| 发起手工结算 | `altSettle.launch` | 结算与账户查询 + 状态与通知 |
| 查询结算结果 | `altSettle.get`、`altSettle.getAutoSettle` | 结算与账户查询 |
| 查询分账方账户余额 | `altAccount.get` | 结算与账户查询 |

## 触发样例与反例

应该触发本 Skill：

- 分账方入网、分账方添加、分账方认证
- 分账方资质图片上传、图片审核、分账方协议签约
- 分账方手工结算、自动结算查询、账户余额查询
- `altmch.create`、`altMchPics.uploadPic`、`altMchSign.getSignUrl`
- `altMchSign.querySignRecord`
- `altSettle.launch`、`altSettle.getAutoSettle`、`altAccount.get`

不应触发本 Skill：

- 微信扫码支付、公众号支付、小程序支付
- 聚合支付退款、关单、订单查询
- 二级商户 `secondaryMch.*` 入网
- 订单延迟分账/多次分账 `altHandle.*` 执行、完结和查询

## 实现原则

1. 先确认用户是在做“分账方”入网/结算，而不是“二级商户”入网或订单“延迟分账/多次分账”执行。
2. 入网到可结算必须完成三项：分账方信息添加并打款认证、资质图片上传并审核、协议签约。
3. 涉及代码生成时，签名逻辑优先引用 [签名规则.md](references/签名规则.md)；`data` 必须使用最终上送的紧凑 JSON 原文参与签名。
4. 金额字段统一按字符串两位小数处理；`settle_fee` 不得超过结算金额的 1%。
5. 异步通知必须验签和幂等；只有本地可靠落库后再返回 `{"resp_code":"A1000","resp_msg":"success"}`。
6. 需要示例代码时，按语言读取 [示例代码索引.md](references/示例代码/接口索引.md) 下的对应文件。
