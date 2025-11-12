package com.furryhub.petservices.controller;

import com.furryhub.petservices.model.dto.CartDto;
import com.furryhub.petservices.model.dto.CartItemDto;
import com.furryhub.petservices.model.dto.CartMergeRequest;
import com.furryhub.petservices.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/merge")
    public ResponseEntity<CartDto> mergeCart(Principal principal,
                                             @RequestBody CartMergeRequest request) {
        if (principal == null) return ResponseEntity.status(401).build();
        CartDto merged = cartService.mergeLocalCart(principal.getName(), request.getItems());
        return ResponseEntity.ok(merged);
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        System.out.println("principal.getName(): " + (principal == null ? null : principal.getName()));
        CartDto cart = cartService.getCartForCustomer(principal.getName());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<CartDto> addItem(Principal principal, @RequestBody CartItemDto itemDto) {
        if (principal == null) return ResponseEntity.status(401).build();
        CartDto updated = cartService.addItemToCart(principal.getName(), itemDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        cartService.clearCartForCustomer(principal.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clearSpecific/{id}")
    public ResponseEntity<Void> clearSpecificItem(Principal principal,@PathVariable Long id){
        if(principal==null) return ResponseEntity.status(401).build();
        cartService.clearSpecificItem(principal.getName(),id);
        return ResponseEntity.noContent().build();
    }
}