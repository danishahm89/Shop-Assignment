package com.assignment.shop.orders.controller;

import com.assignment.shop.orders.dto.OrderDto;
import com.assignment.shop.orders.service.OrderService;
import com.assignment.shop.security.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'PREMIUM_USER', 'ADMIN')")
    @Operation(summary = "Place order")
            @ApiResponse(responseCode = "404", description = "Product not found or out of stock", content = @Content)
    public ResponseEntity<OrderDto> placeOrder(
            @Valid @RequestBody OrderDto orderDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(userId, orderDto.getItems()));
    }
}
