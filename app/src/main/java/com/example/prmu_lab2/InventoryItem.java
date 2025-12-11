package com.example.prmu_lab2;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InventoryItem {
    private String id;
    private String itemName;
    private double estimatedCost;
    private String purchaseDate;

    // Конструкторы
    public InventoryItem() {}

    public InventoryItem(String itemName, double estimatedCost, String purchaseDate) {
        this.itemName = itemName;
        this.estimatedCost = estimatedCost;
        this.purchaseDate = purchaseDate;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(double estimatedCost) { this.estimatedCost = estimatedCost; }

    public String getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }

    // Форматированная дата для отображения
    public String getFormattedDate() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            Date date = inputFormat.parse(purchaseDate);
            return outputFormat.format(date);
        } catch (Exception e) {
            return purchaseDate;
        }
    }

    // Форматированная стоимость
    public String getFormattedCost() {
        return String.format(Locale.getDefault(), "%.2f руб.", estimatedCost);
    }
}