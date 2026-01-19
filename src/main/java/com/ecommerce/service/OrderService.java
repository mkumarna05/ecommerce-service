package com.ecommerce.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.DiscountRequest;
import com.ecommerce.dto.OrderItemDTO;
import com.ecommerce.dto.OrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.strategy.DiscountCalculator;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OrderService {

	private static final Logger log = LoggerFactory.getLogger(OrderService.class);
	private final OrderRepository orderRepository;

	private final ProductRepository productRepository;
	private final UserRepository userRepository;

	private final DiscountCalculator discountCalculator;

	@Transactional
	@CacheEvict(value = "orders", allEntries = true)
	public OrderResponse placeOrder(OrderRequest request) {
		log.info("Creating new order");

		// Get current authenticated user
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

		// Validate stock availability for all items
		validateStock(request);

		// Create order
		Order order = Order.builder().user(user).status(OrderStatus.PENDING).items(new ArrayList<>()).build();

		BigDecimal subtotal = BigDecimal.ZERO;

		// Process each order item
		for (var itemRequest : request.items()) {
			Product product = productRepository.findById(itemRequest.productId()).orElseThrow(
					() -> new InsufficientStockException("Product not found with id: " + itemRequest.productId()));

			BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
			subtotal = subtotal.add(itemTotal);

			OrderItem orderItem = OrderItem.builder().product(product).quantity(itemRequest.quantity())
					.unitPrice(product.getPrice()).discountApplied(BigDecimal.ZERO).totalPrice(itemTotal).build();

			order.addItem(orderItem);
			order.setCouponCode(request.couponCode());

			// Decrease product stock
			product.setQuantity(product.getQuantity() - itemRequest.quantity());
			productRepository.save(product);
		}
		log.info("Initial Order Total = " + subtotal);
		BigDecimal totalDiscount = discountCalculator
				.totalDiscount(new DiscountRequest(subtotal, null, null, order.getCouponCode(), user.getRole()));
		log.info("totalDiscount = " + totalDiscount);
		BigDecimal orderTotal = subtotal.subtract(totalDiscount);

		System.out.println("orderTotal  = " + orderTotal);

		if (totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
			for (OrderItem item : order.getItems()) {

				log.info("Order Product id = " + item.getProduct().getId() + " totalDiscount  = " + totalDiscount);

				BigDecimal itemDiscount = totalDiscount.multiply(item.getTotalPrice()).divide(subtotal, 2,
						RoundingMode.HALF_UP);

				log.info("Item Total Amount = " + item.getTotalPrice() + "itemDiscount  = " + itemDiscount);

				item.setDiscountApplied(itemDiscount);
				item.setTotalPrice(item.getTotalPrice().subtract(itemDiscount));
			}
		}
		order.setDiscountApplied(totalDiscount);
		log.info("Final Order Total  = " + orderTotal);
		order.setOrderTotal(orderTotal);

		Order savedOrder = orderRepository.save(order);
		log.info("Order created successfully with id: {}", savedOrder.getId());

		return buildOrderResponse(savedOrder);
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "orders", key = "#id")
	public OrderResponse getOrderById(Long id) {
		log.debug("Fetching order with id: {}", id);
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

		// Check if user has permission to view this order
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();

		if (!order.getUser().getUsername().equals(username)
				&& !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
			throw new ResourceNotFoundException("Order not found with id: " + id);
		}

		return buildOrderResponse(order);
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "orders", key = "#userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
	public Page<OrderResponse> getMyOrders(Pageable pageable) {
		log.debug("Fetching orders for current user");
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

		return orderRepository.findByUser(user, pageable).map(this::buildOrderResponse);
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "orders", key = "'all'")
	public Page<OrderResponse> getAllOrders(Pageable pageable) {
		log.debug("Fetching all orders");
		return orderRepository.findAll(pageable).map(this::buildOrderResponse);
	}

	private void validateStock(OrderRequest request) {
		for (var itemRequest : request.items()) {
			Product product = productRepository.findById(itemRequest.productId()).orElseThrow(
					() -> new InsufficientStockException("Product not found with id: " + itemRequest.productId()));

			if (product.getDeleted()) {
				throw new InsufficientStockException("Product not found with id: " + itemRequest.productId());
			}

			if (product.getQuantity() < itemRequest.quantity()) {
				throw new InsufficientStockException("Insufficient stock for product: " + product.getName()
						+ ". Available: " + product.getQuantity() + ", Requested: " + itemRequest.quantity());
			}
		}
	}

	private OrderResponse buildOrderResponse(Order order) {
		List<OrderItemDTO> itemDTOs = order.getItems().stream()
				.map(item -> new OrderItemDTO(item.getId(), item.getProduct().getId(), item.getProduct().getName(),
						item.getQuantity(), item.getUnitPrice(), item.getDiscountApplied(), item.getTotalPrice()))
				.toList();

		return new OrderResponse(order.getId(), order.getUser().getId(), order.getUser().getUsername(), itemDTOs,
				order.getOrderTotal(), order.getDiscountApplied(), OrderStatus.PENDING, order.getCreatedAt(),
				order.getUpdatedAt());
	}
}