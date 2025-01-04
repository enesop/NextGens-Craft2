package com.muhammaddaffa.nextgens.multipliers;

import com.muhammaddaffa.nextgens.multipliers.providers.EventMultiplierProvider;
import com.muhammaddaffa.nextgens.multipliers.providers.PermissionMultiplierProvider;
import com.muhammaddaffa.nextgens.multipliers.providers.SellwandMultiplierProvider;
import com.muhammaddaffa.nextgens.multipliers.providers.UserMultiplierProvider;

import java.util.ArrayList;
import java.util.List;

public class MultiplierRegistry {

    private final List<MultiplierProvider> multipliers = new ArrayList<>();

    public MultiplierRegistry() {
        // Register all multipliers
        multipliers.add(new EventMultiplierProvider());
        multipliers.add(new PermissionMultiplierProvider());
        multipliers.add(new SellwandMultiplierProvider());
        multipliers.add(new UserMultiplierProvider());
    }

    public void registerMultiplier(MultiplierProvider provider) {
        multipliers.add(provider);
    }

    public List<MultiplierProvider> getMultipliers() {
        return multipliers;
    }

}
