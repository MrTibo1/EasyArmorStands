package me.m56738.easyarmorstands.display.command;

import me.m56738.easyarmorstands.api.ArmorStandPart;
import me.m56738.easyarmorstands.api.ArmorStandSize;
import me.m56738.easyarmorstands.api.editor.Session;
import me.m56738.easyarmorstands.api.editor.node.ElementSelectionNode;
import me.m56738.easyarmorstands.api.element.Element;
import me.m56738.easyarmorstands.api.property.Property;
import me.m56738.easyarmorstands.api.property.PropertyContainer;
import me.m56738.easyarmorstands.api.property.PropertyMap;
import me.m56738.easyarmorstands.api.property.type.EntityPropertyTypes;
import me.m56738.easyarmorstands.command.SessionCommands;
import me.m56738.easyarmorstands.command.annotation.PropertyPermission;
import me.m56738.easyarmorstands.command.sender.EasPlayer;
import me.m56738.easyarmorstands.context.ChangeContext;
import me.m56738.easyarmorstands.display.DisplayAddon;
import me.m56738.easyarmorstands.display.api.property.type.BlockDisplayPropertyTypes;
import me.m56738.easyarmorstands.display.api.property.type.DisplayPropertyTypes;
import me.m56738.easyarmorstands.display.api.property.type.ItemDisplayPropertyTypes;
import me.m56738.easyarmorstands.display.api.property.type.TextDisplayPropertyTypes;
import me.m56738.easyarmorstands.display.editor.node.DisplayBoxNode;
import me.m56738.easyarmorstands.display.editor.node.DisplayMenuNode;
import me.m56738.easyarmorstands.display.editor.node.DisplayShearNode;
import me.m56738.easyarmorstands.display.element.DisplayElement;
import me.m56738.easyarmorstands.element.ArmorStandElement;
import me.m56738.easyarmorstands.element.SimpleEntityElement;
import me.m56738.easyarmorstands.element.SimpleEntityElementType;
import me.m56738.easyarmorstands.group.Group;
import me.m56738.easyarmorstands.group.node.GroupRootNode;
import me.m56738.easyarmorstands.history.action.Action;
import me.m56738.easyarmorstands.history.action.ElementCreateAction;
import me.m56738.easyarmorstands.history.action.ElementDestroyAction;
import me.m56738.easyarmorstands.message.Message;
import me.m56738.easyarmorstands.permission.Permissions;
import me.m56738.easyarmorstands.property.TrackedPropertyContainer;
import me.m56738.easyarmorstands.session.SessionImpl;
import me.m56738.easyarmorstands.util.ArmorStandPartInfo;
import me.m56738.easyarmorstands.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotation.specifier.Range;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.minecraft.extras.annotation.specifier.Decoder;
import org.joml.Math;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static me.m56738.easyarmorstands.command.SessionCommands.getElementOrError;
import static me.m56738.easyarmorstands.command.SessionCommands.getElementsOrError;
import static me.m56738.easyarmorstands.command.SessionCommands.getPropertiesOrError;
import static me.m56738.easyarmorstands.command.SessionCommands.getSessionOrError;

@Command("eas")
public class DisplayCommands {
    private final DisplayAddon addon;

    public DisplayCommands(DisplayAddon addon) {
        this.addon = addon;
    }

    @Command("block <value>")
    @PropertyPermission("easyarmorstands:block_display/block")
    @CommandDescription("easyarmorstands.command.description.block")
    public void setBlock(EasPlayer sender, @Argument("value") BlockData value) {
        PropertyContainer properties = getPropertiesOrError(sender);
        if (properties == null) {
            return;
        }
        Property<BlockData> property = properties.getOrNull(BlockDisplayPropertyTypes.BLOCK);
        if (property == null) {
            sender.sendMessage(Message.error("easyarmorstands.error.block-unsupported"));
            return;
        }
        if (property.setValue(value)) {
            properties.commit();
            sender.sendMessage(Message.success("easyarmorstands.success.changed-block",
                    property.getType().getValueComponent(value)));
        } else {
            sender.sendMessage(Message.error("easyarmorstands.error.cannot-change"));
        }
    }

