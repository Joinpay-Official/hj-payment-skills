package com.joinpay.sdk.example;

import com.alibaba.fastjson.JSONObject;
import com.joinpay.sdk.JoinPayClient;

/**
 * 4. 退款查询示例
 *
 * 根据退款订单号查询单笔退款的状态和详情。
 */
public class QueryRefundExample {

    public static void main(String[] args) throws Exception {
        JoinPayClient client = new JoinPayClient(
            "https://trade.joinpay.com",
            "your_merchant_no",
            "your_merchant_key_32chars",
            null,
            "MD5"
        );

        String refundOrderNo = "REF_ORDER20260413001";
        JSONObject response = client.queryRefund(refundOrderNo);

        String rb_code = response.getString("rb_Code");
        String ra_status = response.getString("ra_Status");
        
        // 根据ra_Status判断退款状态
        if ("100".equals(ra_status)) {
            // 退款成功
            String refundAmount       = response.getString("r3_RefundAmount");      // 退款金额
            String refundTrxNo        = response.getString("r4_RefundTrxNo");        // 退款流水号
            String refundCompleteTime = response.getString("r5_RefundCompleteTime"); // 完成时间

            System.out.println("退款单号:   " + refundOrderNo);
            System.out.println("退款金额:   " + refundAmount);
            System.out.println("退款流水号: " + refundTrxNo);
            System.out.println("完成时间:   " + refundCompleteTime);
            System.out.println("状态:       ✅ 退款成功");
        } else if ("102".equals(ra_status)) {
            // 退款处理中
            System.out.println("退款单号:   " + refundOrderNo);
            System.out.println("状态:       🔄 退款处理中");
            System.out.println("建议:       稍后再次查询确认最终状态");
        } else if ("101".equals(ra_status)) {
            // 退款失败
            String rc_msg = response.getString("rc_CodeMsg");
            System.out.println("❌ 退款失败!");
            System.out.println("响应码:     " + rb_code);
            System.out.println("错误信息:   " + rc_msg);
        } else {
            String rc_msg = response.getString("rc_CodeMsg");
            System.out.println("查询失败: " + rc_msg);
        }
    }

    /**
     * 翻译退款状态（仅用于显示，实际判断应使用ra_Status数值）
     * @deprecated 已废弃，直接判断ra_Status数值即可
     */
    private static String translateRefundStatus(String status) {
        switch (status != null ? status : "") {
            case "100":  return "退款成功";
            case "101":  return "退款失败";
            case "102":  return "退款处理中";
            default:     return status;
        }
    }
}
