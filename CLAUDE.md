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
  a single `Scene`, swaps roots, and runs a 180ms fade-in. The `Scene`
  fill is set to `#0F172A` to prevent white flash during transitions.
  Never instantiate a new `Scene` from a controller.
- **`DataStore` is the only persistence API.** All database reads/writes go
  through its static methods. It owns:
  - PostgreSQL tables: `users`, `products`, `product_specs`, `orders`,
    `order_items` — created automatically on first run via
    `DatabaseConfig.initializeDatabase()`.
  - Products are seeded from
    `src/main/resources/com/tekup/circuithub/data/seed.sql`
    (uses `ON CONFLICT DO UPDATE SET price = EXCLUDED.price`, so re-running
    updates prices too).
  - Session state: `currentUser`, `selectedProduct`, and the in-memory
    cart list.
- **`DatabaseConfig`** holds the JDBC connection string
  (`jdbc:postgresql://localhost:5432/circuithub`, user/pass `postgres`).
  Matches the `docker-compose.yml` defaults. Uses a character-level SQL
  parser (`splitStatements`) to safely split the seed file on `;` without
  breaking on semicolons inside `--` comments or string literals.
- **Models are plain POJOs** with no-arg constructors and public getters/
  setters. They're mapped manually from `ResultSet` — no ORM.
- **Sidebar is inlined in each BorderPane-based FXML.** It's repetitive
  but keeps the controllers simple. The current screen's button carries an
  extra `active` style class. Admin screens have a separate sidebar
  (`// ADMIN CONSOLE`) with four entries: Dashboard, Products, Orders, Users.
- **Role-based routing.** `User` has a `role` field (`USER` / `ADMIN`).
  Login routes admins to `admin-dashboard`, regular users to `dashboard`.
  Admin controllers gate-check `getCurrentUser().isAdmin()` in `initialize()`.
- **Images are stored as `BYTEA` in PostgreSQL.** The admin product-edit
  form lets the admin upload any PNG/JPG/GIF via `FileChooser`; it is
  stored as bytes in `products.image_data`. `ImageLoader` decodes and
  displays images on a 4-thread daemon `ExecutorService` with
  `Platform.runLater` — never block the FX thread for image I/O.
- **Currency is Tunisian Dinar (TND).** All prices are formatted via
  `utils.Money` (`format`, `formatRound`, `formatEach`). Never scatter
  `String.format("%.2f TND", ...)` across controllers — always use `Money`.
- **Product cache.** `DataStore` holds a static `productCache` list populated
  on first `loadProducts()` call. Subsequent calls (Dashboard, Products screen,
  order item lookup) return the cached list instantly. The cache is nulled by
  `invalidateProductCache()`, which is called automatically by `addProduct`,
  `updateProduct`, `deleteProduct`, and `clearProductImage`. Never call
  `loadProducts()` expecting live data after a mutation without going through
  those methods — the cache will be stale. `loadProducts()` uses a single
  `products LEFT JOIN product_specs` query (not N+1 per-product spec queries).

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
  overlay), Orders (expand fade). `SceneManager.staggerIn` cascades
  card/row appearances on the products screen.
- **FXML `$` prefix** — any `text="$..."` in FXML is treated as a binding
  expression and will throw a `LoadException`. Escape as `text="\$..."` or
  use a non-`$` string entirely.
- **Tax rate** is a `CartController` constant (`TAX_RATE = 0.10`).
  Change it there, not per-order.
- **Products pagination** — `ProductsController` shows 15 products per page.
  `PAGE_SIZE` constant controls this. Filters, sort, and search always reset
  to page 1. Images are only loaded for the current page's products.

## Admin panel

Default admin credentials (seeded idempotently):
- Email: `admin@circuithub.local`
- Password: `admin123`

Four admin screens: `admin-dashboard`, `admin-products`, `admin-product-edit`,
`admin-orders`, `admin-users`. Each gates on `isAdmin()` in `initialize()`.
New admins can only be created by promoting existing users via the Users panel —
self-promotion is impossible by design.

## Adding a new screen

1. Create `views/myscreen.fxml` with `fx:controller="…MyScreenController"`.
   If it needs nav, copy the sidebar `VBox` block from any existing
   screen (e.g. `orders.fxml`) into a `<left>` slot and mark one button
   `styleClass="nav-btn,active"`.
2. Create `controllers/MyScreenController.java`. Implement
   `@FXML public void initialize()` for setup and the nav handlers the
   sidebar expects.
3. Route to it from wherever with
   `SceneManager.getInstance().switchTo("myscreen", true)`.

## Adding a new model / table

1. Add the `CREATE TABLE IF NOT EXISTS` block in
   `DatabaseConfig.initializeDatabase()`. Place it before any `ALTER TABLE`
   statements that reference it.
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
- Don't load images synchronously on the FX thread — always use
  `ImageLoader.setForProductAsync`.
- Don't use `sql.split(";")` to parse SQL files — use `splitStatements()`
  in `DatabaseConfig` which handles comments and string literals correctly.
- Don't add per-product spec queries inside a product load loop — always use
  the JOIN approach in `loadProducts()` to keep it a single round-trip.
- Don't call heavy DB methods directly in `initialize()` without caching — it blocks the FX thread on every navigation. Use the product cache pattern; add equivalent caches for other hot data if needed.

## Testing changes

No test suite exists. Manual smoke test:

1. `docker-compose up -d` to start PostgreSQL.
2. `./mvnw clean javafx:run`.
3. Welcome → Sign Up → Dashboard → Products → paginate, filter, add 2 items
   → Cart → change qty, remove one → Checkout → confirm → Orders (expand
   details) → Profile (verify stats add up).
4. Log out, log back in with the same credentials — cart cleared,
   orders persisted.
5. Sign in as `admin@circuithub.local` / `admin123` → admin dashboard →
   create/edit/delete a product with image upload → update an order status
   → promote/demote a user.

## Known quirks

- Product images are stored as `BYTEA` in the DB. If `image_data` is null,
  `ImageLoader` falls back to `image_url` (a plain URL string), then to a
  placeholder. Images survive `docker-compose down && up` because they live
  in the DB volume, not the filesystem.
- `Fonts.loadAll()` silently skips missing TTFs. The CSS stack is
  `"JetBrains Mono", "Consolas", monospace` / `"Inter", "Segoe UI",
  sans-serif`, so the app still looks reasonable without the bundled
  TTFs. Drop TTFs into
  `src/main/resources/com/tekup/circuithub/assets/fonts/` to enable them.
- Scene transitions use a 180ms fade-in only (no slide). The `Scene` fill
  is `#0F172A` to prevent white flash between screens.
