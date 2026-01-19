package com.ecommerce.strategy;

import java.math.BigDecimal;

import com.ecommerce.dto.DiscountRequest;

public interface DiscountStrategy {
    
 boolean supports(DiscountRequest request);
 
 BigDecimal discountAmount(DiscountRequest request);
}
