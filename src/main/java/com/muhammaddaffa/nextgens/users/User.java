package com.muhammaddaffa.nextgens.users;

import java.util.UUID;

public class User {

    private final UUID uuid;
    private int bonus;

    public User(UUID uuid) {
        this.uuid = uuid;
    }

    public User(UUID uuid, int bonus) {
        this.uuid = uuid;
        this.bonus = bonus;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public int getBonus() {
        return bonus;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
        if (this.bonus < 0) {
            this.bonus = 0;
        }
    }

    public void addBonus(int amount) {
        this.bonus += amount;
    }

    public void removeBonus(int amount) {
        this.bonus -= amount;
        if (this.bonus < 0) {
            this.bonus = 0;
        }
    }

}
