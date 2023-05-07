package me.m56738.easyarmorstands.history.action;

import me.m56738.easyarmorstands.EasyArmorStands;
import me.m56738.easyarmorstands.capability.spawn.SpawnCapability;
import me.m56738.easyarmorstands.property.EntityProperty;
import me.m56738.easyarmorstands.property.entity.EntityLocationProperty;
import me.m56738.easyarmorstands.util.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntitySpawnAction<E extends Entity> implements Action {
    private final Class<E> type;
    private Location location;
    private Map<EntityProperty<E, ?>, Object> properties;
    private UUID uuid;

    @SuppressWarnings("unchecked")
    public EntitySpawnAction(Entity entity) {
        this((Class<E>) entity.getType().getEntityClass(), entity.getLocation(), collectProperties((E) entity), entity.getUniqueId());
    }

    public EntitySpawnAction(Class<E> type, Location location, Map<EntityProperty<E, ?>, Object> properties, UUID uuid) {
        this.type = type;
        this.location = location;
        this.properties = properties;
        this.uuid = uuid;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <E extends Entity> Map<EntityProperty<E, ?>, Object> collectProperties(E entity) {
        Map<EntityProperty<E, ?>, Object> properties = new HashMap<>();
        for (EntityProperty property : EasyArmorStands.getInstance().getEntityPropertyRegistry().getProperties(entity.getClass()).values()) {
            if (property instanceof EntityLocationProperty) {
                continue;
            }
            Object value = property.getValue(entity);
            properties.put(property, value);
        }
        return properties;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean execute() {
        SpawnCapability spawnCapability = EasyArmorStands.getInstance().getCapability(SpawnCapability.class);
        E entity = spawnCapability.spawnEntity(location, type, e -> {
            for (Map.Entry entry : properties.entrySet()) {
                EntityProperty property = (EntityProperty) entry.getKey();
                property.setValue(e, entry.getValue());
            }
        });
        if (uuid != null) {
            EasyArmorStands.getInstance().getHistoryManager().onEntityReplaced(uuid, entity.getUniqueId());
        } else {
            uuid = entity.getUniqueId();
        }
        return true;
    }

    @Override
    public boolean undo() {
        E entity = Util.getEntity(uuid, type);
        if (entity == null) {
            throw new IllegalStateException();
        }
        location = entity.getLocation();
        properties = collectProperties(entity);
        entity.remove();
        return true;
    }

    @Override
    public Component describe() {
        return Component.text("Spawned " + type.getSimpleName());
    }

    @Override
    public void onEntityReplaced(UUID oldId, UUID newId) {
        if (uuid.equals(oldId)) {
            uuid = newId;
        }
    }

    public Class<E> getType() {
        return type;
    }

    public UUID getEntityId() {
        return uuid;
    }

    public @Nullable E findEntity() {
        return Util.getEntity(uuid, type);
    }
}