# MD5/RSA 签名与验签规则

汇聚支付支持两种签名方式：**MD5 签名** 和 **RSA 签名**。商户可在汇聚后台选择并配置。

---

## ⚠️⚠️⚠️ 强制约束：禁止自行实现签名 ⚠️⚠️⚠️

> ‼️ **严禁基于"常见做法"或假设编写签名代码！必须100%复制下方示例！**
>
> 任何偏离文档示例的自行实现都会导致签名验证失败！

### 错误示例（❌ 100%失败）

```java
// ❌ 错误1：使用 key=value& 格式拼接
StringBuilder sb = new StringBuilder();
for (String key : keys) {
    sb.append(key).append("=").append(value).append("&");  // ❌ 大错特错！
}
sb.append("key=").append(merchantKey);  // ❌ 这也错了
String hmac = md5(sb.toString()).toUpperCase();

// ❌ 错误2：空值参与签名
for (String key : keys) {
    sb.append(value);  // ❌ 空值不应该参与
}
```

### 正确示例（✅ 必须复制）

```java
// ✅ 正确：只拼接value，空值跳过，密钥追加在末尾
TreeMap<String, String> sortedParams = new TreeMap<>();
for (Map.Entry<String, String> entry : params.entrySet()) {
    if ("hmac".equalsIgnoreCase(entry.getKey())) continue; // 跳过hmac
    String value = entry.getValue();
    if (value != null && !value.trim().isEmpty()) {
        sortedParams.put(entry.getKey(), value.trim());
    }
}
StringBuilder content = new StringBuilder();
for (String value : sortedParams.values()) {
    content.append(value);  // ✅ 只拼接value
}
String hmac = md5(content.toString() + merchantKey).toUpperCase(); // ✅ 密钥追加在末尾
```

---

## 1. MD5 签名算法

适用于大多数中小商户，配置简单。

### 签名流程

```text
原始参数 Map
    ↓
① 移除 hmac 字段（不参与签名计算）
    ↓
② 按 key 的字母升序排序
    ↓
③ 拼接所有 value（只拼接 value，不拼接 key 名）
    ↓
④ 在末尾追加 merchantKey（32 位密钥）
    ↓
⑤ 对整个字符串做 MD5 哈希
    ↓
⑥ 结果转大写 → 即为 hmac 签名值
```

### Java 实现（参考 SignBiz.java）

```java
Properties properties = new Properties();
for (String name : map.keySet()) {
    String value = map.get(name) == null ? "" : map.get(name).toString().trim();
    if ("hmac".equalsIgnoreCase(name)) continue; // ① 跳过hmac
    properties.setProperty(name, value);
}

// ② 按key字母排序
List<String> keys = new ArrayList(properties.keySet());
Collections.sort(keys);

// ③ 拼接所有value
StringBuilder content = new StringBuilder();
for (String key : keys) {
    content.append(properties.getProperty(key));
}

// ④⑤⑥ MD5(拼接值 + 密钥).toUpperCase()
String signStr = content.toString();
String hmac = DigestUtils.md5Hex(signStr + merchantKey).toUpperCase();
```

### Python 实现

```python
def sign(params, key):
    filtered = {}
    for k, v in params.items():
        if k.lower() == "hmac": continue  # 跳过hmac
        val = str(v).strip() if v is not None else ""
        if val != "":
            filtered[k] = val

    sorted_keys = sorted(filtered.keys())
    sign_str = "".join(filtered[k] for k in sorted_keys)
    return hashlib.md5((sign_str + key).encode("utf-8")).hexdigest().upper()
```

### Go 实现

```go
func SignMD5(params map[string]string, key string) string {
    var keys []string
    for k := range params {
        if strings.ToLower(k) == "hmac" { continue }
        keys = append(keys, k)
    }
    sort.Strings(keys)

    var buf bytes.Buffer
    for _, k := range keys {
        if v := strings.TrimSpace(params[k]); v != "" {
            buf.WriteString(v)
        }
    }

    hash := md5.Sum([]byte(buf.String() + key))
    return strings.ToUpper(hex.EncodeToString(hash[:]))
}
```

