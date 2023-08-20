package me.m56738.easyarmorstands.session;

import me.m56738.easyarmorstands.EasyArmorStands;
import me.m56738.easyarmorstands.capability.item.ItemType;
import me.m56738.easyarmorstands.command.sender.EasPlayer;
import me.m56738.easyarmorstands.element.ArmorStandElementType;
import me.m56738.easyarmorstands.element.Element;
import me.m56738.easyarmorstands.event.SpawnMenuInitializeEvent;
import me.m56738.easyarmorstands.menu.FakeLeftClick;
import me.m56738.easyarmorstands.menu.Menu;
import me.m56738.easyarmorstands.menu.builder.SimpleMenuBuilder;
import me.m56738.easyarmorstands.menu.slot.SpawnSlot;
import me.m56738.easyarmorstands.node.ClickContext;
import me.m56738.easyarmorstands.node.ElementNode;
import me.m56738.easyarmorstands.node.Node;
import me.m56738.easyarmorstands.particle.Particle;
import me.m56738.easyarmorstands.util.Util;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.joml.Intersectiond;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class Session {
    public static final double DEFAULT_SNAP_INCREMENT = 1.0 / 32;
    public static final double DEFAULT_ANGLE_SNAP_INCREMENT = 360.0 / 256;
    private static final Title.Times titleTimes = Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ofSeconds(1));
    private final LinkedList<Node> nodeStack = new LinkedList<>();
    private final EasPlayer player;
    private final Set<Particle> particles = new HashSet<>();
    private int clickTicks = 5;
    private double snapIncrement = DEFAULT_SNAP_INCREMENT;
    private double angleSnapIncrement = DEFAULT_ANGLE_SNAP_INCREMENT;
    private boolean valid = true;
    private Component currentTitle = Component.empty();
    private Component currentSubtitle = Component.empty();
    private Component currentActionBar = Component.empty();
    private Component pendingTitle = Component.empty();
    private Component pendingSubtitle = Component.empty();
    private Component pendingActionBar = Component.empty();
    private int overlayTicks;

    public Session(EasPlayer player) {
        this.player = player;
    }

    public static void openSpawnMenu(EasPlayer player) {
        Locale locale = player.pointers().getOrDefault(Identity.LOCALE, Locale.US);
        SimpleMenuBuilder builder = new SimpleMenuBuilder();
        if (player.permissions().test("easyarmorstands.spawn.armorstand")) {
            ArmorStandElementType type = new ArmorStandElementType();
            builder.addButton(new SpawnSlot(type, Util.createItem(ItemType.ARMOR_STAND, type.getDisplayName(), locale)));
        }
        Bukkit.getPluginManager().callEvent(new SpawnMenuInitializeEvent(player.get(), locale, builder));
        int size = builder.getSize();
        if (size == 0) {
            return;
        }
        Component title = MiniMessage.miniMessage().deserialize(EasyArmorStands.getInstance().getConfig().getString("menu.spawn.title"));
        Menu menu = builder.build(title, locale);
        if (size == 1) {
            // Only one button, click it immediately
            menu.getSlot(0).onClick(new FakeLeftClick(menu, 0, player.get()));
        } else {
            player.get().openInventory(menu.getInventory());
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
        if (!valid) {
            return;
        }
        if (!nodeStack.isEmpty()) {
            nodeStack.peek().onExit();
        }
        nodeStack.push(node);
        node.onAdd();
        node.onEnter();
    }

    public void replaceNode(@NotNull Node node) {
        if (!valid) {
            return;
        }
        Node removed = nodeStack.pop();
        removed.onExit();
        removed.onRemove();
        nodeStack.push(node);
        node.onAdd();
        node.onEnter();
    }

    public void popNode() {
        if (!valid) {
            return;
        }
        Node removed = nodeStack.pop();
        removed.onExit();
        removed.onRemove();
        if (!nodeStack.isEmpty()) {
            nodeStack.peek().onEnter();
        }
    }

    public void clearNode() {
        if (!valid) {
            return;
        }
        if (!nodeStack.isEmpty()) {
            nodeStack.peek().onExit();
        }
        for (Node node : nodeStack) {
            node.onRemove();
        }
        nodeStack.clear();
    }

    public boolean handleClick(ClickContext context) {
        if (!valid) {
            return false;
        }
        Node node = nodeStack.peek();
        if (node == null || clickTicks > 0) {
            return false;
        }
        clickTicks = 5;
        Vector3dc eyes = player.eyePosition();
        Vector3dc target = eyes.fma(getRange(), player.eyeDirection(), new Vector3d());
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

    boolean update() {
        pendingTitle = Component.empty();
        pendingSubtitle = Component.empty();
        pendingActionBar = Component.empty();

        if (clickTicks > 0) {
            clickTicks--;
        }

        while (!nodeStack.isEmpty() && !nodeStack.peek().isValid()) {
            popNode();
        }

        Node currentNode = nodeStack.peek();
        if (currentNode != null) {
            Vector3dc eyes = player.eyePosition();
            Vector3dc target = eyes.fma(getRange(), player.eyeDirection(), new Vector3d());
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

        updateOverlay();

        return player.isValid();
    }

    private void updateOverlay() {
        // Resend everything once per second
        // Send changes immediately

        boolean resendOverlay = overlayTicks >= 20;
        if (resendOverlay) {
            overlayTicks = 0;
            player.sendTitlePart(TitlePart.TIMES, titleTimes);
        }
        overlayTicks++;

        if (resendOverlay || !Objects.equals(currentTitle, pendingTitle) || !Objects.equals(currentSubtitle, pendingSubtitle)) {
            currentTitle = pendingTitle;
            currentSubtitle = pendingSubtitle;
            player.sendTitlePart(TitlePart.SUBTITLE, currentSubtitle);
            player.sendTitlePart(TitlePart.TITLE, currentTitle);
        }

        if (resendOverlay || !Objects.equals(currentActionBar, pendingActionBar)) {
            currentActionBar = pendingActionBar;
            player.sendActionBar(currentActionBar);
        }
    }

    void stop() {
        Node currentNode = nodeStack.peek();
        if (currentNode != null) {
            currentNode.onExit();
        }
        for (Node node : nodeStack) {
            node.onRemove();
        }
        nodeStack.clear();
        player.clearTitle();
        player.sendActionBar(Component.empty());
        for (Particle particle : particles) {
            particle.hide(player.get());
        }
        particles.clear();
        valid = false;
    }

    public EasPlayer getPlayer() {
        return player;
    }

    public World getWorld() {
        return player.get().getWorld();
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

    public void addParticle(Particle particle) {
        if (!valid) {
            return;
        }
        if (particles.add(particle)) {
            particle.show(player.get());
        }
    }

    public void removeParticle(Particle particle) {
        if (!valid) {
            return;
        }
        if (particles.remove(particle)) {
            particle.hide(player.get());
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

    public void setTitle(ComponentLike title) {
        pendingTitle = title.asComponent();
    }

    public void setSubtitle(ComponentLike subtitle) {
        pendingSubtitle = subtitle.asComponent();
    }

    public void setActionBar(ComponentLike actionBar) {
        pendingActionBar = actionBar.asComponent();
    }
}
