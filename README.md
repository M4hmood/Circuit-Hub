# CircuitHub

A JavaFX 21 desktop storefront for embedded-systems hardware (microcontrollers,
sensors, Arduino kits, Raspberry Pi boards, modules, tools, power). Dark
PCB-inspired aesthetic, neon accents, smooth animations, PostgreSQL persistence.

## Requirements

- **JDK 21+** (Temurin or OpenJDK recommended)
- **Maven 3.8+** (the wrapper `mvnw` is included — no install needed)
- **Docker + Docker Compose** (runs the PostgreSQL database)
- Internet connection on first run — product images are pulled from `placeholder.com`

## Quick start

### 1. Start the database

```bash
docker-compose up -d
```

This starts:
- **PostgreSQL 15** on `localhost:5432`
- **pgAdmin 4** on `http://localhost:5050`

### 2. Run the app

```bash
./mvnw clean javafx:run          # macOS / Linux / Git Bash
mvnw.cmd clean javafx:run        # Windows cmd / PowerShell
```

The app window opens at 1280×800 on the Welcome screen.

On first launch `DataStore.init()` creates all tables and seeds the 36 products
from the bundled `resources/.../data/products.json` into PostgreSQL automatically.

## Database

| Setting  | Value         |
|----------|---------------|
| Host     | `localhost`   |
| Port     | `5432`        |
| Database | `circuithub`  |
| User     | `postgres`    |
| Password | `postgres`    |

### Schema

| Table           | Purpose                                           |
|-----------------|---------------------------------------------------|
| `users`         | Registered accounts (SHA-256 hashed passwords)   |
| `products`      | 36 embedded-systems products                     |
| `product_specs` | EAV spec rows per product                        |
| `orders`        | Placed orders per user                           |
| `order_items`   | Line items linking orders to products            |

### pgAdmin 4

Open **http://localhost:5050** and log in with `admin@circuithub.local` / `admin`.

Add a server connection:
- Host: `postgres` (within Docker) or `localhost` (from host)
- Port: `5432` · Database: `circuithub` · User: `postgres` · Password: `postgres`

Useful psql commands:
```bash
docker-compose exec postgres psql -U postgres -d circuithub
```
```sql
\dt                        -- list tables
SELECT * FROM products;
SELECT * FROM users;
SELECT * FROM orders;
```

### Docker volume commands

```bash
docker-compose down           # stop containers (data preserved)
docker-compose down -v        # stop + delete all data
docker-compose logs -f postgres
docker-compose restart
```

## Project layout

```
src/main/java/com/tekup/circuithub/
├── App.java                         # JavaFX entry point
├── controllers/                     # one controller per FXML view
│   ├── WelcomeController
│   ├── SignInController / SignUpController
│   ├── DashboardController
│   ├── ProductsController / ProductDetailController
│   ├── CartController
│   ├── OrdersController
│   ├── ProfileController
│   └── StubController
├── models/                          # plain POJOs (User, Product, CartItem, Order)
└── utils/
    ├── SceneManager                 # singleton, caches Scene, 300ms fade
    ├── DataStore                    # JDBC CRUD + in-memory cart/session
    ├── DatabaseConfig               # connection + CREATE TABLE IF NOT EXISTS
    ├── Hashing                      # SHA-256
    ├── Fonts                        # optional TTF loader
    └── Spinner                      # reusable rotating progress arc

src/main/resources/com/tekup/circuithub/
├── views/*.fxml                     # all screens
├── styles/app.css                   # global stylesheet (palette + components)
└── data/products.json               # bundled seed (copied to DB on first run)
```

## Design tokens

| Token            | Value      | Usage                                |
|------------------|------------|--------------------------------------|
| `bg-primary`     | `#0A0E14`  | app background                       |
| `bg-secondary`   | `#161B22`  | cards, panels, filter bar            |
| `border-color`   | `#1F2937`  | subtle borders                       |
| `accent-green`   | `#00FF9C`  | primary actions, prices, active nav  |
| `accent-blue`    | `#00D9FF`  | secondary accents, info states       |
| `accent-amber`   | `#FFB800`  | warnings, low-stock, pending status  |
| `danger`         | `#FF4D6D`  | invalid inputs, remove, logout       |
| `text-primary`   | `#E6EDF3`  | body copy                            |
| `text-secondary` | `#8B949E`  | meta, sub-labels, datasheet comments |

Drop shadows: `dropshadow(gaussian, <accent>, 10–22, 0.4–0.7, 0, 0)`.
Corners 8–12 px. Buttons hover-scale to `1.03×` with intensified glow.

## Screen flow

```
Welcome ─► Sign In ─┬─► Dashboard ─► Products ─► Product Detail
                    │                    ↓
                    └─► Sign Up ─► Cart ─► Checkout ─► Orders
                                                       │
                                                       ▼
                                                    Profile
```

All transitions are 300 ms `FadeTransition` through the cached `Scene`.

## Optional: bundle real fonts

Drop TTFs into `src/main/resources/com/tekup/circuithub/assets/fonts/`:

- `JetBrainsMono-Regular.ttf`, `JetBrainsMono-Bold.ttf`
- `Inter-Regular.ttf`, `Inter-Bold.ttf`

`Fonts.loadAll()` registers whichever files are present and silently skips the
rest. The CSS falls back to Consolas / Segoe UI.

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `Connection refused` on port 5432 | Run `docker-compose up -d` and wait for the health check to pass |
| `Unsupported JavaFX configuration` | Confirm JDK 21: `java -version` |
| Blank product images | Internet required on first paint; swap `image_url` values in the DB for `file:` paths to work offline |
| Reset all data | `docker-compose down -v && docker-compose up -d`, then restart the app |
