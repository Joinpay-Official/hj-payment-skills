# API Gateway JSON 签名与加密规则

本规则适用于二级商户入网等 API Gateway JSON 协议接口，例如 `/altFunds` 下的 `secondaryMch.*`、`secondaryMchPics.*`、`secondaryMchSign.*`。

它不适用于聚合支付 `/tradeRt/*`。聚合支付继续使用 `p0_/q*/hmac` 和“只拼接 value”的签名规则。

## 公共参数

| 参数 | 说明 | 是否参与签名 |
|------|------|--------------|
| `method` | 方法名 | 是 |
| `version` | 接口版本 | 是 |
| `data` | 业务 JSON 字符串 | 是 |
| `rand_str` | 32 位随机字符串 | 是 |
| `sign_type` | 签名类型，RSA 为 `2` | 是 |
| `mch_no` | 商户号 | 是 |
| `sign` | 签名值 | 否 |
| `sec_key` | RSA 加密后的 AES key | 否 |

## 待签名串

按字段名升序排列参与签名的公共参数，使用 `key=value` 格式，字段间以 `&` 连接。

```text
data=<最终上送的data JSON字符串>&mch_no=<商户号>&method=<方法名>&rand_str=<随机串>&sign_type=2&version=<版本>
```

## RSA 签名

| 项 | 规则 |
|----|------|
| 请求签名私钥 | 商户 RSA 私钥 |
| 平台验签公钥 | 商户 RSA 公钥 |
| 响应/通知签名私钥 | 平台 RSA 私钥 |
| 商户验签公钥 | 平台 RSA 公钥 |
| 算法 | `MD5withRSA` |
| 编码 | UTF-8 |
| 输出 | Base64 签名值 |

## `data` 序列化

`data` 必须是最终请求体中的 JSON 字符串。生成签名后禁止重新序列化或格式化，否则空格、字段顺序、转义方式变化都可能导致验签失败。

推荐：

```python
json.dumps(data, separators=(",", ":"), ensure_ascii=False)
```

禁止：

```text
pretty print JSON
签名后再次 json.dumps
签名用一份 data，请求发送另一份 data
```

## 敏感字段加密

当 `data` 中存在敏感字段：

1. 生成 16 位数字或字母 AES key。
2. 对敏感字段逐个 AES 加密。
3. 用加密后的字段值生成最终 `data` JSON 字符串。
4. 用平台公钥 RSA 加密 AES key，放入 `sec_key`。
5. 服务端使用平台私钥解密 `sec_key`，再解密 `data` 内敏感字段。

响应或回调如包含 `sec_key`，商户使用商户私钥解密该值，再用得到的 AES key 解密响应 `data` 内敏感字段。

## 排错优先级

| 现象 | 优先检查 |
|------|----------|
| 验签失败 | 是否误用了聚合支付只拼 value 规则 |
| 验签失败 | `data` 是否签名后又被重新序列化 |
| 解密失败 | `sec_key` 是否用平台公钥加密 |
| 敏感字段为空或乱码 | AES key 是否与 `sec_key` 对应，字段是否重复加密 |
