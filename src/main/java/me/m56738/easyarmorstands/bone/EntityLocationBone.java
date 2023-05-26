package me.m56738.easyarmorstands.bone;

import me.m56738.easyarmorstands.EasyArmorStands;
import me.m56738.easyarmorstands.session.Session;
import me.m56738.easyarmorstands.util.Util;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.joml.Vector3dc;

public class EntityLocationBone implements PositionAndYawBone {
    private final Session session;
    private final Entity entity;

    public EntityLocationBone(Session session, Entity entity) {
        this.session = session;
        this.entity = entity;
    }

    public Vector3dc getOffset() {
        return Util.ZERO;
    }

    @Override
    public Vector3dc getPosition() {
        return Util.toVector3d(entity.getLocation()).add(getOffset());
    }

    @Override
    public void setPosition(Vector3dc position) {
        Vector3dc offset = getOffset();
        Location location = entity.getLocation();
        location.setX(position.x() - offset.x());
        location.setY(position.y() - offset.y());
        location.setZ(position.z() - offset.z());
        session.setProperty(entity, EasyArmorStands.getInstance().getEntityLocationProperty(), location);
    }

    @Override
    public float getYaw() {
        return entity.getLocation().getYaw();
    }

    @Override
    public void setYaw(float yaw) {
        Location location = entity.getLocation();
        location.setYaw(yaw);
        session.setProperty(entity, EasyArmorStands.getInstance().getEntityLocationProperty(), location);
    }

    @Override
    public void setPositionAndYaw(Vector3dc position, float yaw) {
        Vector3dc offset = getOffset();
        Location location = entity.getLocation();
        location.setX(position.x() - offset.x());
        location.setY(position.y() - offset.y());
        location.setZ(position.z() - offset.z());
        location.setYaw(yaw);
        session.setProperty(entity, EasyArmorStands.getInstance().getEntityLocationProperty(), location);
    }

    @Override
    public boolean isValid() {
        return entity.isValid();
    }
}
