package me.m56738.easyarmorstands.editor.armorstand;

import me.m56738.easyarmorstands.api.property.Property;
import me.m56738.easyarmorstands.api.property.PropertyContainer;
import me.m56738.easyarmorstands.api.property.type.EntityPropertyTypes;
import me.m56738.easyarmorstands.api.util.RotationProvider;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;

public class ArmorStandRotationProvider implements RotationProvider {
    private final Property<Location> locationProperty;
    private final Quaterniond currentRotation = new Quaterniond();

    public ArmorStandRotationProvider(PropertyContainer properties) {
        this.locationProperty = properties.get(EntityPropertyTypes.LOCATION);
    }

    @Override
    public @NotNull Quaterniondc getRotation() {
        Location location = locationProperty.getValue();
        return currentRotation.rotationY(-Math.toRadians(location.getYaw()));
    }
}
