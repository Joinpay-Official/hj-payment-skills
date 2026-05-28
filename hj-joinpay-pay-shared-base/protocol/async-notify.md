# 异步通知规则

## notify_url 配置

支付完成后，汇聚平台会向商户配置的 `p9_NotifyUrl` 发送**异步通知**。

| 项目 | 说明 |
|------|------|
| 通知方式 | HTTP POST (form-urlencoded) |
| 触发时机 | 支付成功后立即通知 |
| 重试机制 | 共通知 **15 次**，频率递增（推荐实现幂等） |
| 响应要求 | 处理成功返回纯文本 `success`（不含 HTML 标签） |
| 超时时间 | 商户接口须在 **5 秒** 内返回 |

## 回调通知参数

与下单接口响应结构一致，关键字段：

| 参数 | 含义 | 示例 |
|------|------|------|
| r2_OrderNo | 商户订单号 | ORDER20260413001 |
| r7_TrxNo | 平台交易流水号 | 20260413000012345 |
| ra_Code | 响应码（100=成功） | 100 |
| ra_Status | 交易状态（100=成功） | 100 |
| rb_CodeMsg | 响应描述 | 成功 |
| rc_Result | 结果数据 | （根据支付方式不同） |
| hmac | 签名 | （用于验签） |

## 回调处理标准流程

```text
收到 POST 请求
    │
    ▼
① 验签（必须先验签！）
    │   - 使用与签名相同的方式重新计算 hmac
    │   - 对比计算的 hmac 与传入的是否一致
    │   - 不一致则直接返回非 success，终止处理
    │
    ▼
② 检查 ra_Code 和 ra_Status
    │   - ra_Code == "100" 且 ra_Status == "100" 表示支付成功
    │
    ▼
③ 业务幂等检查
    │   - 查询本地数据库该订单是否已处理
    │   - 已处理则直接返回 success（防止重复通知）
    │   - 未处理则继续后续流程
    │
    ▼
④ 更新订单状态
    │   - 更新本地订单状态为"支付成功"
    │   - 记录交易流水号(r7_TrxNo)
    │   - 发货/解锁库存等业务操作
    │
    ▼
⑤ 返回 success
    │   - 纯文本 "success"，不要带 HTML/BOM 头
    │
    ▼
完成
```

## Java 回调处理示例代码骨架

```java
@RequestMapping("/notify")
@ResponseBody
public String payNotify(HttpServletRequest request) throws Exception {
    Map<String, String[]> paramMap = request.getParameterMap();
    Map<String, Object> params = new HashMap<>();
    for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
        params.put(entry.getKey(), entry.getValue()[0]);
    }

    // ① 验签
    boolean valid = signBiz.signData(
        (String) params.get("r1_MerchantNo"),
        (String) params.get("r2_OrderNo"),
        params
    );
    if (!valid) {
        log.warn("回调验签失败: {}", params);
        return "fail";
    }

    // ② 检查状态
    String code = (String) params.get("ra_Code");
    String status = (String) params.get("ra_Status");
    if (!"100".equals(code) || !"100".equals(status)) {
        return "fail";
    }

    // ③ 幂等检查
    String orderNo = (String) params.get("r2_OrderNo");
    PaymentOrder existingOrder = orderService.getByOrderNo(orderNo);
    if (existingOrder != null && "PAID".equals(existingOrder.getStatus())) {
        return "success"; // 已处理过，直接返回成功
    }

    // ④ 业务处理
    orderService.markAsPaid(orderNo, (String) params.get("r7_TrxNo"));

    // ⑤ 返回
    return "success";
}
```

## 重试策略

汇聚平台的回调通知重试间隔：

| 次数 | 间隔（约） | 累计时间 |
|------|-----------|---------|
| 第 1 次 | 立即 | 0s |
| 第 2~5 次 | 1~5 分钟 | ~15min |
| 第 6~10 次 | 10~30 分钟 | ~3h |
| 第 11~15 次 | 1~2 小时 | ~18h |

> 总共最多尝试 **15 次**，持续约 **24 小时**。请务必保证幂等！

## 常见问题

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 收不到回调通知 | notify_url 不可达 | 确保URL公网可访问（不要用localhost），检查防火墙/白名单 |
| 验签失败 | 编码问题/参数缺失 | 确认 UTF-8 编码，检查是否有 BOM 头 |
| 重复发货 | 未做幂等 | 用 orderNo 做唯一索引，先查后改 |
| 超时重试过多 | 处理逻辑太耗时 | 异步化处理，先快速返回 success 再异步执行业务 |
| IP 白名单问题 | 汇聚服务器 IP 变化 | 检查汇聚提供的 IP 白名单列表 |
