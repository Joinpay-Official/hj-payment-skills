---
name: hj-payment-integration
display_name: 汇聚支付集成
description: "汇聚支付接入总入口 Skill。用于帮助开发者和 AI 在聚合支付各业务 Skill 与共享协议资料之间做正确路由，先完成场景判断，再进入对应的基础 Skill 和业务 Skill。触发词：汇聚支付接入、JoinPay接入、聚合支付选型。"
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

# 汇聚支付集成

这个 Skill 是汇聚支付接入的总入口。它不直接代替各业务 Skill，而是先帮助判断当前应该进入哪条路径，再路由到对应的 Skill。

---

## 共享规则（Single Source of Truth）

以下三项内容是整个 skill 包的唯一定义，**所有业务 Skill 均引用此处**：

| 共享规则 | 内容 | 引用路径 |
|---------|------|---------|
| 凭据与参数收集 | 统一的收集流程、校验规则、缓存变量 | [shared-rules/credential-collection.md](shared-rules/credential-collection.md) |
| 签名硬约束 | MD5/RSA 签名规范、代码实现、错误排查 | [shared-rules/signing-constraints.md](shared-rules/signing-constraints.md) |
| 接口稳定性 | 接口地址、参数固定值、命名规范 | [shared-rules/interface-stability.md](shared-rules/interface-stability.md) |

> **任何涉及代码生成的操作前，必须先执行 [credential-collection.md](shared-rules/credential-collection.md) 的收集流程。**

---

## 工作模式

本 Skill 支持双模式运行，根据用户意图自动切换：

### 模式A：快速接入模式（推荐）

**触发条件**：用户表达"接入"、"集成"、"添加支付功能"、"帮我把支付加上"等集成意图。

- ✅ **可直接写入用户项目文件**（`write_to_file` / `replace_in_file`）
- ✅ 使用框架级模板生成代码（Controller + Service + Config）
- ✅ **参数前置收集**（在一切操作之前先收齐所有必要参数）
- ✅ 完成后自动触发质量评估

### 模式B：参考咨询模式

**触发条件**：用户问具体技术问题、"查一下"、"给我看示例"、"签名怎么算"等咨询意图。

- ⛔ **只展示、不写入**
- ⛔ **只检索、不生成**（从示例代码文件中检索获取）
- ✅ 分步确认协议保持不变

---

## 全局交互规范

以下规则适用于本 Skill 包所有能力，优先级高于各能力的局部规则：

1. **所有问题必须得到用户明确回答后才能继续。** 多个问题须逐一确认，严禁自行假设或使用默认值。
2. **签名方式前置确认**：任何涉及实际 API 调用或代码生成，须先确认签名方式（MD5 或 RSA），已明确则不重复。详见 → [signing-constraints.md](shared-rules/signing-constraints.md)
3. **分步确认协议**（简单知识问答除外）：
   - **① 明确需求**：先理解用户问题，给出初步判断或原因分析
   - **② 征得同意**：提出下一步能做什么，等用户明确同意后才继续
   - **③ 收集信息**：同意后告知需要哪些信息并逐项收集（遵循 [credential-collection.md](shared-rules/credential-collection.md) 混合模式）
   - **④ 执行前确认**：简要说明即将做什么，确认用户同意后再执行；涉及线上环境须额外提示风险
4. **环境边界与错误归因铁律**：
   - **环境类错误**（JDK/Maven/网络等）→ 提供修复命令引导用户执行，**禁止修改接口/参数/签名逻辑**
   - **代码类错误**（语法/业务逻辑）→ 排查并修正
   - 涉及安装操作需先展示命令并征得用户同意
   - 🚫 **严禁因环境错误修改 SDK 核心逻辑**（接口路径、请求参数名、签名算法、HTTP通信逻辑）

---

## 接口稳定性速查

接口地址、参数固定值和命名规范的完整定义 → 见 [interface-stability.md](shared-rules/interface-stability.md)

核心要点摘要：

| 项目 | 固定值 |
|------|--------|
| 测试域名 | `https://trade.joinpay.cc` |
| 生产域名 | `https://trade.joinpay.com` |
| p0_Version（支付/查询） | `2.6` |
| p0_Version（退款类） | `2.3` |
| p0_Version（其他） | `1.0` |
| p4_Cur | `1`（人民币） |

---

## 适配版本与定位

| 项目 | 内容 |
|------|------|
| Skill 版本 | `1.1.0` |
| 定位 | 总入口 / 总导航 / 场景分诊 |
| 适用范围 | 聚合支付、共享协议资料导航 |
| 不承担 | 具体字段表、语言 SDK 代码细节、单接口完整说明 |

## 快速决策树

```text
用户要接入汇聚支付
        │
        +-- 先判断你要做什么？
        │       +-- 整体路径 / 不知道选哪条线 --> 当前 Skill
        │       +-- 共享规则 / 签名 / 异步通知 --> shared-rules/ 或 hj-joinpay-pay-shared-base/
        │
        +-- 你的目标是什么？
                +-- 标准服务端支付接入 --> 聚合支付
                        +-- 初始化 / 选型 --> aggregation-base
                        +-- 下单 --> aggregation-order
                        +-- 查询 / 关单 / 资金查询 --> aggregation-query
                        +-- 退款 --> aggregation-refund
```

## 接入路由表

| 用户意图 | 推荐入口 | 下一步 |
|---------|---------|--------|
| 第一次接汇聚，不知道怎么开始 | 当前 Skill | 再进 base 或业务 Skill |
| 看共享规则、签名、异步通知 | [hj-joinpay-pay-shared-base](./hj-joinpay-pay-shared-base/) | 再回具体业务 Skill |
| 微信/支付宝/银联支付接入 | [aggregation-base](./hj-joinpay-aggregation-base/) | 再进 order/query/refund |
| 创建支付订单 | [aggregation-order](./hj-joinpay-aggregation-order/) | 后续 query/refund |
| 查询订单或关单 | [aggregation-query](./hj-joinpay-aggregation-query/) | 结合原订单链路 |
| 退款 | [aggregation-refund](./hj-joinpay-aggregation-refund/) | 结合原订单链路 |

