# 汇聚支付 Skill 产品包

这是一个面向第三方客户的汇聚支付（JoinPay）接入 Skill 包，用来帮助开发者借助 AI 工具完成聚合支付 API、二级商户入网 API、分账方入网与结算 API 和多次分账 API 接入开发。

README 按 **产品线 → 开发任务 → 技术栈** 导航，先帮你定位入口，再进入对应 Skill。

- 服务端 Skill：Java、Python、Go、PHP
- 产品能力主线：聚合支付（统一支付下单、订单查询、退款、关单、资金查询）、二级商户入网（新增、存量升级、图片、签约）、分账方入网与结算（分账方添加、图片、签约、结算、账户查询）、多次分账（分账请求、完结分账、单笔查询、全部查询）

> 汇聚支付聚合支付 API 支持微信、支付宝、银联三大主流支付渠道，采用 `hmac` 签名；二级商户入网 API 使用 API Gateway JSON 协议和 `sign/sec_key`；分账方入网与结算 API 使用 `/allocFunds` + `altmch.*`、`altMchPics.*`、`altMchSign.*`、`altSettle.*`；多次分账 API 使用 `/allocFunds` + `altHandle.*`。协议边界禁止混用。

## 如何开始

### 0. 导入方式

如果目标 Agent 提示 “zip 文件应包含带有 SKILL.md 的文件夹”，请确认 zip 内部第一层是：

```text
hj-payment-skills/SKILL.md
```

不要只压缩目录内部文件。推荐在本目录执行：

```bash
bash scripts/package-importable-zip.sh
```

生成的 `dist/hj-payment-skills-importable.zip` 可作为整包导入文件。

### 后续更新

如果后续 `hj-payment-skills` 新增子 Skill、参考资料或脚本，优先到官方仓库下载最新的 Skill 安装包：

