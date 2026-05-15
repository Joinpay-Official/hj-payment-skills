import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 二级商户新增示例：secondaryMch.create。
 * 示例只演示签名、AES敏感字段加密、sec_key加密和请求体组装。
 */
public class SecondaryMchCreateExample {
    private static final String URL = "https://api.joinpay.com/altFunds";
    private static final String MERCHANT_NO = "YOUR_MERCHANT_NO";
    private static final String CALLBACK_URL = "YOUR_CALLBACK_URL";
    private static final String MERCHANT_PRIVATE_KEY_PEM = "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----";
    private static final String PLATFORM_PUBLIC_KEY_PEM = "-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----";

    public static void main(String[] args) throws Exception {
        String aesKey = randomAlphaNum(16);
        String method = "secondaryMch.create";
        String version = "1.0";
        String randStr = randomAlphaNum(32);

        String dataJson = "{"
                + "\"mch_order_no\":\"SM" + System.currentTimeMillis() + "\","
                + "\"callback_url\":\"" + CALLBACK_URL + "\","
                + "\"mch_info\":{"
                + "\"login_name\":\"demo_login_" + System.currentTimeMillis() + "\","
                + "\"alt_mch_name\":\"示例商户名称\","
                + "\"alt_merchant_type\":\"12\","
                + "\"busi_contact_name\":\"" + aesEncrypt("示例联系人", aesKey) + "\","
                + "\"busi_contact_mobile_no\":\"" + aesEncrypt("13800000000", aesKey) + "\","
                + "\"bussiness_type\":\"JP3041\","
                + "\"province\":\"5800\",\"city\":\"5810\",\"county\":\"5817\","
                + "\"manage_scope\":\"示例经营范围\",\"manage_addr\":\"示例经营地址\""
                + "},"
                + "\"id_card_info\":{"
                + "\"legal_person\":\"" + aesEncrypt("示例法人", aesKey) + "\","
                + "\"phone_no\":\"" + aesEncrypt("13800000000", aesKey) + "\","
                + "\"id_card_no\":\"" + aesEncrypt("110101199001011234", aesKey) + "\","
                + "\"id_card_valid_time_begin\":\"2020-01-01\","
                + "\"id_card_valid_time_end\":\"2099-12-31\","
                + "\"id_doc_copy\":\"IMAGE_ID_FACE\","
                + "\"id_card_national\":\"IMAGE_ID_BACK\","
                + "\"beneficiary_owner\":\"1\""
                + "},"
                + "\"license_info\":{"
                + "\"license_no\":\"LICENSE_NO_PLACEHOLDER\","
                + "\"license_expiry\":\"2099-12-31\","
                + "\"trade_licence\":\"IMAGE_ID_LICENSE\""
                + "},"
                + "\"account_type\":{\"sett_mode\":\"2\",\"sett_date_type\":\"1\",\"risk_day\":\"1\"},"
                + "\"account_info\":{"
                + "\"bank_account_type\":\"4\","
                + "\"bank_account_name\":\"" + aesEncrypt("示例商户名称", aesKey) + "\","
                + "\"bank_account_no\":\"" + aesEncrypt("6222000000000000000", aesKey) + "\","
                + "\"bank_channel_no\":\"BANK_CHANNEL_NO\","
                + "\"open_account_licence\":\"IMAGE_ID_BANK\""
                + "},"
                + "\"sales_info\":[{\"material_id\":\"locallife_proof\",\"material_cont\":["
                + "{\"material_type\":\"door_photo\",\"material_info\":\"IMAGE_ID_DOOR\"},"
                + "{\"material_type\":\"environment_photo\",\"material_info\":\"IMAGE_ID_ENV\"},"
                + "{\"material_type\":\"co_agreement\",\"material_info\":\"IMAGE_ID_AGREEMENT\"}"
                + "]}]"
                + "}";

        Map<String, String> signParams = new TreeMap<>();
        signParams.put("data", dataJson);
        signParams.put("mch_no", MERCHANT_NO);
        signParams.put("method", method);
        signParams.put("rand_str", randStr);
        signParams.put("sign_type", "2");
        signParams.put("version", version);

        String signStr = signParams.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        String sign = rsaSign(signStr, MERCHANT_PRIVATE_KEY_PEM);
        String secKey = rsaEncrypt(aesKey, PLATFORM_PUBLIC_KEY_PEM);

        Map<String, String> request = new LinkedHashMap<>(signParams);
        request.put("sign", sign);
        request.put("sec_key", secKey);

        String requestJson = toJson(request);
        System.out.println(postJson(URL, requestJson));
    }

    private static String aesEncrypt(String plainText, String aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), "AES"));
        return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
    }

    private static String rsaSign(String data, String privateKeyPem) throws Exception {
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(
                new PKCS8EncodedKeySpec(parsePem(privateKeyPem)));
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    private static String rsaEncrypt(String data, String publicKeyPem) throws Exception {
        RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(
                new X509EncodedKeySpec(parsePem(publicKeyPem)));
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
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
