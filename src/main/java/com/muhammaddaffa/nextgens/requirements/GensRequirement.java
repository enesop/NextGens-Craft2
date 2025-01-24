package com.muhammaddaffa.nextgens.requirements;

import org.bukkit.entity.Player;

public abstract class GensRequirement {

    public abstract String getId();

    public abstract String getMessage();

    public abstract boolean isSuccessful(Player player);

}
