package com.tekup.circuithub.models;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private final List<CartItem> items = new ArrayList<>();

    public List<CartItem> getItems() { return items; }

    public int itemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public boolean isEmpty() { return items.isEmpty(); }

    public void add(Product p, int qty) {
        for (CartItem ci : items) {
            if (ci.getProduct().getId().equals(p.getId())) {
                ci.setQuantity(ci.getQuantity() + qty);
                return;
            }
        }
        items.add(new CartItem(p, qty));
    }

    public void remove(String productId) {
        items.removeIf(ci -> ci.getProduct().getId().equals(productId));
    }

    public void updateQty(String productId, int qty) {
        for (CartItem ci : items) {
            if (ci.getProduct().getId().equals(productId)) {
                if (qty <= 0) items.remove(ci);
                else ci.setQuantity(qty);
                return;
            }
        }
    }

    public void clear() { items.clear(); }

    public double subtotal() {
        return items.stream().mapToDouble(CartItem::getLineTotal).sum();
    }
}
