package com.muhammaddaffa.nextgens.api.events.sell;

import com.muhammaddaffa.nextgens.users.models.User;
import com.muhammaddaffa.nextgens.utils.SellData;
import org.bukkit.entity.Player;

public class SellCommandUseEvent extends SellEvent{

    public SellCommandUseEvent(Player player, User user, SellData sellData) {
        super(player, user, sellData);
    }

}
