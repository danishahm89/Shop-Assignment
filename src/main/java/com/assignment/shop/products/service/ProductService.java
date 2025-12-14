package com.assignment.shop.products.service;

import com.assignment.shop.constants.AppConstants;
import com.assignment.shop.exceptions.ResourceNotFoundException;
import com.assignment.shop.products.dto.ProductDto;
import com.assignment.shop.products.entity.Product;
import com.assignment.shop.products.mapper.ProductMapper;
import com.assignment.shop.products.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository repo;
    private final ProductMapper mapper;

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        log.info("Creating product: {}", dto.getName());

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .status(AppConstants.STATUS_ACTIVE)
                .build();
        
        Product saved = repo.save(product);
        return mapper.mapProductToProductDto(saved);
    }

    @Transactional
    public ProductDto updateProduct(ProductDto dto) {
        log.info("Updating product: {}", dto.getId());

        Product product = repo.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.ERROR_PRODUCT_NOT_FOUND));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());

        return toDto(repo.save(product));
    }

    public ProductDto getProduct(Long id) {
        Product product = repo.findByIdAndStatus(id, AppConstants.STATUS_ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.ERROR_PRODUCT_NOT_FOUND));
        return toDto(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product: {}", id);

        Product product = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.ERROR_PRODUCT_NOT_FOUND));

        product.setStatus(AppConstants.STATUS_DELETED);
        repo.save(product);
    }

    public Page<ProductDto> findProducts(String name, Double minPrice, Double maxPrice,
                                         Boolean available, Pageable pageable) {
        log.debug("Finding products with filters - name: {}, minPrice: {}, maxPrice: {}, available: {}, page: {}, size: {}",
                  name, minPrice, maxPrice, available, pageable.getPageNumber(), pageable.getPageSize());

        if ((nonNull(minPrice) && isNull(maxPrice)) || (isNull(minPrice) && nonNull(maxPrice))) {
            throw new IllegalArgumentException(AppConstants.ERROR_PRICE_RANGE);
        }

        Specification<Product> spec = Specification.where(isActive())
                .and(nameContains(name))
                .and(priceBetween(minPrice, maxPrice))
                .and(isAvailable(available));

        Page<Product> productPage = repo.findAll(spec, pageable);
        log.debug("Found {} products (total: {})", productPage.getNumberOfElements(), productPage.getTotalElements());

        return productPage.map(this::toDto);
    }

    private Specification<Product> isActive() {
        return (root, query, builder) ->
            builder.equal(root.get("status"), AppConstants.STATUS_ACTIVE);
    }

    private Specification<Product> nameContains(String name) {
        return (root, query, builder) -> {
            if (name == null || name.trim().isEmpty()) {
                return builder.conjunction();
            }
            return builder.like(builder.lower(root.get("name")),
                              "%" + name.toLowerCase() + "%");
        };
    }

    private Specification<Product> priceBetween(Double minPrice, Double maxPrice) {
        return (root, query, builder) -> {
            if (minPrice == null || maxPrice == null) {
                return builder.conjunction();
            }
            return builder.between(root.get("price"), minPrice, maxPrice);
        };
    }

    private Specification<Product> isAvailable(Boolean available) {
        return (root, query, builder) -> {
            if (!Boolean.TRUE.equals(available)) {
                return builder.conjunction();
            }
            return builder.greaterThan(root.get("quantity"), 0);
        };
    }

    private ProductDto toDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .status(product.getStatus())
                .available(product.getQuantity() > 0)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

}
