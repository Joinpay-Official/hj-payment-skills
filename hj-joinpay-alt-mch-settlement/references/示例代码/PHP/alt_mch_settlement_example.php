<?php
/**
 * 分账方入网与结算示例：altmch.create / altSettle.launch / altAccount.get。
 * 依赖 PHP cURL 扩展。示例只演示 /allocFunds 协议骨架，请替换所有占位符。
 * V1.4.2 分账方添加无 AES 加密；RSA 使用 sign_type=21 + MD5withRSA。
 */

$url = 'https://www.joinpay.com/allocFunds';
$merchantNo = 'YOUR_MERCHANT_NO';
$merchantKey = 'YOUR_MERCHANT_KEY';
$callbackUrl = 'YOUR_CALLBACK_URL';
$signType = '1'; // 1=MD5，21=RSA(MD5withRSA)
$merchantPrivateKeyPem = "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----";
$jpPublicKeyPem = "-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----";

function random_alpha_num($length) {
    $chars = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    $out = '';
    for ($i = 0; $i < $length; $i++) {
        $out .= $chars[random_int(0, strlen($chars) - 1)];
    }
    return $out;
}

function md5_sign($signParams, $merchantKey) {
    return md5(build_sign_string($signParams) . '&key=' . $merchantKey);
}

function aes_encrypt($plain, $aesKey) {
    throw new RuntimeException('V1.4.2 altmch.create does not use AES encryption.');
}

function hex_text($text) {
    return strtoupper(bin2hex($text));
}

function rsa_sign_md5($signParams, $privateKeyPem) {
    $ok = openssl_sign(build_sign_string($signParams), $signature, $privateKeyPem, OPENSSL_ALGO_MD5);
    if (!$ok) {
        throw new RuntimeException('RSA sign failed');
    }
    return base64_encode($signature);
}

function rsa_verify_md5($data, $publicKeyPem, $signValue) {
    $base64Text = hex2bin($signValue);
    if ($base64Text === false) {
        throw new InvalidArgumentException('Invalid RSA sign hex text');
    }
    $signature = base64_decode($base64Text, true);
    if ($signature === false) {
        throw new InvalidArgumentException('Invalid RSA sign base64 text');
    }
    return openssl_verify($data, $signature, $publicKeyPem, OPENSSL_ALGO_MD5) === 1;
}

function build_sign_string($signParams) {
    ksort($signParams);
    $pairs = [];
    foreach ($signParams as $k => $v) {
        $pairs[] = $k . '=' . $v;
    }
    return implode('&', $pairs);
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

function request_alloc_funds($method, $version, $data, $aesKey = null) {
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
    if ($aesKey !== null && $aesKey !== '') {
        $request['aes_key'] = $aesKey;
    }

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

function create_alt_mch() {
    global $callbackUrl;
    $altMchName = '示例分账方名称';
    $data = [
        'login_name' => 'alt_demo_' . time(),
        'alt_mch_name' => $altMchName,
        'alt_mch_short_name' => '示例分账方',
        'alt_merchant_type' => '12',
        'phone_no' => '13800000000',
        'legal_person' => '示例法人',
        'id_card_no' => '110101199001011234',
        'id_card_expiry' => '2099-12-31',
        'busi_contact_name' => '示例联系人',
        'busi_contact_mobile_no' => '13800000000',
        'license_no' => '91440101MA00000000',
        'license_expiry' => '2099-12-31',
        'manage_scope' => '信息技术服务',
        'manage_addr' => '广州市示例路1号',
        'sett_mode' => '2',
        'sett_date_type' => '2',
        'risk_day' => '1',
        'bank_account_type' => '4',
        'bank_account_name' => $altMchName,
        'bank_account_no' => '6222000000000000000',
        'bank_channel_no' => 'BANK_CHANNEL_NO_PLACEHOLDER',
        'notify_url' => $callbackUrl,
    ];
    return request_alloc_funds('altmch.create', '1.1', $data);
}

function launch_settle($altMchNo) {
    global $callbackUrl;
    $data = [
        'alt_mch_no' => $altMchNo,
        'product_code' => '2',
        'settle_amount' => '10.00',
        'settle_fee' => '0.00',
        'mch_order_no' => 'ST' . time() . random_alpha_num(6),
        'callback_url' => $callbackUrl,
    ];
    return request_alloc_funds('altSettle.launch', '1.1', $data);
}

function query_account($altMchNo) {
    return request_alloc_funds('altAccount.get', '1.1', ['alt_mch_no' => $altMchNo]);
}

echo create_alt_mch() . PHP_EOL;
echo launch_settle('ALT_MCH_NO_PLACEHOLDER') . PHP_EOL;
echo query_account('ALT_MCH_NO_PLACEHOLDER') . PHP_EOL;
