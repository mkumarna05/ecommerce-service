package com.ecommerce.strategy;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.dto.DiscountRequest;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DiscountCalculator {

	private final List<DiscountStrategy> strategies;

	public BigDecimal totalDiscount(DiscountRequest request) {
		return strategies.stream().filter(s -> s.supports(request)).map(s -> s.discountAmount(request))
				.reduce(new BigDecimal("0.00"), BigDecimal::add);
	}

}
