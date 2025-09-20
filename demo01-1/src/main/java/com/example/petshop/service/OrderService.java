package com.example.petshop.service;

import com.example.petshop.entity.*;
import com.example.petshop.exception.ResourceNotFoundException;
import com.example.petshop.repository.OrderRepository;
import com.example.petshop.util.FileStorageUtil;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final MailService mailService;
    private final AdminLogService adminLogService;
    private final FileStorageUtil fileStorageUtil;

    public OrderService(OrderRepository orderRepository, CartService cartService, 
                       MailService mailService, AdminLogService adminLogService, 
                       FileStorageUtil fileStorageUtil) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.mailService = mailService;
        this.adminLogService = adminLogService;
        this.fileStorageUtil = fileStorageUtil;
    }

    public Page<Order> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public Page<Order> findByUser(User user, Pageable pageable) {
        return orderRepository.findByUserId(user.getId(), pageable);
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Transactional
    public Order createOrder(User user) {
        Cart cart = cartService.getCartByUser(user);
        
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(cartService.calculateTotal(cart));
        order.setStatus(Order.OrderStatus.CREATED);

        // Convert cart items to order items
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPriceAtAdd());
            order.getItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        
        // Clear cart after order creation
        cartService.clearCart(user);
        
        // Send order confirmation email
        mailService.sendOrderConfirmationEmail(savedOrder);
        
        return savedOrder;
    }

    @Transactional
    public void uploadPaymentSlip(Long orderId, MultipartFile file, User user) {
        Order order = findById(orderId);
        
        // Check if user owns this order
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only upload slips for your own orders");
        }

        String slipPath = fileStorageUtil.storeFile(file, "slips");
        order.setPaymentSlipPath(slipPath);
        order.setStatus(Order.OrderStatus.WAITING_CONFIRMATION);
        
        orderRepository.save(order);
    }

    @Transactional
    public void approveOrder(Long orderId, User admin) {
        Order order = findById(orderId);
        order.setStatus(Order.OrderStatus.PAID);
        Order updated = orderRepository.save(order);
        
        adminLogService.log(admin, AdminLogService.Action.APPROVE, "Order", orderId, 
                "Approved payment for order");
        
        mailService.sendOrderStatusEmail(updated, "Your payment has been approved!");
    }

    @Transactional
    public void rejectOrder(Long orderId, User admin) {
        Order order = findById(orderId);
        order.setStatus(Order.OrderStatus.REJECTED);
        Order updated = orderRepository.save(order);
        
        adminLogService.log(admin, AdminLogService.Action.REJECT, "Order", orderId, 
                "Rejected payment for order");
        
        mailService.sendOrderStatusEmail(updated, "Your payment has been rejected. Please contact support.");
    }

    @Transactional
    public void updateStatus(Long orderId, Order.OrderStatus status, User admin) {
        Order order = findById(orderId);
        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        Order updated = orderRepository.save(order);
        
        adminLogService.log(admin, AdminLogService.Action.CHANGE_STATUS, "Order", orderId, 
                "Changed status from " + oldStatus + " to " + status);
        
        mailService.sendOrderStatusEmail(updated, "Your order status has been updated to: " + status);
    }

    @Transactional
    public void addTrackingNumber(Long orderId, String trackingNumber, User admin) {
        Order order = findById(orderId);
        order.setTrackingNumber(trackingNumber);
        order.setStatus(Order.OrderStatus.SHIPPED);
        Order updated = orderRepository.save(order);
        
        adminLogService.log(admin, AdminLogService.Action.UPDATE, "Order", orderId, 
                "Added tracking number: " + trackingNumber);
        
        mailService.sendTrackingEmail(updated);
    }
}
