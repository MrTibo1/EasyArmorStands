package me.m56738.easyarmorstands.capability.item.v1_13;

import me.m56738.easyarmorstands.capability.CapabilityProvider;
import me.m56738.easyarmorstands.capability.Priority;
import me.m56738.easyarmorstands.capability.item.ItemCapability;
import me.m56738.easyarmorstands.capability.item.ItemType;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class ItemCapabilityProvider implements CapabilityProvider<ItemCapability> {
    @Override
    public boolean isSupported() {
        try {
            Material.valueOf("LIGHT_BLUE_STAINED_GLASS_PANE");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    @Override
    public ItemCapability create(Plugin plugin) {
        return new ItemCapabilityImpl();
    }

    private static class ItemCapabilityImpl implements ItemCapability {
        @Override
        public ItemStack createItem(ItemType type) {
            switch (type) {
                case ARMOR_STAND:
                    return new ItemStack(Material.ARMOR_STAND);
                case BLAZE_ROD:
                    return new ItemStack(Material.BLAZE_ROD);
                case BUCKET:
                    return new ItemStack(Material.BUCKET);
                case FEATHER:
                    return new ItemStack(Material.FEATHER);
                case GLOWSTONE_DUST:
                    return new ItemStack(Material.GLOWSTONE_DUST);
                case IRON_BARS:
                    return new ItemStack(Material.IRON_BARS);
                case LEATHER_CHESTPLATE: {
                    ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
                    ItemMeta meta = item.getItemMeta();
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    item.setItemMeta(meta);
                    return item;
                }
                case LEVER:
                    return new ItemStack(Material.LEVER);
                case LIGHT_BLUE_STAINED_GLASS_PANE:
                    return new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
                case NAME_TAG:
                    return new ItemStack(Material.NAME_TAG);
                case PLAYER_HEAD:
                    return new ItemStack(Material.PLAYER_HEAD);
                case STONE:
                    return new ItemStack(Material.STONE);
                case STONE_SLAB:
                    return new ItemStack(Material.STONE_SLAB);
                case TNT:
                    return new ItemStack(Material.TNT);
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public ItemStack createColor(DyeColor color) {
            switch (color) {
                case WHITE:
                    return new ItemStack(Material.WHITE_CONCRETE);
                case ORANGE:
                    return new ItemStack(Material.ORANGE_CONCRETE);
                case MAGENTA:
                    return new ItemStack(Material.MAGENTA_CONCRETE);
                case LIGHT_BLUE:
                    return new ItemStack(Material.LIGHT_BLUE_CONCRETE);
                case YELLOW:
                    return new ItemStack(Material.YELLOW_CONCRETE);
                case LIME:
                    return new ItemStack(Material.LIME_CONCRETE);
                case PINK:
                    return new ItemStack(Material.PINK_CONCRETE);
                case GRAY:
                    return new ItemStack(Material.GRAY_CONCRETE);
                case LIGHT_GRAY:
                    return new ItemStack(Material.LIGHT_GRAY_CONCRETE);
                case CYAN:
                    return new ItemStack(Material.CYAN_CONCRETE);
                case PURPLE:
                    return new ItemStack(Material.PURPLE_CONCRETE);
                case BLUE:
                    return new ItemStack(Material.BLUE_CONCRETE);
                case BROWN:
                    return new ItemStack(Material.BROWN_CONCRETE);
                case GREEN:
                    return new ItemStack(Material.GREEN_CONCRETE);
                case RED:
                    return new ItemStack(Material.RED_CONCRETE);
                case BLACK:
                    return new ItemStack(Material.BLACK_CONCRETE);
                default:
                    throw new IllegalArgumentException();
            }
        }
    }
}
