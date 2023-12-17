package com.muhammaddaffa.nextgens.utils;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public record PlayableSound(
        String name,
        float volume,
        float pitch
) {

    public void play(Player player) {
        try {
            player.playSound(player.getLocation(), Sound.valueOf(this.name), this.volume, this.pitch);
        } catch (IllegalArgumentException ex) {
            // play custom sound instead
            player.playSound(player.getLocation(), this.name, this.volume, this.pitch);
        }
    }

    public static PlayableSound parse(ConfigurationSection section) {
        String sound = section.getString("sound.name");
        float volume = (float) section.getDouble("sound.volume");
        float pitch = (float) section.getDouble("sound.pitch");

        return new PlayableSound(sound, volume, pitch);
    }

    public static PlayableSound parse(FileConfiguration config, String path) {
        String sound = config.getString(path + ".name");
        float volume = (float) config.getDouble(path + ".volume");
        float pitch = (float) config.getDouble(path + ".pitch");

        return new PlayableSound(sound, volume, pitch);
    }

}