    @Command("brightness block <value>")
    @PropertyPermission("easyarmorstands:display/brightness")
    @CommandDescription("easyarmorstands.command.description.brightness.block")
    public void setBlockBrightness(EasPlayer sender, @Argument("value") @Range(min = "0", max = "15") int blockLight) {
        PropertyContainer properties = getPropertiesOrError(sender);
        if (properties == null) {
            return;
        }
        Location location = properties.get(EntityPropertyTypes.LOCATION).getValue();
        Property<Optional<Brightness>> property = properties.getOrNull(DisplayPropertyTypes.BRIGHTNESS);
        if (property == null) {
            sender.sendMessage(Message.error("easyarmorstands.error.brightness-unsupported"));
            return;
        }
        int skyLight = property.getValue()
                .map(Brightness::getSkyLight)
                .orElseGet(() -> (int) location.getBlock().getLightFromSky());
        Optional<Brightness> brightness = Optional.of(new Brightness(blockLight, skyLight));
        if (property.setValue(brightness)) {
            properties.commit();
            sender.sendMessage(Message.success("easyarmorstands.success.changed-brightness",
                    property.getType().getValueComponent(brightness)));
        } else {
            sender.sendMessage(Message.error("easyarmorstands.error.cannot-change"));
        }
    }

    @Command("brightness sky <value>")
    @PropertyPermission("easyarmorstands:display/brightness")
    @CommandDescription("easyarmorstands.command.description.brightness.sky")
    public void setSkyBrightness(EasPlayer sender, @Argument("value") @Range(min = "0", max = "15") int skyLight) {
        PropertyContainer properties = getPropertiesOrError(sender);
        if (properties == null) {
            return;
        }
        Location location = properties.get(EntityPropertyTypes.LOCATION).getValue();
        Property<Optional<Brightness>> property = properties.getOrNull(DisplayPropertyTypes.BRIGHTNESS);
        if (property == null) {
            sender.sendMessage(Message.error("easyarmorstands.error.brightness-unsupported"));
            return;
        }
        int blockLight = property.getValue()
                .map(Brightness::getSkyLight)
                .orElseGet(() -> (int) location.getBlock().getLightFromBlocks());
        Optional<Brightness> brightness = Optional.of(new Brightness(blockLight, skyLight));
        if (property.setValue(brightness)) {
            properties.commit();
            sender.sendMessage(Message.success("easyarmorstands.success.changed-brightness",
                    property.getType().getValueComponent(brightness)));
        } else {
            sender.sendMessage(Message.error("easyarmorstands.error.cannot-change"));
        }
    }

    @Command("brightness here")
    @PropertyPermission("easyarmorstands:display/brightness")
    @CommandDescription("easyarmorstands.command.description.brightness.here")
    public void setLocalBrightness(EasPlayer sender) {
        PropertyContainer properties = getPropertiesOrError(sender);
        if (properties == null) {
            return;
        }
        Property<Optional<Brightness>> property = properties.getOrNull(DisplayPropertyTypes.BRIGHTNESS);
        if (property == null) {
            sender.sendMessage(Message.error("easyarmorstands.error.brightness-unsupported"));
            return;
        }
        Block block = sender.get().getLocation().getBlock();
        Optional<Brightness> brightness = Optional.of(new Brightness(block.getLightFromBlocks(), block.getLightFromSky()));
        if (property.setValue(brightness)) {
            properties.commit();
            sender.sendMessage(Message.success("easyarmorstands.success.changed-brightness",
                    property.getType().getValueComponent(brightness)));
        } else {
            sender.sendMessage(Message.error("easyarmorstands.error.cannot-change"));
        }
    }

