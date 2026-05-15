---
name: hj-joinpay-secondary-mch
display_name: 汇聚二级商户入网
description: "汇聚支付二级商户入网 Skill：覆盖平台二级商户新增、存量升级、修改、查询、资质图片上传、签约发起、签约撤销和签约查询。适用于 secondaryMch.create、secondaryMch.upgrade、secondaryMch.modify、secondaryMch.query、secondaryMchPics.uploadPic、secondaryMchSign.create 等 API Gateway JSON 协议接口。触发词：二级商户入网、平台二级商户、二级商户图片上传、二级商户签约、secondaryMch。"
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
        - JOINPAY_RSA_PRIVATE_KEY
        - JOINPAY_RSA_PUBLIC_KEY
        - JOINPAY_PLATFORM_PUBLIC_KEY
        - JOINPAY_NOTIFY_URL
---

# 汇聚二级商户入网

本 Skill 覆盖二级商户入网相关接口，使用 API Gateway JSON 协议：`POST /altFunds` + `method` 分发。

## 关键边界

二级商户接口不得使用聚合支付 `/tradeRt/*` 的交易签名规则。它使用 `method/version/data/rand_str/sign_type/mch_no/sign/sec_key` 公共参数，签名串为 `key=value&key=value` 格式。

聚合支付接口也不得使用本 Skill 的 `sign/sec_key/data` 协议。

## 前置依赖

| 项目 | 引用 |
|------|------|
| 二级商户凭据收集 | [credential-collection-secondary-mch.md](../hj-payment-integration/shared-rules/credential-collection-secondary-mch.md) |
| 协议族索引 | [protocol-index.md](../hj-joinpay-pay-shared-base/protocol/protocol-index.md) |
| API Gateway 签名与加密 | [api-gateway-signing-rules.md](../hj-joinpay-pay-shared-base/protocol/api-gateway-signing-rules.md) |
| 接口稳定性 | [interface-stability.md](../hj-payment-integration/shared-rules/interface-stability.md) |
| 异步通知 | [async-notify.md](../hj-joinpay-pay-shared-base/protocol/async-notify.md) |

## 适配版本与定位

| 项目 | 内容 |
|------|------|
| Skill 版本 | `1.1.0` |
| 定位 | 二级商户入网 / API Gateway JSON 协议 |
| 适用范围 | 二级商户新增、存量升级、修改、查询、图片、签约 |
| 不承担 | 聚合支付下单、退款、查询、关单 |

## 阅读顺序

| 你要解决什么 | 先看哪里 |
|-------------|---------|
| 判断接口和流程 | [接口总览.md](references/接口总览.md) |
| 写加签、验签、敏感字段加密 | [网关签名与加密.md](references/网关签名与加密.md) |
| 确认字段结构和敏感字段 | [字段与敏感信息.md](references/字段与敏感信息.md) |
| 处理审核、商户、签约状态和回调 | [状态与通知.md](references/状态与通知.md) |
| 查看 Java/PHP/Python/Go 示例 | [示例代码索引.md](references/示例代码/接口索引.md) |

## 快速路由

| 用户意图 | method | 资料 |
|---------|--------|------|
| 新二级商户入网 | `secondaryMch.create` | 接口总览 + 字段与敏感信息 |
| 分账方升级二级商户 | `secondaryMch.upgrade` | 接口总览 + 字段与敏感信息 |
| 修改二级商户资料或结算卡 | `secondaryMch.modify` | 接口总览 + 状态与通知 |
| 查询申请单或二级商户 | `secondaryMch.query` | 接口总览 + 状态与通知 |
| 上传资质图片 | `secondaryMchPics.uploadPic` | 接口总览 + 字段与敏感信息 |
| 发起、撤销、查询签约 | `secondaryMchSign.*` | 接口总览 + 状态与通知 |

## 触发样例与反例

应该触发本 Skill：

- 接入 `secondaryMch.create` 二级商户入网
- 平台二级商户图片上传
- 二级商户签约、撤销签约或签约查询
- 分账方升级为二级商户

不应触发本 Skill：

- 微信扫码支付、公众号支付、小程序支付
- 聚合支付退款、关单、订单查询
- `/tradeRt/*` 接口签名失败
- 报备商户号或 FrpCode 选型

## 实现原则

1. 先确认用户是二级商户接口，而不是聚合支付交易接口。
2. 涉及代码生成时，优先从 [网关签名与加密.md](references/网关签名与加密.md) 复制协议流程。
3. 需要示例代码时，按语言读取 [示例代码索引.md](references/示例代码/接口索引.md) 下的对应文件。
4. 不在示例中写真实手机号、身份证号、银行卡号、商户私钥、平台公钥或内网测试地址。
5. `data` 必须使用最终上送的紧凑 JSON 字符串参与签名，签名后不得再重新序列化。
