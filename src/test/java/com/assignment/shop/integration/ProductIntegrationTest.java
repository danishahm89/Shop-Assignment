package com.assignment.shop.integration;

import com.assignment.shop.products.dto.ProductDto;
import com.assignment.shop.security.dto.AuthResponse;
import com.assignment.shop.security.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setup() throws Exception {
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setUsername("admin");
        adminLogin.setPassword("admin123");

        MvcResult adminResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse adminResponse = objectMapper.readValue(
                adminResult.getResponse().getContentAsString(),
                AuthResponse.class
        );
        adminToken = adminResponse.getToken();

        LoginRequest userLogin = new LoginRequest();
        userLogin.setUsername("user");
        userLogin.setPassword("user123");

        MvcResult userResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLogin)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse userResponse = objectMapper.readValue(
                userResult.getResponse().getContentAsString(),
                AuthResponse.class
        );
        userToken = userResponse.getToken();
    }

    @Test
    @DisplayName("Full product lifecycle - create, read, update, delete")
    void testProductLifecycle() throws Exception {
        ProductDto newProduct = ProductDto.builder()
                .name("Integration Test Product")
                .description("Created during integration test")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Test Product"))
                .andReturn();

        ProductDto createdProduct = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                ProductDto.class
        );
        Long productId = createdProduct.getId();

        mockMvc.perform(get("/api/products/" + productId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId));

        createdProduct.setPrice(new BigDecimal("149.99"));
        mockMvc.perform(put("/api/products/" + productId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdProduct)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/products/" + productId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("User should be able to search products with pagination")
    void testProductSearch() throws Exception {
        mockMvc.perform(get("/api/products")
                .header("Authorization", "Bearer " + userToken)
                .param("minPrice", "100")
                .param("maxPrice", "2000")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @DisplayName("Regular user cannot create product")
    void testUserCannotCreateProduct() throws Exception {
        ProductDto newProduct = ProductDto.builder()
                .name("Unauthorized Product")
                .price(new BigDecimal("50.00"))
                .quantity(5)
                .build();

        mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isForbidden());
    }
}

