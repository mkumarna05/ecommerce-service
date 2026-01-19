package com.ecommerce.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	Page<Product> findByDeletedFalse(Pageable pageable);

	Optional<Product> findByIdAndDeletedFalse(Long id);

	@Query("SELECT p FROM Product p WHERE p.deleted = false AND "
			+ "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
			+ "(:minPrice IS NULL OR p.price >= :minPrice) AND " + "(:maxPrice IS NULL OR p.price <= :maxPrice) AND "
			+ "(:available IS NULL OR (:available = true AND p.quantity > 0) OR (:available = false))")
	Page<Product> searchProducts(@Param("name") String name, @Param("minPrice") BigDecimal minPrice,
			@Param("maxPrice") BigDecimal maxPrice, @Param("available") Boolean available, Pageable pageable);
}