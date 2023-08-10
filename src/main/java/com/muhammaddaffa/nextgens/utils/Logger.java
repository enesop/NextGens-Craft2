package com.muhammaddaffa.nextgens.utils;

import com.muhammaddaffa.nextgens.NextGens;

public class Logger {

    public static void info(String... messages) {
        for (String message : messages) {
            NextGens.getInstance().getLogger().info(message);
        }
    }

    public static void warning(String... messages) {
        for (String message : messages) {
            NextGens.getInstance().getLogger().warning(message);
        }
    }

    public static void severe(String... messages) {
        for (String message : messages) {
            NextGens.getInstance().getLogger().severe(message);
        }
    }

    public static void finest(String... messages) {
        for (String message : messages) {
            NextGens.getInstance().getLogger().finest(message);
        }
    }

}
