package com.oa.business.service.impl;

import com.oa.business.config.WechatPayConfig;
import com.oa.business.service.WechatPayService;
import com.oa.business.util.WechatPayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付服务实现类
 */
@Service
public class WechatPayServiceImpl implements WechatPayService {

    private static final Logger log = LoggerFactory.getLogger(WechatPayServiceImpl.class);

    @Autowired
    private WechatPayUtil payUtil;

    @Autowired
    private WechatPayConfig payConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Map<String, Object> createOrder(String orderNo, Double totalAmount, String body, String openId, String tradeType) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 将元转换为分
            Integer totalFee = (int) (totalAmount * 100);

            // 构建请求参数
            Map<String, String> params = payUtil.buildUnifiedOrderParams(orderNo, totalFee, body, openId, tradeType);

            // 转换为XML
            String xmlData = payUtil.mapToXml(params);

            log.info("微信支付统一下单请求: {}", xmlData);

            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> request = new HttpEntity<>(xmlData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    payConfig.getOrderUrl(), request, String.class);

            String responseXml = response.getBody();
            log.info("微信支付统一下单响应: {}", responseXml);

            // 解析响应
            Map<String, String> responseMap = payUtil.xmlToMap(responseXml);
            String returnCode = responseMap.get("return_code");
            String resultCode = responseMap.get("result_code");

            if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
                result.put("success", true);
                result.put("message", "下单成功");

                // 根据交易类型返回不同的支付参数
                if ("JSAPI".equals(tradeType)) {
                    // JSAPI支付，返回调起支付所需参数
                    Map<String, String> payParams = new HashMap<>();
                    payParams.put("appId", payConfig.getAppId());
                    payParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
                    payParams.put("nonceStr", payUtil.generateNonceStr());
                    payParams.put("package", "prepay_id=" + responseMap.get("prepay_id"));
                    payParams.put("signType", "MD5");

                    // 生成签名
                    String sign = payUtil.generateSign(payParams, payConfig.getMchKey());
                    payParams.put("paySign", sign);

                    result.put("payParams", payParams);
                } else if ("NATIVE".equals(tradeType)) {
                    // 扫码支付，返回二维码链接
                    result.put("codeUrl", responseMap.get("code_url"));
                } else if ("MWEB".equals(tradeType)) {
                    // H5支付，返回跳转URL
                    result.put("mwebUrl", responseMap.get("mweb_url"));
                }

