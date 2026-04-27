# 服务端多语言 SDK 矩阵

## 语言支持总览

| 语言 | 最低版本 | HTTP 库 | JSON 库 | MD5 库 | RSA 库 | 完整度 |
|------|---------|---------|---------|--------|--------|--------|
| Java | 8+ | HttpURLConnection / OkHttp | fastjson / Jackson | Apache Commons Codec | java.security / BouncyCastle | ★★★★★ |
| Python | 3.7+ | urllib (标准库) | json (标准库) | hashlib (标准库) | cryptography / rsa | ★★★★☆ |
| Go | 1.16+ | net/http | encoding/json | crypto/md5 | crypto/rsa | ★★★☆☆ |
| PHP | 7.4+ | cURL / Guzzle | json_encode/decode | md5() | openssl_* | ★★★☆☆ |

## 接口覆盖矩阵

| 接口 | API 路径 | Java | Python | Go | PHP |
|------|---------|------|--------|-----|-----|
| 统一支付下单 | `/tradeRt/uniPay` | ✅ | ✅ | ✅ | ✅ |
| 订单查询 | `/tradeRt/queryOrder` | ✅ | ✅ | ✅ | ✅ |
| 退款申请 | `/tradeRt/refund` | ✅ | ✅ | ✅ | ✅ |
| 退款查询 | `/tradeRt/queryRefund` | ✅ | ✅ | ✅ | ✅ |
| 退款信息查询 | `/tradeRt/queryRefundInfo` | ✅ | ✅ | ✅ | ✅ |
| 关闭订单 | `/tradeRt/closeOrder` | ✅ | ✅ | ✅ | ✅ |
| 资金管控查询 | `/tradeRt/queryFundsControlOrder` | ✅ | ✅ | ✅ | ✅ |

## 签名方式覆盖

| 语言 | MD5 签名 | RSA 签名 | 双模式客户端 |
|------|---------|---------|------------|
| Java | ✅ JoinPaySignature | ✅ JoinPayRsaSignature | ✅ JoinPayClient |
| Python | ✅ joinpay_signature.py | ✅ joinpay_rsa_signature.py | ✅ joinpay_client.py |
| Go | ✅ SignMD5() | ✅ SignRSA() | ✅ JoinPayClient |
| PHP | ✅ signMD5() | ✅ signRSA() | ✅ JoinPayClient |

## 环境地址

| 环境 | 域名 | 说明 |
|------|------|------|
| 测试环境 | `https://trade.joinpay.cc` | 联调专用，不会产生真实扣款 |
| 生产环境 | `https://trade.joinpay.com` | 正式环境 |

## 接口路径

| 接口名称 | 路径 |
|---------|------|
| 统一支付下单 | `/tradeRt/uniPay` |
| 订单查询 | `/tradeRt/queryOrder` |
| 退款 | `/tradeRt/refund` |
| 退款查询 | `/tradeRt/queryRefund` |
| 退款信息查询 | `/tradeRt/queryRefundInfo` |
| 关闭订单 | `/tradeRt/closeOrder` |
| 资金管控订单查询 | `/tradeRt/queryFundsControlOrder` |

## 快速接入模板

| 框架 | 模板位置 | 包含文件 |
|------|----------|----------|
| Spring Boot | `hj-joinpay-aggregation-base/references/quickstart-templates/SpringBoot/` | Config + Service + Controller + NotifyController + SDK 工具类 |
