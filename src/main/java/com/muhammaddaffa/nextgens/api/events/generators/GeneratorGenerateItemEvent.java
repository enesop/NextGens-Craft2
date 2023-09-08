package com.muhammaddaffa.nextgens.api.events.generators;

import com.muhammaddaffa.nextgens.generators.ActiveGenerator;
import com.muhammaddaffa.nextgens.generators.Generator;

public class GeneratorGenerateItemEvent extends GeneratorEvent{

    private final ActiveGenerator activeGenerator;
    private int dropAmount;

    public GeneratorGenerateItemEvent(Generator generator, ActiveGenerator activeGenerator, int dropAmount) {
        super(generator);
        this.activeGenerator = activeGenerator;
        this.dropAmount = dropAmount;
    }

    public ActiveGenerator getActiveGenerator() {
        return activeGenerator;
    }

    public int getDropAmount() {
        return dropAmount;
    }

    public void setDropAmount(int dropAmount) {
        this.dropAmount = dropAmount;
    }

}
