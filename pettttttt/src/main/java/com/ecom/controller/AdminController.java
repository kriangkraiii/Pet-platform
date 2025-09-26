package com.ecom.controller;
import com.ecom.service.AdminLogService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecom.model.AdminLog;
import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private AdminLogService adminLogService;
	
	// Consider adding more specific exception handling
	@ExceptionHandler(IOException.class)
	public String handleIOException(IOException e, HttpSession session) {
	    session.setAttribute("errorMsg", "File operation failed");
	    return "redirect:/admin/";
	}


//	@GetMapping("/deleteOrder/{id}")
//	public String deleteOrder(@PathVariable Integer id, HttpServletRequest request, RedirectAttributes redirectAttributes, Principal p) {
//	    try {
//	        // Get the order before deleting for logging
//	        ProductOrder order = orderService.getOrderById(id);
//	        if (order != null) {
//	            // Check if order can be deleted (only allow deletion of cancelled orders for safety)
//	            if (!"Cancelled".equals(order.getStatus())) {
//	                redirectAttributes.addFlashAttribute("errorMsg", "Only cancelled orders can be deleted");
//	                return "redirect:/admin/orders";
//	            }
//	            
//	            // Delete the order
//	            Boolean isDeleted = orderService.deleteOrder(id);
//	            
//	            if (isDeleted) {
//	                // Log the admin action
//	                UserDtls admin = commonUtil.getLoggedInUserDetails(p);
//	                String ipAddress = getClientIpAddress(request);
//	                adminLogService.logAction(admin.getEmail(), admin.getName(), "DELETE_ORDER", 
//	                    "Deleted order: " + order.getOrderId(), ipAddress);
//	                
//	                redirectAttributes.addFlashAttribute("succMsg", "Order deleted successfully");
//	            } else {
//	                redirectAttributes.addFlashAttribute("errorMsg", "Failed to delete order");
//	            }
//	        } else {
//	            redirectAttributes.addFlashAttribute("errorMsg", "Order not found");
//	        }
//	    } catch (Exception e) {
//	        redirectAttributes.addFlashAttribute("errorMsg", "Cannot delete order: " + e.getMessage());
//	        e.printStackTrace();
//	    }
//	    
//	    return "redirect:/admin/orders";
//	}


//	@GetMapping("/deleteOrder/{id}")
//	public String deleteOrder(@PathVariable Integer id, HttpSession session) {
//	    Boolean isDeleted = orderService.deleteOrder(id);
//	    
//	    if (isDeleted) {
//	        session.setAttribute("succMsg", "Order deleted successfully");
//	    } else {
//	        session.setAttribute("errorMsg", "Failed to delete order");
//	    }
//	    
//	    return "redirect:/admin/orders";
//	}


	// Add logging endpoint
	@GetMapping("/logs")
	public String viewAdminLogs(Model m, 
	                           @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
	                           @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize) {
	    
	    Page<AdminLog> page = adminLogService.getAllLogs(pageNo, pageSize);
	    m.addAttribute("logs", page.getContent());
	    
	    m.addAttribute("pageNo", page.getNumber());
	    m.addAttribute("pageSize", pageSize);
	    m.addAttribute("totalElements", page.getTotalElements());
	    m.addAttribute("totalPages", page.getTotalPages());
	    m.addAttribute("isFirst", page.isFirst());
	    m.addAttribute("isLast", page.isLast());
	    
	    return "admin/logs";
	}

	// Update existing methods to include logging
	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
	                          HttpSession session, Principal p, HttpServletRequest request) throws IOException {
	    
	    UserDtls admin = commonUtil.getLoggedInUserDetails(p);
	    String ipAddress = getClientIpAddress(request);
	    
	    String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
	    category.setImageName(imageName);

	    Boolean existCategory = categoryService.existCategory(category.getName());

	    if (existCategory) {
	        session.setAttribute("errorMsg", "Category Name already exists");
	        adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                "CREATE_CATEGORY_FAILED", 
	                                "Failed to create category: " + category.getName() + " (already exists)", 
	                                ipAddress);
	    } else {
	        Category saveCategory = categoryService.saveCategory(category);

	        if (ObjectUtils.isEmpty(saveCategory)) {
	            session.setAttribute("errorMsg", "Not saved ! internal server error");
	            adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                    "CREATE_CATEGORY_FAILED", 
	                                    "Failed to create category: " + category.getName() + " (server error)", 
	                                    ipAddress);
	        } else {
	            File saveFile = new ClassPathResource("static/img").getFile();
	            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
	                    + file.getOriginalFilename());
	            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

	            session.setAttribute("succMsg", "Saved successfully");
	            adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                    "CREATE_CATEGORY", 
	                                    "Created new category: " + category.getName(), 
	                                    ipAddress);
	        }
	    }

	    return "redirect:/admin/category";
	}
	

