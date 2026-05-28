# 聚合支付签名硬约束（MD5 / RSA）

> **唯一事实来源** — 本文件是整个 skill 包中签名约束的唯一定义。
> 违反以下规则将导致服务端 **100% 验签失败**。

本文件仅适用于聚合支付 `/tradeRt/*` 的 `p0_/q*/hmac` 协议。二级商户入网 `/altFunds` 使用 API Gateway JSON 协议，见 [api-gateway-signing-rules.md](../../hj-joinpay-pay-shared-base/protocol/api-gateway-signing-rules.md)。

---

## [P0] MD5 签名约束（sign_type=MD5 时必须遵守）

### 核心规范

| 规范项 | 正确实现 | 错误实现（禁止） |
|--------|---------|-----------------|
| **拼接格式** | 只拼接 **value**，不包含 key 名 | `"key=value&key=value"` 格式 |
| **空值处理** | 过滤掉空字符串和 null 值 | 将空值参与拼接 |
| **密钥位置** | 在末尾追加密钥后整体 MD5 哈希 | 先对各段分别 MD5 再组合 |
| **大小写** | 最终结果 `.toUpperCase()` | 小写输出 |

### 签名字符串构建示例

```java
// ✅ 正确：只拼接 value（与 SignBiz.java:81-113 一致）
// 参数: p0_Version=2.6, p1_MerchantNo=888100500008456, p3_Amount=0.01
// 排序后 keys: [p0_Version, p1_MerchantNo, p3_Amount]
// 签名串:   "2.68881005000084560.01"
// 最终 hmac = MD5("2.68881005000084560.01" + merchantKey).toUpperCase()

// ❌ 错误：包含 key 名
// 签名串: "p0_Version=2.6&p1_MerchantNo=888100500008456&p3_Amount=0.01"  // 错误！
```

### 签名错误排查速查表

| ❌ 错误做法 | ✅ 正确做法 |
|-----------|-----------|
| `key=value&key=value` 拼接 | 只拼接 `value`，无分隔符 |
| `key=xxx` 追加密钥 | `valuevaluevalue` + `密钥` |
| 空值参与签名 | 空值**跳过**不参与 |
| 先MD5各段再拼接 | 整体MD5 |

### 完整代码实现

> 🔴 **严禁自行实现签名算法！** 必须从下方复制代码并按注释修改。
>
> 多语言版本见：[signing-rules.md](../../hj-joinpay-pay-shared-base/protocol/signing-rules.md)

```java
// ========== MD5签名（必须复制此代码） ==========
import java.security.MessageDigest;
import java.util.*;

public class SignUtils {
    public static String signMD5(Map<String, String> params, String key) {
        // ① 创建TreeMap自动按key排序
        TreeMap<String, String> sortedParams = new TreeMap<>();
        
        // ② 过滤空值和hmac
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = entry.getValue();
            if ("hmac".equalsIgnoreCase(entry.getKey())) continue; // 跳过hmac本身
            if (value != null && !value.trim().isEmpty()) {
                sortedParams.put(entry.getKey(), value.trim());
            }
        }
        
        // ③ 只拼接所有value（❌ 不是 key=value& 格式）
        StringBuilder content = new StringBuilder();
        for (String value : sortedParams.values()) {
            content.append(value);
        }
        
        // ④ 末尾追加密钥后MD5
        String signStr = content.toString() + key;
        
        return md5(signStr).toUpperCase();
    }
    
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xff);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5签名失败", e);
        }
    }
}
```

---

## [P0] RSA 签名约束（sign_type=RSA 时必须遵守）

### 核心规范

| 规范项 | 正确实现 | 错误实现（禁止） |
|--------|---------|-----------------|
| **签名算法** | `MD5withRSA`（与 RSAUtils.java:76 一致） | `SHA256withRSA` / `SHA1withRSA` 等其他算法 |
| **待签名串** | 只拼接 **value**，不包含 key | `"key=" + key + "&value=" + value` 或 `"key=value"` 格式 |
| **私钥格式** | PKCS8 PEM（含 -----BEGIN PRIVATE KEY-----） | PKCS1 / 其他格式 |

### 签名字符串构建示例

```java
// ✅ 正确：只拼接 value
// 参数: p0_Version=2.6, p1_MerchantNo=888100500008456, p3_Amount=0.01
// 排序后 keys: [p0_Version, p1_MerchantNo, p3_Amount]
// 签名串:   "2.68881005000084560.01"
// 签名结果 = MD5withRSA(签名字符串, 私钥)

// ❌ 错误：包含 key 名
// 签名串: "p0_Version=2.6&p1_MerchantNo=888100500008456&p3_Amount=0.01"  // 错误！
```

### 完整签名规则（多语言）

详见 → [signing-rules.md](../../hj-joinpay-pay-shared-base/protocol/signing-rules.md)
