---
name: hj-joinpay-pay-shared-base
display_name: 汇聚支付共享基础资料
description: "汇聚支付共享基础资料 Skill：集中收纳 MD5/RSA 签名规则、异步通知规则、服务端多语言 SDK 矩阵和发布治理清单。适合作为聚合支付体系的公共入口。触发词：签名规则、异步通知、多语言 SDK、发布检查、共享基础资料。"
version: 1.1.0
author: "hj-payment-skills"
homepage: https://www.joinpay.com
license: MIT
compatibility:
  - skillhub
---

# 汇聚支付共享基础资料

这个 Skill 不负责某一个具体接口。它负责放聚合支付路径会反复用到的公共资料，也是整个仓库的共享协议/治理/契约入口。

## 共享规则补充

除了本 Skill 下的 protocol/governance/runtime 目录外，总入口还维护了一份**运行时共享规则**：

| 共享规则 | 内容 | 位置 |
|---------|------|------|
| 凭据与参数收集 | 统一收集流程、校验、缓存 | [integration/shared-rules/credential-collection.md](../hj-payment-integration/shared-rules/credential-collection.md) |
| 签名硬约束 | MD5/RSA 规范、代码实现 | [integration/shared-rules/signing-constraints.md](../hj-payment-integration/shared-rules/signing-constraints.md) |
| 接口稳定性 | 地址、固定值、命名规范 | [integration/shared-rules/interface-stability.md](../hj-payment-integration/shared-rules/interface-stability.md) |

## 适用场景

| 你要解决什么 | 先看哪里 |
|-------------|---------|
| 从 README 选择开发任务后的共享入口 | 当前 Skill |
| MD5/RSA 签名规则 | [protocol/signing-rules.md](protocol/signing-rules.md) |
| 异步通知规则 | [protocol/async-notify.md](protocol/async-notify.md) |
| 服务端多语言 SDK 入口 | [runtime/server-sdk-matrix.md](runtime/server-sdk-matrix.md) |
| 发布前检查 | [governance/release-checklist.md](governance/release-checklist.md) |
| 版本治理 | [governance/versioning-policy.md](governance/versioning-policy.md) |
| 凭据使用边界 | [governance/credential-boundary.md](governance/credential-boundary.md) |

## 这份 Skill 的定位

它是共享资料层和公共契约入口。README 负责帮助选择开发任务方向；确定方向后先回到这里确认共享协议、运行时矩阵和发布治理规则。

对应的基础入口 → [aggregation-base](../hj-joinpay-aggregation-base/SKILL.md)

## 目录导航

### `protocol/`

- `signing-rules.md` — MD5/RSA 双签名和验签规则（语言无关）
- `async-notify.md` — 异步通知、幂等、应答格式和回调限制

### `runtime/`

- `server-sdk-matrix.md` — Java、Python、Go、PHP 服务端 SDK 能力矩阵

### `governance/`

- `versioning-policy.md` — 版本治理规则
- `release-checklist.md` — 发布自检清单
- `credential-boundary.md` — 凭据使用边界

## 使用边界

1. 本 Skill 只提供共享资料，不持有任何商户密钥
2. 本 Skill 不是业务接口 Skill，不回答具体下单字段怎么传
3. 如果进入具体业务场景，要继续看对应的 order / query / refund skill
