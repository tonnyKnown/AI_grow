package com.oa.system.service;

public interface SmsService {
    void sendOrderStatusNotification(String phoneNumber, String orderNo, Integer status);
}
