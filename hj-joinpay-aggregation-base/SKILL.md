---
name: hj-joinpay-aggregation-base
display_name: 汇聚支付聚合支付基础
description: "汇聚支付聚合支付基础 Skill：公共参数、MD5/RSA 签名方式、支付渠道选型、参数命名规范和错误码。当开发者首次接入汇聚聚合支付或需要了解公共配置时使用。所有聚合支付 Skill 的前置依赖。触发词：聚合支付接入、JoinPay SDK、汇聚支付初始化、支付类型、FrpCode。"
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
---

# 汇聚聚合支付 - 基础 Skill

本 Skill 是所有汇聚聚合支付业务 Skill 的公共基座，包含 SDK 初始化、签名方式、支付渠道选型和公共参数。

---

## 适配版本与定位

| 项目 | 内容 |
|------|------|
| Skill 版本 | `1.1.0` |
| 定位 | 聚合支付公共基座 |
| 适用范围 | 聚合支付初始化、产品选型、公共参数 |

## 前置依赖

### 凭据收集（代码生成前必须完成）

完整流程 → [../hj-payment-integration/shared-rules/credential-collection.md](../hj-payment-integration/shared-rules/credential-collection.md)

> 以下场景**不需要收集**：仅咨询业务知识、仅查看示例代码、参数已收集过。

### 签名硬约束

MD5/RSA 签名规则、代码实现和错误排查 → [../hj-payment-integration/shared-rules/signing-constraints.md](../hj-payment-integration/shared-rules/signing-constraints.md)

### 接口稳定性规则

接口地址、参数固定值、命名规范 → [../hj-payment-integration/shared-rules/interface-stability.md](../hj-payment-integration/shared-rules/interface-stability.md)

---

## 凭据要求

本 Skill 需要以下环境变量，由开发者从商户后台获取后通过环境变量注入（**严禁硬编码**）：

| 环境变量 | 用途 | 敏感级别 |
|---------|------|---------|
| `JOINPAY_MERCHANT_NO` | 商户号 | 普通 |
| `JOINPAY_MERCHANT_KEY` | MD5 签名密钥（32位） | **高** |
| `JOINPAY_RSA_PRIVATE_KEY` | RSA 私钥（请求签名） | **高** |
| `JOINPAY_RSA_PUBLIC_KEY` | RSA 公钥（响应验签） | 中 |

---

## 支付渠道选型（核心价值）

### 三大渠道总览

| 渠道 | 前缀 | 支持的支付方式 | 典型场景 |
|------|------|---------------|---------|
| 微信 | `WEIXIN_` | 主扫(NATIVE)、被扫(CARD)、APP、APP3、H5、H5+、公众号(GZH)、小程序(XCX)、收银台(SYT)、小程序插件(CJXCX) | 社交生态内支付 |
| 支付宝 | `ALIPAY_` | 主扫(NATIVE)、被扫(CARD)、APP、H5、服务窗(FWC)、收银台(SYT) | 电商/线下收款 |
| 银联 | `UNIONPAY_` | 主扫(NATIVE)、被扫(CARD)、APP、H5、收银台(SYT)、云微小程序(WXMP) | 大额支付/银行卡 |

### 选型决策树

```text
开始选型
  │
  ├─ 用户在微信环境内？
  │   ├─ 公众号H5页面？ → WEIXIN_GZH（需OpenId + AppId）
  │   ├─ 微信小程序？ → WEIXIN_XCX（需OpenId + AppId）
  │   └─ 普通H5页面？ → WEIXIN_H5 或 WEIXIN_H5+
  │
  ├─ 用户在APP内？
  │   ├─ 原生iOS/Android APP？ → {渠道}_APP
  │   └─ 跨平台/H5嵌入APP？ → {渠道}_H5
  │
  ├─ PC端网页？
  │   └─ 展示二维码让用户扫 → {渠道}_NATIVE（主扫）
  │
  ├─ 线下实体店收银？
  │   └─ 商户扫用户付款码 → {渠道}_CARD（被扫，需AuthCode + 终端信息）
  │
  └─ 需要统一收银入口？
      └─ {渠道}_SYT（收银台，自动识别用户环境）
```

