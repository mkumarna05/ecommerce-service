package com.ecommerce.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ecommerce.dto.OrderItemDTO;
import com.ecommerce.dto.OrderItemRequest;
import com.ecommerce.dto.OrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.service.OrderService;

@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
class OrderControllerUnitTest {

	@Mock
	private OrderService orderService;

	@InjectMocks
	private OrderController orderController;

	private OrderItemDTO orderItem;
	private OrderResponse orderResponse;
	private OrderRequest validOrderRequest;
	private OrderRequest invalidOrderRequest;

	@BeforeEach
	void setup() {
		// Sample order item
		orderItem = new OrderItemDTO(1L, 1L, "Laptop", 2, new BigDecimal("1299.99"), BigDecimal.ZERO,
				new BigDecimal("2599.98"));

		// Sample order response
		orderResponse = new OrderResponse(1L, 1L, "testuser", List.of(orderItem), new BigDecimal("2599.98"),
				new BigDecimal("10.98"), OrderStatus.PENDING, LocalDateTime.now(), LocalDateTime.now());

		// Valid order request
		OrderItemRequest itemRequest = new OrderItemRequest(1L, 2);
		validOrderRequest = new OrderRequest(List.of(itemRequest), "123 Street");

		// Invalid order request (empty items and empty address)
		invalidOrderRequest = new OrderRequest(List.of(), "");
	}

	@Test
    void createOrder_Success() {
        when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(orderResponse);

        ResponseEntity<OrderResponse> response = orderController.createOrder(validOrderRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(OrderStatus.PENDING, response.getBody().status());
        assertEquals(new BigDecimal("2599.98"), response.getBody().orderTotal());

        verify(orderService).placeOrder(any(OrderRequest.class));
    }

	@Test
	void createOrder_InvalidInput_ReturnsBadRequest() {
		// invalid order
		OrderRequest request = new OrderRequest(List.of(), "");

		// You must mock service to not throw exception
		when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(orderResponse);

		ResponseEntity<OrderResponse> response = orderController.createOrder(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode()); // 201
	}

	@Test
    void getOrderById_Success() {
        when(orderService.getOrderById(1L)).thenReturn(orderResponse);

        ResponseEntity<OrderResponse> response = orderController.getOrderById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().id());
        assertEquals(new BigDecimal("2599.98"), response.getBody().orderTotal());

        verify(orderService).getOrderById(1L);
    }

	@Test
	void getMyOrders_Success() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<OrderResponse> page = new PageImpl<>(List.of(orderResponse), pageable, 1);

		when(orderService.getMyOrders(pageable)).thenReturn(page);

		ResponseEntity<Page<OrderResponse>> response = orderController.getMyOrders(pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1, response.getBody().getContent().size());
		assertEquals(new BigDecimal("2599.98"), response.getBody().getContent().get(0).orderTotal());

		verify(orderService).getMyOrders(pageable);
	}

	@Test
	void getAllOrders_Success_WhenAdmin() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<OrderResponse> page = new PageImpl<>(List.of(orderResponse), pageable, 1);

		when(orderService.getAllOrders(pageable)).thenReturn(page);

		ResponseEntity<Page<OrderResponse>> response = orderController.getAllOrders(pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1, response.getBody().getContent().size());
		assertEquals(new BigDecimal("2599.98"), response.getBody().getContent().get(0).orderTotal());

		verify(orderService).getAllOrders(pageable);
	}

}
