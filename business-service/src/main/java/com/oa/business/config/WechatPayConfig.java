package com.oa.business.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信支付配置类
 */
@Configuration
@ConfigurationProperties(prefix = "wechat.pay")
public class WechatPayConfig {
    
    /**
     * 应用ID
     */
    private String appId;
    
    /**
     * 商户号
     */
    private String mchId;
    
    /**
     * 商户密钥
     */
    private String mchKey;
    
    /**
     * API证书路径（退款等需要证书的接口使用）
     */
    private String certPath;
    
    /**
     * 统一下单接口地址
     */
    private String orderUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    
    /**
     * 查询订单接口地址
     */
    private String queryUrl = "https://api.mch.weixin.qq.com/pay/orderquery";
    
    /**
     * 关闭订单接口地址
     */
    private String closeUrl = "https://api.mch.weixin.qq.com/pay/closeorder";
    
    /**
     * 退款接口地址
     */
    private String refundUrl = "https://api.mch.weixin.qq.com/secapi/pay/refund";
    
    /**
     * 查询退款接口地址
     */
    private String refundQueryUrl = "https://api.mch.weixin.qq.com/pay/refundquery";
    
    /**
     * 支付结果通知地址
     */
    private String notifyUrl;

    // Getters and Setters
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getMchKey() {
        return mchKey;
    }

    public void setMchKey(String mchKey) {
        this.mchKey = mchKey;
    }

    public String getCertPath() {
        return certPath;
    }

    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public String getOrderUrl() {
        return orderUrl;
    }

    public void setOrderUrl(String orderUrl) {
        this.orderUrl = orderUrl;
    }

    public String getQueryUrl() {
        return queryUrl;
    }

    public void setQueryUrl(String queryUrl) {
        this.queryUrl = queryUrl;
    }

    public String getCloseUrl() {
        return closeUrl;
    }

    public void setCloseUrl(String closeUrl) {
        this.closeUrl = closeUrl;
    }

    public String getRefundUrl() {
        return refundUrl;
    }

    public void setRefundUrl(String refundUrl) {
        this.refundUrl = refundUrl;
    }

    public String getRefundQueryUrl() {
        return refundQueryUrl;
    }

    public void setRefundQueryUrl(String refundQueryUrl) {
        this.refundQueryUrl = refundQueryUrl;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }
}