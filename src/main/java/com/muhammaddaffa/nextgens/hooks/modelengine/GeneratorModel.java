package com.muhammaddaffa.nextgens.hooks.modelengine;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

public class GeneratorModel {

    private final ArmorStand stand;
    private final ModeledEntity modeledEntity;
    private final ActiveModel activeModel;

    public GeneratorModel(Location location, String modelId) {
        // Uses armor stands to support bedrock and lower versions
        this.stand = location.getWorld().spawn(location, ArmorStand.class, armor -> {
            armor.setVisible(false);
            armor.setGravity(false);
            armor.setMarker(true);
            armor.setSilent(true);
            armor.setCustomNameVisible(false);
            armor.setInvulnerable(true);
            armor.setPersistent(true);
            armor.setRemoveWhenFarAway(false);
        });

        this.modeledEntity = ModelEngineAPI.getOrCreateModeledEntity(stand);
        this.activeModel = ModelEngineAPI.createActiveModel(modelId);

        modeledEntity.addModel(activeModel, true);
    }

    public void remove() {
        if (modeledEntity != null) {
            modeledEntity.destroy();
        }
        if (stand != null && !stand.isDead()) {
            stand.remove();
        }
    }

    public ArmorStand getStand() {
        return stand;
    }

    public int getModelHash() {
        return activeModel.hashCode();
    }
}
