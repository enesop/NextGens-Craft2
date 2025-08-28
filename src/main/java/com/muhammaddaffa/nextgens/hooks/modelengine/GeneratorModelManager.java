package com.muhammaddaffa.nextgens.hooks.modelengine;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GeneratorModelManager {
    private final Map<UUID, GeneratorModel> models = new HashMap<>();

    public void spawnModel(UUID owner, Block block, String modelId) {
        Location loc = block.getLocation().add(0.5, 0, 0.5);
        GeneratorModel generatorModel = new GeneratorModel(loc, modelId);
        models.put(owner, generatorModel);
    }

    public void removeModel(UUID owner) {
        GeneratorModel model = models.remove(owner);
        if (model != null) {
            model.remove();
        }
    }

    public void removeAll() {
        models.values().forEach(GeneratorModel::remove);
        models.clear();
    }
}