- GitHub 仓库：[Joinpay-Official/hj-payment-skills](https://github.com/Joinpay-Official/hj-payment-skills)

下载后按目标 Agent 的导入方式重新安装或覆盖本地旧版本，避免只复制单个新增文件导致目录索引、依赖关系或共享规则不同步。

### 1. 先按产品线定位

| 产品线 | 适合什么场景 | 从这里开始 |
|--------|-------------|-----------|
| 汇聚支付集成（总入口） | 第一次接入汇聚、需要先判断开发任务 / 阅读顺序 | [hj-payment-integration](hj-payment-integration/) |
| 聚合支付 | 标准支付场景，微信/支付宝/银联全渠道 | [hj-joinpay-aggregation-base](hj-joinpay-aggregation-base/) |
| 二级商户入网 | 平台二级商户新增、存量升级、图片上传、签约和状态查询 | [hj-joinpay-secondary-mch](hj-joinpay-secondary-mch/) |
| 分账方入网与结算 | 分账方添加、图片上传、协议签约、手工结算、账户查询 | [hj-joinpay-alt-mch-settlement](hj-joinpay-alt-mch-settlement/) |
| 多次分账 | 延迟分账、完结分账、单笔查询、全部查询 | [hj-joinpay-many-allocate](hj-joinpay-many-allocate/) |

### 2. 再按开发任务进入

| 开发任务 | 对应 Skill |
|---------|-----------|
| 初始化 / 公共配置 / 产品选型 | [hj-joinpay-aggregation-base](hj-joinpay-aggregation-base/) |
| 统一支付下单 | [hj-joinpay-aggregation-order](hj-joinpay-aggregation-order/) |
| 订单查询 / 关单 / 资金查询 | [hj-joinpay-aggregation-query](hj-joinpay-aggregation-query/) |
| 退款申请 / 退款查询 | [hj-joinpay-aggregation-refund](hj-joinpay-aggregation-refund/) |
| 二级商户入网 / 图片 / 签约 | [hj-joinpay-secondary-mch](hj-joinpay-secondary-mch/) |
| 分账方入网 / 图片 / 签约 / 结算 | [hj-joinpay-alt-mch-settlement](hj-joinpay-alt-mch-settlement/) |
| 多次分账 / 完结分账 / 分账查询 | [hj-joinpay-many-allocate](hj-joinpay-many-allocate/) |

### 3. 最后按产品线和技术栈落地

| 产品线 | 技术栈 | 推荐入口 | 说明 |
|--------|--------|---------|------|
| 聚合支付 | Java/Python/Go/PHP | [server-sdk-matrix.md](hj-joinpay-pay-shared-base/runtime/server-sdk-matrix.md) | SDK 工具类、签名规则和服务端接入矩阵 |
| 二级商户入网 | Java | [SecondaryMchCreateExample.java](hj-joinpay-secondary-mch/references/示例代码/Java/SecondaryMchCreateExample.java) | `secondaryMch.create` 加签、AES 加密、`sec_key` 示例 |
| 二级商户入网 | PHP | [secondary_mch_create_example.php](hj-joinpay-secondary-mch/references/示例代码/PHP/secondary_mch_create_example.php) | OpenSSL 实现 |
| 二级商户入网 | Python | [secondary_mch_create_example.py](hj-joinpay-secondary-mch/references/示例代码/Python/secondary_mch_create_example.py) | `cryptography` + `requests` 实现 |
| 二级商户入网 | Go | [secondary_mch_create_example.go](hj-joinpay-secondary-mch/references/示例代码/Go/secondary_mch_create_example.go) | Go 标准库实现 |
| 分账方入网与结算 | Java/Python/Go/PHP | [分账方示例索引](hj-joinpay-alt-mch-settlement/references/示例代码/接口索引.md) | `/allocFunds` MD5/RSA 签名、分账方添加、手工结算、账户查询示例 |
| 多次分账 | 文档规则 | [hj-joinpay-many-allocate](hj-joinpay-many-allocate/) | 当前提供接口文档、金额规则、状态与排障说明 |

## 产品线说明

### 聚合支付

聚合支付是汇聚支付的唯一服务端主线，支持微信/支付宝/银联全渠道支付接入。

推荐阅读顺序：

1. [hj-joinpay-aggregation-base](hj-joinpay-aggregation-base/)
2. [hj-joinpay-aggregation-order](hj-joinpay-aggregation-order/)
3. [hj-joinpay-aggregation-query](hj-joinpay-aggregation-query/)
4. [hj-joinpay-aggregation-refund](hj-joinpay-aggregation-refund/)（按需）

### 二级商户入网

二级商户入网使用独立的 API Gateway JSON 协议，入口为 `/altFunds`，覆盖 `secondaryMch.*`、`secondaryMchPics.*`、`secondaryMchSign.*`。

推荐阅读顺序：

1. [hj-joinpay-secondary-mch](hj-joinpay-secondary-mch/)
2. [hj-joinpay-pay-shared-base/protocol/api-gateway-signing-rules.md](hj-joinpay-pay-shared-base/protocol/api-gateway-signing-rules.md)
3. [hj-joinpay-secondary-mch/references/示例代码/接口索引.md](hj-joinpay-secondary-mch/references/示例代码/接口索引.md)（需要代码示例时）

### 分账方入网与结算

分账方入网与结算使用 `/allocFunds` JSON 协议，覆盖分账方添加、修改、查询、资质图片、协议签约、手工结算、结算查询和账户余额查询。

推荐阅读顺序：

1. [hj-joinpay-alt-mch-settlement](hj-joinpay-alt-mch-settlement/)
2. [hj-joinpay-alt-mch-settlement/references/签名规则.md](hj-joinpay-alt-mch-settlement/references/签名规则.md)
3. [hj-joinpay-alt-mch-settlement/references/入网到可结算流程.md](hj-joinpay-alt-mch-settlement/references/入网到可结算流程.md)
4. [hj-joinpay-alt-mch-settlement/references/结算与账户查询.md](hj-joinpay-alt-mch-settlement/references/结算与账户查询.md)
5. [hj-joinpay-alt-mch-settlement/references/示例代码/接口索引.md](hj-joinpay-alt-mch-settlement/references/示例代码/接口索引.md)（需要代码示例时）

### 多次分账

多次分账使用独立的 `/allocFunds` JSON 协议，覆盖多次分账请求、完结分账、单笔查询、全部查询。

推荐阅读顺序：

1. [hj-joinpay-many-allocate](hj-joinpay-many-allocate/)
2. [hj-joinpay-pay-shared-base/protocol/many-allocate-signing-rules.md](hj-joinpay-pay-shared-base/protocol/many-allocate-signing-rules.md)
3. [hj-joinpay-many-allocate/references/金额规则与分账模式.md](hj-joinpay-many-allocate/references/金额规则与分账模式.md)

## 共享资料层

这些共享资料不再分散在各个 Skill 中重复维护：

| 资料 | 作用 |
|------|------|
| [hj-payment-integration/shared-rules/capability-matrix.md](hj-payment-integration/shared-rules/capability-matrix.md) | 能力矩阵：产品线、接口、渠道、示例和模板覆盖 |
| [hj-payment-integration/shared-rules/error-handling-matrix.md](hj-payment-integration/shared-rules/error-handling-matrix.md) | 错误处理矩阵：网络、签名、参数、渠道、状态冲突处理 |
| [hj-payment-integration/shared-rules/protocol-version-matrix.md](hj-payment-integration/shared-rules/protocol-version-matrix.md) | 协议与版本矩阵：路径、版本、请求格式、签名字段 |
| [hj-payment-integration/shared-rules/callback-idempotency.md](hj-payment-integration/shared-rules/callback-idempotency.md) | 回调与幂等：验签、响应、重试、状态机更新 |
| [hj-joinpay-pay-shared-base/protocol/protocol-index.md](hj-joinpay-pay-shared-base/protocol/protocol-index.md) | 产品线与协议族路由索引 |
| [hj-joinpay-pay-shared-base/protocol/signing-rules.md](hj-joinpay-pay-shared-base/protocol/signing-rules.md) | MD5/RSA 双签名规则 |
| [hj-joinpay-pay-shared-base/protocol/api-gateway-signing-rules.md](hj-joinpay-pay-shared-base/protocol/api-gateway-signing-rules.md) | API Gateway JSON 签名与敏感字段加密规则 |
| [hj-joinpay-alt-mch-settlement/references/签名规则.md](hj-joinpay-alt-mch-settlement/references/签名规则.md) | 分账方入网与结算 `/allocFunds` 签名规则 |
| [hj-joinpay-pay-shared-base/protocol/many-allocate-signing-rules.md](hj-joinpay-pay-shared-base/protocol/many-allocate-signing-rules.md) | 多次分账 `/allocFunds` 签名规则 |
| [hj-joinpay-pay-shared-base/protocol/async-notify.md](hj-joinpay-pay-shared-base/protocol/async-notify.md) | 异步通知规则 |
| [hj-joinpay-pay-shared-base/runtime/server-sdk-matrix.md](hj-joinpay-pay-shared-base/runtime/server-sdk-matrix.md) | 服务端多语言矩阵 |
| [hj-joinpay-pay-shared-base/governance/versioning-policy.md](hj-joinpay-pay-shared-base/governance/versioning-policy.md) | 版本治理规则 |
| [hj-joinpay-pay-shared-base/governance/release-checklist.md](hj-joinpay-pay-shared-base/governance/release-checklist.md) | 发布检查清单 |
| [hj-payment-integration/shared-rules/new-product-line-template.md](hj-payment-integration/shared-rules/new-product-line-template.md) | 新产品线接入模板 |

## 已发布 Skill 列表

### 总入口

| Skill | 功能 | 前置依赖 |
|-------|------|---------|
| [hj-payment-skills](./) | 整包导入入口：兼容只接受单 Skill zip 的 Agent | hj-joinpay-pay-shared-base |
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

### 商户与分账

| Skill | 功能 | 前置依赖 |
|-------|------|---------|
| [hj-joinpay-secondary-mch](hj-joinpay-secondary-mch/) | 二级商户新增、存量升级、修改、查询、图片上传、签约 | hj-joinpay-pay-shared-base |
| [hj-joinpay-alt-mch-settlement](hj-joinpay-alt-mch-settlement/) | 分账方添加、修改、查询、图片上传、协议签约、结算、账户查询，含 Java/Python/Go/PHP 示例 | hj-joinpay-pay-shared-base |
| [hj-joinpay-many-allocate](hj-joinpay-many-allocate/) | 多次分账请求、完结分账、单笔查询、全部查询 | hj-joinpay-pay-shared-base |

## 目录结构

```text
├── README.md
├── SKILL.md
├── CHANGELOG.md
├── LICENSE
├── scripts/
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

### 二级商户入网

```text
① hj-joinpay-secondary-mch
       ↓
② api-gateway-signing-rules
       ↓
③ 接口总览 / 字段与敏感信息 / 状态与通知
```

### 分账方入网与结算

```text
① hj-joinpay-alt-mch-settlement
       ↓
② 签名规则
       ↓
③ 接口总览 / 入网到可结算流程 / 结算与账户查询 / 状态与通知
```

### 多次分账

```text
① hj-joinpay-many-allocate
       ↓
② many-allocate-signing-rules
       ↓
③ 接口总览 / 金额规则与分账模式 / 状态与通知
```

## 文档说明

- 优先阅读各 Skill 目录下的 `SKILL.md` 与 `references/` 文档。
- 判断产品线和签名协议时，先看 `protocol-index.md`。
- 聚合支付服务端接入优先从 base Skill 和 `server-sdk-matrix.md` 开始；二级商户入网优先从 `hj-joinpay-secondary-mch` 和其示例代码索引开始；分账方入网与结算优先从 `hj-joinpay-alt-mch-settlement` 开始。
- AI 生成接入代码时，不应自行猜测商户参数、密钥或最终支付状态。
- 新增产品线时，按 `new-product-line-template.md` 补齐 Skill、协议规则、接口稳定性和 README 导航。

## 官方技术支持

如需官方技术支持或接入答疑，可通过以下官方渠道联系：

- 汇聚支付官网：https://www.joinpay.com
