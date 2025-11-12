package com.furryhub.petservices.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furryhub.petservices.model.dto.CartDto;
import com.furryhub.petservices.model.dto.CartItemDto;
import com.furryhub.petservices.model.dto.CartMergeRequest;
import com.furryhub.petservices.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private CartDto sampleCartDto;
    private CartItemDto sampleCartItemDto;
    private CartMergeRequest sampleMergeRequest;

    @BeforeEach
    void setUp() {
        // Setup sample data
        sampleCartItemDto = CartItemDto.builder()
                .packageId(1L)
                .qty(2)
                .build();

        sampleCartDto = CartDto.builder()
                .userId(1L)
                .items(Arrays.asList(sampleCartItemDto))
                .total(BigDecimal.valueOf(100.0))
                .build();

        sampleMergeRequest = CartMergeRequest.builder()
                .items(Arrays.asList(sampleCartItemDto))
                .build();
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void mergeCart_ShouldReturnMergedCart() throws Exception {
        Mockito.when(cartService.mergeLocalCart(eq("test@example.com"), any()))
                .thenReturn(sampleCartDto);

        mockMvc.perform(post("/api/cart/merge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleMergeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.total").value(100.0));
    }

    @Test
    void mergeCart_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/cart/merge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleMergeRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void getCart_ShouldReturnCart() throws Exception {
        Mockito.when(cartService.getCartForCustomer("test@example.com"))
                .thenReturn(sampleCartDto);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.items[0].packageId").value(1L));
    }

    @Test
    void getCart_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void addItem_ShouldReturnUpdatedCart() throws Exception {
        Mockito.when(cartService.addItemToCart(eq("test@example.com"), any(CartItemDto.class)))
                .thenReturn(sampleCartDto);

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCartItemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    void addItem_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleCartItemDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void clearCart_ShouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(cartService).clearCartForCustomer("test@example.com");

        mockMvc.perform(delete("/api/cart/clear"))
                .andExpect(status().isNoContent());
    }

    @Test
    void clearCart_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/cart/clear"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void clearSpecificItem_ShouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(cartService).clearSpecificItem("test@example.com", 1L);

        mockMvc.perform(delete("/api/cart/clearSpecific/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void clearSpecificItem_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/cart/clearSpecific/1"))
                .andExpect(status().isUnauthorized());
    }
}
