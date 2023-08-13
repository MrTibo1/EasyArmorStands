package me.m56738.easyarmorstands.node.v1_19_4;

import me.m56738.easyarmorstands.EasyArmorStands;
import me.m56738.easyarmorstands.bone.v1_19_4.DisplayBone;
import me.m56738.easyarmorstands.capability.item.ItemType;
import me.m56738.easyarmorstands.element.SimpleEntityElement;
import me.m56738.easyarmorstands.element.SimpleEntityElementType;
import me.m56738.easyarmorstands.menu.Menu;
import me.m56738.easyarmorstands.menu.MenuClick;
import me.m56738.easyarmorstands.menu.builder.SplitMenuBuilder;
import me.m56738.easyarmorstands.menu.slot.ItemPropertySlot;
import me.m56738.easyarmorstands.menu.slot.NodeSlot;
import me.m56738.easyarmorstands.node.Button;
import me.m56738.easyarmorstands.node.ElementNode;
import me.m56738.easyarmorstands.property.Property;
import me.m56738.easyarmorstands.property.PropertyContainer;
import me.m56738.easyarmorstands.property.v1_19_4.display.DisplayHeightProperty;
import me.m56738.easyarmorstands.property.v1_19_4.display.DisplayLeftRotationProperty;
import me.m56738.easyarmorstands.property.v1_19_4.display.DisplayWidthProperty;
import me.m56738.easyarmorstands.property.v1_19_4.display.item.ItemDisplayItemProperty;
import me.m56738.easyarmorstands.session.Session;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class DisplayElement<T extends Display> extends SimpleEntityElement<T> {
    private final T entity;
    private final DisplayRootNodeFactory<T> factory;

    public DisplayElement(T entity, SimpleEntityElementType<T> type, DisplayRootNodeFactory<T> factory) {
        super(entity, type);
        this.entity = entity;
        this.factory = factory;
    }

    @Override
    public boolean hasItemSlots() {
        return entity instanceof ItemDisplay;
    }

    @Override
    public Button createButton(Session session) {
        return new DisplayButton<>(session, entity);
    }

    @Override
    public ElementNode createNode(Session session) {
        PropertyContainer container = PropertyContainer.tracked(this, session.getPlayer());

        DisplayBone bone = new DisplayBone(container, DisplayLeftRotationProperty.TYPE);

        DisplayRootNode localNode = factory.createRootNode(session, Component.text("Local"), this);
        localNode.setRoot(true);
        localNode.addMoveButtons(session, bone, bone, 2);
        localNode.addCarryButtonWithYaw(session, bone);
        localNode.addRotationButtons(session, bone, 1, bone);
        localNode.addScaleButtons(session, bone, 2);

        DisplayRootNode globalNode = factory.createRootNode(session, Component.text("Global"), this);
        globalNode.setRoot(true);
        globalNode.addPositionButtons(session, bone, 3);
        globalNode.addCarryButtonWithYaw(session, bone);
        globalNode.addRotationButtons(session, bone, 1, null);
        globalNode.addYawButton(session, bone, 1.5);

        localNode.setNextNode(globalNode);
        globalNode.setNextNode(localNode);

        return localNode;
    }

    @Override
    protected void populateMenu(Player player, SplitMenuBuilder builder, PropertyContainer properties) {
        super.populateMenu(player, builder, properties);

        Property<ItemStack> property = properties.getOrNull(ItemDisplayItemProperty.TYPE);
        if (property != null) {
            ItemPropertySlot slot = new ItemPropertySlot(property, properties);
            builder.ensureRow(3);
            builder.setSlot(Menu.index(2, 1), slot);
        }

        Session session = EasyArmorStands.getInstance().getSessionManager().getSession(player);
        DisplayRootNode root = null;
        if (session != null) {
            root = session.findNode(DisplayRootNode.class);
        }

        if (root == null || root.getElement().entity != entity) {
            return;
        }

        builder.setSlot(
                Menu.index(3, 8),
                new NodeSlot(
                        session,
                        () -> {
                            Property<Float> widthProperty = properties.getOrNull(DisplayWidthProperty.TYPE);
                            Property<Float> heightProperty = properties.getOrNull(DisplayHeightProperty.TYPE);
                            if (widthProperty != null && widthProperty.getValue() == 0f) {
                                widthProperty.setValue(1f);
                            }
                            if (heightProperty != null && heightProperty.getValue() == 0f) {
                                heightProperty.setValue(1f);
                            }
                            properties.commit();
                            return new DisplayBoxNode(session, properties);
                        },
                        new BoxResetAction(properties),
                        ItemType.STONE,
                        Component.text("bounding box")));
    }

    private static class BoxResetAction implements Consumer<MenuClick> {
        private final PropertyContainer container;
        private final Property<Float> widthProperty;
        private final Property<Float> heightProperty;

        private BoxResetAction(PropertyContainer container) {
            this.container = container;
            this.widthProperty = container.getOrNull(DisplayWidthProperty.TYPE);
            this.heightProperty = container.getOrNull(DisplayHeightProperty.TYPE);
        }

        @Override
        public void accept(MenuClick click) {
            if (widthProperty != null) {
                widthProperty.setValue(0f);
            }
            if (heightProperty != null) {
                heightProperty.setValue(0f);
            }
            container.commit();
        }
    }
}
