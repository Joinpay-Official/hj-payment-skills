# Changelog

所有重要变更记录在此文件中。格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/)。

## [1.1.0] - 2026-04-21

### 变更类型：结构性重构（零功能丢失）

> 本次重构的核心目标是**消除内容重复、解决指令矛盾、降低模型认知负荷**。
> 所有原始功能完整保留，仅改变信息的组织方式和呈现位置。

### 新增
- `hj-payment-integration/shared-rules/` — 共享规则集中目录：
  - `credential-collection.md` — 统一参数收集流程（从5处重复合并为1份）
  - `signing-constraints.md` — 统一 MD5/RSA 签名硬约束 + SignUtils.java 完整代码
  - `interface-stability.md` — 统一接口地址固定值、参数固定值约束、命名规范
- `hj-joinpay-aggregation-order/references/implementation-notes.md` — 下单实现注意事项（P0级约束的完整展开）

### 重构

**消除重复**：
- 参数收集流程：从 integration/base/order/query/refund 共5处 → 合并为 shared-rules/credential-collection.md 唯一份
- ask_followup_question JSON 块：从5处 → 1处
- 签名约束表(MD5/RSA)：从2处内联 → signing-constraints.md 唯一份
- 接口地址固定值表(7接口)：从2处 → interface-stability.md 唯一份
- 参数命名规范禁止推断列表：从2处 → interface-stability.md 唯一份

**解决矛盾指令**（hj-payment-integration）：
- 统一参数收集方式为「混合模式」：选项用 ask_followup_question + 文本用对话逐项
- 消除"一次性收集"与"逐项收集"的矛盾表述
- 消除"结构化收集"与"对话收集"的矛盾表述

**建立三级优先级标记体系**：
- `[P0]` — 致命级（违反导致100%接口调用失败），每个SKILL ≤5条，内联保留
- `[P1]` — 重要级（影响功能正确性），通过引用按需加载
- 替代原有无差别 ⚠️⚠️⚠️ / ‼️ / 🚫 / 严禁 滷用

**主文件瘦身**：

| SKILL.md | 重构前行数 | 重构后行数 | 缩减 |
|----------|-----------|-----------|------|
| hj-payment-integration | 715 | ~250 | **65% ↓** |
| hj-joinpay-aggregation-base | 301 | ~164 | **45% ↓** |
| hj-joinpay-aggregation-order | 423 | ~192 | **55% ↓** |
| hj-joinpay-aggregation-query | 232 | ~155 | **33% ↓** |
| hj-joinpay-aggregation-refund | 313 | ~195 | **38% ↓** |
| hj-joinpay-pay-shared-base | 84 | ~95 | +13%（新增shared-rules索引） |

**抽象层次分离**（aggregation-order）：
- SignUtils.java (84行)、parseResponse() (18行) → implementation-notes.md
- Thymeleaf th:if/CSS避坑指南 → implementation-notes.md
- rc_Result强制三步持久化 → implementation-notes.md
- SKILL.md 仅保留 P0 级摘要（每条2~3行）+ 引用链接

### 验证
- 通过 code-explorer 全面审计：6个SKILL共60+项功能点逐一核对，确认100%功能无丢失
- 所有内部引用链接验证通过（23条markdown链接全部指向真实文件）

---

## [1.0.0] - 2026-04-18

### 新增
- `hj-joinpay-pay-shared-base/` 共享协议层、运行时矩阵和发布治理
- 多语言入口（server-sdk-matrix.md）
- 客户前置准备清单（customer-preparation.md）
- 参数命名规范与固定值约束（parameter-conventions.md）
- 异步通知接收规范（async-notify.md）
- 接入质量检查清单（quality-checklist.md）
- 版本治理规则和发布检查清单
- 凭据使用边界集中文档（credential-boundary.md）
- 自动化校验脚本（scripts/validate-skills.sh）
- 聚合支付 4 个 Skill（aggregation-base、aggregation-order、aggregation-query、aggregation-refund）
- 总入口 Skill（hj-payment-integration）
