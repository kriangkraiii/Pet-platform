package com.example.petshop.service;

import com.example.petshop.entity.Order;
import com.example.petshop.entity.User;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Welcome to PetShop!");
        message.setText("Welcome " + user.getUsername() + "! Thank you for registering with PetShop.");
        mailSender.send(message);
    }

    public void sendOrderConfirmationEmail(Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(order.getUser().getEmail());
        message.setSubject("Order Confirmation - Order #" + order.getId());
        message.setText("Your order #" + order.getId() + " has been created successfully. Total: $" + order.getTotalAmount());
        mailSender.send(message);
    }

    public void sendOrderStatusEmail(Order order, String statusMessage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(order.getUser().getEmail());
        message.setSubject("Order Update - Order #" + order.getId());
        message.setText(statusMessage + "\nOrder #" + order.getId() + " Status: " + order.getStatus());
        mailSender.send(message);
    }

    public void sendTrackingEmail(Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(order.getUser().getEmail());
        message.setSubject("Your Order Has Shipped - Order #" + order.getId());
        message.setText("Your order #" + order.getId() + " has been shipped! Tracking number: " + order.getTrackingNumber());
        mailSender.send(message);
    }
}
