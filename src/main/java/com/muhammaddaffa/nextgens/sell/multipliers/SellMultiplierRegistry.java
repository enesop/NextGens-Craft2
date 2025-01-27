package com.muhammaddaffa.nextgens.sell.multipliers;

import com.muhammaddaffa.nextgens.sell.multipliers.providers.EventSellMultiplierProvider;
import com.muhammaddaffa.nextgens.sell.multipliers.providers.PermissionSellMultiplierProvider;
import com.muhammaddaffa.nextgens.sell.multipliers.providers.SellwandSellMultiplierProvider;
import com.muhammaddaffa.nextgens.sell.multipliers.providers.UserSellMultiplierProvider;

import java.util.ArrayList;
import java.util.List;

public class SellMultiplierRegistry {

    private final List<SellMultiplierProvider> multipliers = new ArrayList<>();

    public SellMultiplierRegistry() {
        // Register all multipliers
        multipliers.add(new EventSellMultiplierProvider());
        multipliers.add(new PermissionSellMultiplierProvider());
        multipliers.add(new SellwandSellMultiplierProvider());
        multipliers.add(new UserSellMultiplierProvider());
    }

    public void registerMultiplier(SellMultiplierProvider provider) {
        multipliers.add(provider);
    }

    public List<SellMultiplierProvider> getMultipliers() {
        return multipliers;
    }

}
