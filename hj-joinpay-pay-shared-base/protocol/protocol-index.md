# 协议族索引

本索引用于先判断产品线和接口路径，再进入正确的签名与加密规则。新增产品线时先补这里，避免把不同协议混写到同一份签名文档。

## 协议路由表

| 协议族 | 适用产品线 | 接口路径 | 请求形态 | 签名字段 | 规则文档 |
|--------|------------|----------|----------|----------|----------|
| 聚合支付交易协议 | 聚合支付 | `/tradeRt/*` | form-urlencoded 请求，JSON 响应 | 聚合支付交易签名字段 | [signing-rules.md](signing-rules.md) |
| API Gateway JSON 协议 | 二级商户入网 | `/altFunds` | JSON body，`data` 为 JSON 字符串 | `sign`，敏感字段使用 `sec_key` | [api-gateway-signing-rules.md](api-gateway-signing-rules.md) |
| 分账方 JSON 协议 | 分账方入网与结算 | `/allocFunds` | JSON body | `sign` | [签名规则.md](../../hj-joinpay-alt-mch-settlement/references/签名规则.md) |
| 多次分账 JSON 协议 | 多次分账 | `/allocFunds` | JSON body | `sign` | [many-allocate-signing-rules.md](many-allocate-signing-rules.md) |

## 选择规则

1. 用户提到统一支付、微信/支付宝/银联下单、退款、订单查询、关单、资金管控查询时，进入聚合支付交易协议。
2. 用户提到二级商户入网、平台二级商户、资质图片、签约、`secondaryMch.*` 时，进入 API Gateway JSON 协议。
3. 用户提到分账方入网、分账方图片、分账方协议签约、分账方结算、`altmch.*`、`altMchPics.*`、`altMchSign.*`、`altSettle.*`、`altAccount.get` 时，进入分账方 JSON 协议。
4. 用户提到多次分账、延迟分账、完结分账、`altHandle.*` 时，进入多次分账 JSON 协议。
5. 如果接口路径、公共参数或签名字段不在本表内，先按 [new-product-line-template.md](../../hj-payment-integration/shared-rules/new-product-line-template.md) 新增产品线和协议族，不要复用现有规则。

## 禁止混用

| 场景 | 禁止做法 |
|------|----------|
| 聚合支付 `/tradeRt/*` | 使用 `method/data/sign/sec_key` |
| 二级商户 `/altFunds` | 使用 `p0_/q*` 聚合支付参数或只拼 value 签名 |
| 分账方 `/allocFunds` | 使用二级商户 `sec_key` 规则，或用 `altHandle.*` 代替 `altmch.*`、`altSettle.*` |
| 多次分账 `/allocFunds` | 使用二级商户 `sec_key` 规则，或沿用聚合支付 `hmac` 规则 |
| 新产品线 | 在没有协议索引登记前套用现有签名规则 |