完整 FrpCode 映射表 → [references/支付渠道与交易类型对比.md](references/支付渠道与交易类型对比.md)

---

## 参数命名规范（核心价值）

汇聚支付采用前缀分类命名，参数名由服务端定义，**不得擅自修改或猜测**：

| 前缀 | 含义 | 示例 |
|------|------|------|
| `p0` ~ `p9` | 通用基础参数 | p0_Version, p1_MerchantNo, p3_Amount, p9_NotifyUrl |
| `q1` ~ `qz` | 扩展/渠道参数 | q1_FrpCode, qa_TradeMerchantNo, q5_OpenId |
| `ra` ~ `rz` | 响应字段 | ra_Code, rb_CodeMsg, rc_Result |
| `hmac` | 签名字段 | 请求和响应均含此字段 |

详细禁止推断列表 → [interface-stability.md](../hj-payment-integration/shared-rules/interface-stability.md) §参数命名规范

## 接入流程

```text
1. 获取商户配置 → 2. 确认签名方式 → 3. 确认支付渠道(FrpCode) → 4. 配置环境变量 → 5. 进入语言适配 → 6. 调用业务接口
```

### application.yml 配置模板

```yaml
joinpay:
  merchant-no: ${JOINPAY_MERCHANT_NO}
  merchant-key: ${JOINPAY_MERCHANT_KEY}
  rsa-private-key: ${JOINPAY_RSA_PRIVATE_KEY}
  rsa-public-key: ${JOINPAY_RSA_PUBLIC_KEY}
  base-url: ${JOINPAY_BASE_URL:https://trade.joinpay.cc}
  notify-url: ${JOINPAY_NOTIFY_URL}
  sign-type: ${JOINPAY_SIGN_TYPE:MD5}
```

## 协议规则入口

真正跨语言共用的协议规则：

- [signing-rules.md](../hj-joinpay-pay-shared-base/protocol/signing-rules.md) — MD5/RSA 签名规则
- [async-notify.md](../hj-joinpay-pay-shared-base/protocol/async-notify.md) — 异步通知规则

## 多语言说明

当前仓库 Java 内容最完整。Python、Go、PHP 提供标准库实现示例。

SDK 能力矩阵 → [server-sdk-matrix.md](../hj-joinpay-pay-shared-base/runtime/server-sdk-matrix.md)
代码示例索引 → [接口索引.md](references/接口索引.md)

## 触发词

"汇聚支付"、"JoinPay"、"聚合支付接入"、"FrpCode"、"交易类型"、"支付渠道"、"微信支付"、"支付宝支付"、"银联支付"

## 参考资料索引

| 文件 | 内容 |
|-----|------|
| [signing-rules.md](../hj-joinpay-pay-shared-base/protocol/signing-rules.md) | MD5/RSA 签名规则（语言无关） |
| [async-notify.md](../hj-joinpay-pay-shared-base/protocol/async-notify.md) | 异步通知规则（语言无关） |
| [server-sdk-matrix.md](../hj-joinpay-pay-shared-base/runtime/server-sdk-matrix.md) | 服务端多语言能力矩阵 |
| [支付渠道与交易类型对比.md](references/支付渠道与交易类型对比.md) | FrpCode 映射表、选型决策树 |
| [开发必要参数说明.md](references/开发必要参数说明.md) | 各渠道参数获取方式 |
| [参数命名规范与固定值.md](references/参数命名规范与固定值.md) | 参数前缀规则、固定值约束 |
| [订单状态流转.md](references/订单状态流转.md) | 支付/退款状态流转、关单规则 |
| [错误码.md](references/错误码.md) | TOP 20 错误码速查 |
| [响应码完整列表.md](references/响应码完整列表.md) | 完整响应码（含退款专用） |
| [接入质量检查清单.md](references/接入质量检查清单.md) | 签名验签、业务逻辑完整性检查 |
| [接口索引.md](references/接口索引.md) | 四语言 x 7接口 代码示例索引 |
| [quickstart-templates/README.md](references/quickstart-templates/README.md) | 快速接入模板使用指南 |
