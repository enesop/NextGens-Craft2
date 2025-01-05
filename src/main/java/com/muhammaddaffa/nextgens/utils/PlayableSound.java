package com.muhammaddaffa.nextgens.utils;

import com.muhammaddaffa.mdlib.xseries.XSound;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record PlayableSound(
        String name,
        float volume,
        float pitch
) {

    public void play(Player player) {
        Optional<XSound> optional = XSound.of(this.name);
        if (optional.isEmpty()) {
            // play custom sound instead
            player.playSound(player.getLocation(), this.name, this.volume, this.pitch);
            return;
        }
        optional.get().play(player, volume, pitch);
        optional.ifPresent(sound -> sound.play(player, volume, pitch));
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
