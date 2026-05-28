<?php
/**
 * 延迟分账与多次分账示例：altHandle.singleLaterAllocate / allocateQuery /
 * manyLaterAllocate / finishAllocate / altManyOrderQuery / altManyTotalQuery。
 * 依赖 PHP cURL 扩展。示例只演示 /allocFunds V1.3.0 协议骨架，请替换所有占位符。
 * 金额字段按文档要求使用字符串；RSA 使用 sign_type=21 + MD5withRSA。
 */

$url = 'https://www.joinpay.com/allocFunds';
$merchantNo = 'YOUR_MERCHANT_NO';
$merchantKey = 'YOUR_MERCHANT_KEY';
$callbackUrl = 'YOUR_CALLBACK_URL';
$signType = '1'; // 1=MD5，21=RSA(MD5withRSA)
$merchantPrivateKeyPem = "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----";

function random_alpha_num($length) {
    $chars = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    $out = '';
    for ($i = 0; $i < $length; $i++) {
        $out .= $chars[random_int(0, strlen($chars) - 1)];
    }
    return $out;
}

function build_sign_string($signParams) {
    ksort($signParams);
    $pairs = [];
    foreach ($signParams as $k => $v) {
        $pairs[] = $k . '=' . $v;
    }
    return implode('&', $pairs);
}

function md5_sign($signParams, $merchantKey) {
    return strtoupper(md5(build_sign_string($signParams) . '&key=' . $merchantKey));
}

function rsa_sign_md5($signParams, $privateKeyPem) {
    $ok = openssl_sign(build_sign_string($signParams), $signature, $privateKeyPem, OPENSSL_ALGO_MD5);
    if (!$ok) {
        throw new RuntimeException('RSA sign failed');
    }
    return base64_encode($signature);
}

function sign_request($signParams) {
    global $signType, $merchantKey, $merchantPrivateKeyPem;
    if ($signType === '1') {
        return md5_sign($signParams, $merchantKey);
    }
    if ($signType === '21') {
        return rsa_sign_md5($signParams, $merchantPrivateKeyPem);
    }
    throw new InvalidArgumentException('signType must be 1 or 21');
}

function request_alloc_funds($method, $version, $data) {
    global $url, $merchantNo, $signType;

    $dataJson = json_encode($data, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    $signParams = [
        'data' => $dataJson,
        'mch_no' => $merchantNo,
        'method' => $method,
        'rand_str' => random_alpha_num(32),
        'sign_type' => $signType,
        'version' => $version,
    ];
    $request = $signParams;
    $request['sign'] = sign_request($signParams);

    $ch = curl_init($url);
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_HTTPHEADER => ['Content-Type: application/json;charset=UTF-8'],
        CURLOPT_POSTFIELDS => json_encode($request, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES),
        CURLOPT_RETURNTRANSFER => true,
    ]);
    $response = curl_exec($ch);
    if ($response === false) {
        throw new RuntimeException(curl_error($ch));
    }
    curl_close($ch);
    return $response;
}

function single_later_allocate($mchOrderNo, $altMchNo) {
    global $callbackUrl;
    $data = [
        'mch_order_no' => $mchOrderNo,
        'alt_order_no' => 'SGL' . time() . random_alpha_num(6),
        'alt_info' => [
            [
                'alt_mch_no' => $altMchNo,
                'alt_amount' => '6.00',
            ],
        ],
        'callback_url' => $callbackUrl,
    ];
    return request_alloc_funds('altHandle.singleLaterAllocate', '1.1', $data);
}

function query_delay_allocate($altOrderNo) {
    return request_alloc_funds('altHandle.allocateQuery', '1.1', ['alt_order_no' => $altOrderNo]);
}

function many_later_allocate($mchOrderNo, $altMchNo) {
    global $callbackUrl;
    $data = [
        'mch_order_no' => $mchOrderNo,
        'alt_order_no' => 'ALT' . time() . random_alpha_num(6),
        'alt_this_amount' => '10.00',
        'alt_this_marketing_amount' => '0.00',
        'alt_info' => [
            [
                'alt_mch_no' => $altMchNo,
                'alt_amount' => '6.00',
            ],
        ],
        'callback_url' => $callbackUrl,
    ];
    return request_alloc_funds('altHandle.manyLaterAllocate', '1.1', $data);
}

function finish_allocate($mchOrderNo) {
    global $callbackUrl;
    $data = [
        'mch_order_no' => $mchOrderNo,
        'alt_order_no' => 'FIN' . time() . random_alpha_num(6),
        'callback_url' => $callbackUrl,
    ];
    return request_alloc_funds('altHandle.finishAllocate', '1.1', $data);
}

function query_single_allocate($altOrderNo) {
    return request_alloc_funds('altHandle.altManyOrderQuery', '1.1', ['alt_order_no' => $altOrderNo]);
}

function query_total_allocate($mchOrderNo) {
    return request_alloc_funds('altHandle.altManyTotalQuery', '1.1', ['mch_order_no' => $mchOrderNo]);
}

$paidOrderNo = 'PAID_ORDER_NO_PLACEHOLDER';
$altMchNo = 'ALT_MCH_NO_PLACEHOLDER';
$altOrderNo = 'ALT_ORDER_NO_PLACEHOLDER';

echo single_later_allocate($paidOrderNo, $altMchNo) . PHP_EOL;
echo query_delay_allocate($altOrderNo) . PHP_EOL;
echo many_later_allocate($paidOrderNo, $altMchNo) . PHP_EOL;
echo finish_allocate($paidOrderNo) . PHP_EOL;
echo query_single_allocate($altOrderNo) . PHP_EOL;
echo query_total_allocate($paidOrderNo) . PHP_EOL;
