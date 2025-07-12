package com.muhammaddaffa.nextgens.database;

import org.bukkit.Location;

public record ChunkCoord(
        String world,
        int x,
        int z
) {

    public static ChunkCoord fromLocation(Location loc) {
        return new ChunkCoord(
                loc.getWorld().getName(),
                loc.getBlockX() >> 4,
                loc.getBlockZ() >> 4
        );
    }

}
