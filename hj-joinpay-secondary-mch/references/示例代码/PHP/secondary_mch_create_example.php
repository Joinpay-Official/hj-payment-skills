<?php
/**
 * 二级商户新增示例：secondaryMch.create。
 * 依赖 PHP OpenSSL 扩展。示例只演示协议骨架，请替换所有占位符。
 */

$url = 'https://api.joinpay.com/altFunds';
$merchantNo = 'YOUR_MERCHANT_NO';
$callbackUrl = 'YOUR_CALLBACK_URL';
$merchantPrivateKeyPem = "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----";
$platformPublicKeyPem = "-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----";

function random_alpha_num($length) {
    $chars = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    $out = '';
    for ($i = 0; $i < $length; $i++) {
        $out .= $chars[random_int(0, strlen($chars) - 1)];
    }
    return $out;
}

function aes_encrypt($plain, $aesKey) {
    $cipher = openssl_encrypt($plain, 'AES-128-ECB', $aesKey, OPENSSL_RAW_DATA);
    return base64_encode($cipher);
}

function rsa_sign_md5($data, $privateKeyPem) {
    $ok = openssl_sign($data, $signature, $privateKeyPem, OPENSSL_ALGO_MD5);
    if (!$ok) {
        throw new RuntimeException('RSA sign failed');
    }
    return base64_encode($signature);
}

function rsa_encrypt_public($data, $publicKeyPem) {
    $ok = openssl_public_encrypt($data, $encrypted, $publicKeyPem, OPENSSL_PKCS1_PADDING);
    if (!$ok) {
        throw new RuntimeException('RSA public encrypt failed');
    }
    return base64_encode($encrypted);
}

$aesKey = random_alpha_num(16);
$method = 'secondaryMch.create';
$version = '1.0';
$randStr = random_alpha_num(32);

$data = [
    'mch_order_no' => 'SM' . time() . random_alpha_num(6),
    'callback_url' => $callbackUrl,
    'mch_info' => [
        'login_name' => 'demo_login_' . time(),
        'alt_mch_name' => '示例商户名称',
        'alt_merchant_type' => '12',
        'busi_contact_name' => aes_encrypt('示例联系人', $aesKey),
        'busi_contact_mobile_no' => aes_encrypt('13800000000', $aesKey),
        'bussiness_type' => 'JP3041',
        'province' => '5800',
        'city' => '5810',
        'county' => '5817',
        'manage_scope' => '示例经营范围',
        'manage_addr' => '示例经营地址',
    ],
    'id_card_info' => [
        'legal_person' => aes_encrypt('示例法人', $aesKey),
        'phone_no' => aes_encrypt('13800000000', $aesKey),
        'id_card_no' => aes_encrypt('110101199001011234', $aesKey),
        'id_card_valid_time_begin' => '2020-01-01',
        'id_card_valid_time_end' => '2099-12-31',
        'id_doc_copy' => 'IMAGE_ID_FACE',
        'id_card_national' => 'IMAGE_ID_BACK',
        'beneficiary_owner' => '1',
    ],
    'license_info' => [
        'license_no' => 'LICENSE_NO_PLACEHOLDER',
        'license_expiry' => '2099-12-31',
        'trade_licence' => 'IMAGE_ID_LICENSE',
    ],
    'account_type' => [
        'sett_mode' => '2',
        'sett_date_type' => '1',
        'risk_day' => '1',
    ],
    'account_info' => [
        'bank_account_type' => '4',
        'bank_account_name' => aes_encrypt('示例商户名称', $aesKey),
        'bank_account_no' => aes_encrypt('6222000000000000000', $aesKey),
        'bank_channel_no' => 'BANK_CHANNEL_NO',
        'open_account_licence' => 'IMAGE_ID_BANK',
    ],
    'sales_info' => [[
        'material_id' => 'locallife_proof',
        'material_cont' => [
            ['material_type' => 'door_photo', 'material_info' => 'IMAGE_ID_DOOR'],
            ['material_type' => 'environment_photo', 'material_info' => 'IMAGE_ID_ENV'],
            ['material_type' => 'co_agreement', 'material_info' => 'IMAGE_ID_AGREEMENT'],
        ],
    ]],
];

$dataJson = json_encode($data, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
$signParams = [
    'data' => $dataJson,
    'mch_no' => $merchantNo,
    'method' => $method,
    'rand_str' => $randStr,
    'sign_type' => '2',
    'version' => $version,
];
ksort($signParams);
$pairs = [];
foreach ($signParams as $k => $v) {
    $pairs[] = $k . '=' . $v;
}
$signStr = implode('&', $pairs);

$request = $signParams;
$request['sign'] = rsa_sign_md5($signStr, $merchantPrivateKeyPem);
$request['sec_key'] = rsa_encrypt_public($aesKey, $platformPublicKeyPem);

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
echo $response . PHP_EOL;
