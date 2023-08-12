package me.m56738.easyarmorstands.session;

import me.m56738.easyarmorstands.EasyArmorStands;
import me.m56738.easyarmorstands.capability.equipment.EquipmentCapability;
import me.m56738.easyarmorstands.capability.item.ItemType;
import me.m56738.easyarmorstands.element.ArmorStandElementType;
import me.m56738.easyarmorstands.element.Element;
import me.m56738.easyarmorstands.element.EntityElement;
import me.m56738.easyarmorstands.event.SpawnMenuInitializeEvent;
import me.m56738.easyarmorstands.menu.MenuClick;
import me.m56738.easyarmorstands.menu.builder.SimpleMenuBuilder;
import me.m56738.easyarmorstands.menu.slot.SpawnSlot;
import me.m56738.easyarmorstands.node.ClickContext;
import me.m56738.easyarmorstands.node.ElementNode;
import me.m56738.easyarmorstands.node.Node;
import me.m56738.easyarmorstands.particle.Particle;
import me.m56738.easyarmorstands.util.Util;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.joml.Intersectiond;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class Session implements ForwardingAudience.Single {
    public static final double DEFAULT_SNAP_INCREMENT = 1.0 / 32;
    public static final double DEFAULT_ANGLE_SNAP_INCREMENT = 360.0 / 256;
    private final LinkedList<Node> nodeStack = new LinkedList<>();
    private final Player player;
    private final Audience audience;
    private final Set<Particle> particles = new HashSet<>();
    private int clickTicks = 5;
    private double snapIncrement = DEFAULT_SNAP_INCREMENT;
    private double angleSnapIncrement = DEFAULT_ANGLE_SNAP_INCREMENT;
    private boolean valid = true;

    public Session(Player player) {
        this.player = player;
        this.audience = EasyArmorStands.getInstance().getAdventure().player(player);
    }

    public static void openSpawnMenu(Player player) {
        SimpleMenuBuilder builder = new SimpleMenuBuilder();
        if (player.hasPermission("easyarmorstands.spawn.armorstand")) {
            ArmorStandElementType type = new ArmorStandElementType();
            builder.addButton(new SpawnSlot(type, Util.createItem(ItemType.ARMOR_STAND, type.getDisplayName())));
        }
        Bukkit.getPluginManager().callEvent(new SpawnMenuInitializeEvent(player, builder));
        int size = builder.getSize();
        if (size == 0) {
            return;
        }
        if (size == 1) {
            // Only one button, click it immediately
            builder.getSlot(0).onClick(new MenuClick.FakeLeftClick(0, player));
        } else {
            player.openInventory(builder.build(Component.text("Spawn")).getInventory());
        }
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
            node.onRemove();
        }
        nodeStack.clear();
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

    @Deprecated
    public Entity getEntity() {
        Element element = getElement();
        if (element instanceof EntityElement<?>) {
            return ((EntityElement<?>) element).getEntity();
        }
        return null;
    }

    public Element getElement() {
        for (Node node : nodeStack) {
            if (node instanceof ElementNode) {
                Element element = ((ElementNode) node).getElement();
                if (element != null) {
                    return element;
                }
            }
        }
        return null;
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
        valid = false;
    }

    public Player getPlayer() {
        return player;
    }

    public World getWorld() {
        return player.getWorld();
    }

    public double getRange() {
        return 10;
    }

    public double getLookThreshold() {
        return 0.1;
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

    public boolean isLookingAtPoint(Vector3dc eyes, Vector3dc target, Vector3dc position) {
        Vector3d closestOnEyeRay = Intersectiond.findClosestPointOnLineSegment(
                eyes.x(), eyes.y(), eyes.z(),
                target.x(), target.y(), target.z(),
                position.x(), position.y(), position.z(),
                new Vector3d());
        double threshold = getLookThreshold();
        return position.distanceSquared(closestOnEyeRay) < threshold * threshold;
    }

    public boolean isValid() {
        return valid;
    }
}
