package com.assignment.shop.orders.repo;

import com.assignment.shop.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {}
