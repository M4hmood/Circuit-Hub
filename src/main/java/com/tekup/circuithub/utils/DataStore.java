package com.tekup.circuithub.utils;

import com.tekup.circuithub.models.CartItem;
import com.tekup.circuithub.models.Order;
import com.tekup.circuithub.models.Product;
import com.tekup.circuithub.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataStore {
    private static User currentUser;
    private static final List<CartItem> cart = new ArrayList<>();
    private static Product selectedProduct;

    public static void init() {
        DatabaseConfig.initializeDatabase();
    }

    // ---- Users ----
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, full_name, email, password_hash, join_date FROM users";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getString("id"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setJoinDate(rs.getString("join_date"));
                users.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        return users;
    }

    public static User findUserByEmail(String email) {
        String sql = "SELECT id, full_name, email, password_hash, join_date FROM users WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getString("id"));
                    u.setFullName(rs.getString("full_name"));
                    u.setEmail(rs.getString("email"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setJoinDate(rs.getString("join_date"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        return null;
    }

    public static void addUser(User u) {
        String sql = "INSERT INTO users (id, full_name, email, password_hash, join_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, u.getId());
            pstmt.setString(2, u.getFullName());
            pstmt.setString(3, u.getEmail());
            pstmt.setString(4, u.getPasswordHash());
            pstmt.setDate(5, java.sql.Date.valueOf(u.getJoinDate()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
        }
    }

    // ---- Products ----
    public static List<Product> loadProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, name, category, price, description, image_url, stock FROM products";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getString("id"));
                p.setName(rs.getString("name"));
                p.setCategory(rs.getString("category"));
                p.setPrice(rs.getDouble("price"));
                p.setDescription(rs.getString("description"));
                p.setImageUrl(rs.getString("image_url"));
                p.setStock(rs.getInt("stock"));
                
                // Load specs
                p.setSpecs(loadProductSpecs(rs.getString("id")));
                products.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error loading products: " + e.getMessage());
        }
        return products;
    }

    private static Map<String, String> loadProductSpecs(String productId) {
        Map<String, String> specs = new LinkedHashMap<>();
        String sql = "SELECT spec_key, spec_value FROM product_specs WHERE product_id = ? ORDER BY id";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    specs.put(rs.getString("spec_key"), rs.getString("spec_value"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading specs: " + e.getMessage());
        }
        return specs;
    }

    public static Product findProductById(String id) {
        String sql = "SELECT id, name, category, price, description, image_url, stock FROM products WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getString("id"));
                    p.setName(rs.getString("name"));
                    p.setCategory(rs.getString("category"));
                    p.setPrice(rs.getDouble("price"));
                    p.setDescription(rs.getString("description"));
                    p.setImageUrl(rs.getString("image_url"));
                    p.setStock(rs.getInt("stock"));
                    p.setSpecs(loadProductSpecs(id));
                    return p;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding product: " + e.getMessage());
        }
        return null;
    }

    // ---- Orders ----
    public static List<Order> loadOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id, user_id, total, order_date, status FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (currentUser != null) {
                pstmt.setString(1, currentUser.getId());
            } else {
                return orders;
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order();
                    o.setId(rs.getString("id"));
                    o.setUserId(rs.getString("user_id"));
                    o.setTotal(rs.getDouble("total"));
                    o.setDate(rs.getString("order_date"));
                    o.setStatus(rs.getString("status"));
                    o.setItems(loadOrderItems(rs.getString("id")));
                    orders.add(o);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading orders: " + e.getMessage());
        }
        return orders;
    }

    private static List<CartItem> loadOrderItems(String orderId) {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT product_id, quantity FROM order_items WHERE order_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product p = findProductById(rs.getString("product_id"));
                    if (p != null) {
                        CartItem ci = new CartItem(p, rs.getInt("quantity"));
                        items.add(ci);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading order items: " + e.getMessage());
        }
        return items;
    }

    public static void addOrder(Order o) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Insert order
            String orderSql = "INSERT INTO orders (id, user_id, total, order_date, status) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(orderSql)) {
                pstmt.setString(1, o.getId());
                pstmt.setString(2, o.getUserId());
                pstmt.setDouble(3, o.getTotal());
                pstmt.setDate(4, java.sql.Date.valueOf(o.getDate()));
                pstmt.setString(5, o.getStatus());
                pstmt.executeUpdate();
            }
            
            // Insert order items
            String itemSql = "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
                for (CartItem ci : o.getItems()) {
                    pstmt.setString(1, o.getId());
                    pstmt.setString(2, ci.getProduct().getId());
                    pstmt.setInt(3, ci.getQuantity());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
        } catch (SQLException e) {
            System.err.println("Error adding order: " + e.getMessage());
        }
    }

    // ---- Cart (in-memory session) ----
    public static List<CartItem> getCart() { return cart; }

    public static void addToCart(Product p, int qty) {
        for (CartItem ci : cart) {
            if (ci.getProduct().getId().equals(p.getId())) {
                ci.setQuantity(ci.getQuantity() + qty);
                return;
            }
        }
        cart.add(new CartItem(p, qty));
    }

    public static void removeFromCart(String productId) {
        cart.removeIf(ci -> ci.getProduct().getId().equals(productId));
    }

    public static void updateCartQty(String productId, int qty) {
        for (CartItem ci : cart) {
            if (ci.getProduct().getId().equals(productId)) {
                if (qty <= 0) { cart.remove(ci); }
                else { ci.setQuantity(qty); }
                return;
            }
        }
    }

    public static void clearCart() { cart.clear(); }

    public static double cartSubtotal() {
        return cart.stream().mapToDouble(CartItem::getLineTotal).sum();
    }

    // ---- Selection ----
    public static Product getSelectedProduct() { return selectedProduct; }
    public static void setSelectedProduct(Product p) { selectedProduct = p; }

    // ---- Session ----
    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User u) { currentUser = u; }
    public static void logout() { currentUser = null; clearCart(); }
}


