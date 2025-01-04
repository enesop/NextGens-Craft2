package com.muhammaddaffa.nextgens.multipliers.providers;

import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.events.Event;
import com.muhammaddaffa.nextgens.multipliers.MultiplierProvider;
import com.muhammaddaffa.nextgens.sellwand.models.SellwandData;
import com.muhammaddaffa.nextgens.users.models.User;
import org.bukkit.entity.Player;

public class EventMultiplierProvider implements MultiplierProvider {

    @Override
    public double getMultiplier(Player player, User user, SellwandData sellwand) {
        Event event = NextGens.getInstance().getEventManager().getActiveEvent();
        // If there is no active event or event is not valid, return 0
        if (event == null || event.getType() != Event.Type.SELL_MULTIPLIER || event.getSellMultiplier() == null) {
            return 0;
        }
        return event.getSellMultiplier();
    }

}