//	@PostMapping("/delete-order")
//	public String deleteOrder(@RequestParam Integer orderId, HttpSession session, Principal p) {
//	    try {
//	        // First check if the order exists and is cancelled
//	        ProductOrder order = orderService.getOrderById(orderId);
//	        
//	        if (order == null) {
//	            session.setAttribute("errorMsg", "Order not found");
//	            return "redirect:/admin/orders";
//	        }
//	        
//	        if (!"Cancelled".equals(order.getStatus())) {
//	            session.setAttribute("errorMsg", "Only cancelled orders can be deleted");
//	            return "redirect:/admin/orders";
//	        }
//	        
//	        // Log admin action
//	        UserDtls loggedInUser = commonUtil.getLoggedInUserDetails(p);
//	        String ipAddress = getClientIpAddress(request);
//	        adminLogService.logAction(loggedInUser.getEmail(), loggedInUser.getName(),
//	                "DELETE_ORDER", "Deleted cancelled order: " + order.getOrderId(), ipAddress);
//	        
//	        // Delete the order
//	       
//	        Boolean isDeleted = orderService.deleteOrder(orderId);
//	        
//	        if (isDeleted) {
//	            session.setAttribute("succMsg", "Order deleted successfully");
//	        } else {
//	            session.setAttribute("errorMsg", "Failed to delete order");
//	        }
//	        
//	    } catch (Exception e) {
//	       
//	        session.setAttribute("errorMsg", "Cannot delete order: " + e.getMessage());
//	        e.printStackTrace();
//	    }
//	    
//	    return "redirect:/admin/orders";
//	}

	private String getClientIpAddress(HttpServletRequest request) {
	    String xForwardedFor = request.getHeader("X-Forwarded-For");
	    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
	        return xForwardedFor.split(",")[0];
	    }
	    return request.getRemoteAddr();
	}

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}

		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}

//	@GetMapping("/")
//	public String index() {
//		return "admin/index";
//	}

	@GetMapping("/loadAddProduct")
	public String loadAddProduct(Model m) {
		List<Category> categories = categoryService.getAllCategory();
		m.addAttribute("categories", categories);
		return "admin/add_product";
	}

	@GetMapping("/category")
	public String category(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
		// m.addAttribute("categorys", categoryService.getAllCategory());
		Page<Category> page = categoryService.getAllCategorPagination(pageNo, pageSize);
		List<Category> categorys = page.getContent();
		m.addAttribute("categorys", categorys);

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "admin/category";
	}

