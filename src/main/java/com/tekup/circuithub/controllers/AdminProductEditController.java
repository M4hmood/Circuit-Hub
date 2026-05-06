package com.tekup.circuithub.controllers;

import com.tekup.circuithub.models.Product;
import com.tekup.circuithub.models.User;
import com.tekup.circuithub.utils.DataStore;
import com.tekup.circuithub.utils.ImageLoader;
import com.tekup.circuithub.utils.SceneManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class AdminProductEditController {

    private static final String[] CATEGORIES = {
            "Microcontrollers", "Sensors", "Displays", "Modules", "Tools", "Power"
    };

    @FXML private Label titleLabel;
    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private TextField imageField;
    @FXML private TextArea descArea;
    @FXML private VBox specsBox;
    @FXML private Label errorLabel;
    @FXML private ImageView imagePreview;
    @FXML private Label imageHint;

    private boolean editMode;
    private byte[] pendingImageBytes;       // newly chosen file (overwrites on save)
    private boolean clearImage;             // user removed the existing image
    private byte[] existingImageBytes;      // bytes already in DB (for preview only)

    @FXML
    public void initialize() {
        User u = DataStore.getCurrentUser();
        if (u == null || !u.isAdmin()) {
            SceneManager.getInstance().switchTo("welcome", true);
            return;
        }
        categoryCombo.setItems(FXCollections.observableArrayList(CATEGORIES));

        Product existing = DataStore.getSelectedProduct();
        editMode = (existing != null);

        if (editMode) {
            titleLabel.setText("Edit Product");
            idField.setText(existing.getId());
            idField.setDisable(true);
            nameField.setText(existing.getName());
            categoryCombo.setValue(existing.getCategory());
            priceField.setText(String.valueOf(existing.getPrice()));
            stockField.setText(String.valueOf(existing.getStock()));
            imageField.setText(existing.getImageUrl());
            descArea.setText(existing.getDescription());
            existingImageBytes = existing.getImageData();
            if (existing.getSpecs() != null) {
                for (Map.Entry<String, String> e : existing.getSpecs().entrySet()) {
                    addSpecRow(e.getKey(), e.getValue());
                }
            }
            renderPreview(existing);
        } else {
            titleLabel.setText("New Product");
            idField.setText("PRD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            categoryCombo.setValue(CATEGORIES[0]);
            renderPreview(null);
        }
    }

    private void renderPreview(Product src) {
        if (src != null) {
            ImageLoader.setForProductAsync(imagePreview, src, 120, 90);
        } else {
            imagePreview.setImage(null);
        }
    }

    @FXML
    private void onChooseImage(ActionEvent e) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pick a product image");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        File f = fc.showOpenDialog(SceneManager.getInstance().getStage());
        if (f == null) return;
        try {
            byte[] bytes = Files.readAllBytes(f.toPath());
            // sanity-check it actually decodes
            Image img = new Image(new ByteArrayInputStream(bytes));
            if (img.isError()) throw new RuntimeException("Image could not be decoded");
            pendingImageBytes = bytes;
            clearImage = false;
            imagePreview.setImage(new Image(new ByteArrayInputStream(bytes), 120, 90, true, true));
            imageHint.setText("// " + f.getName() + " · " + (bytes.length / 1024) + " KB");
        } catch (Exception ex) {
            errorLabel.setText("Could not read image: " + ex.getMessage());
        }
    }

    @FXML
    private void onClearImage(ActionEvent e) {
        pendingImageBytes = null;
        existingImageBytes = null;
        clearImage = true;
        imagePreview.setImage(null);
        imageHint.setText("// no image");
    }

    private void addSpecRow(String key, String value) {
        HBox row = new HBox(8);
        TextField k = new TextField(key);
        k.setPromptText("key");
        k.getStyleClass().add("terminal-field");
        TextField v = new TextField(value);
        v.setPromptText("value");
        v.getStyleClass().add("terminal-field");
        HBox.setHgrow(k, Priority.ALWAYS);
        HBox.setHgrow(v, Priority.ALWAYS);
        Button rm = new Button("✕");
        rm.getStyleClass().add("btn-remove");
        rm.setOnAction(e -> specsBox.getChildren().remove(row));
        row.getChildren().addAll(k, v, rm);
        specsBox.getChildren().add(row);
    }

    @FXML private void onAddSpec(ActionEvent e) { addSpecRow("", ""); }

    @FXML
    private void onSave(ActionEvent e) {
        errorLabel.setText(" ");
        String name = safe(nameField.getText());
        String category = categoryCombo.getValue();
        String priceText = safe(priceField.getText());
        String stockText = safe(stockField.getText());

        if (name.isEmpty() || category == null || priceText.isEmpty() || stockText.isEmpty()) {
            errorLabel.setText("Name, category, price, and stock are required.");
            return;
        }
        double price;
        int stock;
        try {
            price = Double.parseDouble(priceText);
            stock = Integer.parseInt(stockText);
        } catch (NumberFormatException ex) {
            errorLabel.setText("Price must be a number, stock must be an integer.");
            return;
        }
        if (price < 0 || stock < 0) {
            errorLabel.setText("Price and stock must be non-negative.");
            return;
        }

        Product p = new Product();
        p.setId(idField.getText().trim());
        p.setName(name);
        p.setCategory(category);
        p.setPrice(price);
        p.setStock(stock);
        p.setImageUrl(safe(imageField.getText()));
        p.setDescription(safe(descArea.getText()));
        p.setSpecs(collectSpecs());
        // Image bytes: new pick wins; otherwise leave null so updateProduct preserves DB value.
        // For new products, also pass null when nothing was picked (column allows NULL).
        if (pendingImageBytes != null) p.setImageData(pendingImageBytes);

        boolean ok = editMode ? DataStore.updateProduct(p) : DataStore.addProduct(p);
        if (!ok) {
            errorLabel.setText("Save failed. Check the ID is unique and try again.");
            return;
        }
        // Explicit clear: only relevant when editing and the user pressed "Remove image"
        // without choosing a replacement.
        if (editMode && clearImage && pendingImageBytes == null) {
            DataStore.clearProductImage(p.getId());
        }
        DataStore.setSelectedProduct(null);
        SceneManager.getInstance().switchTo("admin-products", true);
    }

    private Map<String, String> collectSpecs() {
        Map<String, String> specs = new LinkedHashMap<>();
        for (var node : specsBox.getChildren()) {
            if (!(node instanceof HBox row)) continue;
            if (row.getChildren().size() < 2) continue;
            String k = ((TextField) row.getChildren().get(0)).getText();
            String v = ((TextField) row.getChildren().get(1)).getText();
            if (k != null && !k.isBlank()) specs.put(k.trim(), v == null ? "" : v.trim());
        }
        return specs;
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }

    @FXML private void onCancel(ActionEvent e) {
        DataStore.setSelectedProduct(null);
        SceneManager.getInstance().switchTo("admin-products", true);
    }

    @FXML private void navAdminDashboard(ActionEvent e) { SceneManager.getInstance().switchTo("admin-dashboard", true); }
    @FXML private void navAdminProducts(ActionEvent e)  { SceneManager.getInstance().switchTo("admin-products", true); }
    @FXML private void navAdminOrders(ActionEvent e)    { SceneManager.getInstance().switchTo("admin-orders", true); }
    @FXML private void navAdminUsers(ActionEvent e)     { SceneManager.getInstance().switchTo("admin-users", true); }
    @FXML private void onLogout(ActionEvent e) {
        DataStore.logout();
        SceneManager.getInstance().switchTo("welcome", true);
    }
}
