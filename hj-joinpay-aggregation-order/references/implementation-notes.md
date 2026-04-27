# 下单实现注意事项

> 本文件收纳 `aggregation-order` SKILL.md 中需要深入展开的实现细节。
> 包含代码级约束、框架级建议和前端避坑指南。

---

## 1. [P0] 签名必须复制粘贴文档示例

**严禁自行实现签名算法！** 完整 SignUtils.java 代码及 MD5/RSA 规范 → [signing-constraints.md](../../hj-payment-integration/shared-rules/signing-constraints.md)

### 签名错误排查速查表

| ❌ 错误做法 | ✅ 正确做法 |
|-----------|-----------|
| `key=value&key=value` 拼接 | 只拼接 `value`，无分隔符 |
| `key=xxx` 追加密钥 | `valuevaluevalue` + `密钥` |
| 空值参与签名 | 空值**跳过**不参与 |
| 先MD5各段再拼接 | 整体MD5 |

---

## 2. [P0] 微信主扫前端必须用 Thymeleaf th:if 控制显示

### 正确做法

```html
<!-- ✅ 用 th:if 精确控制，二维码区域是否存在由后端决定 -->
<div id="qrcodeSection" th:if="${order.status == 'PAYING' and order.qrCodeUrl != null}">
    <div id="qrcode"></div>
</div>
```

### 错误做法

```html
<!-- ❌ 用 th:classappend 动态添加 class，可能导致元素不存在 -->
<div class="qrcode-section" th:classappend="${order.status == 'PAYING' ? 'active' : ''}">
```

### CSS 额外避坑

当使用 `th:if` 控制元素存在性时，CSS **禁止再用 `display:none` 隐藏该元素**！

| ❌ 错误做法 | ✅ 正确做法 |
|-----------|-----------|
| `.qrcode-section { display:none; }` + JS 添加 `.active { display:block; }` | 完全依赖 `th:if` 控制元素存在性，CSS 只负责样式 |
| `visibility: hidden` 配合 JS 切换 | 不需要显隐切换逻辑，th:if=false 时元素不在 DOM 中 |

```css
/* ✅ CSS 只管样式，不管显隐 */
#qrcodeSection {
    text-align: center;
    padding: 20px;
    border: 1px dashed #ccc;
    border-radius: 8px;
}
```

---

## 3. [P0] rc_Result 必须持久化并传递给前端

> **为什么重要**：汇聚返回的 `rc_Result` 是支付二维码URL。如果不存入订单字段、不更新状态、不传给前端，二维码永远无法展示。此错误不报异常，只是静默失败，极难排查。

获取到 `rc_Result` 后必须执行 **强制三步流程**：

```
步骤① → 将 rc_Result 值存入订单的 qrCodeUrl 字段（持久化到数据库）
步骤② → 将订单状态更新为 PAYING
步骤③ → 通过模板引擎将订单对象（含 qrCodeUrl）传给前端页面
```

| ❌ 错误做法 | ✅ 正确做法 |
|-----------|-----------|
| `log.info("二维码URL: " + rc_Result);` 只打印日志 | `order.setQrCodeUrl(rc_Result); order.setStatus("PAYING"); orderRepository.save(order);` |
| 放入 HttpSession / Redis 但不持久化 | 存入数据库，确保刷新后仍可获取 |
| 返回 JSON 但不更新订单状态 | 同时保存 URL 和更新状态，缺一不可 |

---

## 4. 响应解析（JSON 格式）

> ⚠️ **汇聚下单接口请求用 form-urlencoded 发送，但响应是 JSON 格式！**
>
> 如果按 form 格式（`&` 分割、`=` 取值）解析 JSON 响应，会导致所有字段值为 `null`，
> 表现为"支付下单失败: null (code=null)"且不报异常，**极难排查**。

### parseResponse Java 实现

```java
// ========== 响应解析（必须复制此代码） ==========
private Map<String, String> parseResponse(String response) throws Exception {
    String trimmed = response.trim();
    if (trimmed.startsWith("{")) {
        // ✅ JSON格式 → 用Jackson解析（汇聚下单接口标准返回格式）
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(trimmed, new TypeReference<Map<String, String>>() {});
    } else {
        // 降级：form格式解析（兼容可能的接口变更）
        Map<String, String> result = new HashMap<>();
        for (String pair : trimmed.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length >= 1) {
                result.put(kv[0], kv.length > 1 ? kv[1] : "");
            }
        }
        return result;
    }
}

// 调用示例
String responseBody = httpClient.post(url, params);
Map<String, String> resp = parseResponse(responseBody);
String raCode = resp.get("ra_Code");
if (!"100".equals(raCode)) {
    throw new RuntimeException("下单失败: " + resp.get("rb_CodeMsg") + " (code=" + raCode + ")");
}
String qrCodeUrl = resp.get("rc_Result"); // 微信主扫返回的支付二维码URL
```

