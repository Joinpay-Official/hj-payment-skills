---
name: hj-joinpay-aggregation-order
display_name: 汇聚支付聚合支付下单
description: "汇聚支付聚合支付下单 Skill：覆盖微信公众号/小程序/APP、支付宝 JS / 正扫、银联 JS / 正扫、微信 / 支付宝 / 银联付款码等全场景聚合支付下单。参数表和业务规则按协议字段组织。当开发者需要创建聚合支付订单时使用。触发词：聚合支付下单、微信支付下单、支付宝下单、银联下单、二维码支付、付款码支付、uniPay。"
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

# 聚合支付 - 统一下单

覆盖微信/支付宝/银联全场景聚合支付下单。接口路径：`/tradeRt/uniPay`

---

## 前置依赖

| 项目 | 引用 |
|------|------|
| **凭据收集**（代码生成前必须完成） | [credential-collection.md](../hj-payment-integration/shared-rules/credential-collection.md) |
| **签名硬约束** | [signing-constraints.md](../hj-payment-integration/shared-rules/signing-constraints.md) |
| **接口稳定性** | [interface-stability.md](../hj-payment-integration/shared-rules/interface-stability.md) |

---

## [P0] 硬约束速查

以下约束违反将导致接口调用失败：

| # | 约束 | 详情 |
|---|------|------|
| 1 | **签名算法必须严格按规范** | 只拼接 value 不含 key；MD5 整体哈希；RSA 用 MD5withRSA → 见 [signing-constraints.md](../hj-payment-integration/shared-rules/signing-constraints.md) |
| 2 | **rc_Result 必须持久化三步流程** | 存入 qrCodeUrl → 更新状态 PAYING → 传给前端 → 见 [implementation-notes.md §3](references/implementation-notes.md#3-p0-rc_result-必须持久化并传递给前端) |
| 3 | **响应必须按 JSON 解析** | 请求 form-urlencoded，响应 JSON！检查首字符 `{` → 见 [implementation-notes.md §4](references/implementation-notes.md#4-响应解析json-格式) |
| 4 | **微信主扫前端 th:if 控制** | 用 th:if 控制元素存在性，CSS 禁止 display:none → 见 [implementation-notes.md §2](references/implementation-notes.md#2-p0-微信主扫前端必须用-thymeleaf-thif-控制显示) |
| 5 | **Thymeleaf 内联 JS 两个陷阱** | 必须加 `th:inline="javascript"`；`[[${str}]]` 自动加引号禁止手动再套引号 → 见 [implementation-notes.md §6](references/implementation-notes.md#6-p0-thymeleaf-内联-js-的两个致命陷阱) |

完整代码级实现细节（SignUtils.java / parseResponse / Thymeleaf 模板 / CSS 避坑）→ [implementation-notes.md](references/implementation-notes.md)

---

## 接口概述

| 项目 | 内容 |
|------|------|
| 接口名称 | 统一支付下单 |
| 请求方式 | HTTP POST (form-urlencoded) |
| 接口路径 | `/tradeRt/uniPay` |
| 完整测试URL | `https://trade.joinpay.cc/tradeRt/uniPay` |
| 完整生产URL | `https://trade.joinpay.com/tradeRt/uniPay` |
| p0_Version | `2.6` |
| 签名字段 | `hmac` |
| 签名方式 | MD5 或 RSA（商户后台配置决定） |
| **响应格式** | **JSON (application/json)** | ⚠️ 与请求格式不同！请求用 form，响应收 JSON |

> [P1] JSON 响应解析策略：检查响应正文首个非空白字符是否为 `{`：
> - 以 `{` 开头 → 用 JSON 解析器处理
> - 其他情况 → 按 form 格式降级处理

---

## 请求参数

### 基础必填参数

| 参数 | 必填 | 说明 | 示例 |
|------|------|------|------|
| p0_Version | 是 | 接口版本，固定 `2.6` | `2.6` |
| p1_MerchantNo | 是 | 商户号 | `888100500008456` |
| p2_OrderNo | 是 | 商户订单号，全局唯一 | `ORDER20260413001` |
| p3_Amount | 是 | 订单金额（元），两位小数 | `0.01` |
| p4_Cur | 是 | 币种，固定 `1`（人民币） | `1` |
| p5_ProductName | 是 | 商品名称 | `测试商品` |
| q1_FrpCode | 是 | 交易类型（FrpCode） | `WEIXIN_NATIVE` |
| p9_NotifyUrl | 是 | 异步回调通知地址 | `https://your-domain.com/notify` |
| hmac | 是 | 签名 | — |

### 渠道扩展参数

| 参数 | 适用场景 | 说明 |
|------|---------|------|
| qa_TradeMerchantNo | 通用（必填） | 报备商户号（在商户后台「报备管理」中获取） |
| q3_SubMerchantNo | 多子商户模式 | 子商户号 |
| q4_IsShowPic | WEIXIN_NATIVE | 设为1时返回 rd_Pic(base64图片，⚠️ 可能含换行符裂图，推荐用 rc_Result+前端二维码库） |
| q5_OpenId | WEIXIN_GZH / WEIXIN_XCX | 用户在当前 AppId 下的唯一标识 |
| q6_AuthCode | WEIXIN_CARD / ALIPAY_CARD / UNIONPAY_CARD | 用户付款码 |
| q7_AppId | WEIXIN_GZH / WEIXIN_XCX / WEIXIN_APP / WEIXIN_APP3 / WEIXIN_H5_PLUS | 微信 AppId |
| q8_TerminalNo | 被扫(CARD) | 终端设备号 |
| ql_TerminalIp | 被扫(CARD) | 终端 IP 地址 |
| qb_buyerId | ALIPAY_FWC | 支付宝买家ID(2088开头) |
| q9_TransactionModel | ALIPAY_H5 | 支付宝H5交易模式(MODEL1/2/3) |
| qm_ContractId | UNIONPAY_WXMP | 银联云微小程序签约ID |
| p6_ProductDesc | 可选 | 商品描述(最长300字符) |
| p7_Mp | 可选 | 公用回传参数 |
| p8_ReturnUrl | UNIONPAY_H5等 | 页面跳转地址 |
| qh_HbFqNum | ALIPAY | 花呗分期数 |
| qj_DJPlan | WEIXIN | 点金计划JSON |
| qn_SpecialInfo | 通用 | 特殊支付参数JSON |

## 响应参数

| 参数 | 说明 |
|------|------|
| ra_Code | 响应码（`100`=成功） |
| rb_CodeMsg | 响应描述 |
| rc_Result | 结果数据（根据支付方式不同：二维码URL / JSAPI参数等） |
| rd_Pic | 二维码图片base64（q4_IsShowPic=1时，⚠️ 可能含换行符裂图，推荐用 rc_Result+前端库代替） |
| hmac | 签名 |

---

## 各场景下单要点

### 微信主扫 (WEIXIN_NATIVE)

```
必填：p1/p2/p3/p4/p5/q1=WEIXIN_NATIVE/p9/hmac
可选：q4_IsShowPic=1（返回rd_Pic）、q7_AppId（可返回openid）

响应：rc_Result = 支付二维码 URL 链接

═══ Web 商场完整实现模式 ═══

后端4步骤 + 前端三态流转 + 回调闭环
→ 详见 implementation-notes.md §5「微信主扫 Web 完整实现模式」
   含：parseResponse 代码、三态流转说明、Thymeleaf 变量输出、CDN 备份建议

避坑：不要直接用 rd_Pic 的 base64 拼 <img>（可能裂图）
      不要用第三方在线 QR API（可能被墙或超时）
```

### 微信公众号 (WEIXIN_GZH)

```
必填：p1/p2/p3/p4/p5/q1=WEIXIN_GZH/p9/hmac + q5_OpenId + q7_AppId
响应：rc_Result 中包含 JSAPI 调起支付所需参数
注意：OpenId 必须通过微信 OAuth 授权获取
```

### 微信小程序 (WEIXIN_XCX)

```
必填：p1/p2/p3/p4/p5/q1=WEIXIN_XCX/p9/hmac + q5_OpenId + q7_AppId
响应：rc_Result 中包含 wx.requestPayment 所需参数
注意：OpenId 必须通过小程序 wx.login 获取
```

### 被扫/刷卡 (WEIXIN_CARD / ALIPAY_CARD / UNIONPAY_CARD)

```
必填：p1/p2/p3/p4/p5/q1={CHANNEL}_CARD/p9/hmac + q6_AuthCode + q8_TerminalNo + ql_TerminalIp
响应：ra_Code=100 表示支付成功
注意：AuthCode 是用户展示的18位数字付款码
```

### H5支付 (WEIXIN_H5 / ALIPAY_H5 / UNIONPAY_H5)

```
必填：p1/p2/p3/p4/p5/q1={CHANNEL}_H5/p9/hmac
响应：rc_Result 中包含跳转支付页面的 URL
```

## 参考资料索引

| 文件 | 内容 |
|-----|------|
| [implementation-notes.md](references/implementation-notes.md) | **P0 级约束的完整展开**（签名代码/Thymeleaf/rc_Result持久化/JSON解析/Web实现模式） |
| [signing-constraints.md](../hj-payment-integration/shared-rules/signing-constraints.md) | MD5/RSA 签名硬约束 + SignUtils.java 完整代码 |
| [interface-stability.md](../hj-payment-integration/shared-rules/interface-stability.md) | 接口地址固定值、参数命名规范 |
| [支付渠道与交易类型对比.md](../hj-joinpay-aggregation-base/references/支付渠道与交易类型对比.md) | 完整 FrpCode 映射表 |
| [开发必要参数说明.md](../hj-joinpay-aggregation-base/references/开发必要参数说明.md) | 各渠道参数获取方式 |
| [参数命名规范与固定值.md](../hj-joinpay-aggregation-base/references/参数命名规范与固定值.md) | 参数固定值约束 |
| [响应码完整列表.md](../hj-joinpay-aggregation-base/references/响应码完整列表.md) | 完整响应码（含退款专用） |
| [signing-rules.md](../hj-joinpay-pay-shared-base/protocol/signing-rules.md) | 签名规则 |
| [async-notify.md](../hj-joinpay-pay-shared-base/protocol/async-notify.md) | 异步通知规则 |
| [排障手册.md](references/排障手册.md) | 签名失败、回调地址、渠道错误排障 |
| [下单参数速查.md](references/下单参数速查.md) | 按 FrpCode 分类的参数速查表 |
| [基础支付常见问题.md](references/基础支付常见问题.md) | 20 个 FAQ（签名/下单/回调/退款） |
