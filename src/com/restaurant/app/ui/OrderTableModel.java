package com.restaurant.app.ui;

import com.restaurant.app.model.OrderItem;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class OrderTableModel extends AbstractTableModel {
    private final List<OrderItem> rows = new ArrayList<>();
    private final String[] columns = {"Item", "Qty", "Price", "Line Total"};

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        OrderItem item = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> item.getMenuItem().getName();
            case 1 -> item.getQuantity();
            case 2 -> String.format("$%.2f", item.getMenuItem().getPrice());
            case 3 -> String.format("$%.2f", item.getLineTotal());
            default -> "";
        };
    }

    public void setRows(List<OrderItem> items) {
        rows.clear();
        rows.addAll(items);
        fireTableDataChanged();
    }

    public OrderItem getRow(int index) {
        if (index < 0 || index >= rows.size()) {
            return null;
        }
        return rows.get(index);
    }
}