//	@PostMapping("/saveCategory")
//	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
//			HttpSession session) throws IOException {
//
//		String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
//		category.setImageName(imageName);
//
//		Boolean existCategory = categoryService.existCategory(category.getName());
//
//		if (existCategory) {
//			session.setAttribute("errorMsg", "Category Name already exists");
//		} else {
//
//			Category saveCategory = categoryService.saveCategory(category);
//
//			if (ObjectUtils.isEmpty(saveCategory)) {
//				session.setAttribute("errorMsg", "Not saved ! internal server error");
//			} else {
//
//				File saveFile = new ClassPathResource("static/img").getFile();
//
//				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
//						+ file.getOriginalFilename());
//
//				// System.out.println(path);
//				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
//				session.setAttribute("succMsg", "Saved successfully");
//			}
//		}
//
//		return "redirect:/admin/category";
//	}

	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session, Principal p) {
	    UserDtls admin = commonUtil.getLoggedInUserDetails(p);
	    String ipAddress = getClientIpAddress(request);
	    Category category = categoryService.getCategoryById(id);
	    
	    Boolean deleteCategory = categoryService.deleteCategory(id);

	    if (deleteCategory) {
	        session.setAttribute("succMsg", "category delete success");
	        adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                "DELETE_CATEGORY", 
	                                "Deleted category: " + (category != null ? category.getName() : "ID " + id), 
	                                ipAddress);
	    } else {
	        session.setAttribute("errorMsg", "something wrong on server");
	        adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                "DELETE_CATEGORY_FAILED", 
	                                "Failed to delete category ID: " + id, 
	                                ipAddress);
	    }

	    return "redirect:/admin/category";
	}

	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {
		m.addAttribute("category", categoryService.getCategoryById(id));
		return "admin/edit_category";
	}

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
	        HttpSession session, Principal p) throws IOException {

	    UserDtls admin = commonUtil.getLoggedInUserDetails(p);
	    String ipAddress = getClientIpAddress(request);

	    Category oldCategory = categoryService.getCategoryById(category.getId());
	    String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();

	    if (!ObjectUtils.isEmpty(category)) {
	        oldCategory.setName(category.getName());
	        oldCategory.setIsActive(category.getIsActive());
	        oldCategory.setImageName(imageName);
	    }

	    Category updateCategory = categoryService.saveCategory(oldCategory);

	    if (!ObjectUtils.isEmpty(updateCategory)) {
	        if (!file.isEmpty()) {
	            File saveFile = new ClassPathResource("static/img").getFile();
	            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
	                    + file.getOriginalFilename());
	            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
	        }

	        session.setAttribute("succMsg", "Category update success");
	        adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                "UPDATE_CATEGORY", 
	                                "Updated category: " + category.getName(), 
	                                ipAddress);
	    } else {
	        session.setAttribute("errorMsg", "something wrong on server");
	        adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                "UPDATE_CATEGORY_FAILED", 
	                                "Failed to update category: " + category.getName(), 
	                                ipAddress);
	    }

	    return "redirect:/admin/loadEditCategory/" + category.getId();
	}

	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
	        HttpSession session, Principal p) throws IOException {

	    UserDtls admin = commonUtil.getLoggedInUserDetails(p);
	    String ipAddress = getClientIpAddress(request);

	    String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();
	    product.setImage(imageName);
	    product.setDiscount(0);
	    product.setDiscountPrice(product.getPrice());
	    Product saveProduct = productService.saveProduct(product);

	    if (!ObjectUtils.isEmpty(saveProduct)) {
	        File saveFile = new ClassPathResource("static/img").getFile();
	        Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator
	                + image.getOriginalFilename());
	        Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

	        session.setAttribute("succMsg", "Product Saved Success");
	        adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                "CREATE_PRODUCT", 
	                                "Created new product: " + product.getTitle(), 
	                                ipAddress);
	    } else {
	        session.setAttribute("errorMsg", "something wrong on server");
	        adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                "CREATE_PRODUCT_FAILED", 
	                                "Failed to create product: " + product.getTitle(), 
	                                ipAddress);
	    }

	    return "redirect:/admin/loadAddProduct";
	}

	@GetMapping("/products")
	public String loadViewProduct(Model m, @RequestParam(defaultValue = "") String ch,
	        @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
	        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

	    // Limit maximum page size for better performance
	    if (pageSize > 50) {
	        pageSize = 50;
	    }

	    Page<Product> page = null;
	    if (ch != null && ch.length() > 0) {
	        page = productService.searchProductPagination(pageNo, pageSize, ch);
	    } else {
	        page = productService.getAllProductsPagination(pageNo, pageSize);
	    }

	    // Calculate statistics efficiently
	    List<Product> products = page.getContent();
	    long activeProductsCount = products.stream().filter(Product::getIsActive).count();
	    long lowStockCount = products.stream().filter(p -> p.getStock() != null && p.getStock().intValue() < 10).count();

	    // Check if each product has orders and create a map for the template
	    Map<Integer, Boolean> productOrdersMap = new HashMap<>();
	    for (Product product : products) {
	        List<ProductOrder> orders = orderService.getOrdersByProduct(product.getId());
	        productOrdersMap.put(product.getId(), !orders.isEmpty());
	    }

	    // Add attributes for the view
	    m.addAttribute("products", products);
	    m.addAttribute("productOrdersMap", productOrdersMap);
	    m.addAttribute("activeProductsCount", activeProductsCount);
	    m.addAttribute("lowStockCount", lowStockCount);
	    m.addAttribute("pageNo", page.getNumber());
	    m.addAttribute("pageSize", pageSize);
	    m.addAttribute("totalElements", page.getTotalElements());
	    m.addAttribute("totalPages", page.getTotalPages());
	    m.addAttribute("isFirst", page.isFirst());
	    m.addAttribute("isLast", page.isLast());

	    return "admin/products";
	}




	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id, HttpSession session, Principal p) {
	    UserDtls admin = commonUtil.getLoggedInUserDetails(p);
	    String ipAddress = getClientIpAddress(request);
	    Product product = productService.getProductById(id);
	    
	    Boolean deleteProduct = productService.deleteProduct(id);
	    if (deleteProduct) {
	        session.setAttribute("succMsg", "Product delete success");
	        adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                "DELETE_PRODUCT", 
	                                "Deleted product: " + (product != null ? product.getTitle() : "ID " + id), 
	                                ipAddress);
	    } else {
	        session.setAttribute("errorMsg", "Something wrong on server");
	        adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                "DELETE_PRODUCT_FAILED", 
	                                "Failed to delete product ID: " + id, 
	                                ipAddress);
	    }
	    return "redirect:/admin/products";
	}

	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, Model m) {
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("categories", categoryService.getAllCategory());
		return "admin/edit_product";
	}

	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
	        HttpSession session, Model m, Principal p) {

	    UserDtls admin = commonUtil.getLoggedInUserDetails(p);
	    String ipAddress = getClientIpAddress(request);

	    if (product.getDiscount() < 0 || product.getDiscount() > 100) {
	        session.setAttribute("errorMsg", "invalid Discount");
	        adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                "UPDATE_PRODUCT_FAILED", 
	                                "Failed to update product: " + product.getTitle() + " (invalid discount)", 
	                                ipAddress);
	    } else {
	        Product updateProduct = productService.updateProduct(product, image);
	        if (!ObjectUtils.isEmpty(updateProduct)) {
	            session.setAttribute("succMsg", "Product update success");
	            adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                    "UPDATE_PRODUCT", 
	                                    "Updated product: " + product.getTitle(), 
	                                    ipAddress);
	        } else {
	            session.setAttribute("errorMsg", "Something wrong on server");
	            adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                    "UPDATE_PRODUCT_FAILED", 
	                                    "Failed to update product: " + product.getTitle() + " (server error)", 
	                                    ipAddress);
	        }
	    }
	    return "redirect:/admin/editProduct/" + product.getId();
	}

	@GetMapping("/users")
	public String getAllUsers(Model m, @RequestParam Integer type) {
		List<UserDtls> users = null;
		if (type == 1) {
			users = userService.getUsers("ROLE_USER");
		} else {
			users = userService.getUsers("ROLE_ADMIN");
		}
		m.addAttribute("userType",type);
		m.addAttribute("users", users);
		return "/admin/users";
	}

	@GetMapping("/updateSts")
	public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id,
	        @RequestParam Integer type, HttpSession session, Principal p) {
	    
	    UserDtls admin = commonUtil.getLoggedInUserDetails(p);
	    String ipAddress = getClientIpAddress(request);
	    UserDtls user = userService.getUserById(id); // Changed from getId to getUserById
	    
	    Boolean f = userService.updateAccountStatus(id, status);
	    if (f) {
	        session.setAttribute("succMsg", "Account Status Updated");
	        adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                "UPDATE_USER_STATUS", 
	                                "Updated user status - User: " + (user != null ? user.getEmail() : "ID " + id) + 
	                                ", Status: " + (status ? "Active" : "Inactive"), 
	                                ipAddress);
	    } else {
	        session.setAttribute("errorMsg", "Something wrong on server");
	        adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                "UPDATE_USER_STATUS_FAILED", 
	                                "Failed to update user status for ID: " + id, 
	                                ipAddress);
	    }
	    return "redirect:/admin/users?type=" + type;
	}



	@GetMapping("/orders")
	public String getAllOrders(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
	        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
		CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
	    if (csrfToken != null) {
	        m.addAttribute("_csrf", csrfToken);
	    }
	    Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
	    m.addAttribute("orders", page.getContent());
	    m.addAttribute("srch", false);

	    m.addAttribute("pageNo", page.getNumber());
	    m.addAttribute("pageSize", pageSize);
	    m.addAttribute("totalElements", page.getTotalElements());
	    m.addAttribute("totalPages", page.getTotalPages());
	    m.addAttribute("isFirst", page.isFirst());
	    m.addAttribute("isLast", page.isLast());

	    return "admin/orders"; // Remove the leading slash
	}


	@PostMapping("/update-order-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, 
	        HttpSession session, Principal p) {

	    UserDtls admin = commonUtil.getLoggedInUserDetails(p);
	    String ipAddress = getClientIpAddress(request);

	    OrderStatus[] values = OrderStatus.values();
	    String status = null;

	    for (OrderStatus orderSt : values) {
	        if (orderSt.getId().equals(st)) {
	            status = orderSt.getName();
	        }
	    }

	    ProductOrder updateOrder = orderService.updateOrderStatus(id, status);

	    try {
	        commonUtil.sendMailForProductOrder(updateOrder, status);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    if (!ObjectUtils.isEmpty(updateOrder)) {
	        session.setAttribute("succMsg", "Status Updated");
	        adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                "UPDATE_ORDER_STATUS", 
	                                "Updated order status - Order ID: " + id + ", Status: " + status, 
	                                ipAddress);
	    } else {
	        session.setAttribute("errorMsg", "status not updated");
	        adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                "UPDATE_ORDER_STATUS_FAILED", 
	                                "Failed to update order status for ID: " + id, 
	                                ipAddress);
	    }
	    return "redirect:/admin/orders";
	}
	@GetMapping("/search-order")
	public String searchProduct(@RequestParam String orderId, Model m, HttpSession session,
	        @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
	        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

	    if (orderId != null && orderId.length() > 0) {
	        ProductOrder order = orderService.getOrdersByOrderId(orderId.trim());

	        if (ObjectUtils.isEmpty(order)) {
	            session.setAttribute("errorMsg", "Incorrect orderId");
	            m.addAttribute("orderDtls", null);
	        } else {
	            m.addAttribute("orderDtls", order);
	        }
	        m.addAttribute("srch", true);
	    } else {
	        Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
	        m.addAttribute("orders", page.getContent()); // Change from page to page.getContent()
	        m.addAttribute("srch", false);

	        m.addAttribute("pageNo", page.getNumber());
	        m.addAttribute("pageSize", pageSize);
	        m.addAttribute("totalElements", page.getTotalElements());
	        m.addAttribute("totalPages", page.getTotalPages());
	        m.addAttribute("isFirst", page.isFirst());
	        m.addAttribute("isLast", page.isLast());
	    }
	    return "admin/orders"; // Remove the leading slash
	}


	@GetMapping("/add-admin")
	public String loadAdminAdd() {
		return "/admin/add_admin";
	}

	@PostMapping("/save-admin")
	public String saveAdmin(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, 
	        HttpSession session, Principal p) throws IOException {

	    UserDtls admin = commonUtil.getLoggedInUserDetails(p);
	    String ipAddress = getClientIpAddress(request);

	    String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
	    user.setProfileImage(imageName);
	    UserDtls saveUser = userService.saveAdmin(user);

	    if (!ObjectUtils.isEmpty(saveUser)) {
	        if (!file.isEmpty()) {
	            File saveFile = new ClassPathResource("static/img").getFile();
	            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
	                    + file.getOriginalFilename());
	            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
	        }
	        session.setAttribute("succMsg", "Register successfully");
	        adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                "CREATE_ADMIN", 
	                                "Created new admin: " + user.getEmail(), 
	                                ipAddress);
	    } else {
	        session.setAttribute("errorMsg", "something wrong on server");
	        adminLogService.logAction(admin.getEmail(), admin.getName(), 
	                                "CREATE_ADMIN_FAILED", 
	                                "Failed to create admin: " + user.getEmail(), 
	                                ipAddress);
	    }

	    return "redirect:/admin/add-admin";
	}

	@GetMapping("/profile")
	public String profile() {
		return "/admin/profile";
	}

	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
		UserDtls updateUserProfile = userService.updateUserProfile(user, img);
		if (ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Profile not updated");
		} else {
			session.setAttribute("succMsg", "Profile Updated");
		}
		return "redirect:/admin/profile";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,
			HttpSession session) {
		UserDtls loggedInUserDetails = commonUtil.getLoggedInUserDetails(p);

		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());

		if (matches) {
			String encodePassword = passwordEncoder.encode(newPassword);
			loggedInUserDetails.setPassword(encodePassword);
			UserDtls updateUser = userService.updateUser(loggedInUserDetails);
			if (ObjectUtils.isEmpty(updateUser)) {
				session.setAttribute("errorMsg", "Password not updated !! Error in server");
			} else {
				session.setAttribute("succMsg", "Password Updated sucessfully");
			}
		} else {
			session.setAttribute("errorMsg", "Current Password incorrect");
		}

		return "redirect:/admin/profile";
	}
	@GetMapping("/")
	public String index(Model m) {
	    try {
	        // Dashboard metrics - using existing methods or providing alternatives
	        List<UserDtls> allUsers = userService.getUsers("ROLE_USER");
	        m.addAttribute("totalUsers", allUsers.size());
	        
	        // Get all products count (you'll need to implement this or use existing method)
	        m.addAttribute("totalProduct", productService.getAllProducts().size());
	        m.addAttribute("totalOrders", orderService.getCountOrders());
	        
	        // Get all categories count
	        m.addAttribute("totalCategory", categoryService.getAllActiveCategory().size());
	        
	        // Revenue metrics using existing methods
	        Double totalRevenue = orderService.getTotalRevenue();
	        Double todayRevenue = orderService.getTodayRevenue();
	        
	        m.addAttribute("totalRevenue", "฿" + (totalRevenue != null ? String.format("%.2f", totalRevenue) : "0.00"));
	        m.addAttribute("todayRevenue", "฿" + (todayRevenue != null ? String.format("%.2f", todayRevenue) : "0.00"));
	        m.addAttribute("todayOrders", orderService.getTodayOrdersCount());
	        m.addAttribute("newUsersToday", userService.getNewUsersToday());
	        
	        // Recent users using existing method
	        List<UserDtls> recentUsers = userService.getRecentUsers(5);
	        m.addAttribute("recentUsers", recentUsers);
	        
	        // Chart data using existing methods
	        m.addAttribute("dailyRevenueData", orderService.getDailyRevenueData(7));
	        m.addAttribute("dailyRevenueLabels", orderService.getDailyRevenueLabels(7));
	        m.addAttribute("dailyOrdersData", orderService.getDailyOrdersData(7));
	        m.addAttribute("dailyOrdersLabels", orderService.getDailyOrdersLabels(7));
	        
	        // For now, provide empty lists for top categories and products
	        // You can implement these methods later
	        m.addAttribute("topCategoriesData", new ArrayList<>());
	        m.addAttribute("topCategoriesLabels", new ArrayList<>());
	        m.addAttribute("topProductsData", new ArrayList<>());
	        m.addAttribute("topProductsLabels", new ArrayList<>());
	        
	        
	        // Add chart data
	        m.addAttribute("dailyRevenueData", orderService.getDailyRevenueData(7));
	        m.addAttribute("dailyRevenueLabels", orderService.getDailyRevenueLabels(7));
	        m.addAttribute("dailyOrdersData", orderService.getDailyOrdersData(7));
	        m.addAttribute("dailyOrdersLabels", orderService.getDailyOrdersLabels(7));
	        m.addAttribute("topCategoriesData", categoryService.getTopCategoriesData());
	        m.addAttribute("topCategoriesLabels", categoryService.getTopCategoriesLabels());
	        m.addAttribute("topProductsData", productService.getTopProductsData());
	        m.addAttribute("topProductsLabels", productService.getTopProductsLabels());
	        
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        // Set default values in case of error
	        m.addAttribute("totalUsers", 0);
	        m.addAttribute("totalProduct", 0);
	        m.addAttribute("totalOrders", 0);
	        m.addAttribute("totalCategory", 0);
	        m.addAttribute("totalRevenue", "฿0.00");
	        m.addAttribute("todayRevenue", "฿0.00");
	        m.addAttribute("todayOrders", 0);
	        m.addAttribute("newUsersToday", 0);
	        m.addAttribute("recentUsers", new ArrayList<>());
	        m.addAttribute("dailyRevenueData", new ArrayList<>());
	        m.addAttribute("dailyRevenueLabels", new ArrayList<>());
	        m.addAttribute("dailyOrdersData", new ArrayList<>());
	        m.addAttribute("dailyOrdersLabels", new ArrayList<>());
	        m.addAttribute("topCategoriesData", new ArrayList<>());
	        m.addAttribute("topCategoriesLabels", new ArrayList<>());
	        m.addAttribute("topProductsData", new ArrayList<>());
	        m.addAttribute("topProductsLabels", new ArrayList<>());
	    }
	    
	    return "/admin/index";
	}



}
