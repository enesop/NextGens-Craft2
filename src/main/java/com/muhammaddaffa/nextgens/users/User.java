package com.muhammaddaffa.nextgens.users;

import com.muhammaddaffa.mdlib.utils.ItemBuilder;

import java.util.UUID;

public class User {

    private final UUID uuid;
    private int bonus;
    // multiplier system
    private double multiplier = 1.0;
    // statistics
    private double earnings;
    private int itemsSold;
    private int normalSell, sellwandSell;
    // settings
    private boolean toggleCashback = true;

    public User(UUID uuid) {
        this.uuid = uuid;
    }

    public User(UUID uuid, int bonus, double multiplier, double earnings, int itemsSold, int normalSell, int sellwandSell,
                boolean toggleCashback) {
        this.uuid = uuid;
        this.bonus = bonus;
        this.multiplier = multiplier;
        this.earnings = earnings;
        this.itemsSold = itemsSold;
        this.normalSell = normalSell;
        this.sellwandSell = sellwandSell;
        this.toggleCashback = toggleCashback;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public int getBonus() {
        return bonus;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
        if (this.bonus < 0) {
            this.bonus = 0;
        }
    }

    public void addBonus(int amount) {
        this.bonus += amount;
    }

    public void removeBonus(int amount) {
        this.bonus -= amount;
        if (this.bonus < 0) {
            this.bonus = 0;
        }
    }

    public double getMultiplier() {
        if (this.multiplier < 1) {
            this.multiplier = 1;
        }
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public void addMultiplier(double amount) {
        this.multiplier += amount;
    }

    public void removeMultiplier(double amount) {
        this.multiplier -= amount;
    }

    public double getEarnings() {
        return earnings;
    }

    public void setEarnings(double earnings) {
        this.earnings = earnings;
    }

    public void addEarnings(double amount) {
        this.earnings += amount;
    }

    public void removeEarnings(double amount) {
        this.earnings -= amount;
    }

    public int getItemsSold() {
        return itemsSold;
    }

    public void setItemsSold(int itemsSold) {
        this.itemsSold = itemsSold;
    }

    public void addItemsSold(int amount) {
        this.itemsSold += amount;
    }

    public void removeItemsSold(int amount) {
        this.itemsSold -= amount;
    }

    public int getNormalSell() {
        return normalSell;
    }

    public void setNormalSell(int normalSell) {
        this.normalSell = normalSell;
    }

    public void addNormalSell(int amount) {
        this.normalSell += amount;
    }

    public void removeNormalSell(int amount) {
        this.normalSell -= amount;
    }

    public int getSellwandSell() {
        return sellwandSell;
    }

    public void setSellwandSell(int sellwandSell) {
        this.sellwandSell = sellwandSell;
    }

    public void addSellwandSell(int amount) {
        this.sellwandSell += amount;
    }

    public void removeSellwandSell(int amount) {
        this.sellwandSell -= amount;
    }

    public int getTotalSell() {
        return this.normalSell + this.sellwandSell;
    }

    public boolean isToggleCashback() {
        return toggleCashback;
    }

    public void setToggleCashback(boolean toggleCashback) {
        this.toggleCashback = toggleCashback;
    }

}
