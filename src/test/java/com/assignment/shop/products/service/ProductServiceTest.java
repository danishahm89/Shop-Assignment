package com.assignment.shop.products.service;

import com.assignment.shop.constants.AppConstants;
import com.assignment.shop.exceptions.ResourceNotFoundException;
import com.assignment.shop.products.dto.ProductDto;
import com.assignment.shop.products.entity.Product;
import com.assignment.shop.products.mapper.ProductMapper;
import com.assignment.shop.products.repo.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepo;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductDto testProductDto;

    @BeforeEach
    void setup() {

        testProduct = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("Gaming laptop")
                .price(new BigDecimal("1200.00"))
                .quantity(10)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testProductDto = ProductDto.builder()
                .id(1L)
                .name("Laptop")
                .description("Gaming laptop")
                .price(new BigDecimal("1200.00"))
                .quantity(10)
                .status("ACTIVE")
                .available(true)
                .build();
    }

    @Test
    @DisplayName("Should create product successfully")
    void whenCreateProduct_thenReturnSavedProduct() {
        when(productRepo.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.mapProductToProductDto(any(Product.class))).thenReturn(testProductDto);

        ProductDto result = productService.createProduct(testProductDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Laptop");
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should retrieve product by ID when product exists")
    void whenGetProductById_thenReturnProduct() {
        when(productRepo.findByIdAndStatus(1L, AppConstants.STATUS_ACTIVE)).thenReturn(Optional.of(testProduct));

        ProductDto result = productService.getProduct(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(productRepo).findByIdAndStatus(1L, AppConstants.STATUS_ACTIVE);
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void whenGetNonExistentProduct_thenThrowException() {
        when(productRepo.findByIdAndStatus(anyLong(), anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("Should update product details")
    void whenUpdateProduct_thenReturnUpdatedProduct() {
        ProductDto updatedDto = ProductDto.builder()
                .id(1L)
                .name("Updated Laptop")
                .description("Updated description")
                .price(new BigDecimal("1500.00"))
                .quantity(5)
                .build();

        when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepo.save(any(Product.class))).thenReturn(testProduct);

        ProductDto result = productService.updateProduct(updatedDto);

        assertThat(result).isNotNull();
        verify(productRepo).save(any(Product.class));
    }

    @Test
    @DisplayName("Should soft delete product")
    void whenDeleteProduct_thenMarkAsDeleted() {
        when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));

        productService.deleteProduct(1L);

        verify(productRepo).save(argThat(product ->
            product.getStatus().equals(AppConstants.STATUS_DELETED)
        ));
    }

    @Test
    @DisplayName("Should find products by price range with pagination")
    void whenSearchByPriceRangeWithPagination_thenReturnPage() {
        // Given
        Double minPrice = 1000.0;
        Double maxPrice = 2000.0;
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);
        when(productRepo.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);

        // When
        Page<ProductDto> results = productService.findProducts(null, minPrice, maxPrice, null, pageable);

        // Then
        assertThat(results).isNotNull();
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getName()).isEqualTo("Laptop");
        assertThat(results.getTotalElements()).isEqualTo(1L);
        verify(productRepo).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Should throw error when only min price provided")
    void whenOnlyMinPriceProvided_thenThrowException() {
        Pageable pageable = PageRequest.of(0, 10);
        assertThatThrownBy(() -> productService.findProducts(null, 100.0, null, null, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("both min and max price");
    }

    @Test
    @DisplayName("Invalid price range - max only")
    void whenOnlyMaxPriceProvided_thenThrowException() {
        Pageable pageable = PageRequest.of(0, 10);
        assertThatThrownBy(() -> productService.findProducts(null, null, 1000.0, null, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("both min and max price");
    }
}

