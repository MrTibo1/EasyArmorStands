package me.m56738.easyarmorstands.session;

import me.m56738.easyarmorstands.EasyArmorStands;
import me.m56738.easyarmorstands.capability.equipment.EquipmentCapability;
import me.m56738.easyarmorstands.capability.particle.ParticleCapability;
import me.m56738.easyarmorstands.event.SessionCommitEvent;
import me.m56738.easyarmorstands.event.SessionSelectEntityEvent;
import me.m56738.easyarmorstands.history.action.EntityPropertyAction;
import me.m56738.easyarmorstands.menu.SpawnMenu;
import me.m56738.easyarmorstands.node.ClickContext;
import me.m56738.easyarmorstands.node.EntityNode;
import me.m56738.easyarmorstands.node.EntitySelectionNode;
import me.m56738.easyarmorstands.node.Node;
import me.m56738.easyarmorstands.particle.Particle;
import me.m56738.easyarmorstands.property.ChangeContext;
import me.m56738.easyarmorstands.property.EntityProperty;
import me.m56738.easyarmorstands.property.EntityPropertyChange;
import me.m56738.easyarmorstands.util.Util;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.RGBLike;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.joml.Intersectiond;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class Session implements ChangeContext, ForwardingAudience.Single {
    public static final double DEFAULT_SNAP_INCREMENT = 1.0 / 32;
    public static final double DEFAULT_ANGLE_SNAP_INCREMENT = 360.0 / 256;
    private final LinkedList<Node> nodeStack = new LinkedList<>();
    private final EntitySelectionNode rootNode = new EntitySelectionNode(this, Component.text("Select an entity"));
    private final Player player;
    private final Audience audience;
    private final ParticleCapability particleCapability;
    @SuppressWarnings("rawtypes")
    private final Map<ChangeKey, Object> originalValues = new HashMap<>();
    @SuppressWarnings("rawtypes")
    private final Map<ChangeKey, Object> pendingValues = new HashMap<>();
    private final Set<Particle> particles = new HashSet<>();
    private int clickTicks = 5;
    private double snapIncrement = DEFAULT_SNAP_INCREMENT;
    private double angleSnapIncrement = DEFAULT_ANGLE_SNAP_INCREMENT;

    public Session(Player player) {
        this.player = player;
        this.audience = EasyArmorStands.getInstance().getAdventure().player(player);
        this.particleCapability = EasyArmorStands.getInstance().getCapability(ParticleCapability.class);
        this.rootNode.setRoot(true);
        pushNode(this.rootNode);
    }

    public Node getNode() {
        return nodeStack.peek();
    }

    public @UnmodifiableView List<Node> getNodeStack() {
        return Collections.unmodifiableList(nodeStack);
    }

    @SuppressWarnings("unchecked")
    public <T extends Node> @Nullable T findNode(Class<T> type) {
        for (Node node : nodeStack) {
            if (type.isAssignableFrom(node.getClass())) {
                return (T) node;
            }
        }
        return null;
    }

    public void pushNode(@NotNull Node node) {
        if (!nodeStack.isEmpty()) {
            nodeStack.peek().onExit();
        }
        nodeStack.push(node);
        node.onAdd();
        node.onEnter();
    }

    public void replaceNode(@NotNull Node node) {
        Node removed = nodeStack.pop();
        removed.onExit();
        removed.onRemove();
        nodeStack.push(node);
        node.onAdd();
        node.onEnter();
    }

    public void popNode() {
        Node removed = nodeStack.pop();
        removed.onExit();
        removed.onRemove();
        if (!nodeStack.isEmpty()) {
            nodeStack.peek().onEnter();
        }
    }

    public void clearNode() {
        if (!nodeStack.isEmpty()) {
            nodeStack.peek().onExit();
        }
        for (Node node : nodeStack) {
            if (node != rootNode) {
                node.onRemove();
            }
        }
        nodeStack.clear();
        nodeStack.push(rootNode);
        rootNode.onEnter();
    }

    public boolean handleClick(ClickContext context) {
        Node node = nodeStack.peek();
        if (node == null || clickTicks > 0) {
            return false;
        }
        clickTicks = 5;
        Location eyeLocation = player.getEyeLocation();
        Vector3dc eyes = Util.toVector3d(eyeLocation);
        Vector3dc target = eyes.fma(getRange(), Util.toVector3d(eyeLocation.getDirection()), new Vector3d());
        return node.onClick(eyes, target, context);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void commit() {
        Bukkit.getPluginManager().callEvent(new SessionCommitEvent(this));
        List<EntityPropertyAction> actions = new ArrayList<>();
        for (Map.Entry<ChangeKey, Object> entry : pendingValues.entrySet()) {
            ChangeKey key = entry.getKey();
            Object oldValue = originalValues.get(key);
            Object value = entry.getValue();
            if (!Objects.equals(oldValue, value)) {
                actions.add(new EntityPropertyAction(key.entity, key.property, oldValue, value));
            }
        }
        EasyArmorStands.getInstance().getHistory(player).push(actions);
        originalValues.clear();
        pendingValues.clear();
    }

    public double snap(double value) {
        if (player.isSneaking()) {
            return value;
        }
        return Util.snap(value, snapIncrement);
    }

    public double snapAngle(double value) {
        if (player.isSneaking()) {
            return value;
        }
        return Util.snap(value, angleSnapIncrement);
    }

    public Entity getEntity() {
        for (Node node : nodeStack) {
            if (node instanceof EntityNode) {
                return ((EntityNode) node).getEntity();
            }
        }
        return null;
    }

    public void addProvider(EntityButtonProvider provider) {
        rootNode.addProvider(provider);
    }

    public boolean update() {
        if (clickTicks > 0) {
            clickTicks--;
        }

        while (!nodeStack.isEmpty() && !nodeStack.peek().isValid()) {
            popNode();
        }

        Node currentNode = nodeStack.peek();
        if (currentNode != null) {
            Location eyeLocation = player.getEyeLocation();
            Vector3dc eyes = Util.toVector3d(eyeLocation);
            Vector3dc target = eyes.fma(getRange(), Util.toVector3d(eyeLocation.getDirection()), new Vector3d());
            currentNode.onUpdate(eyes, target);
        }
        for (Node node : nodeStack) {
            if (node != currentNode) {
                node.onInactiveUpdate();
            }
        }

        for (Particle particle : particles) {
            particle.update();
        }

        return player.isValid() && isHoldingTool();
    }

    private boolean isHoldingTool() {
        EasyArmorStands plugin = EasyArmorStands.getInstance();
        EquipmentCapability equipmentCapability = plugin.getCapability(EquipmentCapability.class);
        EntityEquipment equipment = player.getEquipment();
        for (EquipmentSlot hand : equipmentCapability.getHands()) {
            ItemStack item = equipmentCapability.getItem(equipment, hand);
            if (Util.isTool(item)) {
                return true;
            }
        }
        return false;
    }

    public void stop() {
        Node currentNode = nodeStack.peek();
        if (currentNode != null) {
            currentNode.onExit();
        }
        for (Node node : nodeStack) {
            node.onRemove();
        }
        nodeStack.clear();
        audience.clearTitle();
        audience.sendActionBar(Component.empty());
        for (Particle particle : particles) {
            particle.hide(player);
        }
        particles.clear();
        commit();
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public <E extends Entity, T> void applyChange(EntityPropertyChange<E, T> change) {
        E entity = change.getEntity();
        EntityProperty<E, T> property = change.getProperty();
        T value = change.getValue();
        T oldValue = property.getValue(entity);
        if (Objects.equals(oldValue, value)) {
            return;
        }
        property.setValue(entity, value);
        ChangeKey<E, T> key = new ChangeKey<>(entity, property);
        originalValues.putIfAbsent(key, oldValue);
        pendingValues.put(key, value);
    }

    public double getRange() {
        return 10;
    }

    public double getLookThreshold() {
        return 0.15;
    }

    public double getSnapIncrement() {
        return snapIncrement;
    }

    public void setSnapIncrement(double snapIncrement) {
        this.snapIncrement = snapIncrement;
    }

    public double getAngleSnapIncrement() {
        return angleSnapIncrement;
    }

    public void setAngleSnapIncrement(double angleSnapIncrement) {
        this.angleSnapIncrement = angleSnapIncrement;
    }

    public EntitySelectionNode getRootNode() {
        return rootNode;
    }

    @Override
    public @NotNull Audience audience() {
        return audience;
    }

    public void addParticle(Particle particle) {
        if (particles.add(particle)) {
            particle.show(player);
        }
    }

    public void removeParticle(Particle particle) {
        if (particles.remove(particle)) {
            particle.hide(player);
        }
    }

    @Deprecated
    private int getParticleCount(double length) {
        return Math.min((int) Math.round(length * particleCapability.getDensity()), 100);
    }

    @Deprecated
    public void showPoint(Vector3dc position, Color color) {
        particleCapability.spawnParticle(player, position.x(), position.y(), position.z(), color);
    }

    @Deprecated
    public void showPoint(Vector3dc position, RGBLike color) {
        showPoint(position, Util.toColor(color));
    }

    @Deprecated
    private void showLine(double x, double y, double z,
                          double dx, double dy, double dz,
                          Color color, boolean includeEnds, int count) {
        if (count < 1) {
            return;
        }
        int min = includeEnds ? 0 : 1;
        int max = includeEnds ? count : count - 1;
        for (int i = min; i <= max; i++) {
            double t = i / (double) count;
            particleCapability.spawnParticle(player,
                    x + t * dx,
                    y + t * dy,
                    z + t * dz,
                    color);
        }
    }

    @Deprecated
    public void showLine(Vector3dc from, Vector3dc to, Color color, boolean includeEnds) {
        double length = from.distance(to);
        double x = from.x();
        double y = from.y();
        double z = from.z();
        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double dz = to.z() - from.z();
        showLine(x, y, z, dx, dy, dz, color, includeEnds, getParticleCount(length));
    }

    @Deprecated
    public void showLine(Vector3dc start, Vector3dc end, RGBLike color, boolean includeEnds) {
        showLine(start, end, Util.toColor(color), includeEnds);
    }

    @Deprecated
    public void showCircle(Vector3dc center, Vector3dc axis, Color color, double radius) {
        double circumference = 2 * Math.PI * radius;
        int count = getParticleCount(circumference);
        Vector3d offset = center.cross(axis, new Vector3d()).normalize(radius);
        double axisX = axis.x();
        double axisY = axis.y();
        double axisZ = axis.z();
        double angle = 2 * Math.PI / count;
        for (int i = 0; i < count; i++) {
            offset.rotateAxis(angle, axisX, axisY, axisZ);
            particleCapability.spawnParticle(player,
                    center.x() + offset.x,
                    center.y() + offset.y,
                    center.z() + offset.z,
                    color);
        }
    }

    @Deprecated
    public void showCircle(Vector3dc center, Vector3dc axis, RGBLike color, double radius) {
        showCircle(center, axis, Util.toColor(color), radius);
    }

    @Deprecated
    public void showAxisAlignedBox(Vector3dc center, Vector3dc size, Color color) {
        double x = center.x();
        double y = center.y();
        double z = center.z();
        double sx = size.x();
        double sy = size.y();
        double sz = size.z();
        double dx = sx / 2;
        double dy = sy / 2;
        double dz = sz / 2;
        int cx = getParticleCount(sx);
        int cy = getParticleCount(sy);
        int cz = getParticleCount(sz);
        showLine(x - dx, y - dy, z - dz, sx, 0, 0, color, false, cx);
        showLine(x - dx, y - dy, z + dz, sx, 0, 0, color, false, cx);
        showLine(x - dx, y + dy, z - dz, sx, 0, 0, color, false, cx);
        showLine(x - dx, y + dy, z + dz, sx, 0, 0, color, false, cx);
        showLine(x - dx, y - dy, z - dz, 0, sy, 0, color, true, cy);
        showLine(x - dx, y - dy, z + dz, 0, sy, 0, color, true, cy);
        showLine(x + dx, y - dy, z - dz, 0, sy, 0, color, true, cy);
        showLine(x + dx, y - dy, z + dz, 0, sy, 0, color, true, cy);
        showLine(x - dx, y - dy, z - dz, 0, 0, sz, color, false, cz);
        showLine(x + dx, y - dy, z - dz, 0, 0, sz, color, false, cz);
        showLine(x - dx, y + dy, z - dz, 0, 0, sz, color, false, cz);
        showLine(x + dx, y + dy, z - dz, 0, 0, sz, color, false, cz);
    }

    @Deprecated
    public void showAxisAlignedBox(Vector3dc center, Vector3dc size, RGBLike color) {
        showAxisAlignedBox(center, size, Util.toColor(color));
    }

    public boolean isLookingAtPoint(Vector3dc eyes, Vector3dc target, Vector3dc position) {
        Vector3d closestOnEyeRay = Intersectiond.findClosestPointOnLineSegment(
                eyes.x(), eyes.y(), eyes.z(),
                target.x(), target.y(), target.z(),
                position.x(), position.y(), position.z(),
                new Vector3d());
        double threshold = getLookThreshold();
        return position.distanceSquared(closestOnEyeRay) < threshold * threshold;
    }

    public void selectEntity(Entity entity) {
        clearNode();
        rootNode.selectEntity(entity);
    }

    public void openSpawnMenu() {
        player.openInventory(new SpawnMenu(this).getInventory());
    }

    public boolean canSelectEntity(Entity entity) {
        SessionSelectEntityEvent event = new SessionSelectEntityEvent(this, entity);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    private static class ChangeKey<E extends Entity, T> {
        private final E entity;
        private final EntityProperty<E, T> property;

        public ChangeKey(E entity, EntityProperty<E, T> property) {
            this.entity = entity;
            this.property = property;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChangeKey<?, ?> changeKey = (ChangeKey<?, ?>) o;
            return Objects.equals(entity, changeKey.entity) && Objects.equals(property, changeKey.property);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entity, property);
        }
    }
}
