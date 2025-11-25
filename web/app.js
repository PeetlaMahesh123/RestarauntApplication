const TAX_RATE = 0.08;
const menuGrid = document.getElementById("menuGrid");
const categoryTabs = document.getElementById("categoryTabs");
const cartItems = document.getElementById("cartItems");
const subtotalLabel = document.getElementById("subtotalLabel");
const taxLabel = document.getElementById("taxLabel");
const totalLabel = document.getElementById("totalLabel");
const tableInput = document.getElementById("tableInput");
const notesInput = document.getElementById("notesInput");
const toast = document.getElementById("toast");
const historyList = document.getElementById("historyList");
const serverStatus = document.getElementById("serverStatus");

const menuCardTemplate = document.getElementById("menuCardTemplate");
const cartRowTemplate = document.getElementById("cartRowTemplate");
const historyItemTemplate = document.getElementById("historyItemTemplate");

let menuData = [];
let activeCategory = null;
const cart = new Map();

document.getElementById("resetOrder").addEventListener("click", () => {
    cart.clear();
    renderCart();
});

document.getElementById("placeOrder").addEventListener("click", submitOrder);
document.getElementById("refreshHistory").addEventListener("click", loadHistory);

async function init() {
    await loadMenu();
    await loadHistory();
}

async function loadMenu() {
    try {
        const response = await fetch("/api/menu");
        if (!response.ok) {
            throw new Error("Unable to load menu");
        }
        const data = await response.json();
        menuData = data.categories;
        activeCategory = menuData[0]?.category ?? null;
        renderCategories();
        renderMenuCards();
        setServerStatus(true);
    } catch (error) {
        console.error(error);
        showToast("Failed to load menu", "error");
        setServerStatus(false);
    }
}

function renderCategories() {
    categoryTabs.innerHTML = "";
    menuData.forEach(({ category }) => {
        const tab = document.createElement("button");
        tab.className = `tab ${category === activeCategory ? "active" : ""}`;
        tab.textContent = category;
        tab.addEventListener("click", () => {
            activeCategory = category;
            renderCategories();
            renderMenuCards();
        });
        categoryTabs.appendChild(tab);
    });
}

function renderMenuCards() {
    menuGrid.innerHTML = "";
    const category = menuData.find((c) => c.category === activeCategory);
    if (!category) return;

    category.items.forEach((item) => {
        const card = menuCardTemplate.content.firstElementChild.cloneNode(true);
        card.querySelector("h3").textContent = item.name;
        card.querySelector("p").textContent = item.description;
        card.querySelector(".price").textContent = currency(item.price);
        card.querySelector("button").addEventListener("click", () => addToCart(item));
        menuGrid.appendChild(card);
    });
}

function addToCart(item) {
    const existing = cart.get(item.code) ?? { item, quantity: 0 };
    existing.quantity += 1;
    cart.set(item.code, existing);
    renderCart();
}

function renderCart() {
    cartItems.innerHTML = "";
    for (const { item, quantity } of cart.values()) {
        const row = cartRowTemplate.content.firstElementChild.cloneNode(true);
        row.querySelector(".name").textContent = item.name;
        row.querySelector(".desc").textContent = item.description;
        row.querySelector(".qty").textContent = quantity;
        row.querySelector(".line-total").textContent = currency(item.price * quantity);

        row.querySelectorAll(".circle-btn").forEach((btn) => {
            btn.addEventListener("click", () => {
                const action = btn.dataset.action;
                if (action === "increment") {
                    cart.get(item.code).quantity += 1;
                } else {
                    cart.get(item.code).quantity -= 1;
                    if (cart.get(item.code).quantity <= 0) {
                        cart.delete(item.code);
                    }
                }
                renderCart();
            });
        });

        cartItems.appendChild(row);
    }
    updateSummary();
}

function updateSummary() {
    let subtotal = 0;
    for (const { item, quantity } of cart.values()) {
        subtotal += item.price * quantity;
    }
    const tax = subtotal * TAX_RATE;
    const total = subtotal + tax;
    subtotalLabel.textContent = currency(subtotal);
    taxLabel.textContent = currency(tax);
    totalLabel.textContent = currency(total);
}

async function submitOrder() {
    if (!cart.size) {
        showToast("Add items before submitting", "error");
        return;
    }
    const payload = {
        table: tableInput.value || "TBD",
        notes: notesInput.value || "",
        items: Array.from(cart.values()).map(({ item, quantity }) => ({
            code: item.code,
            quantity,
        })),
    };

    try {
        const response = await fetch("/api/orders", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });
        if (!response.ok) {
            const message = await response.text();
            throw new Error(message);
        }

        cart.clear();
        renderCart();
        tableInput.value = "A1";
        notesInput.value = "";
        showToast("Order sent to kitchen!", "success");
        await loadHistory();
    } catch (error) {
        console.error(error);
        showToast(error.message || "Failed to submit order", "error");
    }
}

async function loadHistory() {
    try {
        const response = await fetch("/api/orders");
        if (!response.ok) {
            throw new Error("Unable to fetch history");
        }
        const data = await response.json();
        renderHistory(data.orders ?? []);
    } catch (error) {
        console.error(error);
        showToast("Could not refresh history", "error");
    }
}

function renderHistory(orders) {
    historyList.innerHTML = "";
    if (!orders.length) {
        historyList.innerHTML = `<p class="muted">No orders yet. Send one from the Live Order panel.</p>`;
        return;
    }
    orders.forEach((order) => {
        const item = historyItemTemplate.content.firstElementChild.cloneNode(true);
        item.querySelector(".title").textContent = `Table ${order.table} • #${order.id}`;
        item.querySelector(".details").textContent = `${order.items.length} items • ${currency(order.total)}`;
        item.querySelector(".badge").textContent = order.placedAt;
        historyList.appendChild(item);
    });
}

function currency(value) {
    return `$${value.toFixed(2)}`;
}

function showToast(message, type = "success") {
    toast.textContent = message;
    toast.className = `toast ${type}`;
    setTimeout(() => toast.classList.remove("hidden"), 10);
    setTimeout(() => toast.classList.add("hidden"), 2600);
}

function setServerStatus(isOnline) {
    serverStatus.textContent = isOnline ? "Server Online" : "Server Offline";
    serverStatus.previousElementSibling.style.background = isOnline ? "#23b26d" : "#f39c12";
}

init();


