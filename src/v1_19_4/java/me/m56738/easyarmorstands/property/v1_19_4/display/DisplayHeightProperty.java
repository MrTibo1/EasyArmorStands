package me.m56738.easyarmorstands.property.v1_19_4.display;

import me.m56738.easyarmorstands.EasyArmorStands;
import me.m56738.easyarmorstands.addon.display.DisplayAddon;
import me.m56738.easyarmorstands.property.EntityPropertyChange;
import me.m56738.easyarmorstands.property.entity.EntityLocationProperty;
import me.m56738.easyarmorstands.session.Session;
import me.m56738.easyarmorstands.util.v1_19_4.JOMLMapper;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class DisplayHeightProperty extends DisplaySizeProperty {
    private final DisplayAddon addon;

    public DisplayHeightProperty(DisplayAddon addon) {
        this.addon = addon;
    }

    @Override
    public Float getValue(Display entity) {
        return entity.getDisplayHeight();
    }

    @Override
    public void setValue(Display entity, Float value) {
        entity.setDisplayHeight(value);
    }

    @Override
    public @NotNull String getName() {
        return "height";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.text("height");
    }

    @Override
    public boolean performChange(Session session, Display entity, Float value) {
        JOMLMapper mapper = addon.getMapper();
        DisplayTransformationProperty displayTransformationProperty = addon.getDisplayTransformationProperty();
        EntityLocationProperty entityLocationProperty = EasyArmorStands.getInstance().getEntityLocationProperty();

        float oldValue = entity.getDisplayHeight();
        float delta = (value - oldValue) / 2;

        // Attempt to keep the center in the same place
        List<EntityPropertyChange<?, ?>> changes = new ArrayList<>(2);

        // Move down using location
        Location location = entityLocationProperty.getValue(entity);
        Location newLocation = location.clone();
        newLocation.setY(location.getY() - delta);
        changes.add(new EntityPropertyChange<>(entity, entityLocationProperty, newLocation));

        // Move up using offset
        Transformation transformation = displayTransformationProperty.getValue(entity);
        Vector3f translation = mapper.getTranslation(transformation);
        translation.y += delta;
        mapper.setTranslation(transformation, translation);
        changes.add(new EntityPropertyChange<>(entity, displayTransformationProperty, transformation));

        // Ignore the return value, failure is allowed
        session.setProperties(changes);

        return super.performChange(session, entity, value);
    }
}