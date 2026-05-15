# 能力矩阵

本矩阵用于快速判断 `hj-payment-skills` 支持哪些产品线、接口能力、语言示例和落地方式。需要具体字段时再进入对应业务 Skill。

## 产品线能力

| 产品线 | 能力范围 | 接口路径 / method | 渠道或接口族 | 示例代码 | 快速接入模板 | 签名协议 |
|--------|----------|-------------------|--------------|----------|--------------|----------|
| 聚合支付 | 下单、查询、关单、退款、退款查询、退款信息查询、资金查询 | `/tradeRt/uniPay`、`/tradeRt/queryOrder`、`/tradeRt/closeOrder`、`/tradeRt/refund`、`/tradeRt/queryRefund`、`/tradeRt/queryRefundInfo`、`/tradeRt/queryFundsControlOrder` | 微信、支付宝、银联 | Java/Python/Go/PHP，见 [聚合支付示例索引](../../hj-joinpay-aggregation-base/references/示例代码/接口索引.md) | Spring Boot 聚合支付模板 | `p0_/q*/hmac`，按 key 排序后只拼 value |
| 二级商户入网 | 新增、存量升级、修改、查询、图片上传、签约、撤销签约、签约查询 | `/altFunds` + `secondaryMch.create`、`secondaryMch.upgrade`、`secondaryMch.modify`、`secondaryMch.query`、`secondaryMchPics.uploadPic`、`secondaryMchSign.*` | API Gateway JSON method 分发 | Java/PHP/Python/Go，见 [二级商户示例索引](../../hj-joinpay-secondary-mch/references/示例代码/接口索引.md) | 暂无工程模板，提供协议示例 | `method/version/data/rand_str/sign_type/mch_no/sign/sec_key` |
| 多次分账 | 分账请求、完结分账、单笔查询、全部查询 | `/allocFunds` + `altHandle.manyLaterAllocate`、`altHandle.finishAllocate`、`altHandle.altManyOrderQuery`、`altHandle.altManyTotalQuery` | 多次分账 JSON method 分发 | 当前提供文档和规则说明，见 [多次分账 Skill](../../hj-joinpay-many-allocate/SKILL.md) | 暂无工程模板 | `method/version/data/rand_str/sign_type/mch_no/sign` |

## 聚合支付渠道

| 渠道 | 常见 FrpCode | 典型场景 |
|------|--------------|----------|
| 微信 | `WEIXIN_NATIVE`、`WEIXIN_GZH`、`WEIXIN_XCX`、`WEIXIN_APP`、`WEIXIN_H5`、`WEIXIN_CARD` | 扫码、公众号、小程序、APP、H5、付款码 |
| 支付宝 | `ALIPAY_NATIVE`、`ALIPAY_H5`、`ALIPAY_APP`、`ALIPAY_FWC`、`ALIPAY_CARD` | 扫码、H5、APP、服务窗、付款码 |
| 银联 | `UNIONPAY_NATIVE`、`UNIONPAY_H5`、`UNIONPAY_APP`、`UNIONPAY_CARD`、`UNIONPAY_WXMP` | 扫码、H5、APP、付款码、云微小程序 |

完整 FrpCode 和渠道参数见 [支付渠道与交易类型对比](../../hj-joinpay-aggregation-base/references/支付渠道与交易类型对比.md) 与 [下单参数速查](../../hj-joinpay-aggregation-order/references/下单参数速查.md)。

## 使用建议

| 用户问题 | 推荐入口 |
|----------|----------|
| 支持哪些支付渠道、接口和版本 | 本文 + [协议版本矩阵](protocol-version-matrix.md) |
| 接入微信/支付宝/银联支付 | [聚合支付基础 Skill](../../hj-joinpay-aggregation-base/SKILL.md) |
| 接入二级商户入网或签约 | [二级商户 Skill](../../hj-joinpay-secondary-mch/SKILL.md) |
| 接入多次分账或完结分账 | [多次分账 Skill](../../hj-joinpay-many-allocate/SKILL.md) |
| 需要直接写 Spring Boot 支付代码 | [快速接入模板指南](../../hj-joinpay-aggregation-base/references/quickstart-templates/README.md) |
