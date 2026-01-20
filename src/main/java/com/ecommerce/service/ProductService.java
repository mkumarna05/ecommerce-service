package com.ecommerce.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private static final Logger log = LoggerFactory.getLogger(ProductService.class);

	private final ProductRepository productRepository;

	@Transactional(readOnly = true)
	@Cacheable(value = "products", key = "'all'")
	public Page<ProductDTO> getAllProducts(Pageable pageable) {
		log.debug("Fetching all products with pagination");
		return productRepository.findByDeletedFalse(pageable).map(this::buildProductResponse);
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "products", key = "#id")
	public ProductDTO getProductById(Long id) {
		log.debug("Fetching product with id: {}", id);
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new InsufficientStockException("Product not found with id: " + id));

		if (product.getDeleted()) {
			throw new InsufficientStockException("Product not found with id: " + id);
		}

		return buildProductResponse(product);
	}

	@Transactional
	@CacheEvict(value = "products", allEntries = true)
	public ProductDTO createProduct(ProductDTO productDTO) {
		log.info("Creating new product: {}", productDTO.name());
		Product product = Product.builder().name(productDTO.name()).description(productDTO.description())
				.price(productDTO.price()).quantity(productDTO.quantity()).deleted(false).build();

		Product savedProduct = productRepository.save(product);
		log.info("Product created successfully with id: {}", savedProduct.getId());
		return buildProductResponse(savedProduct);
	}

	@Transactional
	@CachePut(value = "products", key = "#id")
	@CacheEvict(value = "products", key = "'all'")
	public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
		log.info("Updating product with id: {}", id);
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

		if (product.getDeleted()) {
			throw new ResourceNotFoundException("Product not found with id: " + id);
		}

		product.setName(productDTO.name());
		product.setDescription(productDTO.description());
		product.setPrice(productDTO.price());
		product.setQuantity(productDTO.quantity());

		Product updatedProduct = productRepository.save(product);
		log.info("Product updated successfully with id: {}", id);
		return buildProductResponse(updatedProduct);
	}

	@Transactional
	@CacheEvict(value = "products", key = "#id")
	public String deleteProduct(Long id) {
		log.info("Soft deleting product with id: {}", id);
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

		product.setDeleted(true);
		productRepository.save(product);
		log.info("Product soft deleted successfully with id: {}", id);
		return "Product soft deleted successfully";
	}

	@Transactional(readOnly = true)
	public Page<ProductDTO> searchProducts(String name, BigDecimal minPrice, BigDecimal maxPrice, Boolean available,
			Pageable pageable) {
		log.debug("Searching products with filters - name: {}, minPrice: {}, maxPrice: {}, available: {}", name,
				minPrice, maxPrice, available);
		return productRepository.searchProducts(name, minPrice, maxPrice, available, pageable)
				.map(this::buildProductResponse);
	}

	@Transactional
	public void updateStock(Long productId, int quantity) {
		log.debug("Decreasing stock for product id: {} by quantity: {}", productId, quantity);
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

		if (product.getQuantity() < quantity) {
			throw new IllegalStateException("Insufficient stock for product: " + product.getName());
		}

		product.setQuantity(product.getQuantity() - quantity);
		productRepository.save(product);
	}

	private ProductDTO buildProductResponse(Product product) {
		return new ProductDTO(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
				product.getQuantity(), product.getDeleted());
	}
}