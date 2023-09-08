package com.muhammaddaffa.nextgens.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Utils {

    private static final FormatBalance formatBalance = new FormatBalance();

    public static String formatBalance(long value) {
        return formatBalance.format(value);
    }

    public static void bassSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 2.0f);
    }

}
