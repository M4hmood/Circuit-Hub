# CLAUDE.md

Guidance for Claude Code when working in this repo.

## What this project is

**CircuitHub** — a JavaFX 21 desktop storefront for embedded-systems
components. Maven build, FXML views, PostgreSQL persistence via JDBC.
Dark PCB-inspired theme with neon accents.

## Run

```bash
# 1. Start the database
docker-compose up -d

# 2. Run the app
./mvnw clean javafx:run
```

Entry point: `com.tekup.circuithub.App`. Main class is declared in the
`javafx-maven-plugin` section of `pom.xml`, not a manifest.

## Architecture

- **One controller per FXML.** Controllers live in
  `com.tekup.circuithub.controllers`, FXMLs in
  `src/main/resources/com/tekup/circuithub/views/`. Filenames match
  (`products.fxml` ↔ `ProductsController`).
- **`SceneManager` singleton** is the only way to navigate. Controllers
  call `SceneManager.getInstance().switchTo("viewName", true)`. It caches
  a single `Scene`, swaps roots, and runs a 300ms fade. Never instantiate
  a new `Scene` from a controller.
- **`DataStore` is the only persistence API.** All database reads/writes go
  through its static methods. It owns:
  - PostgreSQL tables: `users`, `products`, `product_specs`, `orders`,
    `order_items` — created automatically on first run via
    `DatabaseConfig.initializeDatabase()`.
  - Products are seeded from
    `src/main/resources/com/tekup/circuithub/data/seed.sql`
    (uses `ON CONFLICT DO NOTHING`, so re-running is safe).
  - Session state: `currentUser`, `selectedProduct`, and the in-memory
    cart list.
- **`DatabaseConfig`** holds the JDBC connection string
  (`jdbc:postgresql://localhost:5432/circuithub`, user/pass `postgres`).
  Matches the `docker-compose.yml` defaults.
- **Models are plain POJOs** with no-arg constructors and public getters/
  setters. They're mapped manually from `ResultSet` — no ORM.
- **Sidebar is inlined in each BorderPane-based FXML.** It's repetitive
  but keeps the controllers simple. Six buttons; the current screen's
  button carries an extra `active` style class.

## Conventions that matter

- **Passwords are SHA-256 hashed** via `utils.Hashing.sha256`. Never store
  plaintext, never add a plaintext field to `User`.
- **Passing data between screens** — store it on `DataStore` (e.g.
  `setSelectedProduct`) before calling `switchTo`. The destination
  controller reads it in `initialize()`. Don't try to pass it through
  `FXMLLoader.setController` — the controller is already wired from FXML.
- **Styling** — no inline style strings for reusable looks. Add a class
  to `resources/com/tekup/circuithub/styles/app.css`. Inline
  `style="…"` is OK for one-offs (padding tweaks, per-instance colors).
- **Animations** — use JavaFX `Timeline`, `FadeTransition`,
  `ScaleTransition`, `TranslateTransition`, `RotateTransition`. Already
  in use on Welcome (pulsing logo, drift), Dashboard (carousel fade,
  card lift), Product Detail (pulsing add button), Cart (success
  overlay), Orders (expand fade).
- **Tax rate** is a `CartController` constant (`TAX_RATE = 0.10`).
  Change it there, not per-order.

## Adding a new screen

1. Create `views/myscreen.fxml` with `fx:controller="…MyScreenController"`.
   If it needs nav, copy the sidebar `VBox` block from any existing
   screen (e.g. `orders.fxml`) into a `<left>` slot and mark one button
   `styleClass="nav-btn,active"`.
2. Create `controllers/MyScreenController.java`. Implement
   `@FXML public void initialize()` for setup and the six
   `navXxx(ActionEvent)` handlers the sidebar expects.
3. Route to it from wherever with
   `SceneManager.getInstance().switchTo("myscreen", true)`.

## Adding a new model / table

1. Add the `CREATE TABLE IF NOT EXISTS` block in
   `DatabaseConfig.initializeDatabase()`.
2. Create the POJO in `com.tekup.circuithub.models` — no-arg constructor,
   public getters/setters. No annotation needed (no ORM).
3. Add load/save methods in `DataStore` using `PreparedStatement`.
4. The `models` package is already exported in `module-info.java`; no
   further module changes needed.

## What NOT to do

- Don't add Gson or a JSON file store — persistence lives in PostgreSQL.
- Don't fabricate a second `Stage` — everything is single-window.
- Don't pull new UI frameworks (ControlsFX, etc.) unless the task
  explicitly requires them. Plain JavaFX is the constraint.
- Don't remove `module-info.java` — FXML reflection depends on the
  `opens` directives there.

## Testing changes

No test suite exists. Manual smoke test:

1. `docker-compose up -d` to start PostgreSQL.
2. `./mvnw clean javafx:run`.
3. Welcome → Sign Up → Dashboard → Products → add 2 items → Cart →
   change qty, remove one → Checkout → confirm → Orders (expand
   details) → Profile (verify stats add up).
4. Log out, log back in with the same credentials — cart cleared,
   orders persisted.

## Known quirks

- Product images load from `via.placeholder.com` via JavaFX
  `Image(url, …, backgroundLoading=true)`. Offline = gray tiles.
- `Fonts.loadAll()` silently skips missing TTFs. The CSS stack is
  `"JetBrains Mono", "Consolas", monospace` / `"Inter", "Segoe UI",
  sans-serif`, so the app still looks reasonable without the bundled
  TTFs. Drop TTFs into
  `src/main/resources/com/tekup/circuithub/assets/fonts/` to enable them.
