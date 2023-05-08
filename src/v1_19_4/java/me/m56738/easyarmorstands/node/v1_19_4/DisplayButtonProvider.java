package me.m56738.easyarmorstands.node.v1_19_4;

import me.m56738.easyarmorstands.addon.display.DisplayAddon;
import me.m56738.easyarmorstands.session.EntityButtonPriority;
import me.m56738.easyarmorstands.session.EntityButtonProvider;
import me.m56738.easyarmorstands.session.Session;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;

public class DisplayButtonProvider<T extends Display> implements EntityButtonProvider {
    private final Class<T> type;
    private final DisplayAddon addon;
    private final DisplayRootNodeFactory<T> factory;
    private final EntityButtonPriority priority;

    public DisplayButtonProvider(Class<T> type, DisplayAddon addon, DisplayRootNodeFactory<T> factory, EntityButtonPriority priority) {
        this.type = type;
        this.addon = addon;
        this.factory = factory;
        this.priority = priority;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DisplayButton<T> createButton(Session session, Entity entity) {
        if (type.isAssignableFrom(entity.getClass())) {
            return new DisplayButton<>(session, (T) entity, addon, factory);
        } else {
            return null;
        }
    }

    @Override
    public EntityButtonPriority getPriority() {
        return priority;
    }
}
