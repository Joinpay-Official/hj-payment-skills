# 凭据与参数收集（统一流程）

> **唯一事实来源** — 本文件是整个 skill 包中参数收集流程的唯一定义。
> 各业务 Skill（base/order/query/refund）均引用此处，**禁止在别处重复此流程**。

---

## 触发条件

当用户意图涉及以下任一场景时，必须先完成参数收集：

| 场景 | 是否需要收集 |
|------|-------------|
| 用户要求接入支付、写支付代码、集成支付功能 | ✅ 必须收集 |
| 用户要求查询/关单/退款的代码实现 | ✅ 必须收集 |
| 用户只是咨询业务知识（问参数含义、签名规则等） | ❌ 不收集 |
| 用户只是查看示例代码（只展示不写入） | ❌ 不收集 |
| 参数已在本次对话中收集过 | ❌ 不重复收集 |

---

## 收集方式：混合模式

> **统一交互规范**：
> - **选项型参数** → 使用 `ask_followup_question` 工具以结构化表单形式一次性收集
> - **文本输入型参数** → 通过普通对话消息逐项询问（每问一个等回复后再问下一个）
> - **校验** → 收到每个参数后当场校验，不合格立即让用户修正
> - 所有校验通过后方可继续后续操作

### 第一步：选项参数（ask_followup_question）

```json
{
  "title": "汇聚支付接入 - 基础配置",
  "questions": [
    {
      "id": "sign_type",
      "question": "请选择签名方式：",
      "options": ["MD5（简单快捷）", "RSA（安全性更高）"],
      "multiSelect": false
    },
    {
      "id": "base_url",
      "question": "请选择接口环境：",
      "options": [
        "测试环境 https://trade.joinpay.cc",
        "生产环境 https://trade.joinpay.com"
      ],
      "multiSelect": false
    }
  ]
}
```

### 第二步：文本参数（逐项对话）

收到用户选项回答后，通过普通对话消息逐项询问。每问一个等回复后再问下一个。

| 顺序 | 参数ID | 问题文案 | 输入提示 | 备注 |
|------|--------|---------|---------|------|
| 1 | merchant_no | 请输入您的汇聚支付商户号（在商户后台「账户信息」中查看）： | 纯数字字符串 | 必填 |
| 2 | notify_url | 请输入异步回调通知地址： | 格式如 `https://your-domain.com/api/pay/notify`、`http://127.0.0.1:8080/notify` 或 `http://localhost:8080/notify` | 下单/退款接口必填；查询/关单不需要 |
| 3a | merchant_key | 请输入MD5签名密钥（32位，在商户后台「秘钥管理」中获取）： | 32位字符串 | 仅 MD5 模式必填 |
| 3b | rsa_private_key | 请输入RSA私钥（PKCS8 PEM格式，含 -----BEGIN/END----- 标记）： | PEM格式文本块 | 仅 RSA 模式必填 |
| 4 | trade_merchant_no | 请输入报备商户号（在商户后台「报备管理」中获取）： | 纯数字字符串 | 必填 |

### 第三步：当场校验

| 校验项 | 规则 | 不合格时的提示 |
|--------|------|---------------|
| merchant_no | 非空、纯数字 | 商户号不能为空，请在汇聚商户后台 → 账户信息中查看 |
| base_url | 以 https:// 开头 | 接口地址格式错误 |
| notify_url | 以 http(s):// 开头 | 回调地址格式错误，请输入完整 URL（例如 `https://your-domain.com/api/pay/notify` 或 `http://localhost:8080/notify`） |
| merchant_key(MD5) | 非空 | MD5密钥不能为空，请在商户后台「秘钥管理」中获取 |
| rsa_private_key(RSA) | 含 BEGIN PRIVATE KEY / END 标记 | RSA私钥格式不正确，应为 PKCS8 PEM 格式（含 -----BEGIN/END----- 标记） |
| trade_merchant_no | 非空、纯数字 | 报备商户号不能为空，请在汇聚商户后台 → 报备管理中查看 |

> **所有校验通过后方可进入下一步操作。任何一项不通过都应立即拦截，不继续后续操作。**

---

## 参数缓存

校验通过后将参数缓存到上下文供后续所有步骤引用：

```
__jp_sign_type         = "MD5" | "RSA"
__jp_merchant_no       = 商户号
__jp_base_url          = 接口地址(全称)
__jp_notify_url        = 回调地址（下单/退款时需要）
__jp_merchant_key      = MD5密钥 (MD5模式)
__jp_rsa_private_key   = RSA私钥 (RSA模式)
__jp_rsa_public_key    = RSA公钥 (RSA模式, 可选)
__jp_trade_merchant_no = 报备商户号
```

## 各 Skill 差异说明

本文件定义的是完整参数集。各业务 Skill 在引用时可根据自身需求简化：

| Skill | 需要的参数子集 | 差异说明 |
|-------|--------------|---------|
| aggregation-order | 全部（merchant_no + notify_url + key + trade_merchant_no） | 下单接口需要回调地址和报备商户号 |
| aggregation-query | merchant_no + key（无需 notify_url / trade_merchant_no） | 查询/关单只需基础凭据 |
| aggregation-refund | merchant_no + notify_url + key（无需 trade_merchant_no） | 退款申请接口需要回调地址；退款查询类场景只需基础凭据 |
