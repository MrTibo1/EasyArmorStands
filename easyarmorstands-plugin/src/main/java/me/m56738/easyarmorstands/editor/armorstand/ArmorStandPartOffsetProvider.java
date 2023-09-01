package me.m56738.easyarmorstands.editor.armorstand;

import me.m56738.easyarmorstands.api.ArmorStandPart;
import me.m56738.easyarmorstands.api.ArmorStandSize;
import me.m56738.easyarmorstands.api.property.Property;
import me.m56738.easyarmorstands.api.property.PropertyContainer;
import me.m56738.easyarmorstands.api.property.type.ArmorStandPropertyTypes;
import me.m56738.easyarmorstands.api.property.type.EntityPropertyTypes;
import me.m56738.easyarmorstands.editor.OffsetProvider;
import me.m56738.easyarmorstands.util.ArmorStandPartInfo;
import org.bukkit.Location;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class ArmorStandPartOffsetProvider implements OffsetProvider {
    private final Property<Location> locationProperty;
    private final Property<ArmorStandSize> sizeProperty;
    private final ArmorStandPartInfo partInfo;

    public ArmorStandPartOffsetProvider(PropertyContainer container, ArmorStandPart part) {
        this.partInfo = ArmorStandPartInfo.of(part);
        this.locationProperty = container.get(EntityPropertyTypes.LOCATION);
        this.sizeProperty = container.get(ArmorStandPropertyTypes.SIZE);
    }

    @Override
    public Vector3dc getOffset() {
        return partInfo.getOffset(sizeProperty.getValue())
                .rotateY(-Math.toRadians(locationProperty.getValue().getYaw()), new Vector3d());
    }
}
