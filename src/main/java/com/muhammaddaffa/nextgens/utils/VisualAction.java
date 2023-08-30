package com.muhammaddaffa.nextgens.utils;

import com.muhammaddaffa.mdlib.utils.Common;
import com.muhammaddaffa.mdlib.utils.Placeholder;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class VisualAction {

    public static void send(Player player, FileConfiguration config, String path, @Nullable Placeholder placeholder) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            return;
        }
        send(player, section, placeholder);
    }

    public static void send(Player player, ConfigurationSection section, @Nullable Placeholder placeholder) {
        // title bar
        if (section.getBoolean("titles.enabled")) {
            // get the title and subtitle
            String title = section.getString("titles.title");
            String subTitle = section.getString("titles.sub-title");
            // send the title
            Common.sendTitle(player, title, subTitle, placeholder);
        }
        // chat messages
        if (section.getBoolean("messages.enabled")) {
            Common.sendMessage(player, section.getStringList("messages.message"), placeholder);
        }
        // action bar
        if (section.getBoolean("action-bar.enabled")) {
            Common.actionBar(player, section.getString("action-bar.message"), placeholder);
        }
        // sound
        if (section.getBoolean("sound.enabled")) {
            Sound sound = Sound.valueOf(section.getString("sound.name"));
            float volume = (float) section.getDouble("sound.volume");
            float pitch = (float) section.getDouble("sound.pitch");
            // play the sound
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

}
