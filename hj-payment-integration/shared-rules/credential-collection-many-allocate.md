# 多次分账凭据与参数收集

本文件仅适用于多次分账 `/allocFunds`。聚合支付 `/tradeRt/*` 和二级商户 `/altFunds` 继续使用各自的收集规则。

## 触发条件

| 场景 | 是否需要收集 |
|------|--------------|
| 用户要求接入多次分账、延迟分账、完结分账、查询分账 | 必须收集 |
| 用户只是咨询金额规则、状态、签名规则 | 不收集 |
| 参数已在本次对话中收集过 | 不重复收集 |

## 选项参数

| 参数ID | 选项 | 说明 |
|--------|------|------|
| `many_allocate_base_url` | 生产 `https://www.joinpay.com/allocFunds` / 测试自定义 URL | 测试环境由接入方提供 |
| `many_allocate_sign_type` | `1`=MD5 / `21`=RSA | 由商户后台配置决定 |
| `many_allocate_api_scope` | 分账请求 / 完结分账 / 单笔查询 / 全量查询 | 用于判断是否必须收集 `callback_url` |

## 文本参数

| 顺序 | 参数ID | 问题文案 | 校验 |
|------|--------|----------|------|
| 1 | `merchant_no` | 请输入商户编号 `mch_no` | 非空、纯数字 |
| 2a | `merchant_key` | 如使用 MD5，请输入商户密钥 | 非空 |
| 2b | `merchant_private_key` | 如使用 RSA，请输入商户 RSA 私钥 | PEM 格式，含 PRIVATE KEY 标记 |
| 3 | `platform_public_key` | 如使用 RSA 验签响应/通知，请输入汇聚平台 RSA 公钥 | RSA 模式必填，PEM 格式 |
| 4 | `callback_url` | 请输入异步通知地址；分账请求和完结分账需要 | 以 `http://` 或 `https://` 开头 |

## 参数缓存

```text
__jp_many_allocate_base_url        = /allocFunds 完整地址
__jp_many_allocate_sign_type       = 1 或 21
__jp_many_allocate_merchant_no     = 商户编号
__jp_many_allocate_merchant_key    = MD5 密钥
__jp_many_allocate_private_key     = RSA 私钥
__jp_many_allocate_platform_pubkey = 平台公钥
__jp_many_allocate_callback_url    = 分账/完结分账回调地址
```
