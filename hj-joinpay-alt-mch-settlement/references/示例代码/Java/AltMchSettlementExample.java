import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 分账方入网与结算示例：altmch.create / altSettle.launch / altAccount.get。
 * 示例只演示 /allocFunds 协议骨架，请替换所有占位符。
 */
public class AltMchSettlementExample {
    private static final String URL = "https://www.joinpay.com/allocFunds";
    private static final String MERCHANT_NO = "YOUR_MERCHANT_NO";
    private static final String MERCHANT_KEY = "YOUR_MERCHANT_KEY";
    private static final String CALLBACK_URL = "YOUR_CALLBACK_URL";
    private static final String SIGN_TYPE = "1"; // 1=MD5，21=RSA(MD5withRSA)
    private static final String MERCHANT_PRIVATE_KEY_PEM = "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----";
    private static final String JP_PUBLIC_KEY_PEM = "-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----";
    private static final String SIGNATURE_ALGORITHM_SHA1 = "SHA1withRSA";
    private static final String SIGNATURE_ALGORITHM_MD5 = "MD5withRSA";

    public static void main(String[] args) throws Exception {
        System.out.println(createAltMch());
        System.out.println(launchSettle("ALT_MCH_NO_PLACEHOLDER"));
        System.out.println(queryAccount("ALT_MCH_NO_PLACEHOLDER"));
    }

    private static String createAltMch() throws Exception {
        String altMchName = "示例分账方名称";
        String dataJson = "{"
                + "\"login_name\":\"alt_demo_" + System.currentTimeMillis() + "\","
                + "\"alt_mch_name\":\"" + altMchName + "\","
                + "\"alt_mch_short_name\":\"示例分账方\","
                + "\"alt_merchant_type\":\"12\","
                + "\"phone_no\":\"13800000000\","
                + "\"legal_person\":\"示例法人\","
                + "\"id_card_no\":\"110101199001011234\","
                + "\"id_card_expiry\":\"2099-12-31\","
                + "\"busi_contact_name\":\"示例联系人\","
                + "\"busi_contact_mobile_no\":\"13800000000\","
                + "\"license_no\":\"91440101MA00000000\","
                + "\"license_expiry\":\"2099-12-31\","
                + "\"manage_scope\":\"信息技术服务\","
                + "\"manage_addr\":\"广州市示例路1号\","
                + "\"sett_mode\":\"2\","
                + "\"sett_date_type\":\"2\","
                + "\"risk_day\":\"1\","
                + "\"bank_account_type\":\"4\","
                + "\"bank_account_name\":\"" + altMchName + "\","
                + "\"bank_account_no\":\"6222000000000000000\","
                + "\"bank_channel_no\":\"BANK_CHANNEL_NO_PLACEHOLDER\","
                + "\"notify_url\":\"" + CALLBACK_URL + "\""
                + "}";
        return requestAllocFunds("altmch.create", "1.1", dataJson, null);
    }

    private static String launchSettle(String altMchNo) throws Exception {
        BigDecimal settleAmount = new BigDecimal("10.00");
        String dataJson = "{"
                + "\"alt_mch_no\":\"" + altMchNo + "\","
                + "\"product_code\":\"2\","
                + "\"settle_amount\":\"" + settleAmount.setScale(2) + "\","
                + "\"settle_fee\":\"0.00\","
                + "\"mch_order_no\":\"ST" + System.currentTimeMillis() + randomAlphaNum(6) + "\","
                + "\"callback_url\":\"" + CALLBACK_URL + "\""
                + "}";
        return requestAllocFunds("altSettle.launch", "1.1", dataJson, null);
    }

    private static String queryAccount(String altMchNo) throws Exception {
        return requestAllocFunds("altAccount.get", "1.1", "{\"alt_mch_no\":\"" + altMchNo + "\"}", null);
    }

    private static String requestAllocFunds(String method, String version, String dataJson, String aesKey) throws Exception {
        Map<String, String> signParams = new TreeMap<>();
        signParams.put("data", dataJson);
        signParams.put("mch_no", MERCHANT_NO);
        signParams.put("method", method);
        signParams.put("rand_str", randomAlphaNum(32));
        signParams.put("sign_type", SIGN_TYPE);
        signParams.put("version", version);

        String sign = sign(signParams);
        Map<String, String> request = new LinkedHashMap<>(signParams);
        request.put("sign", sign);
        if (aesKey != null && !aesKey.isEmpty()) {
            request.put("aes_key", aesKey);
        }
        return postJson(URL, toJson(request));
    }

    private static String sign(Map<String, String> signParams) throws Exception {
        if ("1".equals(SIGN_TYPE)) {
            return md5Sign(signParams);
        }
        if ("21".equals(SIGN_TYPE)) {
            return rsaSignMd5(signParams);
        }
        throw new IllegalArgumentException("SIGN_TYPE must be 1 or 21");
    }

    private static String buildSignString(Map<String, String> signParams) {
        return signParams.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }

    private static String md5Sign(Map<String, String> signParams) throws Exception {
        String signStr = buildSignString(signParams);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] digest = md5.digest((signStr + "&key=" + MERCHANT_KEY).getBytes(StandardCharsets.UTF_8));
        StringBuilder out = new StringBuilder();
        for (byte b : digest) {
            out.append(String.format("%02X", b & 0xff));
        }
        return out.toString();
    }

    private static String rsaSignMd5(Map<String, String> signParams) throws Exception {
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(
                new PKCS8EncodedKeySpec(parsePem(MERCHANT_PRIVATE_KEY_PEM)));
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM_MD5);
        signature.initSign(privateKey);
        signature.update(buildSignString(signParams).getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    private static boolean rsaVerify(String data, String jpPublicKeyPem, String sign, boolean isSha) throws Exception {
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(
                new X509EncodedKeySpec(parsePem(jpPublicKeyPem)));
        String algorithm = isSha ? SIGNATURE_ALGORITHM_SHA1 : SIGNATURE_ALGORITHM_MD5;
        Signature signature = Signature.getInstance(algorithm);
        signature.initVerify(publicKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signBytes = Base64.getDecoder().decode(hexToText(sign));
        return signature.verify(signBytes);
    }

    private static boolean rsaVerifyMd5(String data, String jpPublicKeyPem, String sign) throws Exception {
        return rsaVerify(data, jpPublicKeyPem, sign, false);
    }

    private static byte[] parsePem(String pem) {
        String normalized = pem.replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(normalized);
    }

    private static String postJson(String url, String body) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        try (InputStream in = conn.getInputStream()) {
            return new String(readAll(in), StandardCharsets.UTF_8);
        }
    }

    private static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int n;
        while ((n = in.read(chunk)) != -1) {
            buffer.write(chunk, 0, n);
        }
        return buffer.toByteArray();
    }

    private static String toJson(Map<String, String> map) {
        return map.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\":\"" + jsonEscape(e.getValue()) + "\"")
                .collect(Collectors.joining(",", "{", "}"));
    }

    private static String jsonEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String hexText(String value) {
        StringBuilder out = new StringBuilder();
        for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
            out.append(String.format("%02X", b & 0xff));
        }
        return out.toString();
    }

    private static String hexToText(String hex) {
        if ((hex.length() & 1) != 0) {
            throw new IllegalArgumentException("Invalid hex text length");
        }
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            out[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return new String(out, StandardCharsets.UTF_8);
    }

    private static String randomAlphaNum(int len) {
        String chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
