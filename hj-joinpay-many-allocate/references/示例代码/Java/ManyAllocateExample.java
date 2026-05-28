import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 延迟分账与多次分账示例：altHandle.singleLaterAllocate / allocateQuery /
 * manyLaterAllocate / finishAllocate / altManyOrderQuery / altManyTotalQuery。
 * 示例只演示 /allocFunds V1.3.0 协议骨架，请替换所有占位符。
 */
public class ManyAllocateExample {
    private static final String URL = "https://www.joinpay.com/allocFunds";
    private static final String MERCHANT_NO = "YOUR_MERCHANT_NO";
    private static final String MERCHANT_KEY = "YOUR_MERCHANT_KEY";
    private static final String CALLBACK_URL = "YOUR_CALLBACK_URL";
    private static final String SIGN_TYPE = "1"; // 1=MD5，21=RSA(MD5withRSA)
    private static final String MERCHANT_PRIVATE_KEY_PEM = "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----";
    private static final String SIGNATURE_ALGORITHM_MD5 = "MD5withRSA";

    public static void main(String[] args) throws Exception {
        String paidOrderNo = "PAID_ORDER_NO_PLACEHOLDER";
        String altMchNo = "ALT_MCH_NO_PLACEHOLDER";
        String altOrderNo = "ALT_ORDER_NO_PLACEHOLDER";

        System.out.println(singleLaterAllocate(paidOrderNo, altMchNo));
        System.out.println(queryDelayAllocate(altOrderNo));
        System.out.println(manyLaterAllocate(paidOrderNo, altMchNo));
        System.out.println(finishAllocate(paidOrderNo));
        System.out.println(querySingleAllocate(altOrderNo));
        System.out.println(queryTotalAllocate(paidOrderNo));
    }

    private static String singleLaterAllocate(String mchOrderNo, String altMchNo) throws Exception {
        String dataJson = "{"
                + "\"mch_order_no\":\"" + jsonEscape(mchOrderNo) + "\","
                + "\"alt_order_no\":\"SGL" + System.currentTimeMillis() + randomAlphaNum(6) + "\","
                + "\"alt_info\":[{\"alt_mch_no\":\"" + jsonEscape(altMchNo) + "\",\"alt_amount\":\"6.00\"}],"
                + "\"callback_url\":\"" + jsonEscape(CALLBACK_URL) + "\""
                + "}";
        return requestAllocFunds("altHandle.singleLaterAllocate", "1.1", dataJson);
    }

    private static String queryDelayAllocate(String altOrderNo) throws Exception {
        return requestAllocFunds(
                "altHandle.allocateQuery",
                "1.1",
                "{\"alt_order_no\":\"" + jsonEscape(altOrderNo) + "\"}");
    }

    private static String manyLaterAllocate(String mchOrderNo, String altMchNo) throws Exception {
        String dataJson = "{"
                + "\"mch_order_no\":\"" + jsonEscape(mchOrderNo) + "\","
                + "\"alt_order_no\":\"ALT" + System.currentTimeMillis() + randomAlphaNum(6) + "\","
                + "\"alt_this_amount\":\"10.00\","
                + "\"alt_this_marketing_amount\":\"0.00\","
                + "\"alt_info\":[{\"alt_mch_no\":\"" + jsonEscape(altMchNo) + "\",\"alt_amount\":\"6.00\"}],"
                + "\"callback_url\":\"" + jsonEscape(CALLBACK_URL) + "\""
                + "}";
        return requestAllocFunds("altHandle.manyLaterAllocate", "1.1", dataJson);
    }

    private static String finishAllocate(String mchOrderNo) throws Exception {
        String dataJson = "{"
                + "\"mch_order_no\":\"" + jsonEscape(mchOrderNo) + "\","
                + "\"alt_order_no\":\"FIN" + System.currentTimeMillis() + randomAlphaNum(6) + "\","
                + "\"callback_url\":\"" + jsonEscape(CALLBACK_URL) + "\""
                + "}";
        return requestAllocFunds("altHandle.finishAllocate", "1.1", dataJson);
    }

    private static String querySingleAllocate(String altOrderNo) throws Exception {
        return requestAllocFunds(
                "altHandle.altManyOrderQuery",
                "1.1",
                "{\"alt_order_no\":\"" + jsonEscape(altOrderNo) + "\"}");
    }

    private static String queryTotalAllocate(String mchOrderNo) throws Exception {
        return requestAllocFunds(
                "altHandle.altManyTotalQuery",
                "1.1",
                "{\"mch_order_no\":\"" + jsonEscape(mchOrderNo) + "\"}");
    }

    private static String requestAllocFunds(String method, String version, String dataJson) throws Exception {
        Map<String, String> signParams = new TreeMap<>();
        signParams.put("data", dataJson);
        signParams.put("mch_no", MERCHANT_NO);
        signParams.put("method", method);
        signParams.put("rand_str", randomAlphaNum(32));
        signParams.put("sign_type", SIGN_TYPE);
        signParams.put("version", version);

        Map<String, String> request = new LinkedHashMap<>(signParams);
        request.put("sign", sign(signParams));
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
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] digest = md5.digest((buildSignString(signParams) + "&key=" + MERCHANT_KEY)
                .getBytes(StandardCharsets.UTF_8));
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
