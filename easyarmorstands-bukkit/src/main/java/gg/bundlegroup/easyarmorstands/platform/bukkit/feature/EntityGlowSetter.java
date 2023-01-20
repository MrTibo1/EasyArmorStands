package gg.bundlegroup.easyarmorstands.platform.bukkit.feature;

import org.bukkit.entity.Entity;

public interface EntityGlowSetter {
    void setGlowing(Entity entity, boolean glowing);

    boolean isGlowing(Entity entity);

    interface Provider extends FeatureProvider<EntityGlowSetter> {
    }
}
