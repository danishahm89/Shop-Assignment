package com.assignment.shop.orders.mapper;

import com.assignment.shop.orders.dto.OrderDto;
import com.assignment.shop.orders.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);
    OrderDto mapOrderToOrderDto(Order order);
    Order mapOrderDtoToOrder(OrderDto orderDto);
}
