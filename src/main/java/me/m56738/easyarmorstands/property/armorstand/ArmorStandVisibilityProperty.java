package me.m56738.easyarmorstands.property.armorstand;

import me.m56738.easyarmorstands.capability.item.ItemType;
import me.m56738.easyarmorstands.property.BooleanEntityProperty;
import me.m56738.easyarmorstands.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ArmorStandVisibilityProperty extends BooleanEntityProperty<ArmorStand> {
    @Override
    public Boolean getValue(ArmorStand entity) {
        return entity.isVisible();
    }

    @Override
    public void setValue(ArmorStand entity, Boolean value) {
        entity.setVisible(value);
    }

    @Override
    public @NotNull String getName() {
        return "visibility";
    }

    @Override
    public @NotNull Class<ArmorStand> getEntityType() {
        return ArmorStand.class;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.text("visibility");
    }

    @Override
    public @NotNull Component getValueName(Boolean value) {
        return value
                ? Component.text("visible", NamedTextColor.GREEN)
                : Component.text("invisible", NamedTextColor.RED);
    }

    @Override
    public String getPermission() {
        return "easyarmorstands.property.visible";
    }

    @Override
    public ItemStack createToggleButton(ArmorStand entity) {
        return Util.createItem(
                ItemType.INVISIBILITY_POTION,
                Component.text("Toggle visibility", NamedTextColor.BLUE),
                Arrays.asList(
                        Component.text("Currently ", NamedTextColor.GRAY)
                                .append(getValueName(getValue(entity)))
                                .append(Component.text(".")),
                        Component.text("Changes whether the", NamedTextColor.GRAY),
                        Component.text("armor stand is visible.", NamedTextColor.GRAY)
                )
        );
    }
}
