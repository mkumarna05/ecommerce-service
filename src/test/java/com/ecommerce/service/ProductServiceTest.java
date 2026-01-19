package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductService productService;

	private Product testProduct;
	private ProductDTO testProductDTO;

	@BeforeEach
	void setUp() {
		testProduct = Product.builder().id(1L).name("Test Product").description("Test Description")
				.price(new BigDecimal("99.99")).quantity(10).deleted(false).build();

		testProductDTO = new ProductDTO(null, "Test Product", "Test Description", new BigDecimal("99.99"), 10, false);
	}

	@Test
    void getProductById_Success() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        
        // Act
        ProductDTO result = productService.getProductById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.name());
        assertEquals(new BigDecimal("99.99"), result.price());
        verify(productRepository, times(1)).findById(1L);
    }

	@Test
    void getProductById_NotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(1L));
        verify(productRepository, times(1)).findById(1L);
    }

	@Test
    void createProduct_Success() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        // Act
        ProductDTO result = productService.createProduct(testProductDTO);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.name());
        verify(productRepository, times(1)).save(any(Product.class));
    }

	@Test
    void updateProduct_Success() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        // Act
        ProductDTO result = productService.updateProduct(1L, testProductDTO);
        
        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

	@Test
    void deleteProduct_Success() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        // Act
        productService.deleteProduct(1L);
        
        // Assert
        assertTrue(testProduct.getDeleted());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(testProduct);
    }
}