---

## 5. 微信主扫 Web 完整实现模式

### 后端处理（含响应解析与持久化）

```
步骤1 - 构建请求参数并签名 → HTTP POST /tradeRt/uniPay
步骤2 - 解析响应（⚠️ 必须按 JSON 格式解析！见上方 §4）
步骤3 - 校验 ra_Code == "100"，提取 rc_Result（支付二维码 URL）
步骤4 - 【强制】三步持久化：
         (1) order.setQrCodeUrl(rc_Result)
         (2) order.setStatus("PAYING")
         (3) orderRepository.save(order)
步骤5 - 将订单对象传给前端模板（含 qrCodeUrl / orderNo / amount / status）
```

### 前端展示（三态流转）

| 状态 | 展示内容 |
|------|---------|
| PENDING | 显示"立即支付"按钮 |
| PAYING | 展示二维码（qrcode.js 将 qrCodeUrl 转图片）+ 扫码提示 + 每5秒轮询状态 |
| PAID | 显示"支付成功" |

### 关键实现要点

- ⭐ qrCodeUrl 必须通过模板引擎内联为 JS 变量输出，避免 data-* 属性时序问题
  - Thymeleaf: `th:inline="javascript"` + `var url = [[${order.qrCodeUrl}]];`
  - Vue/React: props 或 API 响应直接绑定到 state
- ⭐ qrcode.js 推荐双 CDN 备份加载（jsdelivr 主 + cdnjs 备），防止单点失败
- ⭐ 必须提供订单状态查询接口供轮询（GET /api/order/status?id=xxx）
- 支付页面结构建议：订单信息区 + 二维码容器 + 提示文案

### 回调闭环

```
汇聚异步通知到达 → 验签 → 更新订单为 PAID → 前端轮询检测到后自动刷新
```

## 6. [P0] Thymeleaf 内联 JS 的两个致命陷阱

> **为什么重要**：这两个错误都不会报编译错误或运行时异常，页面能正常渲染，但二维码永远不会生成。表现为"点击支付后白屏"或"看不到二维码"，极难排查。

### 陷阱一：缺少 `th:inline="javascript"`

Thymeleaf 的 `[[${...}]]` 语法**只在** `<script th:inline="javascript">` 标签内才会被解析。缺少该属性时，`[[...]]` 被当作普通文本原样输出，JS 变量得到的是字面量字符串而非实际值。

| ❌ 错误做法 | ✅ 正确做法 |
|-----------|-----------|
| `<script>` （缺少 th:inline） | `<script th:inline="javascript">` |

### 陷阱二：手动给 `[[${...}]]` 加引号导致双重引号

在 `th:inline="javascript"` 模式下，`[[${stringVar}]]` 对字符串类型**自动加引号**。如果手动再套一层引号，会产生双重引号导致 JS 语法错误或值不对。

| ❌ 错误做法 | ✅ 正确做法 |
|-----------|-----------|
| `var status = '[[${order.status}]]';` → 输出 `var status = ''PAYING'';` | `var status = [[${order.status}]];` → 输出 `var status = 'PAYING';` |
| `var url = "[[${order.qrCodeUrl}]]";` → 输出 `var url = "'https://...'";` | `var url = [[${order.qrCodeUrl}]];` → 输出 `var url = 'https://...';` |

> **规则**：`th:inline="javascript"` 模式下，`[[${var}]]` 对 String 自动加 `'...'`，对 Number/Boolean 直接输出。一律不要手动加引号。

### 完整正确示例

```html
<!-- ✅ 必须有 th:inline="javascript"，变量不加手写引号 -->
<script th:inline="javascript">
    var orderId = [[${order.id}]];          // 数字类型：输出 42
    var status = [[${order.status}]];       // 字符串：输出 'PAYING'
    var qrUrl = [[${order.qrCodeUrl}]];     // 字符串：输出 'https://...'
</script>
```

### 避坑提醒

- 不要直接用 rd_Pic 的 base64 拼 `<img>`（可能裂图）
- 不要用第三方在线 QR API 生成二维码（可能被墙或超时）
- 推荐 rc_Result（二维码 URL）+ 前端 qrcode.js 库生成图片
