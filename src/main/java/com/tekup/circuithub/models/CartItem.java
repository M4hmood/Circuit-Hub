package com.tekup.circuithub.models;

public class CartItem {
    private Product product;
    private int quantity;

    public CartItem() {}
    public CartItem(Product p, int q) { this.product = p; this.quantity = q; }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setProduct(Product p) { this.product = p; }
    public void setQuantity(int q) { this.quantity = q; }

    public double getLineTotal() { return product.getPrice() * quantity; }
}
