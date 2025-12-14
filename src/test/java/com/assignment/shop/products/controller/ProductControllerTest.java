package com.assignment.shop.products.controller;

import com.assignment.shop.products.dto.ProductDto;
import com.assignment.shop.products.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;


    private ProductDto sampleProduct;

    @BeforeEach
    void setup() {
        sampleProduct = ProductDto.builder()
                .id(1L)
                .name("Wireless Mouse")
                .description("Ergonomic wireless mouse")
                .price(new BigDecimal("29.99"))
                .quantity(50)
                .status("ACTIVE")
                .available(true)
                .build();
    }

    @Test
    @DisplayName("Create product as admin")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminCreatesProduct_thenSuccess() throws Exception {
        when(productService.createProduct(any(ProductDto.class))).thenReturn(sampleProduct);

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Wireless Mouse"))
                .andExpect(jsonPath("$.price").value(29.99));
    }

    @Test
    @DisplayName("User cannot create product")
    @WithMockUser(username = "user", roles = {"USER"})
    void whenUserCreatesProduct_thenForbidden() throws Exception {
        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleProduct)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("View product")
    @WithMockUser(username = "user", roles = {"USER"})
    void whenUserGetsProduct_thenSuccess() throws Exception {
        when(productService.getProduct(1L)).thenReturn(sampleProduct);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Wireless Mouse"));
    }

    @Test
    @DisplayName("Product not found")
    @WithMockUser(username = "user", roles = {"USER"})
    void whenProductNotFound_thenNotFound() throws Exception {
        when(productService.getProduct(anyLong()))
                .thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Search products")
    @WithMockUser(username = "user", roles = {"USER"})
    void whenSearchProducts_thenReturnFiltered() throws Exception {
        ProductDto product2 = ProductDto.builder()
                .id(2L)
                .name("Keyboard")
                .price(new BigDecimal("79.99"))
                .quantity(30)
                .build();

        List<ProductDto> products = Arrays.asList(sampleProduct, product2);
        Page<ProductDto> page = new PageImpl<>(products, PageRequest.of(0, 20), products.size());
        when(productService.findProducts(any(), any(), any(), any(), any())).thenReturn(page);

        // when & then
        mockMvc.perform(get("/api/products")
                .param("minPrice", "20")
                .param("maxPrice", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("Admin can update product")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminUpdatesProduct_thenSuccess() throws Exception {
        // given
        ProductDto updatedProduct = ProductDto.builder()
                .id(1L)
                .name("Updated Mouse")
                .price(new BigDecimal("24.99"))
                .quantity(100)
                .build();

        when(productService.updateProduct(any(ProductDto.class))).thenReturn(updatedProduct);

        // when & then
        mockMvc.perform(put("/api/products/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Mouse"));
    }

    @Test
    @DisplayName("Admin can delete product")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminDeletesProduct_thenSuccess() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/products/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Unauthenticated access denied")
    void whenNoAuth_thenForbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isForbidden());
    }
}

