package com.muhammaddaffa.nextgens.users.managers;

import com.muhammaddaffa.mdlib.hooks.VaultEconomy;
import com.muhammaddaffa.mdlib.utils.Common;
import com.muhammaddaffa.mdlib.utils.Config;
import com.muhammaddaffa.mdlib.utils.Logger;
import com.muhammaddaffa.mdlib.utils.Placeholder;
import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.api.GeneratorAPI;
import com.muhammaddaffa.nextgens.api.events.sell.SellCommandUseEvent;
import com.muhammaddaffa.nextgens.api.events.sell.SellEvent;
import com.muhammaddaffa.nextgens.api.events.sell.SellwandUseEvent;
import com.muhammaddaffa.nextgens.database.DatabaseManager;
import com.muhammaddaffa.nextgens.events.Event;
import com.muhammaddaffa.nextgens.events.managers.EventManager;
import com.muhammaddaffa.nextgens.multiplier.Multiplier;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {

    private final Map<UUID, User> userMap = new HashMap<>();

    private final DatabaseManager dbm;
    private final EventManager eventManager;
    public UserManager(DatabaseManager dbm, EventManager eventManager) {
        this.dbm = dbm;
        this.eventManager = eventManager;
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

    public SellData performSell(Player player, Inventory inventory, boolean sellwand) {
        GeneratorAPI api = NextGens.getApi();
        double totalValue = 0.0;
        int totalItems = 0;
        // loop through inventory contents
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
        // check if player has anything to sell
        if (totalItems == 0 || totalValue <= 0) {
            // send message
            Common.configMessage("config.yml", player, "messages.no-sell");
            // play bass sound
            Utils.bassSound(player);
            return null;
        }
        // get all variables needed
        User user = this.getUser(player);
        double multiplier = user.getMultiplier();
        Event event = this.eventManager.getActiveEvent();
        // check if there is sell multiplier event
        if (event != null && event.getType() == Event.Type.SELL_MULTIPLIER && event.getSellMultiplier() != null) {
            // accumulate the multiplier
            multiplier += event.getSellMultiplier();
        }
        // set the final amount
        final double finalAmount = totalValue * multiplier;
        // create the sell data
        SellData sellData = new SellData(finalAmount, totalItems, multiplier, sellwand);
        SellEvent sellEvent = new SellCommandUseEvent(player, user, sellData);
        if (sellwand) sellEvent = new SellwandUseEvent(player, user, sellData);
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
        VisualAction.send(player, Config.getFileConfiguration("config.yml"), "sell-options", new Placeholder()
                .add("{amount}", Common.digits(data.totalItems()))
                .add("{amount_formatted}", Utils.formatBalance(data.totalItems()))
                .add("{value}", Common.digits(data.totalValue()))
                .add("{value_formatted}", Utils.formatBalance((long) data.totalValue()))
                .add("{multiplier}", Common.digits(data.multiplier())));

        // set the statistics
        user.addEarnings(data.totalValue());
        user.addItemsSold(data.totalItems());
        if (data.sellwand()) {
            user.addSellwandSell(1);
        } else {
            user.addNormalSell(1);
        }
        return data;
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
        // get the bonus slot
        return max + this.getUser(player).getBonus();
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

                // store it on the map
                this.userMap.put(uuid, new User(
                        uuid, bonus, multiplier, earnings, itemsSold, normalSell, sellwandSell
                ));
            }
            // send log message
            Logger.info("Successfully loaded " + this.userMap.size() + " users data!");
        });
    }

    public void saveUser() {
        try (Connection connection = this.dbm.getConnection()) {
            String query = "REPLACE INTO " + DatabaseManager.USER_TABLE + " VALUES (?,?,?,?,?,?,?);";

            for (User user : this.userMap.values()) {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, user.getUniqueId().toString());
                    statement.setInt(2, user.getBonus());
                    statement.setDouble(3, user.getMultiplier());
                    statement.setDouble(4, user.getEarnings());
                    statement.setInt(5, user.getItemsSold());
                    statement.setInt(6, user.getNormalSell());
                    statement.setInt(7, user.getSellwandSell());

                    statement.executeUpdate();
                }
            }

            // send log message
            Logger.info("Successfully saved " + this.userMap.size() + " users data!");

        } catch (SQLException ex) {
            Logger.severe("Failed to save all users data!");
            ex.printStackTrace();
        }
    }

    private double getOrDefault(Integer result, double defaultValue) {
        if (result == null) return defaultValue;
        return result;
    }

}
