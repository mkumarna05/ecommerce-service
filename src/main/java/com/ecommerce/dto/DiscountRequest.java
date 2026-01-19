package com.ecommerce.dto;

import java.math.BigDecimal;

import com.ecommerce.entity.Role;

public record DiscountRequest(BigDecimal orderTotal, BigDecimal itemTotal, Integer quantity, String couponCode,
		Role customerType) {

}
