package com.muhammaddaffa.nextgens.hooks.modelengine;

import com.muhammaddaffa.nextgens.events.managers.EventManager;
import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import org.bukkit.event.Listener;

public class ModelEngineLoad implements Listener {

    private final GeneratorManager generatorManager;
    private final EventManager eventManager;

    public ModelEngineLoad(GeneratorManager generatorManager, EventManager eventManager) {
        this.generatorManager = generatorManager;
        this.eventManager = eventManager;
    }
}
