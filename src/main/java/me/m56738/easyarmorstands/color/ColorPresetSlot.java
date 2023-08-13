package me.m56738.easyarmorstands.color;

import me.m56738.easyarmorstands.EasyArmorStands;
import me.m56738.easyarmorstands.capability.component.ComponentCapability;
import me.m56738.easyarmorstands.capability.item.ItemCapability;
import me.m56738.easyarmorstands.menu.MenuClick;
import me.m56738.easyarmorstands.menu.slot.MenuSlot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Locale;

public class ColorPresetSlot implements MenuSlot {
    private final ColorPickerContext context;
    private final DyeColor color;
    private final String name;

    public ColorPresetSlot(ColorPickerContext context, DyeColor color) {
        this.context = context;
        this.color = color;
        String name = color.name().replace('_', ' ').toLowerCase(Locale.ROOT);
        this.name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    @Override
    public ItemStack getItem() {
        EasyArmorStands plugin = EasyArmorStands.getInstance();
        ItemCapability itemCapability = plugin.getCapability(ItemCapability.class);
        ComponentCapability componentCapability = plugin.getCapability(ComponentCapability.class);
        ItemStack item = itemCapability.createColor(color);
        ItemMeta meta = item.getItemMeta();
        componentCapability.setDisplayName(meta, Component.text(name, TextColor.color(color.getColor().asRGB())));
        componentCapability.setLore(meta, Arrays.asList(
                Component.text()
                        .content("Left click: ")
                        .append(Component.text("Select", NamedTextColor.GOLD))
                        .color(NamedTextColor.GRAY)
                        .build(),
                Component.text()
                        .content("Right click: ")
                        .append(Component.text("Mix", NamedTextColor.GOLD))
                        .color(NamedTextColor.GRAY)
                        .build()
        ));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void onClick(MenuClick click) {
        if (click.isLeftClick()) {
            context.setColor(color.getColor(), click.menu());
        } else if (click.isRightClick()) {
            Color currentColor = context.getColor();
            if (currentColor != null) {
                context.setColor(currentColor.mixDyes(color), click.menu());
            }
        }
    }
}
