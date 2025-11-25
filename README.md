# Restaurant Order Management System

An interactive Java Swing application for managing restaurant orders with a colorful and modern interface. The project is intentionally lightweight so it can be compiled and executed with the JDK alone—no external build tools required.

## Features

- Curated menu with categorized food and beverage items
- Visual menu grid with color-coded item cards
- Order cart with editable quantities, tax calculation, and formatted totals
- Table assignment, order notes, and quick status updates
- Order history tracker to keep a log of recently served tables

## Project Structure

```
JavaProject/
├── README.md
└── src/
    └── com/restaurant/app/
        ├── AppLauncher.java
        ├── data/MenuData.java
        ├── model/
        │   ├── MenuItem.java
        │   ├── Order.java
        │   ├── OrderItem.java
        │   └── OrderStatus.java
        ├── service/OrderManager.java
        └── ui/
            ├── ColorPalette.java
            ├── OrderTableModel.java
            └── RestaurantApp.java
```

## Running the Application

1. Ensure you have JDK 17 (or later) installed and available on your `PATH`.
2. Compile the sources:
   ```powershell
   cd "C:\Users\Windows 11\OneDrive\Desktop\JavaProject"
   javac -d out src/com/restaurant/app/**/*.java
   ```
3. Run the launcher:
   ```powershell
   java -cp out com.restaurant.app.AppLauncher
   ```

The main window opens with the vibrant dashboard. Use the left menu to pick categories, add dishes to the cart, tweak quantities, and place orders. Order history is displayed on the right and can be cleared with a single click.

## Customization Tips

- Update `MenuData` to reflect your own restaurant catalog.
- Adjust colors or typography inside `ColorPalette`.
- Extend `OrderManager` to persist data to files or databases as needed.

Enjoy building on top of this foundation!