                result.put("prepayId", responseMap.get("prepay_id"));
            } else {
                result.put("success", false);
                result.put("message", responseMap.get("err_code_des") != null ?
                        responseMap.get("err_code_des") : responseMap.get("return_msg"));
                log.error("微信支付下单失败: {}", result.get("message"));
            }

        } catch (Exception e) {
            log.error("微信支付下单异常", e);
            result.put("success", false);
            result.put("message", "支付下单失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> queryOrder(String orderNo) {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, String> params = new HashMap<>();
            params.put("appid", payConfig.getAppId());
            params.put("mch_id", payConfig.getMchId());
            params.put("nonce_str", payUtil.generateNonceStr());
            params.put("out_trade_no", orderNo);
            params.put("sign", payUtil.generateSign(params, payConfig.getMchKey()));

            String xmlData = payUtil.mapToXml(params);

            log.info("微信支付查询订单请求: {}", xmlData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> request = new HttpEntity<>(xmlData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    payConfig.getQueryUrl(), request, String.class);

            String responseXml = response.getBody();
            log.info("微信支付查询订单响应: {}", responseXml);

            Map<String, String> responseMap = payUtil.xmlToMap(responseXml);
            String returnCode = responseMap.get("return_code");

            if ("SUCCESS".equals(returnCode)) {
                result.put("success", true);
                result.put("tradeState", responseMap.get("trade_state"));
                result.put("tradeStateDesc", responseMap.get("trade_state_desc"));
                result.put("transactionId", responseMap.get("transaction_id"));
                result.put("outTradeNo", responseMap.get("out_trade_no"));
                result.put("totalFee", responseMap.get("total_fee"));
                result.put("timeEnd", responseMap.get("time_end"));
            } else {
                result.put("success", false);
                result.put("message", responseMap.get("return_msg"));
            }

        } catch (Exception e) {
            log.error("微信支付查询订单异常", e);
            result.put("success", false);
            result.put("message", "查询订单失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> closeOrder(String orderNo) {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, String> params = new HashMap<>();
            params.put("appid", payConfig.getAppId());
            params.put("mch_id", payConfig.getMchId());
            params.put("nonce_str", payUtil.generateNonceStr());
            params.put("out_trade_no", orderNo);
            params.put("sign", payUtil.generateSign(params, payConfig.getMchKey()));

            String xmlData = payUtil.mapToXml(params);

            log.info("微信支付关闭订单请求: {}", xmlData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> request = new HttpEntity<>(xmlData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    payConfig.getCloseUrl(), request, String.class);

            String responseXml = response.getBody();
            log.info("微信支付关闭订单响应: {}", responseXml);

            Map<String, String> responseMap = payUtil.xmlToMap(responseXml);
            String returnCode = responseMap.get("return_code");
            String resultCode = responseMap.get("result_code");

            if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
                result.put("success", true);
                result.put("message", "订单已关闭");
            } else {
                result.put("success", false);
                result.put("message", responseMap.get("err_code_des") != null ?
                        responseMap.get("err_code_des") : responseMap.get("return_msg"));
            }

        } catch (Exception e) {
            log.error("微信支付关闭订单异常", e);
            result.put("success", false);
            result.put("message", "关闭订单失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> refund(String orderNo, String refundNo, Double totalFee, Double refundFee) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 退款需要证书，这里简化处理
            log.warn("退款功能需要配置API证书，请参考微信支付文档配置certPath");

            Map<String, String> params = new HashMap<>();
            params.put("appid", payConfig.getAppId());
            params.put("mch_id", payConfig.getMchId());
            params.put("nonce_str", payUtil.generateNonceStr());
            params.put("out_trade_no", orderNo);
            params.put("out_refund_no", refundNo);
            params.put("total_fee", String.valueOf((int) (totalFee * 100)));
            params.put("refund_fee", String.valueOf((int) (refundFee * 100)));
            params.put("sign", payUtil.generateSign(params, payConfig.getMchKey()));

            result.put("success", false);
            result.put("message", "退款功能需要配置API证书，请联系管理员");

        } catch (Exception e) {
            log.error("微信支付退款异常", e);
            result.put("success", false);
            result.put("message", "退款失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> queryRefund(String refundNo) {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, String> params = new HashMap<>();
            params.put("appid", payConfig.getAppId());
            params.put("mch_id", payConfig.getMchId());
            params.put("nonce_str", payUtil.generateNonceStr());
            params.put("out_refund_no", refundNo);
            params.put("sign", payUtil.generateSign(params, payConfig.getMchKey()));

            String xmlData = payUtil.mapToXml(params);

            log.info("微信支付查询退款请求: {}", xmlData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> request = new HttpEntity<>(xmlData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    payConfig.getRefundQueryUrl(), request, String.class);

            String responseXml = response.getBody();
            log.info("微信支付查询退款响应: {}", responseXml);

            Map<String, String> responseMap = payUtil.xmlToMap(responseXml);
            String returnCode = responseMap.get("return_code");

            if ("SUCCESS".equals(returnCode)) {
                result.put("success", true);
                result.put("refundStatus", responseMap.get("refund_status_0"));
                result.put("refundFee", responseMap.get("refund_fee_0"));
                result.put("refundTime", responseMap.get("refund_time_0"));
            } else {
                result.put("success", false);
                result.put("message", responseMap.get("return_msg"));
            }

        } catch (Exception e) {
            log.error("微信支付查询退款异常", e);
            result.put("success", false);
            result.put("message", "查询退款失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> handleNotify(String xmlData) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("收到微信支付回调: {}", xmlData);

            // 解析XML
            Map<String, String> params = payUtil.xmlToMap(xmlData);

            // 验证签名
            if (!payUtil.verifySign(params, payConfig.getMchKey())) {
                result.put("success", false);
                result.put("xmlResponse", payUtil.buildFailXml("签名验证失败"));
                return result;
            }

            String returnCode = params.get("return_code");
            String resultCode = params.get("result_code");

            if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
                // 支付成功，处理业务逻辑
                String orderNo = params.get("out_trade_no");
                String transactionId = params.get("transaction_id");
                String totalFee = params.get("total_fee");
                String timeEnd = params.get("time_end");

                log.info("支付成功 - 订单号: {}, 交易ID: {}, 金额: {}, 时间: {}",
                        orderNo, transactionId, totalFee, timeEnd);

                result.put("success", true);
                result.put("orderNo", orderNo);
                result.put("transactionId", transactionId);
                result.put("totalFee", Integer.parseInt(totalFee) / 100.0);
                result.put("timeEnd", timeEnd);
                result.put("xmlResponse", payUtil.buildSuccessXml());
            } else {
                result.put("success", false);
                result.put("message", params.get("err_code_des"));
                result.put("xmlResponse", payUtil.buildFailXml(params.get("err_code_des")));
            }

        } catch (Exception e) {
            log.error("处理微信支付回调异常", e);
            result.put("success", false);
            result.put("message", "处理回调失败: " + e.getMessage());
            result.put("xmlResponse", payUtil.buildFailXml("处理失败"));
        }

        return result;
    }
}