    @Command("brightness reset")
    @PropertyPermission("easyarmorstands:display/brightness")
    @CommandDescription("easyarmorstands.command.description.brightness.reset")
    public void setDefaultBrightness(EasPlayer sender) {
        PropertyContainer properties = getPropertiesOrError(sender);
        if (properties == null) {
            return;
        }
        Property<Optional<Brightness>> property = properties.getOrNull(DisplayPropertyTypes.BRIGHTNESS);
        if (property == null) {
            sender.sendMessage(Message.error("easyarmorstands.error.brightness-unsupported"));
            return;
        }
        if (property.setValue(Optional.empty())) {
            properties.commit();
            sender.sendMessage(Message.success("easyarmorstands.success.changed-brightness",
                    property.getType().getValueComponent(Optional.empty())));
        } else {
            sender.sendMessage(Message.error("easyarmorstands.error.cannot-change"));
        }
    }

    @Command("box width <width>")
    @PropertyPermission("easyarmorstands:display/box/width")
    @CommandDescription("easyarmorstands.command.description.box.width")
    public void setBoxWidth(EasPlayer sender, @Argument("width") float value) {
        PropertyContainer properties = getPropertiesOrError(sender);
        if (properties == null) {
            return;
        }
        Property<Float> widthProperty = properties.getOrNull(DisplayPropertyTypes.BOX_WIDTH);
        if (widthProperty == null) {
            sender.sendMessage(Message.error("easyarmorstands.error.box-unsupported"));
            return;
        }
        if (widthProperty.setValue(value)) {
            Property<Float> heightProperty = properties.getOrNull(DisplayPropertyTypes.BOX_HEIGHT);
            if (heightProperty != null && heightProperty.getValue() == 0f) {
                heightProperty.setValue(value);
            }
            properties.commit();
            sender.sendMessage(Message.success("easyarmorstands.success.changed-box-width",
                    widthProperty.getType().getValueComponent(value)));
        } else {
            sender.sendMessage(Message.error("easyarmorstands.error.cannot-change"));
        }
    }

    @Command("box height <height>")
    @PropertyPermission("easyarmorstands:display/box/height")
    @CommandDescription("easyarmorstands.command.description.box.height")
    public void setBoxHeight(EasPlayer sender, @Argument("height") float value) {
        PropertyContainer properties = getPropertiesOrError(sender);
        if (properties == null) {
            return;
        }
        Property<Float> heightProperty = properties.getOrNull(DisplayPropertyTypes.BOX_HEIGHT);
        if (heightProperty == null) {
            sender.sendMessage(Message.error("easyarmorstands.error.box-unsupported"));
            return;
        }
        if (heightProperty.setValue(value)) {
            Property<Float> widthProperty = properties.getOrNull(DisplayPropertyTypes.BOX_WIDTH);
            if (widthProperty != null && widthProperty.getValue() == 0f) {
                widthProperty.setValue(value);
            }
            properties.commit();
            sender.sendMessage(Message.success("easyarmorstands.success.changed-box-height",
                    heightProperty.getType().getValueComponent(value)));
        } else {
            sender.sendMessage(Message.error("easyarmorstands.error.cannot-change"));
        }
    }

    @Command("box remove")
    @PropertyPermission("easyarmorstands:display/box/height")
    @CommandDescription("easyarmorstands.command.description.box.remove")
    public void removeBox(EasPlayer sender) {
        PropertyContainer properties = getPropertiesOrError(sender);
        if (properties == null) {
            return;
        }

        int success = 0;
        Property<Float> widthProperty = properties.getOrNull(DisplayPropertyTypes.BOX_WIDTH);
        Property<Float> heightProperty = properties.getOrNull(DisplayPropertyTypes.BOX_HEIGHT);
        if (widthProperty == null && heightProperty == null) {
            sender.sendMessage(Message.error("easyarmorstands.error.box-unsupported"));
            return;
        }

        if (widthProperty != null && widthProperty.setValue(0f)) {
            success++;
        }
        if (heightProperty != null && heightProperty.setValue(0f)) {
            success++;
        }

        if (success > 0) {
            properties.commit();
            sender.sendMessage(Message.success("easyarmorstands.success.removed-box"));
        } else {
            sender.sendMessage(Message.error("easyarmorstands.error.cannot-change"));
        }
    }

