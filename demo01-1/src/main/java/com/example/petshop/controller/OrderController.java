package com.example.petshop.controller;

import com.example.petshop.entity.User;
import com.example.petshop.service.OrderService;
import com.example.petshop.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/my")
    public String myOrders(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size,
                         Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("orders", orderService.findByUser(user, pageable));
        return "my-orders";
    }

    @PostMapping
    public String createOrder(Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName());
            var order = orderService.createOrder(user);
            redirectAttributes.addFlashAttribute("successMessage", "Order created successfully!");
            return "redirect:/orders/" + order.getId() + "/checkout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating order: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/{id}/checkout")
    public String checkout(@PathVariable Long id, Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        var order = orderService.findById(id);
        
        // Verify user owns this order
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        model.addAttribute("order", order);
        return "checkout";
    }

    @PostMapping("/{id}/upload-slip")
    public String uploadPaymentSlip(@PathVariable Long id,
                                  @RequestParam("file") MultipartFile file,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName());
            orderService.uploadPaymentSlip(id, file, user);
            redirectAttributes.addFlashAttribute("successMessage", "Payment slip uploaded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error uploading payment slip: " + e.getMessage());
        }
        return "redirect:/orders/my";
    }
}
