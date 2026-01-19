package com.ecommerce.strategy;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.ecommerce.dto.DiscountRequest;

@Component("userDiscount")
public class UserDiscount implements DiscountStrategy {

	@Override
	public boolean supports(DiscountRequest discountRequest) {
		return true;
	}

	@Override
	public BigDecimal discountAmount(DiscountRequest discountRequest) {
		System.out.println("User Discount Applied 0");
		return BigDecimal.ZERO;
	}

}