    @Command("box")
    @PropertyPermission("easyarmorstands:display/translation")
    @CommandDescription("easyarmorstands.command.description.box")
    public void moveBox(EasPlayer sender) {
        Session session = getSessionOrError(sender);
        Element element = getElementOrError(sender, session);
        if (element == null) {
            return;
        }
        if (!(element instanceof DisplayElement<?>)) {
            sender.sendMessage(Message.error("easyarmorstands.error.box-unsupported"));
            return;
        }
        PropertyContainer properties = new TrackedPropertyContainer(element, sender);
        session.pushNode(new DisplayBoxNode(session, properties));
    }

    @Command("text")
    @PropertyPermission("easyarmorstands:text_display/text")
    @CommandDescription("easyarmorstands.command.description.text")
    public void showText(EasPlayer sender) {
        Element element = getElementOrError(sender);
        if (element == null) {
            return;
        }
        Property<Component> property = element.getProperties().getOrNull(TextDisplayPropertyTypes.TEXT);
        if (property == null) {
            sender.sendMessage(Message.error("easyarmorstands.error.text-unsupported"));
            return;
        }
        Component text = property.getValue();
        SessionCommands.showText(sender, TextDisplayPropertyTypes.TEXT.getName(), text, "/eas text set");
    }

    @Command("text set <value>")
    @PropertyPermission("easyarmorstands:text_display/text")
    @CommandDescription("easyarmorstands.command.description.text.set")
    public void setText(EasPlayer sender, @Argument("value") @Decoder.MiniMessage @Greedy Component value) {
        PropertyContainer properties = getPropertiesOrError(sender);
        if (properties == null) {
            return;
        }
        Property<Component> property = properties.getOrNull(TextDisplayPropertyTypes.TEXT);
        if (property == null) {
            sender.sendMessage(Message.error("easyarmorstands.error.text-unsupported"));
            return;
        }
        if (property.setValue(value)) {
            properties.commit();
            sender.sendMessage(Message.success("easyarmorstands.success.changed-text",
                    property.getType().getValueComponent(value)));
        } else {
            sender.sendMessage(Message.error("easyarmorstands.error.cannot-change"));
        }
    }

    @Command("text width <value>")
    @PropertyPermission("easyarmorstands:text_display/line_width")
    @CommandDescription("easyarmorstands.command.description.text.width")
    public void setTextWidth(EasPlayer sender, @Argument("value") int value) {
        PropertyContainer properties = getPropertiesOrError(sender);
        if (properties == null) {
            return;
        }
        Property<Integer> property = properties.getOrNull(TextDisplayPropertyTypes.LINE_WIDTH);
        if (property == null) {
            sender.sendMessage(Message.error("easyarmorstands.error.text-line-width-unsupported"));
            return;
        }
        if (property.setValue(value)) {
            properties.commit();
            sender.sendMessage(Message.success("easyarmorstands.success.changed-text-line-width",
                    property.getType().getValueComponent(value)));
        } else {
            sender.sendMessage(Message.error("easyarmorstands.error.cannot-change"));
        }
    }

