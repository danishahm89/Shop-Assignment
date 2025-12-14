package com.assignment.shop.orders.mapper;

import com.assignment.shop.orders.dto.OrderItemDto;
import com.assignment.shop.orders.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    OrderItemMapper INSTANCE = Mappers.getMapper(OrderItemMapper.class);
    OrderItemDto mapOrderItemToOrderItemDto(OrderItem orderItem);
    OrderItem mapOrderItemDtoToOrderItem(OrderItemDto orderItemDto);
}