## 与子 Skill 的关系

```
hj-payment-integration          (current: 总入口、路由、共享规则)
├── shared-rules/               (唯一事实来源：参数收集 / 签名约束 / 接口稳定)
├── hj-joinpay-pay-shared-base/ (协议层：signing-rules / async-notify / governance)
├── hj-joinpay-aggregation-base/    (公共基座：渠道选型 / 参数规范)
├── hj-joinpay-aggregation-order/   (下单：uniPay)
├── hj-joinpay-aggregation-query/   (查询 / 关单 / 资金查询)
└── hj-joinpay-aggregation-refund/  (退款 / 退款查询 / 退款信息查询)
```

## 能力概览

| 能力 | 模式 | 说明 |
|------|------|------|
| **能力0：快速接入** | A | 一键生成框架级代码并写入项目 |
| **能力1：产品选型** | B | 根据场景推荐交易类型(FrpCode) → [aggregation-base](./hj-joinpay-aggregation-base/) |
| **能力2：示例代码** | B | 四语言代码示例（只展示不写入）→ [base/references/接口索引.md](./hj-joinpay-aggregation-base/references/接口索引.md) |
| **能力3：业务知识速查** | B | 参数/签名/状态/回调 → 各 Skill references/ |
| **能力4：接入质量评估** | A+B | 签名验签/业务完整性检查 → [base/references/接入质量检查清单.md](./hj-joinpay-aggregation-base/references/接入质量检查清单.md) |
| **能力5：问题排查** | A+B | 响应码/FAQ/排障 → [order/references/排障手册.md](./hj-joinpay-aggregation-order/references/排障手册.md) |

---

## 快速接入流程（模式A）

> 用户触发快速接入模式且获得同意后执行。**步骤0 必须是第一个动作。**

### 步骤0：参数前置收集（最重要！）

> ‼️ 不要先扫描项目！不要先生成代码！

完整流程 → [shared-rules/credential-collection.md](shared-rules/credential-collection.md)

要点：
1. **第一步**：调用 `ask_followup_question` 收集选项型参数（签名方式 + 接口环境）
2. **第二步**：通过对话逐项询问文本输入参数（每问一个等回复后再问下一个）
3. **第三步**：当场校验每个参数，不合格立即让用户修正
4. 全部通过后参数缓存供后续步骤使用

### 步骤1~4：扫描→加载模板→写入→报告

| 步骤 | 操作 | 说明 |
|------|------|------|
| 1 | 扫描项目识别框架 + 运行时预检 | pom.xml→Spring Boot; 仅检测报告不自动安装 |
| 2 | 加载模板 + 替换占位符 | 模板位置: `aggregation-base/references/quickstart-templates/SpringBoot/` |
| 3 | 写入8个文件到项目 | SDK工具类+Config+Service+Controller+NotifyController+application.yml |
| 4 | 输出接入报告 | 含代码状态和环境状态 |

支持的框架模板：目前仅 **Spring Boot**（`JoinPayConfig` + `JoinPayService` + `PayController` + `NotifyController` + 3个SDK工具类）

### 完整流程示意

```text
用户："帮我在我的项目里接入微信扫码支付"

  Agent 执行：
  ├─ ① 确认意图，征得用户同意
  ├─ ② ⚡ 步骤0：参数前置收集（→ credential-collection.md）
  │     a) ask_followup_question → 签名方式 + 接口环境
  │     b) 对话逐项提问 → 商户号 → 回调地址 → 密钥
  ├─ ③ 当场校验参数合法性
  ├─ ④ 步骤1：扫描项目 → 识别 Spring Boot + 环境预检
  ├─ ⑤ 步骤2：加载模板 + 替换占位符
  ├─ ⑥ 步骤3：写入8个文件到项目
  └─ ⑦ 步骤4：输出接入报告（含环境状态）
```

---

## 配置说明

快速接入模式的最终配置结构（`application.yml`）：

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

最小可用配置只需3项：`merchant-no` + `merchant-key`(MD5) 或 `rsa-private-key`(RSA) + `base-url`

---

## CLI 脚本执行层

保留 `scripts/uni_pay_client.py` 作为底层 API 调用工具（支持 MD5 签名）。RSA 签名请参考能力2中的代码示例。

```bash
python3 <skill_dir>/scripts/uni_pay_client.py <command> [args...]
```

| 命令 | 说明 | 核心必填参数 |
|------|------|-------------|
| `pay` | 统一支付下单 | --order-no, --amount, --product-name, --frp-code |
| `query` | 订单查询 | --order-no |
| `refund` | 退款申请 | --order-no, --refund-order-no, --refund-amount, --reason |
| `query_refund` | 退款查询 | --refund-order-no |
| `query_refund_info` | 退款信息查询 | --order-no |
| `close` | 关闭订单 | --order-no, --frp-code |
| `query_funds` | 资金查询 | --order-no |

## 注意事项

- 本 Skill 是**总入口**，不代替具体业务 Skill 的字段表和实现说明
- 首次接入时，优先判断开发任务再进入对应 Skill，不要跳过依赖链
- 密钥信息只能留在服务端，严禁写入前端或仓库
- 子 Skill 可不经本入口被直接触发，此时子 Skill 自带的引用同样生效
- 前端支付完成回调不等于最终交易成功；最终状态以查询接口和异步通知为准
