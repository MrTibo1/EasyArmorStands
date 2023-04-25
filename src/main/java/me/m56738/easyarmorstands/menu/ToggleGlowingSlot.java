package me.m56738.easyarmorstands.menu;

import me.m56738.easyarmorstands.capability.glow.GlowCapability;
import me.m56738.easyarmorstands.capability.item.ItemType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.ArmorStand;

import java.util.Arrays;

public class ToggleGlowingSlot extends ToggleSlot {
    private final ArmorStandMenu menu;
    private final GlowCapability glowCapability;

    public ToggleGlowingSlot(ArmorStandMenu menu, GlowCapability glowCapability) {
        super(
                menu,
                ItemType.GLOWSTONE_DUST,
                Component.text("Toggle glowing", NamedTextColor.BLUE), Arrays.asList(
                        Component.text("Changes whether the", NamedTextColor.GRAY),
                        Component.text("armor stand has an", NamedTextColor.GRAY),
                        Component.text("outline.", NamedTextColor.GRAY)
                )
        );
        this.menu = menu;
        this.glowCapability = glowCapability;
    }

    @Override
    protected Component getValue() {
        return glowCapability.isGlowing(menu.getEntity())
                ? Component.text("glowing", NamedTextColor.GREEN)
                : Component.text("not glowing", NamedTextColor.RED);
    }

    @Override
    protected void onClick() {
        ArmorStand entity = menu.getEntity();
        glowCapability.setGlowing(entity, !glowCapability.isGlowing(entity));
    }
}
