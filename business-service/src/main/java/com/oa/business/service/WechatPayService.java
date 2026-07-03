package com.oa.business.service;

import java.util.Map;

/**
 * 微信支付服务接口
 */
public interface WechatPayService {

    /**
     * 创建支付订单（统一下单）
     *
     * @param orderNo     订单号
     * @param totalAmount 总金额（单位：元，会自动转换为分）
     * @param body        商品描述
     * @param openId      用户openId（JSAPI支付必填）
     * @param tradeType   交易类型（JSAPI/NATIVE/APP/MWEB）
     * @return 支付参数Map
     */
    Map<String, Object> createOrder(String orderNo, Double totalAmount, String body, String openId, String tradeType);

    /**
     * 查询订单状态
     *
     * @param orderNo 订单号
     * @return 订单状态信息
     */
    Map<String, Object> queryOrder(String orderNo);

    /**
     * 关闭订单
     *
     * @param orderNo 订单号
     * @return 关闭结果
     */
    Map<String, Object> closeOrder(String orderNo);

    /**
     * 申请退款
     *
     * @param orderNo    订单号
     * @param refundNo   退款单号
     * @param totalFee   订单总金额（单位：元）
     * @param refundFee  退款金额（单位：元）
     * @return 退款结果
     */
    Map<String, Object> refund(String orderNo, String refundNo, Double totalFee, Double refundFee);

    /**
     * 查询退款
     *
     * @param refundNo 退款单号
     * @return 退款状态信息
     */
    Map<String, Object> queryRefund(String refundNo);

    /**
     * 处理支付回调
     *
     * @param xmlData 回调XML数据
     * @return 处理结果
     */
    Map<String, Object> handleNotify(String xmlData);
}