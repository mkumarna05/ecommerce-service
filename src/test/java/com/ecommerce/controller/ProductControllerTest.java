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

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.service.ProductService;

@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
class ProductControllerUnitTest {

	@Mock
	private ProductService productService;

	@InjectMocks
	private ProductController productController;

	private ProductDTO sampleProduct;

	@BeforeEach
	void setup() {
		sampleProduct = new ProductDTO(1L, "Laptop", "Gaming laptop", new BigDecimal("1299.99"), 50, false);
	}

	@Test
	void getAllProducts_Success() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<ProductDTO> page = new PageImpl<>(List.of(sampleProduct), pageable, 1);

		when(productService.getAllProducts(any(Pageable.class))).thenReturn(page);

		ResponseEntity<Page<ProductDTO>> response = productController.getAllProducts(pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1, response.getBody().getContent().size());
		assertEquals("Laptop", response.getBody().getContent().get(0).name());

		verify(productService).getAllProducts(any(Pageable.class));
	}

	@Test
    void getProductById_Success() {
        when(productService.getProductById(1L)).thenReturn(sampleProduct);

        ResponseEntity<ProductDTO> response = productController.getProductById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Laptop", response.getBody().name());

        verify(productService).getProductById(1L);
    }

	@Test
	void searchProducts_Success() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<ProductDTO> page = new PageImpl<>(List.of(sampleProduct), pageable, 1);

		when(productService.searchProducts(any(), any(), any(), any(), any())).thenReturn(page);

		ResponseEntity<Page<ProductDTO>> response = productController.searchProducts("Laptop", null, null, null,
				pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1, response.getBody().getContent().size());
		assertEquals("Laptop", response.getBody().getContent().get(0).name());

		verify(productService).searchProducts(any(), any(), any(), any(), any());
	}

	@Test
	void createProduct_Success() {
		ProductDTO request = new ProductDTO(null, "New Product", "Description", new BigDecimal("99.99"), 100, false);
		ProductDTO responseDto = new ProductDTO(1L, "New Product", "Description", new BigDecimal("99.99"), 100, false);

		when(productService.createProduct(any(ProductDTO.class))).thenReturn(responseDto);

		ResponseEntity<ProductDTO> response = productController.createProduct(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("New Product", response.getBody().name());

		verify(productService).createProduct(any(ProductDTO.class));
	}

	@Test
    void updateProduct_Success() {
        when(productService.updateProduct(any(Long.class), any(ProductDTO.class))).thenReturn(sampleProduct);

        ResponseEntity<ProductDTO> response = productController.updateProduct(1L, sampleProduct);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Laptop", response.getBody().name());

        verify(productService).updateProduct(any(Long.class), any(ProductDTO.class));
    }

	@Test
	void deleteProduct_Success() {
		 // Arrange: mock the service to return a message
        when(productService.deleteProduct(1L)).thenReturn("Product soft deleted successfully");

		ResponseEntity<String> response = productController.deleteProduct(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Product soft deleted successfully", response.getBody());

		verify(productService).deleteProduct(1L);
	}
}
