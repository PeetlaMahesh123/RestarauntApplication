package com.restaurant.app.data;

import com.restaurant.app.model.MenuItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MenuData {

    private MenuData() {
    }

    public static Map<String, List<MenuItem>> loadMenu() {
        Map<String, List<MenuItem>> menu = new LinkedHashMap<>();
        menu.put("Signatures", List.of(
            new MenuItem("SGN-01", "Chef's Tasting Platter", "Signatures", "Seasonal bites with artisanal dips", 18.5),
            new MenuItem("SGN-02", "Truffle Mushroom Risotto", "Signatures", "Creamy Arborio rice with wild mushrooms", 16.0),
            new MenuItem("SGN-03", "Citrus Glazed Salmon", "Signatures", "Pan-seared salmon with citrus glaze", 21.0)
        ));

        menu.put("Small Plates", List.of(
            new MenuItem("SMP-01", "Crispy Calamari", "Small Plates", "Served with harissa aioli", 12.0),
            new MenuItem("SMP-02", "Avocado Bruschetta", "Small Plates", "Heirloom tomatoes & basil oil", 10.5),
            new MenuItem("SMP-03", "Spiced Cauliflower Bites", "Small Plates", "Tamarind glaze & mint yogurt", 9.0)
        ));

        menu.put("Mains", List.of(
            new MenuItem("MNS-01", "Charcoal BBQ Burger", "Mains", "Smoked cheddar, caramelized onions", 15.0),
            new MenuItem("MNS-02", "Thai Coconut Curry", "Mains", "Vegetables, jasmine rice, toasted peanuts", 14.5),
            new MenuItem("MNS-03", "Garlic Butter Steak", "Mains", "Grilled sirloin, herb butter", 24.0)
        ));

        menu.put("Greens", List.of(
            new MenuItem("GRN-01", "Harvest Bowl", "Greens", "Quinoa, roasted veggies, tahini drizzle", 13.0),
            new MenuItem("GRN-02", "Mediterranean Salad", "Greens", "Feta, olives, sun-dried tomatoes", 12.5)
        ));

        menu.put("Desserts", List.of(
            new MenuItem("DES-01", "Molten Lava Cake", "Desserts", "Dark chocolate, vanilla gelato", 9.5),
            new MenuItem("DES-02", "Coconut Panna Cotta", "Desserts", "Mango coulis & toasted coconut", 8.5)
        ));

        menu.put("Beverages", List.of(
            new MenuItem("BEV-01", "Cold Brew Tonic", "Beverages", "Citrus, espresso & tonic fizz", 6.5),
            new MenuItem("BEV-02", "Ginger Lime Spritz", "Beverages", "House-made ginger syrup & lime", 5.5),
            new MenuItem("BEV-03", "Herbal Iced Tea", "Beverages", "Lemongrass & mint", 4.5)
        ));

        return new LinkedHashMap<>(menu);
    }

    public static List<MenuItem> flattenMenu(Map<String, List<MenuItem>> menu) {
        List<MenuItem> combined = new ArrayList<>();
        menu.values().forEach(combined::addAll);
        return combined;
    }
}


