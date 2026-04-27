# 汇聚支付 Skill 产品包

这是一个面向第三方客户的汇聚支付（JoinPay）接入 Skill 包，用来帮助开发者借助 AI 工具完成聚合支付 API 接入开发。

README 按 **产品线 → 开发任务 → 技术栈** 导航，先帮你定位入口，再进入对应 Skill。

- 服务端 Skill：Java、Python、Go、PHP
- 产品能力主线：聚合支付（统一支付下单、订单查询、退款、关单、资金查询）

> 汇聚支付聚合支付 API 支持微信、支付宝、银联三大主流支付渠道，采用 MD5/RSA 双签名方式。

## 如何开始

### 1. 先按产品线定位

| 产品线 | 适合什么场景 | 从这里开始 |
|--------|-------------|-----------|
| 汇聚支付集成（总入口） | 第一次接入汇聚、需要先判断开发任务 / 阅读顺序 | [hj-payment-integration](hj-payment-integration/) |
| 聚合支付 | 标准支付场景，微信/支付宝/银联全渠道 | [hj-joinpay-aggregation-base](hj-joinpay-aggregation-base/) |

### 2. 再按开发任务进入

| 开发任务 | 对应 Skill |
|---------|-----------|
| 初始化 / 公共配置 / 产品选型 | [hj-joinpay-aggregation-base](hj-joinpay-aggregation-base/) |
| 统一支付下单 | [hj-joinpay-aggregation-order](hj-joinpay-aggregation-order/) |
| 订单查询 / 关单 / 资金查询 | [hj-joinpay-aggregation-query](hj-joinpay-aggregation-query/) |
| 退款申请 / 退款查询 | [hj-joinpay-aggregation-refund](hj-joinpay-aggregation-refund/) |

### 3. 最后按技术栈落地

| 技术栈 | 推荐入口 | 说明 |
|--------|---------|------|
| Java | [hj-joinpay-pay-shared-base/runtime/server-sdk-matrix.md](hj-joinpay-pay-shared-base/runtime/server-sdk-matrix.md) | 当前资料最完整 |
| Python | [hj-joinpay-pay-shared-base/runtime/server-sdk-matrix.md](hj-joinpay-pay-shared-base/runtime/server-sdk-matrix.md) | 标准库实现 |
| Go | [hj-joinpay-pay-shared-base/runtime/server-sdk-matrix.md](hj-joinpay-pay-shared-base/runtime/server-sdk-matrix.md) | 标准库实现 |
| PHP | [hj-joinpay-pay-shared-base/runtime/server-sdk-matrix.md](hj-joinpay-pay-shared-base/runtime/server-sdk-matrix.md) | cURL/Guzzle |

## 产品线说明

### 聚合支付

聚合支付是汇聚支付的唯一服务端主线，支持微信/支付宝/银联全渠道支付接入。

推荐阅读顺序：

1. [hj-joinpay-aggregation-base](hj-joinpay-aggregation-base/)
2. [hj-joinpay-aggregation-order](hj-joinpay-aggregation-order/)
3. [hj-joinpay-aggregation-query](hj-joinpay-aggregation-query/)
4. [hj-joinpay-aggregation-refund](hj-joinpay-aggregation-refund/)（按需）

## 共享资料层

这些共享资料不再分散在各个 Skill 中重复维护：

| 资料 | 作用 |
|------|------|
| [hj-joinpay-pay-shared-base/protocol/signing-rules.md](hj-joinpay-pay-shared-base/protocol/signing-rules.md) | MD5/RSA 双签名规则 |
| [hj-joinpay-pay-shared-base/protocol/async-notify.md](hj-joinpay-pay-shared-base/protocol/async-notify.md) | 异步通知规则 |
| [hj-joinpay-pay-shared-base/runtime/server-sdk-matrix.md](hj-joinpay-pay-shared-base/runtime/server-sdk-matrix.md) | 服务端多语言矩阵 |
| [hj-joinpay-pay-shared-base/governance/versioning-policy.md](hj-joinpay-pay-shared-base/governance/versioning-policy.md) | 版本治理规则 |
| [hj-joinpay-pay-shared-base/governance/release-checklist.md](hj-joinpay-pay-shared-base/governance/release-checklist.md) | 发布检查清单 |

## 已发布 Skill 列表

### 总入口

| Skill | 功能 | 前置依赖 |
|-------|------|---------|
| [hj-payment-integration](hj-payment-integration/) | 汇聚支付总入口：产品线判断、任务路由、阅读顺序、关键边界提醒 | hj-joinpay-pay-shared-base |

### 共享基础资料

| Skill | 功能 | 前置依赖 |
|-------|------|---------|
| [hj-joinpay-pay-shared-base](hj-joinpay-pay-shared-base/) | 共享协议层、运行时矩阵、版本治理和发布检查入口 | 无 |

### 聚合支付

| Skill | 功能 | 前置依赖 |
|-------|------|---------|
| [hj-joinpay-aggregation-base](hj-joinpay-aggregation-base/) | 公共基座：SDK 初始化、签名方式、支付渠道选型、公共参数 | hj-joinpay-pay-shared-base |
| [hj-joinpay-aggregation-order](hj-joinpay-aggregation-order/) | 统一支付下单：微信/支付宝/银联全场景 | hj-joinpay-aggregation-base |
| [hj-joinpay-aggregation-query](hj-joinpay-aggregation-query/) | 订单查询、关单、资金管控查询 | hj-joinpay-aggregation-base |
| [hj-joinpay-aggregation-refund](hj-joinpay-aggregation-refund/) | 退款申请、退款查询、退款信息查询 | hj-joinpay-aggregation-base |

## 目录结构

```text
├── README.md
├── CHANGELOG.md
├── LICENSE
├── scripts/
├── hj-payment-integration/
├── hj-joinpay-pay-shared-base/
├── hj-joinpay-aggregation-base/
├── hj-joinpay-aggregation-order/
├── hj-joinpay-aggregation-query/
└── hj-joinpay-aggregation-refund/
```

## 推荐接入主链路

### 聚合支付

```text
① hj-joinpay-aggregation-base
       ↓
② hj-joinpay-aggregation-order
       ↓
③ hj-joinpay-aggregation-query
       ↓
④ hj-joinpay-aggregation-refund（按需）
```

## 文档说明

- 优先阅读各 Skill 目录下的 `SKILL.md` 与 `references/` 文档。
- 服务端接入优先从 base Skill 和 `server-sdk-matrix.md` 开始。
- AI 生成接入代码时，不应自行猜测商户参数、密钥或最终支付状态。

## 官方技术支持

如需官方技术支持或接入答疑，可通过以下官方渠道联系：

- 汇聚支付官网：https://www.joinpay.com
- 商户后台：https://b.joinpay.com