    @Command("text background color <value>")
    @PropertyPermission("easyarmorstands:text_display/background")
    @CommandDescription("easyarmorstands.command.description.text.background.color")
    public void setTextBackground(EasPlayer sender, @Argument("value") TextColor color) {
        PropertyContainer properties = getPropertiesOrError(sender);
        if (properties == null) {
            return;
        }
        Property<Optional<Color>> property = properties.getOrNull(TextDisplayPropertyTypes.BACKGROUND);
        if (property == null) {
            sender.sendMessage(Message.error("easyarmorstands.error.text-background-unsupported"));
            return;
        }
        Optional<Color> value = Optional.of(Color.fromRGB(color.value()));
        if (property.setValue(value)) {
            properties.commit();
            sender.sendMessage(Message.success("easyarmorstands.success.changed-text-background",
                    property.getType().getValueComponent(value)));
        } else {
            sender.sendMessage(Message.error("easyarmorstands.error.cannot-change"));
        }
    }

    @Command("text background reset")
    @PropertyPermission("easyarmorstands:text_display/background")
    @CommandDescription("easyarmorstands.command.description.text.background.reset")
    public void resetTextBackground(EasPlayer sender) {
        PropertyContainer properties = getPropertiesOrError(sender);
        if (properties == null) {
            return;
        }
        Property<Optional<Color>> property = properties.getOrNull(TextDisplayPropertyTypes.BACKGROUND);
        if (property == null) {
            sender.sendMessage(Message.error("easyarmorstands.error.text-background-unsupported"));
            return;
        }
        if (property.setValue(Optional.empty())) {
            properties.commit();
            sender.sendMessage(Message.success("easyarmorstands.success.changed-text-background",
                    property.getType().getValueComponent(Optional.empty())));
        } else {
            sender.sendMessage(Message.error("easyarmorstands.error.cannot-change"));
        }
    }

    @Command("text background none")
    @PropertyPermission("easyarmorstands:text_display/background")
    @CommandDescription("easyarmorstands.command.description.text.background.none")
    public void hideTextBackground(EasPlayer sender) {
        PropertyContainer properties = getPropertiesOrError(sender);
        if (properties == null) {
            return;
        }
        Property<Optional<Color>> property = properties.getOrNull(TextDisplayPropertyTypes.BACKGROUND);
        if (property == null) {
            sender.sendMessage(Message.error("easyarmorstands.error.text-background-unsupported"));
            return;
        }
        Optional<Color> value = Optional.of(Color.fromARGB(0));
        if (property.setValue(value)) {
            properties.commit();
            sender.sendMessage(Message.success("easyarmorstands.success.changed-text-background",
                    property.getType().getValueComponent(value)));
        } else {
            sender.sendMessage(Message.error("easyarmorstands.error.cannot-change"));
        }
    }

    @Command("text background alpha <value>")
    @PropertyPermission("easyarmorstands:text_display/background")
    @CommandDescription("easyarmorstands.command.description.text.background.alpha")
    public void hideTextBackground(EasPlayer sender, @Argument("value") @Range(min = "0", max = "255") int alpha) {
        PropertyContainer properties = getPropertiesOrError(sender);
        if (properties == null) {
            return;
        }
        Property<Optional<Color>> property = properties.getOrNull(TextDisplayPropertyTypes.BACKGROUND);
        if (property == null) {
            sender.sendMessage(Message.error("easyarmorstands.error.text-background-unsupported"));
            return;
        }
        Optional<Color> oldValue = property.getValue();
        if (oldValue.isEmpty()) {
            sender.sendMessage(Message.error("easyarmorstands.error.cannot-change"));
            return;
        }

        Optional<Color> value = oldValue.map(v -> v.setAlpha(alpha));
        if (property.setValue(value)) {
            properties.commit();
            sender.sendMessage(Message.success("easyarmorstands.success.changed-text-background-alpha",
                    Component.text(alpha)));
        } else {
            sender.sendMessage(Message.error("easyarmorstands.error.cannot-change"));
        }
    }

