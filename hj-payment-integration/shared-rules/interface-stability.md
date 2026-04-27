# 接口稳定性与参数命名约束

> **唯一事实来源** — 本文件是接口地址、参数固定值、命名规范的唯一定义。
> Agent 在生成任何代码时必须严格遵守以下内容，**不得凭记忆或猜测编造**。

---

## [P0] 接口地址固定值

汇聚支付的接口地址是固定不变的，测试环境和生产环境域名如下：

| 项目 | 测试环境 | 生产环境 |
|------|---------|---------|
| 域名 | `https://trade.joinpay.cc` | `https://trade.joinpay.com` |

完整 URL = 域名 + 路径：

| 接口名称 | 路径 | 完整测试URL |
|---------|------|------------|
| 统一支付下单 | `/tradeRt/uniPay` | `https://trade.joinpay.cc/tradeRt/uniPay` |
| 订单查询 | `/tradeRt/queryOrder` | `https://trade.joinpay.cc/tradeRt/queryOrder` |
| 退款 | `/tradeRt/refund` | `https://trade.joinpay.cc/tradeRt/refund` |
| 退款查询 | `/tradeRt/queryRefund` | `https://trade.joinpay.cc/tradeRt/queryRefund` |
| 退款信息查询 | `/tradeRt/queryRefundInfo` | `https://trade.joinpay.cc/tradeRt/queryRefundInfo` |
| 关闭订单 | `/tradeRt/closeOrder` | `https://trade.joinpay.cc/tradeRt/closeOrder` |
| 资金管控订单查询 | `/tradeRt/queryFundsControlOrder` | `https://trade.joinpay.cc/tradeRt/queryFundsControlOrder` |

> 🚫 **禁止行为**：**严禁使用 web_search、web_fetch 等工具搜索汇聚支付官方文档或接口地址**。本文件已包含所有必要信息，无需外部查询。

---

## [P0] 参数固定值约束

以下参数值为汇聚支付服务端规定的固定常量，Agent 生成代码时必须显式使用规定值：

| 参数名 | 版本要求 | 适用接口 | 固定值 | 说明 |
|--------|---------|----------|--------|------|
| `p0_Version` | ≥ `2.6` | 统一支付(`/tradeRt/uniPay`)、订单查询(`/tradeRt/queryOrder`) | `2.6` | 支付类接口版本线 |
| `p0_Version` | ≥ `2.3` | 退款(`/tradeRt/refund`)、退款查询(`/tradeRt/queryRefund`)、退款信息查询(`/tradeRt/queryRefundInfo`) | `2.3` | 退款类接口版本线 |
| `p0_Version` | ≥ `1.0` | 关闭订单(`/tradeRt/closeOrder`)、资金管控订单查询(`/tradeRt/queryFundsControlOrder`) | `1.0` | 其他接口版本线 |
| `p4_Cur` | 全部 | 统一支付(`/tradeRt/uniPay`) | `1` | 人民币币种标识 |

> 🚫 **禁止行为**：
> - 禁止将 `p0_Version` 设为上述范围以外的任何值
> - 禁止将 `p4_Cur` 设为除 `"1"` 以外的任何值（如 `"CNY"` / `"RMB"` / `"156"` 等）
> - 禁止省略 `p0_Version` 或 `p4_Cur` 参数

---

## [P0] 参数命名规范（禁止推断/修改参数名）

汇聚支付采用前缀分类命名，参数名由汇聚服务端定义，**Agent 不得根据前缀规律自行推断或修改**。

### 前缀分类

| 前缀 | 含义 | 示例 |
|------|------|------|
| `p0` ~ `p9` | 通用基础参数 | p0_Version, p1_MerchantNo, p3_Amount, p9_NotifyUrl |
| `q1` ~ `q9` | 扩展/渠道参数 | q1_FrpCode, q3_SubMerchantNo, q5_OpenId |
| `qa` ~ `qz` | 报备/扩展参数 | qa_TradeMerchantNo（接口必填参数） |
| `ra` ~ `rz` | 响应基础字段 | ra_Code(状态), rb_CodeMsg(消息), rc_Result(结果) |
| `hmac` | 签名字段 | 请求和响应均含此字段 |

### 易混淆参数（常见错误纠正）

| 参数名 | 说明 | ❌ 常见错误（禁止） |
|--------|------|-------------------|
| `q1_FrpCode` | 支付渠道编码 | 禁止写成 `p6_FrpCode`（错误推断为 p 序列延续） |
| `qa_TradeMerchantNo` | 报备商户号（必填） | 禁止写成 `p10_TradeMerchantNo` |
| `q3_SubMerchantNo` | 子商户号 | 禁止写成 `p_SubMerchantNo` |

> **禁止行为**：禁止将 `q1_FrpCode` 改为任何其他名称（如 `p6_FrpCode` / `frpCode` / `frp_code` 等）。所有请求参数名大小写敏感，**不得擅自修改或猜测**。

---

## [P0] 响应字段固定性

响应字段名同样固定不变：

| 字段 | 含义 | 注意事项 |
|------|------|---------|
| `ra_Code` | 响应码（`100`=成功） | 判断成功的唯一标准 |
| `rb_CodeMsg` | 响应描述 | 失败时提供原因 |
| `rc_Result` | 结果数据 | 内容因接口而异（二维码URL/支付参数等） |
| `hmac` | 响应签名 | 需验签 |

> ⚠️ **重要提醒**：汇聚支付部分接口（下单/退款/关单等）的**响应格式为 JSON**，而请求用 form-urlencoded 发送。如果按 form 格式解析 JSON 响应会导致所有字段值为 `null`。解析策略：检查响应正文首个非空白字符是否为 `{`，若是则用 JSON 解析器处理。
