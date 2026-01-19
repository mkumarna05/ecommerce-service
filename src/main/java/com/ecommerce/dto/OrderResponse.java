package com.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ecommerce.entity.OrderStatus;

public record OrderResponse(
		Long id, 
		Long userId, 
		String username, 
		List<OrderItemDTO> items, 
		BigDecimal orderTotal,
		BigDecimal discountApplied, 
		OrderStatus status, 
		LocalDateTime createdAt, 
		LocalDateTime updatedAt) 
{
}
