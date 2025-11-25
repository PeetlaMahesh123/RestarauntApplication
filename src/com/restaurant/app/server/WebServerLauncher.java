package com.restaurant.app.server;

import com.restaurant.app.data.MenuData;
import com.restaurant.app.model.MenuItem;
import com.restaurant.app.model.Order;
import com.restaurant.app.model.OrderItem;
import com.restaurant.app.service.OrderManager;
import com.restaurant.app.util.JsonUtil;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Lightweight HTTP server that serves the web UI and exposes JSON endpoints.
 */
public final class WebServerLauncher {

    private static final int PORT = 8080;
    private static final DateTimeFormatter ORDER_TIME = DateTimeFormatter.ofPattern("MMM dd HH:mm");

    private WebServerLauncher() {
    }

    public static void main(String[] args) throws IOException {
        Map<String, List<MenuItem>> menu = MenuData.loadMenu();
        Map<String, MenuItem> menuIndex = buildMenuIndex(menu);
        OrderManager orderManager = new OrderManager();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/menu", new MenuHandler(menu));
        server.createContext("/api/orders", new OrderHandler(orderManager, menuIndex));
        server.createContext("/", new StaticFileHandler(Path.of("web")));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.printf(Locale.US, "Web server running at http://localhost:%d%n", PORT);
    }

    private static Map<String, MenuItem> buildMenuIndex(Map<String, List<MenuItem>> menu) {
        Map<String, MenuItem> index = new HashMap<>();
        menu.values().forEach(list -> list.forEach(item -> index.put(item.getCode(), item)));
        return index;
    }

    private static final class MenuHandler implements HttpHandler {
        private final Map<String, List<MenuItem>> menu;

        private MenuHandler(Map<String, List<MenuItem>> menu) {
            this.menu = menu;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendStatus(exchange, 405, "Method Not Allowed");
                return;
            }
            Map<String, Object> payload = new HashMap<>();
            List<Map<String, Object>> categories = new ArrayList<>();
            menu.forEach((category, items) -> {
                Map<String, Object> node = new HashMap<>();
                node.put("category", category);
                List<Map<String, Object>> itemNodes = new ArrayList<>();
                for (MenuItem item : items) {
                    Map<String, Object> info = new HashMap<>();
                    info.put("code", item.getCode());
                    info.put("name", item.getName());
                    info.put("description", item.getDescription());
                    info.put("price", item.getPrice());
                    itemNodes.add(info);
                }
                node.put("items", itemNodes);
                categories.add(node);
            });
            payload.put("categories", categories);
            sendJson(exchange, 200, payload);
        }
    }

    private static final class OrderHandler implements HttpHandler {
        private final OrderManager orderManager;
        private final Map<String, MenuItem> menuIndex;

        private OrderHandler(OrderManager orderManager, Map<String, MenuItem> menuIndex) {
            this.orderManager = orderManager;
            this.menuIndex = menuIndex;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod().toUpperCase(Locale.ROOT);
            switch (method) {
                case "GET" -> handleList(exchange);
                case "POST" -> handleCreate(exchange);
                default -> sendStatus(exchange, 405, "Method Not Allowed");
            }
        }

        private void handleList(HttpExchange exchange) throws IOException {
            List<Map<String, Object>> summaries = new ArrayList<>();
            for (Order order : orderManager.getHistory()) {
                summaries.add(orderSummary(order));
            }
            Map<String, Object> payload = Map.of("orders", summaries);
            sendJson(exchange, 200, payload);
        }

        private void handleCreate(HttpExchange exchange) throws IOException {
            String body = readBody(exchange.getRequestBody());
            Map<String, Object> payload;
            try {
                payload = JsonUtil.parseObject(body);
            } catch (IllegalArgumentException ex) {
                sendStatus(exchange, 400, "Invalid JSON: " + ex.getMessage());
                return;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
            if (items == null || items.isEmpty()) {
                sendStatus(exchange, 400, "Order must include at least one item");
                return;
            }

            Order order = new Order();
            order.setTableNumber(String.valueOf(payload.getOrDefault("table", "TBD")));
            order.setNotes(String.valueOf(payload.getOrDefault("notes", "")));

            for (Map<String, Object> item : items) {
                String code = String.valueOf(item.get("code"));
                Number qtyNumber = (Number) item.get("quantity");
                int quantity = qtyNumber == null ? 1 : Math.max(1, qtyNumber.intValue());
                MenuItem menuItem = menuIndex.get(code);
                if (menuItem == null) {
                    sendStatus(exchange, 400, "Unknown menu item code: " + code);
                    return;
                }
                order.addItem(menuItem, quantity);
            }

            orderManager.addOrder(order);
            sendJson(exchange, 201, Map.of(
                "message", "Order received",
                "order", orderSummary(order)
            ));
        }

        private Map<String, Object> orderSummary(Order order) {
            List<Map<String, Object>> items = new ArrayList<>();
            for (OrderItem item : order.getItems()) {
                items.add(Map.of(
                    "name", item.getMenuItem().getName(),
                    "quantity", item.getQuantity(),
                    "lineTotal", item.getLineTotal()
                ));
            }
            return Map.of(
                "id", order.getId(),
                "table", order.getTableNumber(),
                "notes", order.getNotes(),
                "subtotal", order.getSubtotal(),
                "tax", order.getTax(),
                "total", order.getTotal(),
                "placedAt", ORDER_TIME.format(order.getCreatedAt()),
                "items", items
            );
        }
    }

    private static final class StaticFileHandler implements HttpHandler {
        private final Path webRoot;

        private StaticFileHandler(Path webRoot) {
            this.webRoot = webRoot.toAbsolutePath().normalize();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/api/")) {
                sendStatus(exchange, 404, "Endpoint not found");
                return;
            }

            Path resolved = resolvePath(path);
            if (resolved == null || Files.isDirectory(resolved) || !Files.exists(resolved)) {
                resolved = webRoot.resolve("index.html");
            }

            if (!Files.exists(resolved)) {
                sendStatus(exchange, 404, "File not found");
                return;
            }

            byte[] data = Files.readAllBytes(resolved);
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", detectMimeType(resolved));
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
            }
        }

        private Path resolvePath(String uriPath) {
            String cleaned = uriPath.replaceFirst("^/", "").replace("/", java.io.File.separator);
            if (cleaned.isBlank()) {
                cleaned = "index.html";
            }
            Path candidate = webRoot.resolve(cleaned).normalize();
            return candidate.startsWith(webRoot) ? candidate : null;
        }

        private String detectMimeType(Path path) {
            String file = path.getFileName().toString().toLowerCase(Locale.ROOT);
            if (file.endsWith(".html")) {
                return "text/html; charset=UTF-8";
            }
            if (file.endsWith(".css")) {
                return "text/css; charset=UTF-8";
            }
            if (file.endsWith(".js")) {
                return "application/javascript; charset=UTF-8";
            }
            if (file.endsWith(".json")) {
                return "application/json; charset=UTF-8";
            }
            if (file.endsWith(".png")) {
                return "image/png";
            }
            if (file.endsWith(".jpg") || file.endsWith(".jpeg")) {
                return "image/jpeg";
            }
            return "application/octet-stream";
        }
    }

    private static void sendJson(HttpExchange exchange, int status, Object payload) throws IOException {
        byte[] data = JsonUtil.stringify(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private static void sendStatus(HttpExchange exchange, int status, String message) throws IOException {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private static String readBody(InputStream stream) throws IOException {
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
}