### PHP 实现

```php
function signMD5($params, $key) {
    unset($params['hmac']); // 移除hmac
    ksort($params);         // 按key排序
    
    $signStr = '';
    foreach ($params as $v) {
        $v = trim($v);
        if ($v !== '') {
            $signStr .= $v;
        }
    }
    
    return strtoupper(md5($signStr . $key));
}
```

### MD5 签名关键约束

| 规范项 | 正确实现 | 错误实现（禁止） |
|--------|---------|----------------|
| **拼接格式** | 只拼接 **value**，不包含 key 名 | `"key=value&key=value"` 格式 |
| **空值处理** | 过滤掉空字符串和 null 值 | 将空值参与拼接 |
| **密钥位置** | 在末尾追加密钥后整体 MD5 哈希 | 先对各段分别 MD5 再组合 |

---

## 2. RSA 签名算法

适用于安全性要求较高的商户，使用非对称加密。

### 签名流程

```text
原始参数 Map
    ↓
① 同 MD5 步骤①~③：移除 hmac、按 key 排序、拼接 value
    ↓
② 使用商户 RSA 私钥对 signStr 进行签名（**MD5withRSA**，与 RSAUtils.java:76 一致）
    ↓
③ Base64 编码 → 即为 hmac 签名值
```

### 验签流程

```text
接收到的参数 + 返回的 hmac
    ↓
① 同样移除 hmac、排序、拼接 value 得到 signStr
    ↓
② 使用商户 RSA 公钥验证签名
    ↓
③ 验证通过则数据未被篡改
```

### RSA 关键要点

1. 私钥用于**签名**（请求时），公钥用于**验签**（验证回调时）
2. 公钥长度 > 1024 时使用 X.509 格式解析
3. Base64 解码时注意空格替换为 `+`
4. 必须使用 **MD5withRSA** 算法（与 RSAUtils.java:76 SIGNATURE_ALGORITHM 一致，其他算法会导致验签失败）

### RSA 签名关键约束

| 规范项 | 正确实现 | 错误实现（禁止） |
|--------|---------|----------------|
| **签名算法** | `MD5withRSA` | `SHA256withRSA` / `SHA1withRSA` 等其他算法 |
| **待签名串** | 只拼接 **value**，不包含 key | `"key=" + key + "&value=" + value` 或 `"key=value"` 格式 |

---

## 3. 签名方式选择指南

| 维度 | MD5 签名 | RSA 签名 |
|------|---------|---------|
| 安全性 | 中（密钥共享） | 高（非对称加密） |
| 配置复杂度 | 低（仅需 32 位密钥） | 高（需生成密钥对、上传公钥） |
| 适用场景 | 一般商户 | 金融/高安全需求商户 |
| 性能影响 | 几乎无 | RSA 运算略慢 |
| 密钥泄露风险 | 高（泄露即可伪造签名） | 低（仅泄露公钥无法签名） |

---

## 4. 常见错误排查

| 错误码 | 含义 | 排查方向 |
|--------|------|---------|
| 10080000 | 签名验证失败 | ① 检查参与签名的参数是否完整 ② 检查 key 是否正确 ③ 检查大小写转换 |
| 10080015 | 版本号有误 | 确认 p0_Version 值是否正确（支付用 2.6，退款用 2.3） |
| 10080017 | 订单号格式有误 | 只允许英文、数字、下划线，最长 50 字符 |
| 10080026 | 商户不存在或未激活 | 确认 merchant_no 是否正确，商户状态是否正常 |

### 签名调试步骤

1. **打印参与签名的参数列表**：确认哪些参数参与了签名
2. **打印排序后的 key 序列**：确认排序结果是否符合预期
3. **打印拼接后的字符串**：手动复现拼接过程
4. **对比 MD5/RSA 中间结果**：与服务端日志比对差异
5. **检查编码**：确保统一使用 UTF-8 编码