    @Command("scale <scale>")
    @PropertyPermission("easyarmorstands:display/scale")
    @CommandDescription("easyarmorstands.command.description.scale")
    public void editScale(EasPlayer sender, @Argument("scale") float scale) {
        PropertyContainer properties = getPropertiesOrError(sender);
        if (properties == null) {
            return;
        }
        Property<Vector3fc> property = properties.getOrNull(DisplayPropertyTypes.SCALE);
        if (property == null) {
            sender.sendMessage(Message.error("easyarmorstands.error.scale-unsupported"));
            return;
        }
        Vector3f value = new Vector3f(scale);
        if (property.setValue(value)) {
            properties.commit();
            sender.sendMessage(Message.success("easyarmorstands.success.changed-scale",
                    property.getType().getValueComponent(value)));
        } else {
            sender.sendMessage(Message.error("easyarmorstands.error.cannot-change"));
        }
    }

    @Command("shear")
    @PropertyPermission("easyarmorstands:display/right_rotation")
    @CommandDescription("easyarmorstands.command.description.shear")
    public void editRightRotation(EasPlayer sender) {
        Session session = getSessionOrError(sender);
        Element element = getElementOrError(sender, session);
        if (element == null) {
            return;
        }
        if (!(element instanceof DisplayElement<?>)) {
            sender.sendMessage(Message.error("easyarmorstands.error.shearing-unsupported"));
            return;
        }
        PropertyContainer properties = new TrackedPropertyContainer(element, sender);
        DisplayMenuNode node = new DisplayShearNode(session, properties, (DisplayElement<?>) element);
        session.pushNode(node);
    }

    @Command("convert")
    @Permission(Permissions.CONVERT)
    @CommandDescription("easyarmorstands.command.description.convert")
    public void convert(EasPlayer sender) {
        SessionImpl session = getSessionOrError(sender);
        Collection<Element> elements = getElementsOrError(sender, session);
        if (elements == null) {
            return;
        }

        List<SimpleEntityElement<ItemDisplay>> createdElements = new ArrayList<>();
        List<Action> allActions = new ArrayList<>();
        boolean foundArmorStand = false;
        int count = 0;
        boolean isInverted = Bukkit.getBukkitVersion().equals("1.19.4-R0.1-SNAPSHOT");
        for (Element element : elements) {
            if (!(element instanceof ArmorStandElement)) {
                continue;
            }
            foundArmorStand = true;
            ArmorStand entity = ((ArmorStandElement) element).getEntity();
            EntityEquipment equipment = entity.getEquipment();
            if (equipment == null) {
                continue;
            }

            Matrix4d headMatrix = new Matrix4d();
            Matrix4d rightMatrix = new Matrix4d();
            Matrix4d leftMatrix = new Matrix4d();

            if (entity.isSmall()) {
                headMatrix.scale(0.7);
                rightMatrix.scale(0.5);
                leftMatrix.scale(0.5);
            }

            if (isSkull(equipment.getHelmet())) {
                headMatrix.scale(1.1875);
                headMatrix.translate(0, 0.5, 0);
                headMatrix.rotateY(Math.PI);
            } else {
                headMatrix.translate(0, 0.25, 0);
                headMatrix.scale(0.625);
            }

            rightMatrix.translate(-0.0625, -0.625, 0.125);
            rightMatrix.rotateX(Math.PI / 2);

            leftMatrix.translate(0.0625, -0.625, 0.125);
            leftMatrix.rotateX(Math.PI / 2);

            if (isInverted) {
                headMatrix.rotateY(Math.PI);
                rightMatrix.rotateY(Math.PI);
                leftMatrix.rotateY(Math.PI);
            }

            List<Action> actions = new ArrayList<>();
            convert(sender, entity, equipment.getHelmet(), ArmorStandPart.HEAD, ItemDisplay.ItemDisplayTransform.HEAD, headMatrix, actions, createdElements);
            convert(sender, entity, equipment.getItemInMainHand(), ArmorStandPart.RIGHT_ARM, ItemDisplay.ItemDisplayTransform.THIRDPERSON_RIGHTHAND, rightMatrix, actions, createdElements);
            convert(sender, entity, equipment.getItemInOffHand(), ArmorStandPart.LEFT_ARM, ItemDisplay.ItemDisplayTransform.THIRDPERSON_LEFTHAND, leftMatrix, actions, createdElements);
            if (!actions.isEmpty()) {
                actions.add(new ElementDestroyAction(element));
                entity.remove();
                count++;
            }
            allActions.addAll(actions);
        }

        session.context().history().push(allActions, Message.component("easyarmorstands.history.converted-armor-stand"));

        if (!foundArmorStand) {
            // None of the elements are armor stands
            sender.sendMessage(Message.error("easyarmorstands.error.convert-unsupported"));
        } else if (count == 0) {
            // Nothing happened
            sender.sendMessage(Message.error("easyarmorstands.error.cannot-convert"));
        } else if (count == 1) {
            sender.sendMessage(Message.success("easyarmorstands.success.armor-stand-converted"));
        } else {
            sender.sendMessage(Message.success("easyarmorstands.success.armor-stand-converted.multiple", Component.text(count)));
        }

        GroupRootNode groupRootNode = session.findNode(GroupRootNode.class);
        if (groupRootNode != null) {
            // Add created entities to the selected group
            Group group = groupRootNode.getGroup();
            session.returnToNode(groupRootNode);
            ChangeContext context = new EasPlayer(session.player());
            for (SimpleEntityElement<ItemDisplay> element : createdElements) {
                if (context.canEditElement(element)) {
                    group.addMember(element);
                }
            }
        } else {
            // Select the created elements
            ElementSelectionNode selectionNode = session.findNode(ElementSelectionNode.class);
            if (selectionNode != null) {
                selectionNode.selectElements(createdElements);
            }
        }
    }

