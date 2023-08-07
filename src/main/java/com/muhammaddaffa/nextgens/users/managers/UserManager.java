package com.muhammaddaffa.nextgens.users.managers;

import com.muhammaddaffa.nextgens.database.DatabaseManager;
import com.muhammaddaffa.nextgens.generators.ActiveGenerator;
import com.muhammaddaffa.nextgens.users.User;
import com.muhammaddaffa.nextgens.utils.LocationSerializer;
import com.muhammaddaffa.nextgens.utils.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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
    public UserManager(DatabaseManager dbm) {
        this.dbm = dbm;
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

    public int getMaxSlot(Player player) {
        int max = 0;
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

                // store it on the map
                this.userMap.put(uuid, new User(uuid, bonus));
            }
            // send log message
            Logger.info("Successfully loaded " + this.userMap.size() + " users data!");
        });
    }

    public void saveUser() {
        try (Connection connection = this.dbm.getConnection()) {
            String query = "REPLACE INTO " + DatabaseManager.USER_TABLE + " VALUES (?,?);";

            for (User user : this.userMap.values()) {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, user.getUniqueId().toString());
                    statement.setInt(2, user.getBonus());

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

}
