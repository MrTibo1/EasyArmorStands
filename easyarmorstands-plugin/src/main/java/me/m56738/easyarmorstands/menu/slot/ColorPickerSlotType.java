package me.m56738.easyarmorstands.menu.slot;

import me.m56738.easyarmorstands.api.menu.MenuSlotFactory;
import me.m56738.easyarmorstands.api.menu.MenuSlotType;
import me.m56738.easyarmorstands.item.ItemTemplate;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class ColorPickerSlotType implements MenuSlotType {
    public static final Key KEY = Key.key("easyarmorstands", "color_picker");

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @Override
    public @NotNull MenuSlotFactory load(@NotNull ConfigurationNode node) throws SerializationException {
        ConfigurationNode itemNode = node.node("item");
        ConfigurationNode activeItemNode = node.node("active-item");
        activeItemNode.mergeFrom(itemNode);
        return new ColorPickerSlotFactory(
                itemNode.get(ItemTemplate.class),
                activeItemNode.get(ItemTemplate.class));
    }
}
