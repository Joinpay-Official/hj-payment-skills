# 新产品线接入模板

当新增产品线、接口族或签名加密方式时，按本模板补齐资料。不要把新协议硬塞进现有聚合支付或二级商户规则。

## 必做清单

| 步骤 | 文件/位置 | 要求 |
|------|-----------|------|
| 1 | 新建 `hj-joinpay-<product>/SKILL.md` | frontmatter 含 `name/description/version/license`，正文有 `Skill 版本` 行 |
| 2 | 新建产品 references | 至少包含接口总览、签名与加密、字段与敏感信息、状态与通知 |
| 3 | 更新协议索引 | 在 [protocol-index.md](../../hj-joinpay-pay-shared-base/protocol/protocol-index.md) 登记协议族 |
| 4 | 新增协议规则 | 在 `hj-joinpay-pay-shared-base/protocol/` 下新增独立规则文档 |
| 5 | 更新接口稳定性 | 在 [interface-stability.md](interface-stability.md) 增加地址、method 或路径映射 |
| 6 | 更新总入口 | 在 `hj-payment-integration/SKILL.md` 加产品线分诊、触发词、禁止混用边界 |
| 7 | 更新 README | 加产品线导航、阅读顺序、发布列表 |
| 8 | 更新凭据收集 | 复用现有收集规则或新增产品线专属凭据收集文档 |
| 9 | 更新总览矩阵 | 补齐 [能力矩阵](capability-matrix.md)、[错误处理矩阵](error-handling-matrix.md)、[协议与版本矩阵](protocol-version-matrix.md)、[回调与幂等](callback-idempotency.md) |
| 10 | 校验 | 执行 `bash scripts/validate-skills.sh` |

## 新产品线 SKILL.md 最小结构

```markdown
# 产品线名称

一句话说明接口族、路径和协议族。

## 关键边界

说明当前产品线使用哪套协议，禁止使用哪些既有协议。

## 前置依赖

链接到协议规则、接口稳定性、凭据收集。

## 适配版本与定位

| 项目 | 内容 |
|------|------|
| Skill 版本 | `x.y.z` |
| 定位 | 产品线 / 协议族 |
| 适用范围 | ... |
| 不承担 | ... |

## 阅读顺序

链接到 references。

## 触发样例与反例

列出 3 到 5 个应该触发和不应触发的用户问题。
```

## 决策原则

1. 新签名或加密方式必须新增协议规则文档。
2. 新接口路径必须登记到接口稳定性文档。
3. 产品线 SKILL.md 只做路由和硬约束，字段表和流程放 references。
4. 如果补充文档和 API 文档冲突，字段名、method、签名规则以 API 文档为准。
5. 新产品线必须在能力、错误处理、协议版本、回调幂等四个总览文档中有明确条目。
