package com.example.petshop.service;

import com.example.petshop.entity.*;
import com.example.petshop.repository.CartItemRepository;
import com.example.petshop.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, 
                      ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    public Cart getCartByUser(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> createCartForUser(user));
    }

    private Cart createCartForUser(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    @Transactional
    public void addToCart(User user, Long productId, Integer quantity) {
        Product product = productService.findById(productId);
        Cart cart = getCartByUser(user);

        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        var existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
        
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            
            if (product.getStock() < newQuantity) {
                throw new RuntimeException("Insufficient stock");
            }
            
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setPriceAtAdd(product.getPrice());
            cartItemRepository.save(item);
        }
    }

    @Transactional
    public void updateQuantity(User user, Long itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        Cart cart = getCartByUser(user);
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            if (item.getProduct().getStock() < quantity) {
                throw new RuntimeException("Insufficient stock");
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    @Transactional
    public void removeFromCart(User user, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        Cart cart = getCartByUser(user);
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getCartByUser(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    public BigDecimal calculateTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getPriceAtAdd().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
