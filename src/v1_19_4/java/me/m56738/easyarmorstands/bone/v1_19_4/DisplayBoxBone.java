package me.m56738.easyarmorstands.bone.v1_19_4;

import me.m56738.easyarmorstands.bone.PositionBone;
import me.m56738.easyarmorstands.bone.RotationProvider;
import me.m56738.easyarmorstands.bone.ScaleBone;
import me.m56738.easyarmorstands.property.PendingChange;
import me.m56738.easyarmorstands.property.Property;
import me.m56738.easyarmorstands.property.PropertyContainer;
import me.m56738.easyarmorstands.property.entity.EntityLocationProperty;
import me.m56738.easyarmorstands.property.v1_19_4.display.DisplayHeightProperty;
import me.m56738.easyarmorstands.property.v1_19_4.display.DisplayTranslationProperty;
import me.m56738.easyarmorstands.property.v1_19_4.display.DisplayWidthProperty;
import me.m56738.easyarmorstands.util.Axis;
import me.m56738.easyarmorstands.util.Util;
import org.bukkit.Location;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class DisplayBoxBone implements PositionBone, ScaleBone, RotationProvider {
    private final PropertyContainer container;
    private final Property<Location> entityLocationProperty;
    private final Property<Vector3fc> displayTranslationProperty;
    private final Property<Float> displayWidthProperty;
    private final Property<Float> displayHeightProperty;

    public DisplayBoxBone(PropertyContainer container) {
        this.container = container;
        this.entityLocationProperty = container.get(EntityLocationProperty.TYPE);
        this.displayTranslationProperty = container.get(DisplayTranslationProperty.TYPE);
        this.displayWidthProperty = container.get(DisplayWidthProperty.TYPE);
        this.displayHeightProperty = container.get(DisplayHeightProperty.TYPE);
    }

    @Override
    public boolean isValid() {
        return container.isValid();
    }

    @Override
    public void commit() {
        container.commit();
    }

    @Override
    public Vector3dc getPosition() {
        return Util.toVector3d(entityLocationProperty.getValue());
    }

    @Override
    public void setPosition(Vector3dc position) {
        Vector3dc delta = position.sub(getPosition(), new Vector3d());

        // Move box by modifying the location
        Location location = entityLocationProperty.getValue().clone()
                .add(delta.x(), delta.y(), delta.z());
        PendingChange locationChange = entityLocationProperty.prepareChange(location);

        // Make sure the display stays in the same place by performing the inverse using the translation
        Vector3fc rotatedDelta = delta.get(new Vector3f())
                .rotate(Util.getRoundedYawPitchRotation(location, new Quaternionf()).conjugate());
        Vector3fc translation = displayTranslationProperty.getValue()
                .sub(rotatedDelta, new Vector3f());
        PendingChange translationChange = displayTranslationProperty.prepareChange(translation);

        // Only execute changes if both are allowed
        if (locationChange != null && translationChange != null) {
            if (locationChange.execute()) {
                translationChange.execute();
            }
        }
    }

    @Override
    public Vector3dc getOrigin() {
        return getPosition();
    }

    @Override
    public Quaterniondc getRotation() {
        return Util.IDENTITY;
    }

    @Override
    public double getScale(Axis axis) {
        if (axis == Axis.Y) {
            return displayHeightProperty.getValue();
        } else {
            return displayWidthProperty.getValue();
        }
    }

    @Override
    public void setScale(Axis axis, double scale) {
        if (axis == Axis.Y) {
            displayHeightProperty.setValue((float) scale);
        } else {
            displayWidthProperty.setValue((float) scale);
        }
    }
}
