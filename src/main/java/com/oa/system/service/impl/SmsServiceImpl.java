package com.oa.system.service.impl;

import com.oa.system.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.TreeMap;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    private static final String HOST = "sms.tencentcloudapi.com";
    private static final String SERVICE = "sms";
    private static final String VERSION = "2019-07-11";
    private static final String ACTION = "SendSms";
    private static final String REGION = "ap-guangzhou";

    @Value("${tencent.sms.secret-id:}")
    private String secretId;

    @Value("${tencent.sms.secret-key:}")
    private String secretKey;

    @Value("${tencent.sms.app-id:}")
    private String appId;

    @Value("${tencent.sms.sign-name:}")
    private String signName;

    @Value("${tencent.sms.template-id:}")
    private String templateId;

    @Override
    @Async
    public void sendOrderStatusNotification(String phoneNumber, String orderNo, Integer status) {
        String statusText = getStatusText(status);

        if (secretId == null || secretId.isEmpty() || secretKey == null || secretKey.isEmpty() ||
            appId == null || appId.isEmpty() || templateId == null || templateId.isEmpty()) {
            log.warn("【短信通知】腾讯云SMS配置不完整，跳过发送: phone={}, orderNo={}, status={}", phoneNumber, orderNo, statusText);
            return;
        }

        try {
            sendSms(phoneNumber, orderNo, statusText);
            log.info("【短信通知】发送成功: phone={}, orderNo={}, status={}", phoneNumber, orderNo, statusText);
        } catch (Exception e) {
            log.error("【短信通知】发送失败: phone={}, orderNo={}, error={}", phoneNumber, orderNo, e.getMessage());
        }
    }

    private void sendSms(String phoneNumber, String orderNo, String statusText) throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = String.valueOf((int) (Math.random() * 1000000));

        TreeMap<String, String> params = new TreeMap<>();
        params.put("Action", ACTION);
        params.put("Version", VERSION);
        params.put("Timestamp", timestamp);
        params.put("Nonce", nonce);
        params.put("Region", REGION);
        params.put("SmsSdkAppid", appId);
        params.put("Sign", signName);
        params.put("TemplateID", templateId);
        params.put("PhoneNumberSet.0", phoneNumber);
        params.put("TemplateParamSet.0", orderNo);
        params.put("TemplateParamSet.1", statusText);

        String payload = buildPayload(params);
        String authorization = generateAuthorization(timestamp, nonce, payload);

        StringBuilder requestUrl = new StringBuilder();
        requestUrl.append("https://").append(HOST).append("/?");
        for (TreeMap.Entry<String, String> entry : params.entrySet()) {
            requestUrl.append(entry.getKey()).append("=").append(java.net.URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).append("&");
        }
        requestUrl.deleteCharAt(requestUrl.length() - 1);

        java.net.URL url = new java.net.URL(requestUrl.toString());
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Host", HOST);
        conn.setRequestProperty("X-TC-Action", ACTION);
        conn.setRequestProperty("X-TC-Version", VERSION);
        conn.setRequestProperty("X-TC-Timestamp", timestamp);
        conn.setRequestProperty("X-TC-Nonce", nonce);
        conn.setRequestProperty("X-TC-Region", REGION);
        conn.setRequestProperty("Authorization", authorization);

        conn.setDoOutput(true);
        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        StringBuilder response = new StringBuilder();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(
                        responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        if (responseCode != 200) {
            throw new RuntimeException("SMS API returned HTTP " + responseCode + ": " + response);
        }

        if (response.toString().contains("\"Error\"")) {
            throw new RuntimeException("SMS API Error: " + response);
        }

        log.debug("【短信通知】API响应: {}", response);
    }

    private String buildPayload(TreeMap<String, String> params) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (TreeMap.Entry<String, String> entry : params.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":\"").append(escapeJson(entry.getValue())).append("\"");
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private String generateAuthorization(String timestamp, String nonce, String payload) throws Exception {
        String canonicalRequest = "POST\n/\n\ncontent-type:application/json\nhost:" + HOST + "\n\n" +
                hashSha256("content-type:application/json\nhost:" + HOST + "\n\n" + hashSha256(payload));

        String credentialScope = timestamp + "/" + SERVICE + "/tc3_request";
        String stringToSign = "TC3-HMAC-SHA256\n" + timestamp + "\n" + credentialScope + "\n" +
                hashSha256Hex(canonicalRequest);

        byte[] secretDate = hmacSha256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), timestamp);
        byte[] secretService = hmacSha256(secretDate, SERVICE);
        byte[] secretSigning = hmacSha256(secretService, "tc3_request");
        String signature = hmacSha256Hex(secretSigning, stringToSign);

        return "TC3-HMAC-SHA256 Credential=" + secretId + "/" + credentialScope +
                ", SignedHeaders=content-type;host, Signature=" + signature;
    }

    private String hashSha256(String data) {
        return hashSha256Hex(data.getBytes(StandardCharsets.UTF_8));
    }

    private String hashSha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String hashSha256Hex(String data) {
        return hashSha256Hex(data.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] hmacSha256(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String hmacSha256Hex(byte[] key, String data) throws Exception {
        byte[] result = hmacSha256(key, data);
        StringBuilder hexString = new StringBuilder();
        for (byte b : result) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知状态";
        }
        return switch (status) {
            case 1 -> "待发货";
            case 2 -> "已发货";
            case 3 -> "配送中";
            case 4 -> "已收货";
            case 5 -> "已完成";
            case 6 -> "已取消";
            case 7 -> "退货中";
            case 8 -> "已退货";
            case 9 -> "已退款";
            default -> "状态(" + status + ")";
        };
    }
}
