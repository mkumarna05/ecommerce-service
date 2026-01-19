package com.ecommerce.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecommerce.dto.DiscountRequest;
import com.ecommerce.entity.Role;

class DiscountCalculatorTest {

	private DiscountCalculator discountCalculator;

	@BeforeEach
	void setUp() {

		List<DiscountStrategy> strategies = new ArrayList<>();
		strategies.add(new AmountThresholdDiscount());
		strategies.add(new PremiumUserDiscount());
		strategies.add(new UserDiscount());

		discountCalculator = new DiscountCalculator(strategies);
	}

	@Test
	void testUserDiscount_NoDiscount() {
		// Regular user, order under $500
		DiscountRequest request = new DiscountRequest(new BigDecimal("200.00"), null, null, null, Role.USER);
		BigDecimal discount = discountCalculator.totalDiscount(request);
		System.out.println("testUserDiscount_NoDiscount" + discount);
		assertEquals(new BigDecimal("0.00"), discount);
	}

	@Test
	void testPremiumUserDiscount() {
		// Premium user, order under $500
		BigDecimal orderTotal = new BigDecimal("300.00");
		DiscountRequest request = new DiscountRequest(orderTotal, null, null, null, Role.PREMIUM_USER);
		BigDecimal discount = discountCalculator.totalDiscount(request);

		// 10% of 300 = 30
		assertEquals(new BigDecimal("30.00"), discount);
	}

	@Test
	void testLargeOrderDiscount_User() {
		// Regular user, order above $500
		BigDecimal orderTotal = new BigDecimal("600.00");
		DiscountRequest request = new DiscountRequest(orderTotal, null, null, null, Role.USER);
		BigDecimal discount = discountCalculator.totalDiscount(request);

		// 5% of 600 = 30
		assertEquals(new BigDecimal("30.00"), discount);
	}

	@Test
	void testCombinedDiscount_PremiumUserLargeOrder() {
		// Premium user, order above $500
		BigDecimal orderTotal = new BigDecimal("600.00");
		DiscountRequest request = new DiscountRequest(orderTotal, null, null, null, Role.PREMIUM_USER);

		BigDecimal discount = discountCalculator.totalDiscount(request);

		// 10% of 600 = 60, plus 5% of 600 = 30, total = 90
		assertEquals(new BigDecimal("90.00"), discount);
	}

	@Test
	void testAdminDiscount() {
		// Admin, order under $500
		BigDecimal orderTotal = new BigDecimal("300.00");
		DiscountRequest request = new DiscountRequest(orderTotal, null, null, null, Role.ADMIN);
		BigDecimal discount = discountCalculator.totalDiscount(request);
		System.out.println("testAdminDiscount" + discount);
		assertEquals(new BigDecimal("0.00"), discount);
	}
}
