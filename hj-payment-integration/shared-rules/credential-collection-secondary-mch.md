# 二级商户凭据与参数收集

本文件仅适用于二级商户入网 API Gateway JSON 协议。聚合支付 `/tradeRt/*` 继续使用 [credential-collection.md](credential-collection.md)。

## 触发条件

| 场景 | 是否需要收集 |
|------|--------------|
| 用户要求接入二级商户入网、写 `secondaryMch.*` 调用代码 | 必须收集 |
| 用户要求写图片上传、签约、签约查询代码 | 必须收集 |
| 用户只是咨询字段、状态、签名规则 | 不收集 |
| 参数已在本次对话中收集过 | 不重复收集 |

## 选项参数

| 参数ID | 选项 | 说明 |
|--------|------|------|
| `secondary_mch_base_url` | 生产 `https://api.joinpay.com/altFunds` / 测试自定义 URL | 测试环境由接入方提供，禁止硬编码内网地址 |
| `secondary_mch_api_scope` | 入网 / 图片 / 签约 / 查询 | 用于决定是否需要 `callback_url` 和示例字段 |

## 文本参数

| 顺序 | 参数ID | 问题文案 | 校验 |
|------|--------|----------|------|
| 1 | `merchant_no` | 请输入平台商户号 `mch_no` | 非空，纯数字 |
| 2 | `merchant_private_key` | 请输入商户 RSA 私钥，用于请求签名 | PEM 格式，含 PRIVATE KEY 标记 |
| 3 | `platform_public_key` | 请输入汇聚平台 RSA 公钥，用于加密 `sec_key` 和验签响应 | PEM 格式，含 PUBLIC KEY 标记 |
| 4 | `callback_url` | 请输入异步通知地址；仅入网、修改、签约需要 | 以 `http://` 或 `https://` 开头 |
| 5 | `merchant_public_key` | 如需生成验签示例，请提供商户 RSA 公钥 | 可选，PEM 格式 |

## 参数缓存

```text
__jp_secondary_base_url            = /altFunds 完整地址
__jp_secondary_merchant_no         = 平台商户号
__jp_secondary_merchant_private_key = 商户请求签名私钥
__jp_secondary_platform_public_key = 平台公钥，用于 sec_key 加密和响应验签
__jp_secondary_callback_url        = 入网/修改/签约回调地址
__jp_secondary_merchant_public_key = 商户公钥，可选
```

## 与聚合支付凭据的差异

| 项 | 二级商户 | 聚合支付 |
|----|----------|----------|
| 接口地址 | `/altFunds` | `/tradeRt/*` |
| 签名方式 | RSA，`sign_type=2` | MD5 或 RSA |
| 签名字段 | `sign` | 聚合支付交易签名字段 |
| 加密字段 | `sec_key` 加密 AES key | 通常不需要 |
| 报备商户号 | 不需要 | 下单常需要 `trade_merchant_no` |
