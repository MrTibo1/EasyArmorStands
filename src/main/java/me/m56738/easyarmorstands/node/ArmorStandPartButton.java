package me.m56738.easyarmorstands.node;

import me.m56738.easyarmorstands.EasyArmorStands;
import me.m56738.easyarmorstands.capability.particle.ParticleCapability;
import me.m56738.easyarmorstands.particle.LineParticle;
import me.m56738.easyarmorstands.session.Session;
import me.m56738.easyarmorstands.util.ArmorStandPart;
import me.m56738.easyarmorstands.util.Axis;
import me.m56738.easyarmorstands.util.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.*;

public class ArmorStandPartButton implements Button {
    private final Session session;
    private final ArmorStand entity;
    private final ArmorStandPart part;
    private final Node node;
    private final Vector3d start = new Vector3d();
    private final Vector3d end = new Vector3d();
    private final Vector3d center = new Vector3d();
    private final Quaterniond rotation = new Quaterniond();
    private final LineParticle particle;
    private Vector3dc lookTarget;

    public ArmorStandPartButton(Session session, ArmorStand entity, ArmorStandPart part, Node node) {
        this.session = session;
        this.entity = entity;
        this.part = part;
        this.node = node;
        this.particle = EasyArmorStands.getInstance().getCapability(ParticleCapability.class).createLine();
        this.particle.setAxis(Axis.Y);
    }

    @Override
    public void update() {
        Location location = entity.getLocation();
        // rotation = combination of yaw and pose
        Util.fromEuler(part.getPose(entity), rotation)
                .rotateLocalY(-Math.toRadians(location.getYaw()));
        // start = where the bone is attached to the armor stand, depends on yaw
        part.getOffset(entity)
                .rotateY(-Math.toRadians(location.getYaw()), start)
                .add(location.getX(), location.getY(), location.getZ());
        // end = where the bone ends, depends on yaw and pose
        part.getLength(entity)
                .rotate(rotation, end)
                .add(start);
        // particles on the lower 2/3 of the bone
        start.lerp(end, 2.0 / 3, center);
    }

    @Override
    public void updateLookTarget(Vector3dc eyes, Vector3dc target) {
        Vector3d closestOnLookRay = new Vector3d();
        Vector3d closestOnBone = new Vector3d();
        double distanceSquared = Intersectiond.findClosestPointsLineSegments(
                eyes.x(), eyes.y(), eyes.z(),
                target.x(), target.y(), target.z(),
                start.x(), start.y(), start.z(),
                end.x(), end.y(), end.z(),
                closestOnLookRay,
                closestOnBone
        );

        double threshold = session.getLookThreshold();
        if (distanceSquared < threshold * threshold) {
            lookTarget = closestOnLookRay;
        } else {
            lookTarget = null;
        }
    }

    @Override
    public void updatePreview(boolean focused) {
        particle.setRotation(rotation);
        particle.setCenter(center);
        particle.setLength(center.distance(end) * 2);
    }

    @Override
    public void showPreview() {
        session.addParticle(particle);
    }

    @Override
    public void hidePreview() {
        session.removeParticle(particle);
    }

    @Override
    public @Nullable Vector3dc getLookTarget() {
        return lookTarget;
    }

    @Override
    public Component getName() {
        return part.getDisplayName();
    }

    @Override
    public Node createNode() {
        return node;
    }
}
