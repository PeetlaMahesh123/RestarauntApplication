package com.restaurant.app.ui;

import com.restaurant.app.data.MenuData;
import com.restaurant.app.model.MenuItem;
import com.restaurant.app.model.Order;
import com.restaurant.app.model.OrderItem;
import com.restaurant.app.service.OrderManager;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;

public class RestaurantApp extends JFrame {

    private final Map<String, List<MenuItem>> menu = MenuData.loadMenu();
    private final OrderManager orderManager = new OrderManager();
    private Order currentOrder = new Order();

    private final OrderTableModel tableModel = new OrderTableModel();
    private final DefaultListModel<String> historyModel = new DefaultListModel<>();

    private JPanel menuGridPanel;
    private JTable orderTable;
    private JTextField tableField;
    private JTextArea noteArea;
    private JLabel subtotalLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;

    public RestaurantApp() {
        super("Restaurant Order Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 740));
        setLocationRelativeTo(null);
        getContentPane().setBackground(ColorPalette.BACKGROUND);
        initUi();
        refreshOrderTable();
        updateSummary();
    }

    private void initUi() {
        JPanel root = new JPanel(new BorderLayout(20, 0));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.setBackground(ColorPalette.BACKGROUND);

        root.add(buildMenuSection(), BorderLayout.WEST);
        root.add(buildOrderSection(), BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildMenuSection() {
        JPanel container = createCardPanel();
        container.setPreferredSize(new Dimension(420, 0));
        container.setLayout(new BorderLayout(0, 16));

        JLabel title = new JLabel("Seasonal Menu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ColorPalette.TEXT);
        container.add(title, BorderLayout.NORTH);

        JList<String> categoryList = new JList<>(menu.keySet().toArray(new String[0]));
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setSelectedIndex(0);
        categoryList.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        categoryList.setForeground(ColorPalette.TEXT);
        categoryList.setBackground(new Color(247, 249, 252));
        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = categoryList.getSelectedValue();
                if (selected != null) {
                    renderMenuCards(menu.get(selected));
                }
            }
        });

        JScrollPane categoryScroll = new JScrollPane(categoryList);
        categoryScroll.setBorder(createSoftBorder());
        categoryScroll.setPreferredSize(new Dimension(200, 140));
        container.add(categoryScroll, BorderLayout.WEST);

        menuGridPanel = new JPanel(new GridLayout(0, 2, 14, 14));
        menuGridPanel.setOpaque(false);

        JScrollPane menuScroll = new JScrollPane(menuGridPanel);
        menuScroll.setBorder(BorderFactory.createEmptyBorder());
        menuScroll.getVerticalScrollBar().setUnitIncrement(14);
        container.add(menuScroll, BorderLayout.CENTER);

        renderMenuCards(menu.values().iterator().next());
        return container;
    }

    private JPanel buildOrderSection() {
        JPanel container = new JPanel(new BorderLayout(0, 16));
        container.setOpaque(false);
        container.add(buildCurrentOrderCard(), BorderLayout.CENTER);
        container.add(buildHistoryCard(), BorderLayout.SOUTH);
        return container;
    }

    private JPanel buildCurrentOrderCard() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 16));

        JLabel title = new JLabel("Table Service");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ColorPalette.TEXT);
        card.add(title, BorderLayout.NORTH);

        JPanel metaPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        metaPanel.setOpaque(false);

        tableField = new JTextField();
        tableField.setBorder(BorderFactory.createTitledBorder(createSoftBorder(), "Table"));
        tableField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableField.setText("A1");
        metaPanel.add(tableField);

        noteArea = new JTextArea(3, 20);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBorder(BorderFactory.createTitledBorder(createSoftBorder(), "Notes"));
        noteArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        metaPanel.add(new JScrollPane(noteArea));

        card.add(metaPanel, BorderLayout.CENTER);

        orderTable = new JTable(tableModel);
        orderTable.setRowHeight(26);
        orderTable.setShowGrid(false);
        orderTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.getTableHeader().setReorderingAllowed(false);
        orderTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane tableScroll = new JScrollPane(orderTable);
        tableScroll.setBorder(createSoftBorder());
        card.add(tableScroll, BorderLayout.SOUTH);

        JPanel summaryPanel = buildSummaryPanel();
        card.add(summaryPanel, BorderLayout.EAST);

        JPanel actions = buildActionBar();
        card.add(actions, BorderLayout.SOUTH);

        JPanel centerStack = new JPanel(new BorderLayout(0, 12));
        centerStack.setOpaque(false);
        centerStack.add(metaPanel, BorderLayout.NORTH);
        centerStack.add(tableScroll, BorderLayout.CENTER);
        centerStack.add(summaryPanel, BorderLayout.EAST);
        centerStack.add(actions, BorderLayout.SOUTH);
        card.remove(metaPanel);
        card.remove(tableScroll);
        card.remove(summaryPanel);
        card.remove(actions);
        card.add(centerStack, BorderLayout.CENTER);

    return card;
    }

    private JPanel buildSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 16, 0, 0));

        subtotalLabel = createSummaryLabel("Subtotal", "$0.00");
        taxLabel = createSummaryLabel("Tax (8%)", "$0.00");
        totalLabel = createSummaryLabel("Due", "$0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalLabel.setForeground(ColorPalette.PRIMARY_DARK);

        panel.add(subtotalLabel);
        panel.add(taxLabel);
        panel.add(totalLabel);
        return panel;
    }

    private JPanel buildActionBar() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 12, 0));
        panel.setOpaque(false);

        panel.add(primaryButton("Add Qty", e -> adjustQuantity(1)));
        panel.add(primaryButton("Reduce Qty", e -> adjustQuantity(-1)));
        panel.add(accentButton("Remove", e -> removeSelectedItem()));
        panel.add(primaryButton("Place Order", e -> finalizeOrder()));
        return panel;
    }

    private JPanel buildHistoryCard() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 8));
        JLabel title = new JLabel("Recently Served");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ColorPalette.TEXT);
        card.add(title, BorderLayout.NORTH);

        JList<String> historyList = new JList<>(historyModel);
        historyList.setFont(new Font("Consolas", Font.PLAIN, 13));
        historyList.setForeground(ColorPalette.MUTED);
        historyList.setBackground(new Color(249, 250, 252));

        JScrollPane historyScroll = new JScrollPane(historyList);
        historyScroll.setBorder(createSoftBorder());
        historyScroll.setPreferredSize(new Dimension(0, 150));
        card.add(historyScroll, BorderLayout.CENTER);

        JButton clear = accentButton("Clear History", e -> {
            orderManager.clearHistory();
            historyModel.clear();
        });
        clear.setPreferredSize(new Dimension(160, 36));
        card.add(clear, BorderLayout.SOUTH);

        return card;
    }

    private void renderMenuCards(List<MenuItem> items) {
        menuGridPanel.removeAll();
        for (MenuItem item : items) {
            JPanel card = new JPanel(new BorderLayout(0, 6));
            card.setBackground(ColorPalette.CARD);
            card.setBorder(BorderFactory.createCompoundBorder(
                createSoftBorder(),
                new EmptyBorder(12, 12, 12, 12)
            ));

            JLabel name = new JLabel(item.getName());
            name.setFont(new Font("Segoe UI", Font.BOLD, 15));
            name.setForeground(ColorPalette.TEXT);
            card.add(name, BorderLayout.NORTH);

            JLabel desc = new JLabel("<html><span style='color: #6E7681;'>" + item.getDescription() + "</span></html>");
            desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            card.add(desc, BorderLayout.CENTER);

            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(false);

            JLabel price = new JLabel(String.format("$%.2f", item.getPrice()));
            price.setFont(new Font("Segoe UI", Font.BOLD, 16));
            price.setForeground(ColorPalette.PRIMARY);
            footer.add(price, BorderLayout.WEST);

            JButton addButton = primaryButton("Add", e -> {
                handleAddItem(item);
            });
            addButton.setPreferredSize(new Dimension(90, 32));
            footer.add(addButton, BorderLayout.EAST);

            card.add(footer, BorderLayout.SOUTH);
            menuGridPanel.add(card);
        }
        menuGridPanel.revalidate();
        menuGridPanel.repaint();
    }

    private void handleAddItem(MenuItem item) {
        currentOrder.addOrIncrement(item);
        refreshOrderTable();
        updateSummary();
    }

    private void adjustQuantity(int delta) {
        int row = orderTable.getSelectedRow();
        OrderItem selected = tableModel.getRow(row);
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select an item to adjust quantity.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int newQty = Math.max(0, selected.getQuantity() + delta);
        if (newQty <= 0) {
            currentOrder.remove(selected.getMenuItem());
        } else {
            selected.setQuantity(newQty);
        }
        refreshOrderTable();
        updateSummary();
    }

    private void removeSelectedItem() {
        int row = orderTable.getSelectedRow();
        OrderItem selected = tableModel.getRow(row);
        if (selected == null) {
            return;
        }
        currentOrder.remove(selected.getMenuItem());
        refreshOrderTable();
        updateSummary();
    }

    private void finalizeOrder() {
        if (currentOrder.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add at least one item before placing an order.", "Empty Order", JOptionPane.WARNING_MESSAGE);
            return;
        }
        currentOrder.setTableNumber(tableField.getText().isBlank() ? "TBD" : tableField.getText().trim());
        currentOrder.setNotes(noteArea.getText().trim());
        orderManager.addOrder(currentOrder);
        historyModel.add(0, currentOrder.summaryLine());

        currentOrder = new Order();
        tableField.setText("A1");
        noteArea.setText("");
        refreshOrderTable();
        updateSummary();

        JOptionPane.showMessageDialog(this, "Order sent to kitchen!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshOrderTable() {
        tableModel.setRows(currentOrder.getItems());
    }

    private void updateSummary() {
        subtotalLabel.setText(formatSummaryValue("Subtotal", currentOrder.getSubtotal()));
        taxLabel.setText(formatSummaryValue("Tax (8%)", currentOrder.getTax()));
        totalLabel.setText(formatSummaryValue("Due", currentOrder.getTotal()));
    }

    private JLabel createSummaryLabel(String label, String value) {
        JLabel lbl = new JLabel(label + ": " + value, SwingConstants.LEFT);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lbl.setForeground(ColorPalette.MUTED);
        return lbl;
    }

    private String formatSummaryValue(String label, double amount) {
        return label + ": $" + String.format("%.2f", amount);
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(ColorPalette.CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            createSoftBorder(),
            new EmptyBorder(18, 18, 18, 18)
        ));
        return panel;
    }

    private javax.swing.border.Border createSoftBorder() {
        return BorderFactory.createLineBorder(ColorPalette.BORDER, 1, true);
    }

    private JButton primaryButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        styleButton(button, ColorPalette.PRIMARY, Color.WHITE);
        button.addActionListener(listener);
        return button;
    }

    private JButton accentButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        styleButton(button, ColorPalette.ACCENT, Color.WHITE);
        button.addActionListener(listener);
        return button;
    }

    private void styleButton(JButton button, Color bg, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}


