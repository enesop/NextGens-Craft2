package com.muhammaddaffa.nextgens.utils;

import com.muhammaddaffa.nextgens.sellwand.SellwandData;
import com.muhammaddaffa.nextgens.users.User;

public class SellData {

    private final User user;
    private final double totalValue;
    private final int totalItems;
    private double multiplier;
    private final SellwandData sellwandData;

    public SellData(User user, double totalValue, int totalItems, double multiplier, SellwandData sellwandData) {
        this.user = user;
        this.totalValue = totalValue;
        this.totalItems = totalItems;
        this.multiplier = multiplier;
        this.sellwandData = sellwandData;
    }

    public User user() {
        return user;
    }

    public double totalValue() {
        return totalValue;
    }

    public int totalItems() {
        return totalItems;
    }

    public double multiplier() {
        return multiplier;
    }

    public void multiplier(double amount) {
        this.multiplier = amount;
    }

    public SellwandData sellwandData() {
        return sellwandData;
    }

}
