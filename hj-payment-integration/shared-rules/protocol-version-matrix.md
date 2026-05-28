# 协议与版本矩阵

本矩阵用于快速确认接口路径、请求格式、版本号和签名协议。接口路径、版本和签名字段属于稳定性约束，不要猜测或自行改名。

## 协议族

| 产品线 | 路径 | 请求格式 | 签名字段 | 签名串 | 规则文档 |
|--------|------|----------|----------|--------|----------|
| 聚合支付 | `/tradeRt/*` | `application/x-www-form-urlencoded` | `hmac` | 排除 `hmac`，按 key 排序后只拼 value | [signing-constraints.md](signing-constraints.md) |
| 二级商户入网 | `/altFunds` | JSON body，`data` 为 JSON 字符串 | `sign`，敏感字段配合 `sec_key` | 排除 `sign/sec_key`，按 key 排序拼 `key=value&key=value` | [api-gateway-signing-rules.md](../../hj-joinpay-pay-shared-base/protocol/api-gateway-signing-rules.md) |
| 分账方入网与结算 | `/allocFunds` | JSON body | `sign` | 排除 `sign`/`aes_key`，按 key 排序拼 `key=value&key=value` | [签名规则.md](../../hj-joinpay-alt-mch-settlement/references/签名规则.md) |
| 多次分账 | `/allocFunds` | JSON body | `sign` | 排除 `sign`，按 key 排序拼 `key=value&key=value` | [many-allocate-signing-rules.md](../../hj-joinpay-pay-shared-base/protocol/many-allocate-signing-rules.md) |

## 聚合支付接口版本

| 接口 | 路径 | `p0_Version` | 说明 |
|------|------|--------------|------|
| 统一支付下单 | `/tradeRt/uniPay` | `2.6` | 微信/支付宝/银联全渠道下单 |
| 订单查询 | `/tradeRt/queryOrder` | `2.6` | 查询支付结果 |
| 退款申请 | `/tradeRt/refund` | `2.3` | 发起退款 |
| 退款查询 | `/tradeRt/queryRefund` | `2.3` | 查询单笔退款 |
| 退款信息查询 | `/tradeRt/queryRefundInfo` | `2.3` | 查询订单维度退款信息 |
| 关闭订单 | `/tradeRt/closeOrder` | `1.0` | 关闭未支付订单 |
| 资金查询 | `/tradeRt/queryFundsControlOrder` | `1.0` | 查询资金管控或担保交易资金状态 |

## 二级商户 method

| method | 路径 | `version` | 说明 |
|--------|------|-----------|------|
| `secondaryMch.create` | `/altFunds` | 以接入文档为准，当前示例使用 `1.0` | 新增二级商户，会触发打款认证 |
| `secondaryMch.upgrade` | `/altFunds` | 以接入文档为准，当前示例使用 `1.0` | 存量分账方升级二级商户 |
| `secondaryMch.modify` | `/altFunds` | 以接入文档为准，当前示例使用 `1.0` | 修改商户资料或结算账户 |
| `secondaryMch.query` | `/altFunds` | 以接入文档为准，当前示例使用 `1.0` | 查询申请单或商户状态 |
| `secondaryMchPics.uploadPic` | `/altFunds` | 以接入文档为准，当前示例使用 `1.0` | 上传资质图片并换取 `image_id` |
| `secondaryMchSign.create` | `/altFunds` | 以接入文档为准，当前示例使用 `1.0` | 发起签约 |
| `secondaryMchSign.revoke` | `/altFunds` | 以接入文档为准，当前示例使用 `1.0` | 撤销签约 |
| `secondaryMchSign.query` | `/altFunds` | 以接入文档为准，当前示例使用 `1.0` | 查询签约状态 |

## 多次分账 method

| method | 路径 | `version` | 说明 |
|--------|------|-----------|------|
| `altHandle.manyLaterAllocate` | `/allocFunds` | `1.1` | 发起一次多次分账请求 |
| `altHandle.finishAllocate` | `/allocFunds` | `1.1` | 完结分账，剩余金额归平台 |
| `altHandle.altManyOrderQuery` | `/allocFunds` | `1.1` | 查询单笔分账 |
| `altHandle.altManyTotalQuery` | `/allocFunds` | `1.1` | 查询订单维度全部分账和剩余金额 |

## 分账方入网与结算 method

| method | 路径 | `version` | 说明 |
|--------|------|-----------|------|
| `altmch.create` | `/allocFunds` | `1.1` | 添加分账方并触发打款认证；V1.4.2 无 AES 加密 |
| `altmch.modify` | `/allocFunds` | 以接入文档为准 | 修改分账方资料 |
| `altmch.query` | `/allocFunds` | 以接入文档为准 | 查询分账方信息和状态 |
| `altMchPics.uploadPic` | `/allocFunds` | 以接入文档为准 | 上传分账方资质图片 |
| `altMchPics.modifyPic` | `/allocFunds` | 以接入文档为准 | 修改分账方资质图片 |
| `altMchPics.queryPicsInfo` | `/allocFunds` | 以接入文档为准 | 查询分账方图片信息 |
| `altMchSign.querySignContent` | `/allocFunds` | 以接入文档为准 | 获取内容签约协议 |
| `altMchSign.sign` | `/allocFunds` | 以接入文档为准 | 提交内容签约 |
| `altMchSign.getSignUrl` | `/allocFunds` | 以接入文档为准 | 获取页面签约 URL |
| `altMchSign.querySignRecord` | `/allocFunds` | 以接入文档为准 | 查询分账方签约状态 |
| `altSettle.launch` | `/allocFunds` | `1.1` | 发起手工结算 |
| `altSettle.get` | `/allocFunds` | `1.1` | 查询手工结算结果 |
| `altSettle.getAutoSettle` | `/allocFunds` | `1.1` | 查询自动结算记录 |
| `altAccount.get` | `/allocFunds` | `1.1` | 查询分账方账户余额 |

## 环境地址

| 产品线 | 测试环境 | 生产环境 |
|--------|----------|----------|
| 聚合支付 | `https://trade.joinpay.cc` | `https://trade.joinpay.com` |
| 二级商户入网 | 按接入方配置，不在 skill 中硬编码内网地址 | `https://api.joinpay.com/altFunds` |
| 分账方入网与结算 | 按接入方配置 | `https://www.joinpay.com/allocFunds` |
| 多次分账 | 按接入方配置 | `https://www.joinpay.com/allocFunds` |

## 固定值与禁区

| 项 | 规则 |
|----|------|
| 聚合支付版本 | 不要把退款 `2.3` 写成下单 `2.6`，也不要把关单/资金查询 `1.0` 改成其他值 |
| 聚合支付签名 | 不要拼 `key=value`，不要让 `hmac` 参与签名 |
| 二级商户 `data` | 必须用最终上送的紧凑 JSON 原文参与签名，签名后不得重新格式化 |
| 二级商户 `sec_key` | 用平台公钥加密 AES key，`sign/sec_key` 不参与签名 |
| 多次分账 `sign_type` | `1`=MD5，`21`=RSA，不要沿用二级商户 `2` |
| 分账方入网与结算 `sign_type` | V1.4.2：`1`=MD5，`21`=RSA，RSA 算法为 `MD5withRSA`；`altmch.create` 无 AES 加密 |
| 协议混用 | `/tradeRt/*` 禁止使用 `method/data/sign/sec_key`；`/altFunds` 禁止使用 `p0_/q*/hmac`；`/allocFunds` 禁止沿用二级商户 `sec_key` 规则 |