    private boolean isSkull(ItemStack item) {
        return item != null && item.getItemMeta() instanceof SkullMeta;
    }

    private void convert(ChangeContext context, ArmorStand entity, ItemStack item, ArmorStandPart part, ItemDisplay.ItemDisplayTransform itemTransform, Matrix4dc matrix, List<Action> actions, List<SimpleEntityElement<ItemDisplay>> elements) {
        if (item == null || item.getType().isAir()) {
            return;
        }

        ArmorStandPartInfo info = ArmorStandPartInfo.of(part);
        Location location = entity.getLocation();
        Vector3d offset = info.getOffset(ArmorStandSize.get(entity), 1).rotateY(Util.getRoundedYawAngle(location.getYaw()), new Vector3d());
        location.add(offset.x, offset.y, offset.z);

        EulerAngle angle = part.getPose(entity);
        Matrix4d transform = new Matrix4d()
                .rotateY(Util.getRoundedYawAngle(location.getYaw()))
                .rotateZYX(-angle.getZ(), -angle.getY(), angle.getX())
                .mul(matrix);

        location.setYaw(0);
        location.setPitch(0);

        PropertyMap properties = new PropertyMap();
        properties.put(EntityPropertyTypes.LOCATION, location);
        properties.put(ItemDisplayPropertyTypes.ITEM, item);
        properties.put(ItemDisplayPropertyTypes.TRANSFORM, itemTransform);
        properties.put(DisplayPropertyTypes.TRANSLATION, transform.getTranslation(new Vector3d()).get(new Vector3f()));
        properties.put(DisplayPropertyTypes.LEFT_ROTATION, transform.getUnnormalizedRotation(new Quaternionf()));
        properties.put(DisplayPropertyTypes.SCALE, transform.getScale(new Vector3d()).get(new Vector3f()));

        SimpleEntityElementType<ItemDisplay> type = addon.getItemDisplayType();

        if (!context.canCreateElement(type, properties)) {
            return;
        }

        SimpleEntityElement<ItemDisplay> element = type.createElement(properties);
        if (element == null) {
            return;
        }

        actions.add(new ElementCreateAction(element));
        elements.add(element);
    }
}
