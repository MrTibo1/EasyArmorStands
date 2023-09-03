package me.m56738.easyarmorstands.api.property.type;

import me.m56738.easyarmorstands.api.EasyArmorStands;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

@SuppressWarnings("PatternValidation")
public class EntityPropertyTypes {
    public static final PropertyType<Component> CUSTOM_NAME = get("entity/custom_name", Component.class);
    public static final PropertyType<Boolean> CUSTOM_NAME_VISIBLE = get("entity/custom_name/visible", Boolean.class);
    public static final KeyedPropertyType<EquipmentSlot, ItemStack> EQUIPMENT = new EnumKeyedPropertyType<>(EquipmentSlot.class,
            slot -> get("entity/equipment/" + slot.name().toLowerCase(Locale.ROOT), ItemStack.class));
    public static final PropertyType<Boolean> GLOWING = get("entity/glowing", Boolean.class);
    public static final PropertyType<Location> LOCATION = get("entity/location", Location.class);
    public static final PropertyType<Boolean> VISIBLE = get("entity/visible", Boolean.class);

    private EntityPropertyTypes() {
    }

    private static <T> PropertyType<T> get(@KeyPattern.Value String name, Class<T> type) {
        return EasyArmorStands.get().propertyTypeRegistry().get(Key.key("easyarmorstands", name), type);
    }
}