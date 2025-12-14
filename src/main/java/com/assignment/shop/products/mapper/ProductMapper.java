package com.assignment.shop.products.mapper;

import com.assignment.shop.products.dto.ProductDto;
import com.assignment.shop.products.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);
    ProductDto mapProductToProductDto(Product product);
    Product mapProductDtoToProduct(ProductDto productDto);
}
