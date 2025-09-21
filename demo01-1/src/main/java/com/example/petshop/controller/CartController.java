package com.example.petshop.controller;

import com.example.petshop.entity.User;
import com.example.petshop.service.CartService;
import com.example.petshop.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public String viewCart(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        var cart = cartService.getCartByUser(user);
        model.addAttribute("cart", cart);
        model.addAttribute("total", cartService.calculateTotal(cart));
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                           @RequestParam Integer quantity,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName());
            cartService.addToCart(user, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Product added to cart!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/update/{itemId}")
    public String updateQuantity(@PathVariable Long itemId,
                                @RequestParam Integer quantity,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName());
            cartService.updateQuantity(user, itemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cart updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove/{itemId}")
    public String removeFromCart(@PathVariable Long itemId,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName());
            cartService.removeFromCart(user, itemId);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/cart";
    }
}
