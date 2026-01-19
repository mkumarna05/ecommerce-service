package com.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.OrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin
@Tag(name = "Orders", description = "Order Management APIs")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@PostMapping
	@Operation(summary = "Place a new order")
	public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
		OrderResponse order = orderService.placeOrder(request);
		return new ResponseEntity<>(order, HttpStatus.CREATED);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get order by ID")
	public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
		OrderResponse order = orderService.getOrderById(id);
		return ResponseEntity.ok(order);
	}

	@GetMapping("/my-orders")
	@Operation(summary = "Get current user's orders")
	public ResponseEntity<Page<OrderResponse>> getMyOrders(
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<OrderResponse> orders = orderService.getMyOrders(pageable);
		return ResponseEntity.ok(orders);
	}

	@GetMapping("/all-orders")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get all orders (Admin only)")
	public ResponseEntity<Page<OrderResponse>> getAllOrders(
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<OrderResponse> orders = orderService.getAllOrders(pageable);
		return ResponseEntity.ok(orders);
	}
}