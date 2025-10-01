package com.ecom.controller;

import com.ecom.model.Notification;
import com.ecom.model.UserDtls;
import com.ecom.service.NotificationService;
import com.ecom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String notificationsPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/signin";
        }

        UserDtls user = userService.getUserByEmail(principal.getName());
        List<Notification> notifications = notificationService.getUserNotifications(user);
        
        model.addAttribute("notifications", notifications);
        model.addAttribute("user", user);
        
        return "notifications";
    }

    @GetMapping("/count")
    @ResponseBody
    public Map<String, Object> getUnreadCount(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        if (principal == null) {
            response.put("count", 0);
            return response;
        }

        UserDtls user = userService.getUserByEmail(principal.getName());
        long unreadCount = notificationService.getUnreadCount(user);
        
        response.put("count", unreadCount);
        return response;
    }

    @PostMapping("/{id}/read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "Unauthorized");
                return ResponseEntity.status(401).body(response);
            }

            notificationService.markAsRead(id);
            response.put("success", true);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error marking notification as read");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/read-all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "Unauthorized");
                return ResponseEntity.status(401).body(response);
            }

            UserDtls user = userService.getUserByEmail(principal.getName());
            notificationService.markAllAsRead(user);
            response.put("success", true);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error marking all notifications as read");
            return ResponseEntity.status(500).body(response);
        }
    }
}