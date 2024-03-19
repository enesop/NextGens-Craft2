package com.muhammaddaffa.nextgens.users.managers;

import com.muhammaddaffa.mdlib.hooks.VaultEconomy;
import com.muhammaddaffa.mdlib.utils.*;
import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.api.GeneratorAPI;
import com.muhammaddaffa.nextgens.api.events.sell.SellCommandUseEvent;
import com.muhammaddaffa.nextgens.api.events.sell.SellEvent;
import com.muhammaddaffa.nextgens.api.events.sell.SellwandUseEvent;
import com.muhammaddaffa.nextgens.database.DatabaseManager;
import com.muhammaddaffa.nextgens.events.Event;
import com.muhammaddaffa.nextgens.events.managers.EventManager;
import com.muhammaddaffa.nextgens.multiplier.Multiplier;
import com.muhammaddaffa.nextgens.sellwand.SellwandData;
import com.muhammaddaffa.nextgens.users.User;
import com.muhammaddaffa.nextgens.utils.SellData;
import com.muhammaddaffa.nextgens.utils.Utils;
import com.muhammaddaffa.nextgens.utils.VisualAction;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class UserManager {

    private final Map<UUID, User> userMap = new HashMap<>();

    private final DatabaseManager dbm;
    private final EventManager eventManager;
    public UserManager(DatabaseManager dbm, EventManager eventManager) {
        this.dbm = dbm;
        this.eventManager = eventManager;
    }

    public List<String> getUsersName() {
        return this.userMap.values().stream()
                .map(User::getName)
                .collect(Collectors.toList());
    }

    @Nullable
    public User getUser(String name) {
        return this.userMap.values().stream()
                .filter(user -> {
                    if (user.getName() == null) return false;
                    return user.getName().equalsIgnoreCase(name);
                })
                .findFirst()
                .orElse(null);
    }

    @NotNull
    public User getUser(Player player) {
        return this.getUser(player.getUniqueId());
    }

    @NotNull
    public User getUser(UUID uuid) {
        return this.userMap.computeIfAbsent(uuid, k -> new User(uuid));
    }

    public Collection<User> getUsers() {
        return this.userMap.values();
    }

    public boolean sell(Player player, ItemStack stack) {
        GeneratorAPI api = NextGens.getApi();
        User user = this.getUser(player);
        Double value = api.getWorth(stack);
        if (value == null) return false;
        // sell the item
        VaultEconomy.deposit(player, value);
        // set the statistics
        user.addEarnings(value);
        user.addItemsSold(stack.getAmount());
        // remove the item
        stack.setAmount(0);
        return true;
    }

    public SellData performSell(Player player, SellwandData sellwand, Inventory... inventories) {
        return this.performSell(player, sellwand, false, inventories);
    }

    public SellData performSell(Player player, SellwandData sellwand, boolean silent, Inventory... inventories) {
        GeneratorAPI api = NextGens.getApi();
        double totalValue = 0.0;
        int totalItems = 0;
        // loop through inventory contents
        for (Inventory inventory : inventories) {
            for (ItemStack stack : inventory) {
                // get value
                Double value = api.getWorth(stack);
                if (value == null) continue;
                // if the item has value, register it
                if (value > 0) {
                    totalItems += stack.getAmount();
                    totalValue += value;
                    // remove the item
                    stack.setAmount(0);
                }
            }
        }
        // check if player has anything to sell
        if (totalItems == 0) {
            if (!silent) {
                // send message
                Common.configMessage("config.yml", player, "messages.no-sell");
                // play bass sound
                Utils.bassSound(player);
            }
            return null;
        }
        // get all variables needed
        User user = this.getUser(player);
        // create the sell data
        final SellData sellData = this.getSellData(player, sellwand, totalValue, totalItems, user);

        // inject the multiplier limit system
        FileConfiguration config = Config.getFileConfiguration("config.yml");
        if (config.getBoolean("player-multiplier-limit.enabled")) {
            double limit = config.getDouble("player-multiplier-limit.limit");
            if (sellData.multiplier() > limit) {
                sellData.multiplier(limit);
            }
        }

        SellEvent sellEvent = new SellCommandUseEvent(player, user, sellData);
        if (sellwand != null) sellEvent = new SellwandUseEvent(player, user, sellData);
        // call the event
        Bukkit.getPluginManager().callEvent(sellEvent);
        // if event is cancelled, skip this
        if (sellEvent.isCancelled()) {
            return null;
        }
        // do the rest with value from event
        SellData data = sellEvent.getSellData();
        VaultEconomy.deposit(player, data.totalValue());
        // send the visual action
        if (!silent) {
            VisualAction.send(player, config, "sell-options", new Placeholder()
                    .add("{amount}", Common.digits(data.totalItems()))
                    .add("{amount_formatted}", Utils.formatBalance(data.totalItems()))
                    .add("{value}", Common.digits(data.totalValue()))
                    .add("{value_formatted}", Utils.formatBalance((long) data.totalValue()))
                    .add("{multiplier}", Common.digits(data.multiplier())));
        }
        // set the statistics
        user.addEarnings(data.totalValue());
        user.addItemsSold(data.totalItems());
        if (sellwand != null) {
            user.addSellwandSell(1);
        } else {
            user.addNormalSell(1);
        }
        // save the user data
        Executor.async(() -> this.saveUser(user));

        return data;
    }

    private SellData getSellData(Player player, SellwandData sellwand, double totalValue, int totalItems, User user) {
        Event event = this.eventManager.getActiveEvent();
        double playerMultiplier = user.getMultiplier();
        double sellwandMultiplier = sellwand != null ? sellwand.multiplier() : 0;
        double eventMultiplier = (event != null && event.getType() == Event.Type.SELL_MULTIPLIER && event.getSellMultiplier() != null) ? event.getSellMultiplier() : 0;
        double permissionMultiplier = Multiplier.getSellMultiplier(player);
        // get the final amount
        double playerBonus = totalValue * playerMultiplier;
        double sellwandBonus = totalValue * Math.max(0, sellwandMultiplier - 1);
        double eventBonus = totalValue * Math.max(0, eventMultiplier - 1);
        double permissionBonus = totalValue * permissionMultiplier;
        double finalAmount = totalValue + playerBonus + sellwandBonus + eventBonus + permissionBonus;
        // the display
        double playerMultiplierDisplay = playerMultiplier >= 1.0 ? (playerMultiplier + 1) : playerMultiplier;
        double sellwandMultiplierDisplay = (sellwandMultiplier - 1) >= 1.0 ? sellwandMultiplier : Math.max(0, sellwandMultiplier - 1);
        double eventMultiplierDisplay = (eventMultiplier - 1) >= 1.0 ? eventMultiplier : Math.max(0, eventMultiplier - 1);
        double permissionMultiplierDisplay = permissionMultiplier >= 1.0 ? (permissionMultiplier + 1) : permissionMultiplier;
        // get the total multiplier
        double totalMultiplier = playerMultiplierDisplay + sellwandMultiplierDisplay + eventMultiplierDisplay + permissionMultiplierDisplay;
        // return the sell data
        return new SellData(user, finalAmount, totalItems, totalMultiplier, sellwand);
    }

    public int getMaxSlot(Player player) {
        int max = 0;
        FileConfiguration config = Config.getFileConfiguration("config.yml");
        if (config.getBoolean("default-max-generator.enabled")) {
            max += config.getInt("default-max-generator.amount");
        }
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String permission = pai.getPermission();
            if (!permission.startsWith("nextgens.max.")) {
                continue;
            }
            int current = Integer.parseInt(permission.split("\\.")[2]);
            if (current > max) {
                max = current;
            }
        }

        int bonusMax = max + this.getUser(player).getBonus();
        int limit = config.getInt("player-generator-limit.limit");
        if (config.getBoolean("player-generator-limit.enabled") && bonusMax > limit) {
            return limit;
        }
        return bonusMax;
    }

    public void loadUser() {
        String query = "SELECT * FROM " + DatabaseManager.USER_TABLE;
        this.dbm.executeQuery(query, result -> {
            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString(1));
                int bonus = result.getInt(2);
                double multiplier = result.getDouble(3);
                double earnings = result.getDouble(4);
                int itemsSold = result.getInt(5);
                int normalSell = result.getInt(6);
                int sellwandSell = result.getInt(7);
                boolean toggleCashback = result.getBoolean(8);
                boolean toggleInventorySell = result.getBoolean(9);
                boolean toggleGensSell = result.getBoolean(10);

                // store it on the map
                this.userMap.put(uuid, new User(uuid, bonus, multiplier, earnings, itemsSold, normalSell,
                        sellwandSell, toggleCashback, toggleInventorySell, toggleGensSell
                ));
            }
            // send log message
            Logger.info("Successfully loaded " + this.userMap.size() + " users data!");
        });
    }

    public void saveUser(User user) {
        this.saveUser(List.of(user));
    }

    public void saveUser() {
        this.saveUser(this.userMap.values().stream().toList());
    }

    public void saveUser(List<User> users) {
        String query = "REPLACE INTO " + DatabaseManager.USER_TABLE + " VALUES (?,?,?,?,?,?,?,?,?,?);";
        try (Connection connection = this.dbm.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            for (User user : users) {
                statement.setString(1, user.getUniqueId().toString());
                statement.setInt(2, user.getBonus());
                statement.setDouble(3, user.getMultiplier());
                statement.setDouble(4, user.getEarnings());
                statement.setInt(5, user.getItemsSold());
                statement.setInt(6, user.getNormalSell());
                statement.setInt(7, user.getSellwandSell());
                statement.setBoolean(8, user.isToggleCashback());
                statement.setBoolean(9, user.isToggleInventoryAutoSell());
                statement.setBoolean(10, user.isToggleGensAutoSell());

                // add batch
                statement.addBatch();
            }

            // execute the batch
            statement.executeBatch();

            // send log message
            Logger.info("Successfully saved " + users.size() + " users data!");

        } catch (SQLException ex) {
            Logger.severe("Failed to save " + users.size() + " users data!");
            ex.printStackTrace();
        }
    }

    private double getOrDefault(Integer result, double defaultValue) {
        if (result == null) return defaultValue;
        return result;
    }

}
