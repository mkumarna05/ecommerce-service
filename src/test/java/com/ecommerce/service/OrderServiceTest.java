package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ecommerce.dto.DiscountRequest;
import com.ecommerce.dto.OrderItemRequest;
import com.ecommerce.dto.OrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.strategy.DiscountCalculator;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private DiscountCalculator discountCalculator;

	@InjectMocks
	private OrderService orderService;

	private User user;

	private Product product;

	@BeforeEach
	void setupSecurity() {
		user = User.builder().id(1L).username("manoj").role(Role.USER).build();

		product = Product.builder().id(1L).name("Laptop").price(new BigDecimal("1000")).quantity(10).deleted(false)
				.build();

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("manoj", null,
				List.of(new SimpleGrantedAuthority("ROLE_USER"))));
	}

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	// ---------- PLACE ORDER ----------

	@Test
	void placeOrder_success() {

		OrderItemRequest itemRequest = new OrderItemRequest(1L, 2);
		OrderRequest request = new OrderRequest(List.of(itemRequest), "COUPON10");

		User user = User.builder().id(1L).username("manoj").role(Role.USER).build();

		Product product = Product.builder().id(1L).name("Laptop").price(new BigDecimal("1000")).quantity(10)
				.deleted(false).build();

		when(userRepository.findByUsername("manoj")).thenReturn(Optional.of(user));

		when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));

		when(discountCalculator.totalDiscount(any(DiscountRequest.class))).thenReturn(new BigDecimal("100"));

		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
			Order order = invocation.getArgument(0);
			order.setId(1L);
			return order;
		});

		// ---------- WHEN ----------
		OrderResponse response = orderService.placeOrder(request);

		// ---------- THEN ----------
		assertNotNull(response);
		assertEquals(1L, response.id());
		assertEquals(0, response.orderTotal().compareTo(new BigDecimal("1900.00")));
		assertEquals(OrderStatus.PENDING, response.status());

		verify(userRepository).findByUsername("manoj");
		verify(productRepository).save(product);
		verify(orderRepository).save(any(Order.class));
	}

	@Test
	void placeOrder_insufficientStock() {
		product.setQuantity(1);
		product.setDeleted(true);
		OrderRequest request = new OrderRequest(List.of(new OrderItemRequest(1L, 5)), null);
		when(userRepository.findByUsername("manoj")).thenReturn(Optional.of(user));
		when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));
		assertThrows(InsufficientStockException.class, () -> orderService.placeOrder(request));
	}

	// ---------- GET ORDER ----------

	@Test
	void getOrderById_success_owner() {
		Order order = Order.builder().id(1L).user(user).items(List.of()).build();

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		var response = orderService.getOrderById(1L);

		assertEquals(1L, response.id());
	}

	@Test
	void getOrderById_forbidden() {
		User otherUser = User.builder().id(2L).username("other").build();

		Order order = Order.builder().id(1L).user(otherUser).items(List.of()).build();

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(1L));
	}

	// ---------- GET MY ORDERS ----------

	@Test
	void getMyOrders_success() {
		Order order = Order.builder().id(1L).user(user).items(List.of()).build();

		when(userRepository.findByUsername("manoj")).thenReturn(Optional.of(user));

		when(orderRepository.findByUser(eq(user), any())).thenReturn(new PageImpl<>(List.of(order)));

		Page<?> page = orderService.getMyOrders(PageRequest.of(0, 10));

		assertEquals(1, page.getTotalElements());
	}

	// ---------- GET ALL ORDERS ----------

	@Test
	void getAllOrders_success() {
		Order order = Order.builder().id(1L).user(user).items(List.of()).build();

		when(orderRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(order)));

		Page<?> page = orderService.getAllOrders(PageRequest.of(0, 10));

		assertEquals(1, page.getTotalElements());
	}
}
