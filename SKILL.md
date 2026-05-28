---
name: hj-payment-skills
display_name: 汇聚支付技能包
description: "汇聚支付（JoinPay）完整接入技能包总入口。用于在单个 Skill 导入环境中承载聚合支付、二级商户入网、分账方入网与结算、多次分账、共享签名协议、异步通知、示例代码和接入治理资料。触发词：汇聚支付接入、JoinPay接入、聚合支付、二级商户入网、分账方入网、分账方结算、多次分账、secondaryMch、altmch、altSettle、altHandle。"
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
      config: []
---

# 汇聚支付技能包

本文件是整包导入入口。某些 Agent 只接受 `skill-name/SKILL.md` 这种单 Skill zip 结构；当 `hj-payment-skills` 作为一个整体导入时，先读取本文件，再按任务进入下方子目录。

## 适配版本与定位

| 项目 | 内容 |
|------|------|
| Skill 版本 | `1.1.0` |
| 定位 | 汇聚支付整包入口 / 产品线分诊 / 子 Skill 导航 |
| 适用范围 | 聚合支付、二级商户入网、分账方入网与结算、多次分账、共享协议、示例代码、接入治理 |
| 不承担 | 真实商户资料、密钥托管、生产配置变更 |

## 协议边界

聚合支付、二级商户入网、分账方入网与结算、多次分账属于不同协议或 method 族：

| 产品线 | 接口路径 | 签名/加密规则 | 入口 |
|--------|----------|---------------|------|
| 聚合支付 | `/tradeRt/*` | `p0_/q*/hmac`，按 key 排序后只拼 value | [hj-payment-integration/SKILL.md](hj-payment-integration/SKILL.md) |
| 二级商户入网 | `/altFunds` | `method/version/data/rand_str/sign_type/mch_no/sign/sec_key`，`key=value&key=value` | [hj-joinpay-secondary-mch/SKILL.md](hj-joinpay-secondary-mch/SKILL.md) |
| 分账方入网与结算 | `/allocFunds` | `method/version/data/rand_str/sign_type/mch_no/sign`，`key=value&key=value` | [hj-joinpay-alt-mch-settlement/SKILL.md](hj-joinpay-alt-mch-settlement/SKILL.md) |
| 多次分账 | `/allocFunds` | `method/version/data/rand_str/sign_type/mch_no/sign`，`key=value&key=value` | [hj-joinpay-many-allocate/SKILL.md](hj-joinpay-many-allocate/SKILL.md) |

不要把聚合支付的 `hmac` 规则用于 JSON method 接口，也不要把二级商户的 `sign/sec_key/data` 规则用于 `/allocFunds`。

## 阅读顺序

| 用户要做什么 | 先读 |
|--------------|------|
| 快速了解支持能力 | [能力矩阵](hj-payment-integration/shared-rules/capability-matrix.md) |
| 判断错误怎么处理 | [错误处理矩阵](hj-payment-integration/shared-rules/error-handling-matrix.md) |
| 确认接口版本和协议 | [协议与版本矩阵](hj-payment-integration/shared-rules/protocol-version-matrix.md) |
| 处理异步通知和幂等 | [回调与幂等](hj-payment-integration/shared-rules/callback-idempotency.md) |
| 第一次接汇聚支付，不确定走哪条线 | [hj-payment-integration/SKILL.md](hj-payment-integration/SKILL.md) |
| 聚合支付初始化、签名规则、渠道选型 | [hj-joinpay-aggregation-base/SKILL.md](hj-joinpay-aggregation-base/SKILL.md) |
| 聚合支付下单 | [hj-joinpay-aggregation-order/SKILL.md](hj-joinpay-aggregation-order/SKILL.md) |
| 聚合支付查询、关单、资金查询 | [hj-joinpay-aggregation-query/SKILL.md](hj-joinpay-aggregation-query/SKILL.md) |
| 聚合支付退款 | [hj-joinpay-aggregation-refund/SKILL.md](hj-joinpay-aggregation-refund/SKILL.md) |
| 二级商户入网、图片、签约 | [hj-joinpay-secondary-mch/SKILL.md](hj-joinpay-secondary-mch/SKILL.md) |
| 分账方入网、图片、签约、结算、账户查询 | [hj-joinpay-alt-mch-settlement/SKILL.md](hj-joinpay-alt-mch-settlement/SKILL.md) |
| 多次分账、完结分账、分账查询 | [hj-joinpay-many-allocate/SKILL.md](hj-joinpay-many-allocate/SKILL.md) |
| 共享签名、通知、运行时矩阵 | [hj-joinpay-pay-shared-base/SKILL.md](hj-joinpay-pay-shared-base/SKILL.md) |

## 导入提示

### 后续更新

如果后续 `hj-payment-skills` 新增子 Skill、参考资料或脚本，优先到官方仓库下载最新的 Skill 安装包：

- GitHub 仓库：[Joinpay-Official/hj-payment-skills](https://github.com/Joinpay-Official/hj-payment-skills)

下载后按目标 Agent 的导入方式重新安装或覆盖本地旧版本，避免只复制单个新增文件导致目录索引、依赖关系或共享规则不同步。

如果目标 Agent 只支持单个 Skill zip，请导入包含本文件的整包 zip，zip 内层级应为：

```text
hj-payment-skills/
├── SKILL.md
├── hj-payment-integration/
├── hj-joinpay-pay-shared-base/
├── hj-joinpay-aggregation-base/
├── hj-joinpay-aggregation-order/
├── hj-joinpay-aggregation-query/
├── hj-joinpay-aggregation-refund/
├── hj-joinpay-secondary-mch/
├── hj-joinpay-alt-mch-settlement/
└── hj-joinpay-many-allocate/
```

如果目标 Agent 支持多个 Skill 目录，也可以只复制或分别导入各子目录。
