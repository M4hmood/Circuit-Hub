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

    // Product cache — invalidated whenever admin adds/updates/deletes a product.
    private static List<Product> productCache = null;

    public static void invalidateProductCache() { productCache = null; }

    public static void init() {
        DatabaseConfig.initializeDatabase();
    }

    // ---- Users ----
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, full_name, email, password_hash, join_date, role FROM users ORDER BY join_date DESC";
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
                u.setRole(rs.getString("role"));
                users.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        return users;
    }

    public static User findUserByEmail(String email) {
        String sql = "SELECT id, full_name, email, password_hash, join_date, role FROM users WHERE email = ?";
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
                    u.setRole(rs.getString("role"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        return null;
    }

    public static void addUser(User u) {
        String sql = "INSERT INTO users (id, full_name, email, password_hash, join_date, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, u.getId());
            pstmt.setString(2, u.getFullName());
            pstmt.setString(3, u.getEmail());
            pstmt.setString(4, u.getPasswordHash());
            pstmt.setDate(5, java.sql.Date.valueOf(u.getJoinDate()));
            pstmt.setString(6, u.getRole() == null ? "USER" : u.getRole());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
        }
    }

    public static boolean updateUserRole(String userId, String role) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role);
            pstmt.setString(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user role: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    // ---- Products ----
    public static List<Product> loadProducts() {
        if (productCache != null) return productCache;

        // Single query: products LEFT JOIN specs — avoids N+1 round-trips.
        String sql = """
                SELECT p.id, p.name, p.category, p.price, p.description,
                       p.image_url, p.image_data, p.stock,
                       s.spec_key, s.spec_value
                FROM products p
                LEFT JOIN product_specs s ON s.product_id = p.id
                ORDER BY p.id, s.id
                """;
        Map<String, Product> byId = new LinkedHashMap<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                Product p = byId.computeIfAbsent(id, k -> {
                    try {
                        Product np = new Product();
                        np.setId(id);
                        np.setName(rs.getString("name"));
                        np.setCategory(rs.getString("category"));
                        np.setPrice(rs.getDouble("price"));
                        np.setDescription(rs.getString("description"));
                        np.setImageUrl(rs.getString("image_url"));
                        np.setImageData(rs.getBytes("image_data"));
                        np.setStock(rs.getInt("stock"));
                        np.setSpecs(new LinkedHashMap<>());
                        return np;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                String key = rs.getString("spec_key");
                if (key != null) p.getSpecs().put(key, rs.getString("spec_value"));
            }
        } catch (SQLException | RuntimeException e) {
            System.err.println("Error loading products: " + e.getMessage());
        }
        productCache = new ArrayList<>(byId.values());
        return productCache;
    }

    public static Product findProductById(String id) {
        // Check cache first.
        if (productCache != null) {
            for (Product p : productCache) {
                if (p.getId().equals(id)) return p;
            }
        }
        String sql = """
                SELECT p.id, p.name, p.category, p.price, p.description,
                       p.image_url, p.image_data, p.stock,
                       s.spec_key, s.spec_value
                FROM products p
                LEFT JOIN product_specs s ON s.product_id = p.id
                WHERE p.id = ?
                ORDER BY s.id
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                Product p = null;
                while (rs.next()) {
                    if (p == null) {
                        p = new Product();
                        p.setId(rs.getString("id"));
                        p.setName(rs.getString("name"));
                        p.setCategory(rs.getString("category"));
                        p.setPrice(rs.getDouble("price"));
                        p.setDescription(rs.getString("description"));
                        p.setImageUrl(rs.getString("image_url"));
                        p.setImageData(rs.getBytes("image_data"));
                        p.setStock(rs.getInt("stock"));
                        p.setSpecs(new LinkedHashMap<>());
                    }
                    String key = rs.getString("spec_key");
                    if (key != null) p.getSpecs().put(key, rs.getString("spec_value"));
                }
                return p;
            }
        } catch (SQLException e) {
            System.err.println("Error finding product: " + e.getMessage());
        }
        return null;
    }

    public static boolean addProduct(Product p) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            String prodSql = "INSERT INTO products (id, name, category, price, description, image_url, image_data, stock) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(prodSql)) {
                pstmt.setString(1, p.getId());
                pstmt.setString(2, p.getName());
                pstmt.setString(3, p.getCategory());
                pstmt.setDouble(4, p.getPrice());
                pstmt.setString(5, p.getDescription());
                pstmt.setString(6, p.getImageUrl());
                pstmt.setBytes(7, p.getImageData());
                pstmt.setInt(8, p.getStock());
                pstmt.executeUpdate();
            }

            if (p.getSpecs() != null && !p.getSpecs().isEmpty()) {
                String specSql = "INSERT INTO product_specs (product_id, spec_key, spec_value) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(specSql)) {
                    for (Map.Entry<String, String> e : p.getSpecs().entrySet()) {
                        pstmt.setString(1, p.getId());
                        pstmt.setString(2, e.getKey());
                        pstmt.setString(3, e.getValue());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
            }

            conn.commit();
            invalidateProductCache();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException ex) { /* ignore */ }
        }
    }

    public static boolean updateProduct(Product p) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // Only overwrite image_data when caller supplied new bytes — else keep what's there.
            boolean writeImage = p.getImageData() != null;
            String prodSql = writeImage
                    ? "UPDATE products SET name = ?, category = ?, price = ?, description = ?, image_url = ?, image_data = ?, stock = ? WHERE id = ?"
                    : "UPDATE products SET name = ?, category = ?, price = ?, description = ?, image_url = ?, stock = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(prodSql)) {
                int i = 1;
                pstmt.setString(i++, p.getName());
                pstmt.setString(i++, p.getCategory());
                pstmt.setDouble(i++, p.getPrice());
                pstmt.setString(i++, p.getDescription());
                pstmt.setString(i++, p.getImageUrl());
                if (writeImage) pstmt.setBytes(i++, p.getImageData());
                pstmt.setInt(i++, p.getStock());
                pstmt.setString(i, p.getId());
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM product_specs WHERE product_id = ?")) {
                pstmt.setString(1, p.getId());
                pstmt.executeUpdate();
            }

            if (p.getSpecs() != null && !p.getSpecs().isEmpty()) {
                String specSql = "INSERT INTO product_specs (product_id, spec_key, spec_value) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(specSql)) {
                    for (Map.Entry<String, String> e : p.getSpecs().entrySet()) {
                        pstmt.setString(1, p.getId());
                        pstmt.setString(2, e.getKey());
                        pstmt.setString(3, e.getValue());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
            }

            conn.commit();
            invalidateProductCache();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException ex) { /* ignore */ }
        }
    }

    public static boolean clearProductImage(String productId) {
        String sql = "UPDATE products SET image_data = NULL WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productId);
            boolean ok = pstmt.executeUpdate() > 0;
            if (ok) invalidateProductCache();
            return ok;
        } catch (SQLException e) {
            System.err.println("Error clearing product image: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteProduct(String id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            boolean ok = pstmt.executeUpdate() > 0;
            if (ok) invalidateProductCache();
            return ok;
        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
            return false;
        }
    }

    // ---- Admin stats ----
    public static record AdminStats(double totalRevenue, int orderCount,
                                    int userCount, int productCount, int lowStockCount) {}

    public static AdminStats loadAdminStats() {
        double revenue = 0; int orders = 0, users = 0, products = 0, lowStock = 0;
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(total), 0) FROM orders")) {
                if (rs.next()) revenue = rs.getDouble(1);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM orders")) {
                if (rs.next()) orders = rs.getInt(1);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next()) users = rs.getInt(1);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {
                if (rs.next()) products = rs.getInt(1);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products WHERE stock < 20")) {
                if (rs.next()) lowStock = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error loading admin stats: " + e.getMessage());
        }
        return new AdminStats(revenue, orders, users, products, lowStock);
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

    public static List<Order> loadAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id, user_id, total, order_date, status FROM orders ORDER BY order_date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
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
        } catch (SQLException e) {
            System.err.println("Error loading all orders: " + e.getMessage());
        }
        return orders;
    }

    public static boolean updateOrderStatus(String orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            return false;
        }
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


