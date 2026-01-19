package com.ecommerce.strategy;

import org.springframework.stereotype.Component;

import com.ecommerce.dto.DiscountRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("amountThresholdDiscount")
public class AmountThresholdDiscount implements DiscountStrategy {

	private static final BigDecimal AMOUNT_THRESHOLD = new BigDecimal("500.00");
	private static final BigDecimal LARGE_ORDER_DISCOUNT_RATE = new BigDecimal("0.05");

	@Override
	public boolean supports(DiscountRequest discountRequest) {
		return discountRequest.orderTotal() != null && discountRequest.orderTotal().compareTo(AMOUNT_THRESHOLD) >= 0;
	}

	@Override
	public BigDecimal discountAmount(DiscountRequest discountRequest) {
		System.out.println("Amount Discount Applied 5%");
		return discountRequest.orderTotal().multiply(LARGE_ORDER_DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
	}

}
