package com.tekup.circuithub.controllers;

import com.tekup.circuithub.models.Product;
import com.tekup.circuithub.utils.DataStore;
import com.tekup.circuithub.utils.ImageLoader;
import com.tekup.circuithub.utils.Money;
import com.tekup.circuithub.utils.SceneManager;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProductsController {

    private static final String SORT_DEFAULT  = "Default";
    private static final String SORT_PRICE_LO = "Price: low → high";
    private static final String SORT_PRICE_HI = "Price: high → low";
    private static final String SORT_NAME     = "Name (A → Z)";
    private static final String SORT_STOCK    = "Stock (high → low)";

    @FXML private ComboBox<String> categoryCombo;
    @FXML private Slider priceSlider;
    @FXML private Label priceLabel;
    @FXML private TextField searchField;
    @FXML private FlowPane productsPane;
    @FXML private FlowPane activeChipsPane;
    @FXML private Label resultCount;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Button gridBtn;
    @FXML private Button listBtn;
    @FXML private HBox   paginationBar;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Label  pageLabel;

    private static final int PAGE_SIZE = 15;

    private List<Product> all;
    private List<Product> currentFiltered;
    private boolean listView    = false;
    private int     currentPage = 0;

    @FXML
    public void initialize() {
        all = DataStore.loadProducts();

        categoryCombo.setItems(FXCollections.observableArrayList(
                "All", "Microcontrollers", "Sensors", "Displays", "Modules", "Tools", "Power"));
        categoryCombo.getSelectionModel().select("All");
        categoryCombo.valueProperty().addListener((o, a, b) -> resetAndRender());

        double max = all.stream().mapToDouble(Product::getPrice).max().orElse(200);
        priceSlider.setMax(Math.ceil(max));
        priceSlider.setValue(Math.ceil(max));
        priceLabel.setText(Money.formatRound(priceSlider.getValue()));
        priceSlider.valueProperty().addListener((o, a, b) -> {
            priceLabel.setText(Money.formatRound(b.doubleValue()));
            resetAndRender();
        });

        sortCombo.setItems(FXCollections.observableArrayList(
                SORT_DEFAULT, SORT_PRICE_LO, SORT_PRICE_HI, SORT_NAME, SORT_STOCK));
        sortCombo.setValue(SORT_DEFAULT);
        sortCombo.valueProperty().addListener((o, a, b) -> resetAndRender());

        searchField.textProperty().addListener((o, a, b) -> resetAndRender());

        render();
    }

    private void resetAndRender() {
        currentPage = 0;
        render();
    }

    private void render() {
        String cat = categoryCombo.getValue();
        double maxP = priceSlider.getValue();
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String sort = sortCombo.getValue() == null ? SORT_DEFAULT : sortCombo.getValue();

        currentFiltered = all.stream()
                .filter(p -> "All".equals(cat) || cat == null || p.getCategory().equals(cat))
                .filter(p -> p.getPrice() <= maxP)
                .filter(p -> q.isEmpty()
                        || p.getName().toLowerCase().contains(q)
                        || p.getCategory().toLowerCase().contains(q)
                        || p.getSpecs().values().stream().anyMatch(v -> v.toLowerCase().contains(q)))
                .collect(Collectors.toList());

        Comparator<Product> cmp = switch (sort) {
            case SORT_PRICE_LO -> Comparator.comparingDouble(Product::getPrice);
            case SORT_PRICE_HI -> Comparator.comparingDouble(Product::getPrice).reversed();
            case SORT_NAME     -> Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
            case SORT_STOCK    -> Comparator.comparingInt(Product::getStock).reversed();
            default            -> null;
        };
        if (cmp != null) currentFiltered.sort(cmp);

        int totalPages = Math.max(1, (int) Math.ceil((double) currentFiltered.size() / PAGE_SIZE));
        currentPage = Math.min(currentPage, totalPages - 1);

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, currentFiltered.size());
        List<Product> page = currentFiltered.subList(from, to);

        resultCount.setText("// " + currentFiltered.size() + " components");
        productsPane.getChildren().clear();
        for (Product p : page) {
            productsPane.getChildren().add(listView ? buildRow(p) : buildCard(p));
        }
        SceneManager.staggerIn(productsPane.getChildren());

        pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);
        prevBtn.setDisable(currentPage == 0);
        nextBtn.setDisable(currentPage >= totalPages - 1);

        rebuildActiveChips(cat, maxP, q, sort);
    }

    private void rebuildActiveChips(String cat, double maxP, String q, String sort) {
        if (activeChipsPane == null) return;
        activeChipsPane.getChildren().clear();
        if (cat != null && !"All".equals(cat))
            activeChipsPane.getChildren().add(filterChip("category: " + cat, () -> categoryCombo.setValue("All")));
        if (priceSlider.getValue() < priceSlider.getMax())
            activeChipsPane.getChildren().add(filterChip("≤ " + Money.formatRound(maxP),
                    () -> priceSlider.setValue(priceSlider.getMax())));
        if (!q.isEmpty())
            activeChipsPane.getChildren().add(filterChip("\"" + q + "\"", () -> searchField.clear()));
        if (sort != null && !SORT_DEFAULT.equals(sort))
            activeChipsPane.getChildren().add(filterChip("sort: " + sort, () -> sortCombo.setValue(SORT_DEFAULT)));
    }

    private HBox filterChip(String text, Runnable onRemove) {
        HBox chip = new HBox(6);
        chip.getStyleClass().add("filter-chip");
        Label lbl = new Label(text);
        Button x = new Button("✕");
        x.getStyleClass().add("filter-chip-x");
        x.setOnAction(e -> onRemove.run());
        chip.getChildren().addAll(lbl, x);
        return chip;
    }

    private VBox buildCard(Product p) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(260);
        card.setPrefHeight(360);
        card.setMaxWidth(260);

        // Image
        ImageView iv = new ImageView();
        iv.setFitWidth(232);
        iv.setFitHeight(150);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        ImageLoader.setForProductAsync(iv, p, 232, 150);
        VBox thumb = new VBox(iv);
        thumb.setPrefHeight(160);
        thumb.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-border-radius: 10; "
                + "-fx-alignment: center; -fx-padding: 8;");

        // Stock badge — overlaid via top row
        Label stockChip = stockChip(p.getStock());

        HBox topMeta = new HBox(8);
        Label cat = new Label(p.getCategory().toUpperCase());
        cat.getStyleClass().add("chip-category");
        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        topMeta.getChildren().addAll(cat, gap, stockChip);
        topMeta.setStyle("-fx-alignment: center-left;");

        Label name = new Label(p.getName());
        name.getStyleClass().add("product-name");
        name.setWrapText(true);

        String firstSpec = p.getSpecs().isEmpty() ? "—"
                : p.getSpecs().entrySet().iterator().next().getKey() + ": "
                + p.getSpecs().entrySet().iterator().next().getValue();
        Label specs = new Label(firstSpec);
        specs.getStyleClass().add("product-specs");
        specs.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Price + add row
        HBox priceRow = new HBox();
        priceRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label price = new Label(Money.format(p.getPrice()));
        price.getStyleClass().add("price");
        price.setStyle("-fx-font-size: 20px;");
        Region g = new Region();
        HBox.setHgrow(g, Priority.ALWAYS);
        Button add = new Button(p.getStock() == 0 ? "OUT" : "+ CART");
        add.getStyleClass().add("btn-add");
        add.setDisable(p.getStock() == 0);
        add.setOnAction(e -> {
            DataStore.addToCart(p, 1);
            add.setText("✓ ADDED");
            javafx.animation.PauseTransition pt =
                    new javafx.animation.PauseTransition(javafx.util.Duration.millis(900));
            pt.setOnFinished(x -> add.setText("+ CART"));
            pt.play();
            e.consume();
        });
        priceRow.getChildren().addAll(price, g, add);

        card.getChildren().addAll(thumb, topMeta, name, specs, spacer, priceRow);

        TranslateTransition liftUp   = new TranslateTransition(Duration.millis(160), card);
        liftUp.setToY(-6);
        TranslateTransition liftDown = new TranslateTransition(Duration.millis(160), card);
        liftDown.setToY(0);
        card.setOnMouseEntered(e -> { liftDown.stop(); liftUp.playFromStart(); });
        card.setOnMouseExited(e ->  { liftUp.stop(); liftDown.playFromStart(); });

        card.setOnMouseClicked(e -> {
            DataStore.setSelectedProduct(p);
            SceneManager.getInstance().switchTo("product-detail", true);
        });
        return card;
    }

    private HBox buildRow(Product p) {
        HBox row = new HBox(16);
        row.getStyleClass().add("product-row");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPrefHeight(96);
        row.setMinWidth(700);

        ImageView iv = new ImageView();
        iv.setFitWidth(110);
        iv.setFitHeight(72);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        ImageLoader.setForProductAsync(iv, p, 110, 72);
        VBox thumb = new VBox(iv);
        thumb.setPrefSize(120, 80);
        thumb.setStyle("-fx-background-color: white; -fx-background-radius: 8; "
                + "-fx-alignment: center; -fx-padding: 6;");

        Label cat = new Label(p.getCategory().toUpperCase());
        cat.getStyleClass().add("chip-category");
        Label name = new Label(p.getName());
        name.getStyleClass().add("product-name");
        VBox info = new VBox(4, cat, name);
        info.setPrefWidth(280);
        HBox.setHgrow(info, Priority.SOMETIMES);

        Label stockChip = stockChip(p.getStock());

        Label price = new Label(Money.format(p.getPrice()));
        price.getStyleClass().add("price");
        price.setStyle("-fx-font-size: 20px;");
        price.setMinWidth(90);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button add = new Button(p.getStock() == 0 ? "OUT" : "+ CART");
        add.getStyleClass().add("btn-add");
        add.setDisable(p.getStock() == 0);
        add.setOnAction(e -> {
            DataStore.addToCart(p, 1);
            add.setText("✓ ADDED");
            javafx.animation.PauseTransition pt =
                    new javafx.animation.PauseTransition(javafx.util.Duration.millis(900));
            pt.setOnFinished(x -> add.setText("+ CART"));
            pt.play();
            e.consume();
        });

        row.getChildren().addAll(thumb, info, stockChip, spacer, price, add);
        row.setOnMouseClicked(e -> {
            DataStore.setSelectedProduct(p);
            SceneManager.getInstance().switchTo("product-detail", true);
        });
        return row;
    }

    private Label stockChip(int stock) {
        Label l = new Label();
        if (stock == 0) {
            l.setText("OUT");
            l.getStyleClass().add("stock-chip-out");
        } else if (stock < 20) {
            l.setText(stock + " LEFT");
            l.getStyleClass().add("stock-chip-low");
        } else {
            l.setText("IN STOCK");
            l.getStyleClass().add("stock-chip-in");
        }
        return l;
    }

    @FXML private void onPrevPage(ActionEvent e) {
        if (currentPage > 0) { currentPage--; render(); }
    }

    @FXML private void onNextPage(ActionEvent e) {
        if (currentFiltered == null) return;
        int totalPages = Math.max(1, (int) Math.ceil((double) currentFiltered.size() / PAGE_SIZE));
        if (currentPage < totalPages - 1) { currentPage++; render(); }
    }

    @FXML private void onClearFilters(ActionEvent e) {
        currentPage = 0;
        categoryCombo.getSelectionModel().select("All");
        priceSlider.setValue(priceSlider.getMax());
        if (sortCombo != null) sortCombo.setValue(SORT_DEFAULT);
        searchField.clear();
    }

    @FXML private void onGridView(ActionEvent e) {
        if (!listView) return;
        listView = false;
        currentPage = 0;
        if (gridBtn != null && !gridBtn.getStyleClass().contains("active")) gridBtn.getStyleClass().add("active");
        if (listBtn != null) listBtn.getStyleClass().remove("active");
        render();
    }

    @FXML private void onListView(ActionEvent e) {
        if (listView) return;
        listView = true;
        currentPage = 0;
        if (listBtn != null && !listBtn.getStyleClass().contains("active")) listBtn.getStyleClass().add("active");
        if (gridBtn != null) gridBtn.getStyleClass().remove("active");
        render();
    }

    @FXML private void navHome(ActionEvent e)     { SceneManager.getInstance().switchTo("dashboard", true); }
    @FXML private void navProducts(ActionEvent e) { /* here */ }
    @FXML private void navCart(ActionEvent e)     { SceneManager.getInstance().switchTo("cart", true); }
    @FXML private void navOrders(ActionEvent e)   { SceneManager.getInstance().switchTo("orders", true); }
    @FXML private void navProfile(ActionEvent e)  { SceneManager.getInstance().switchTo("profile", true); }
    @FXML private void onLogout(ActionEvent e) {
        DataStore.logout();
        SceneManager.getInstance().switchTo("welcome", true);
    }
}
