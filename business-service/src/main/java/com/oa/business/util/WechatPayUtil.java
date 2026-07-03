package com.oa.business.util;

import com.oa.business.config.WechatPayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * 微信支付工具类
 */
@Component
public class WechatPayUtil {

    private static final Logger log = LoggerFactory.getLogger(WechatPayUtil.class);

    @Autowired
    private WechatPayConfig payConfig;

    /**
     * 生成随机字符串
     */
    public String generateNonceStr() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    /**
     * 生成订单号
     */
    public String generateOrderNo() {
        return "wx" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(9999));
    }

    /**
     * 生成签名
     *
     * @param params 参数集合
     * @param key    密钥
     * @return 签名
     */
    public String generateSign(Map<String, String> params, String key) {
        // 1. 去除空值和sign参数
        Map<String, String> filteredParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            if (v != null && !v.isEmpty() && !"sign".equals(k)) {
                filteredParams.put(k, v);
            }
        }

        // 2. 拼接成key=value&key=value形式
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : filteredParams.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }

        // 3. 拼接密钥
        sb.append("&key=").append(key);

        // 4. MD5加密
        return md5(sb.toString()).toUpperCase();
    }

    /**
     * MD5加密
     */
    public String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("MD5加密失败", e);
            return "";
        }
    }

    /**
     * HMAC-SHA256加密
     */
    public String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            log.error("HMAC-SHA256加密失败", e);
            return "";
        }
    }

    /**
     * 构建统一下单请求参数
     *
     * @param orderNo     订单号
     * @param totalAmount 总金额（单位：分）
     * @param body        商品描述
     * @param openId      用户openId（JSAPI支付必填）
     * @param tradeType   交易类型（JSAPI/NATIVE/APP/MWEB）
     * @return 请求参数Map
     */
    public Map<String, String> buildUnifiedOrderParams(String orderNo, Integer totalAmount, String body,
                                                       String openId, String tradeType) {
        Map<String, String> params = new HashMap<>();
        params.put("appid", payConfig.getAppId());
        params.put("mch_id", payConfig.getMchId());
        params.put("nonce_str", generateNonceStr());
        params.put("body", body);
        params.put("out_trade_no", orderNo);
        params.put("total_fee", String.valueOf(totalAmount));
        params.put("spbill_create_ip", "127.0.0.1");
        params.put("notify_url", payConfig.getNotifyUrl());
        params.put("trade_type", tradeType);

        if ("JSAPI".equals(tradeType) && openId != null && !openId.isEmpty()) {
            params.put("openid", openId);
        }

        // 生成签名
        params.put("sign", generateSign(params, payConfig.getMchKey()));

        return params;
    }

    /**
     * Map转XML字符串
     */
    public String mapToXml(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append("<").append(entry.getKey()).append(">");
            sb.append(entry.getValue());
            sb.append("</").append(entry.getKey()).append(">");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    /**
     * XML字符串转Map
     */
    public Map<String, String> xmlToMap(String xml) {
        Map<String, String> result = new HashMap<>();
        try {
            String[] parts = xml.substring(5, xml.length() - 6).split("</[^>]+>");
            for (String part : parts) {
                if (part.trim().isEmpty()) continue;
                int start = part.indexOf(">");
                String key = part.substring(1, start);
                String value = part.substring(start + 1);
                result.put(key, value);
            }
        } catch (Exception e) {
            log.error("XML解析失败", e);
        }
        return result;
    }

    /**
     * 验证签名
     *
     * @param params 接收到的参数
     * @param key    密钥
     * @return 是否验证通过
     */
    public boolean verifySign(Map<String, String> params, String key) {
        String sign = params.get("sign");
        if (sign == null || sign.isEmpty()) {
            return false;
        }
        Map<String, String> paramsWithoutSign = new HashMap<>(params);
        paramsWithoutSign.remove("sign");
        String calculatedSign = generateSign(paramsWithoutSign, key);
        return sign.equalsIgnoreCase(calculatedSign);
    }

    /**
     * 构建成功响应XML
     */
    public String buildSuccessXml() {
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }

    /**
     * 构建失败响应XML
     */
    public String buildFailXml(String message) {
        return String.format("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[%s]]></return_msg></xml>", message);
    }

    /**
     * 获取支付配置
     */
    public WechatPayConfig getPayConfig() {
        return payConfig;
    }
}