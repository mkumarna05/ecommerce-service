package com.ecommerce.strategy;

import org.springframework.stereotype.Component;

import com.ecommerce.dto.DiscountRequest;
import com.ecommerce.entity.Role;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("premiumUserDiscount")
public class PremiumUserDiscount implements DiscountStrategy {

	private static final BigDecimal PREMIUM_DISCOUNT_RATE = new BigDecimal("0.10");

	@Override
	public boolean supports(DiscountRequest discountRequest) {
		return discountRequest.customerType() != null && discountRequest.customerType() == Role.PREMIUM_USER;
	}

	@Override
	public BigDecimal discountAmount(DiscountRequest discountRequest) {
		System.out.println("Premium Discount Applied 10%");
		return discountRequest.orderTotal().multiply(PREMIUM_DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
	}
}
