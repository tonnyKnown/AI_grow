package com.oa.business.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MarketingTypeEnum {
    DISCOUNT("discount", "折扣"),
    FLASH("flash", "秒杀"),
    GROUP("group", "团购"),
    COUPON("coupon", "优惠券");

    private final String code;
    private final String name;
}
