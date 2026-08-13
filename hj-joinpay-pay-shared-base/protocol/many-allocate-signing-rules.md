# 延迟分账与多次分账签名规则

本规则仅适用于延迟分账与多次分账 `/allocFunds`，例如：

- `altHandle.singleLaterAllocate`
- `altHandle.allocateQuery`
- `altHandle.manyLaterAllocate`
- `altHandle.finishAllocate`
- `altHandle.altManyOrderQuery`
- `altHandle.altManyTotalQuery`

它不适用于聚合支付 `/tradeRt/*`，也不等同于二级商户 `/altFunds` 的 `sec_key` 协议。

## 公共参数

| 参数 | 说明 | 是否参与签名 |
|------|------|--------------|
| `method` | 方法名 | 是 |
| `version` | 接口版本 | 是 |
| `data` | HTTP 请求中为业务 JSON 对象；签名时取该对象的紧凑 JSON 字符串 | 是 |
| `rand_str` | 32 位随机串 | 是 |
| `sign_type` | `1`=MD5，`21`=RSA | 是 |
| `mch_no` | 商户号 | 是 |
| `sign` | 签名值 | 否 |

## 待签名串

1. 排除 `sign`
2. 参数按 ASCII 字典序排序
3. 使用 `key=value` 形式拼接，字段间用 `&`

```text
data=<data JSON>&mch_no=<商户号>&method=<方法名>&rand_str=<随机串>&sign_type=<1或21>&version=<版本>
```

## MD5 签名

拼接规则：

```text
待签串&key=商户密钥
```

然后做 MD5 加密。

## RSA 签名

| 项 | 规则 |
|----|------|
| sign_type | `21` |
| 算法 | `MD5withRSA` |
| 编码 | UTF-8 |
| 请求签名私钥 | 商户 RSA 私钥 |
| 响应/通知验签公钥 | 汇聚平台 RSA 公钥 |

## data 处理建议

文档要求 `data` 参与签名。实现时必须区分请求字段类型和签名参数值：

- HTTP 请求体中的 `data` 必须是 JSON 对象，不能是 JSON 字符串
- 签名时单独将该对象序列化为紧凑 JSON 字符串，并将该字符串作为 `data` 的值拼入待签名串
- 请求发送时仍放入原 JSON 对象；不要把签名用字符串赋回请求体的 `data`
- 签名序列化必须固定字符编码、字段顺序、数字和转义规则，避免同一对象在签名端与服务端得到不同文本
- 金额字段统一保留两位小数字符串

## 排错优先级

| 现象 | 优先检查 |
|------|----------|
| 验签失败 | `sign_type` 是否与签名实现匹配（`1` 或 `21`） |
| 验签失败 | 是否按字典序拼 `key=value&key=value` |
| 验签失败 | 请求 `data` 是否误发成字符串；签名用紧凑 JSON 是否由同一个 `data` 对象生成 |
| MD5 失败 | 是否漏掉 `&key=` + 商户密钥 |
| RSA 失败 | 是否错误使用了 `SHA256withRSA` 等其他算法 |
| 状态未知 | 单次延迟分账先用 `altHandle.allocateQuery` 查分账订单；多次分账先查单笔分账，再查订单所有分账 |
