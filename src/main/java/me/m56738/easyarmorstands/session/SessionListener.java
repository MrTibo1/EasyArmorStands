package me.m56738.easyarmorstands.session;

import me.m56738.easyarmorstands.EasyArmorStands;
import me.m56738.easyarmorstands.capability.armswing.ArmSwingEvent;
import me.m56738.easyarmorstands.capability.equipment.EquipmentCapability;
import me.m56738.easyarmorstands.inventory.InventoryListener;
import me.m56738.easyarmorstands.util.Util;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class SessionListener implements Listener {
    private final Plugin plugin;
    private final SessionManager manager;
    private final BukkitAudiences adventure;
    private final EquipmentCapability equipmentCapability;

    public SessionListener(Plugin plugin, SessionManager manager, BukkitAudiences adventure) {
        this.plugin = plugin;
        this.manager = manager;
        this.adventure = adventure;
        this.equipmentCapability = EasyArmorStands.getInstance().getCapability(EquipmentCapability.class);
    }

    private boolean isTool(Player player, ItemStack item) {
        return Util.isTool(item) && player.hasPermission("easyarmorstands.edit");
    }

    private boolean startEditing(Player player, ArmorStand armorStand, ItemStack item, boolean cancelled) {
        if (cancelled || !isTool(player, item)) {
            return false;
        }

        Session oldSession = manager.getSession(armorStand);
        if (oldSession != null) {
            adventure.player(player).sendMessage(Component.text()
                    .color(NamedTextColor.RED)
                    .append(Component.text(oldSession.getPlayer().getName() + " is editing this armor stand")));
            return true;
        }

        manager.start(player, armorStand);
        return true;
    }

    public boolean onLeftClick(Player player, ItemStack item) {
        Session session = manager.getSession(player);
        if (session != null) {
            session.handleLeftClick();
            return true;
        }
        return isTool(player, item);
    }

    public boolean onLeftClickArmorStand(Player player, ArmorStand armorStand, ItemStack item, boolean cancelled) {
        Session session = manager.getSession(player);
        if (session != null) {
            session.handleLeftClick();
            return true;
        }
        return startEditing(player, armorStand, item, cancelled);
    }

    public boolean onRightClick(Player player, ItemStack item) {
        Session session = manager.getSession(player);
        if (session != null) {
            session.handleRightClick();
            return true;
        }
        if (!isTool(player, item)) {
            return false;
        }
        if (player.isSneaking() && player.hasPermission("easyarmorstands.spawn")) {
            manager.spawnAndStart(player);
        }
        return true;
    }

    public boolean onRightClickArmorStand(Player player, ArmorStand armorStand, ItemStack item, boolean cancelled) {
        Session session = manager.getSession(player);
        if (session != null) {
            session.handleRightClick();
            return true;
        }
        return startEditing(player, armorStand, item, cancelled);
    }

    public boolean onDrop(Player player, ItemStack item) {
        return manager.stop(player);
    }

    public void onLogin(Player player) {
        manager.hideSkeletons(player);
    }

    public void onJoin(Player player) {
        manager.hideSkeletons(player);
    }

    public void onQuit(Player player) {
        manager.stop(player);
    }

    @EventHandler
    public void onLeftClick(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        if (onLeftClick(event.getPlayer(), event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeftClick(ArmSwingEvent event) {
        Player player = event.getPlayer();
        EntityEquipment equipment = player.getEquipment();
        ItemStack item = equipmentCapability.getItem(equipment, event.getHand());
        if (onLeftClick(player, item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeftClickEntity(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        if (!(attacker instanceof Player)) {
            return;
        }
        Player player = (Player) attacker;
        EntityEquipment equipment = player.getEquipment();
        Entity entity = event.getEntity();
        if (!(entity instanceof ArmorStand)) {
            for (EquipmentSlot hand : equipmentCapability.getHands()) {
                ItemStack item = equipmentCapability.getItem(equipment, hand);
                if (onLeftClick(player, item)) {
                    event.setCancelled(true);
                }
            }
            return;
        }
        ArmorStand armorStand = (ArmorStand) entity;

        for (EquipmentSlot hand : equipmentCapability.getHands()) {
            ItemStack item = equipmentCapability.getItem(equipment, hand);
            if (onLeftClickArmorStand(player, armorStand, item, event.isCancelled())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (onRightClick(event.getPlayer(), event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRightClickEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        EntityEquipment equipment = player.getEquipment();
        Entity entity = event.getRightClicked();
        if (!(entity instanceof ArmorStand)) {
            for (EquipmentSlot hand : equipmentCapability.getHands()) {
                ItemStack item = equipmentCapability.getItem(equipment, hand);
                if (onRightClick(player, item)) {
                    event.setCancelled(true);
                }
            }
            return;
        }
        ArmorStand armorStand = (ArmorStand) entity;

        for (EquipmentSlot hand : equipmentCapability.getHands()) {
            ItemStack item = equipmentCapability.getItem(equipment, hand);
            if (onRightClickArmorStand(player, armorStand, item, event.isCancelled())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onRightClickAtEntity(PlayerInteractAtEntityEvent event) {
        onRightClickEntity(event);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (onDrop(event.getPlayer(), event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        onLogin(event.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        onJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        onQuit(event.getPlayer());
    }

    private InventoryListener getInventoryListener(InventoryEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof InventoryListener) {
            return (InventoryListener) holder;
        } else {
            return null;
        }
    }

    private boolean onInventoryClick(InventoryListener inventoryListener,
                                     int slot, boolean click, boolean put, boolean take, ItemStack cursor) {
        Bukkit.getScheduler().runTask(plugin, inventoryListener::update);
        return !inventoryListener.onClick(slot, click, put, take, cursor);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryListener inventoryListener = getInventoryListener(event);
        if (inventoryListener == null) {
            return;
        }
        InventoryAction action = event.getAction();
        if (action == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
            return;
        }
        int slot = event.getSlot();
        if (slot != event.getRawSlot()) {
            // Not the upper inventory
            if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true);
            }
            return;
        }
        boolean click = false;
        boolean put = false;
        boolean take = false;
        switch (action) {
            case NOTHING:
            case CLONE_STACK:
                click = true;
                break;
            case PICKUP_ALL:
            case PICKUP_SOME:
            case PICKUP_HALF:
            case PICKUP_ONE:
            case MOVE_TO_OTHER_INVENTORY:
                click = true;
                take = true;
                break;
            case PLACE_ALL:
            case PLACE_SOME:
            case PLACE_ONE:
                click = true;
                put = true;
                break;
            case SWAP_WITH_CURSOR:
                click = true;
                put = true;
                take = true;
                break;
            case DROP_ALL_SLOT:
            case DROP_ONE_SLOT:
            case HOTBAR_MOVE_AND_READD:
            case HOTBAR_SWAP:
                take = true;
                break;
            case DROP_ALL_CURSOR:
            case DROP_ONE_CURSOR:
                return;
            default:
                event.setCancelled(true);
                return;
        }
        ItemStack cursor = event.getCursor();
        if (onInventoryClick(inventoryListener, slot, click, put, take, cursor)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryListener inventoryListener = getInventoryListener(event);
        if (inventoryListener == null) {
            return;
        }
        if (event.getRawSlots().size() != 1) {
            event.setCancelled(true);
            return;
        }
        int slot = event.getRawSlots().iterator().next();
        if (slot != event.getView().convertSlot(slot)) {
            return;
        }
        ItemStack cursor = event.getOldCursor();
        if (onInventoryClick(inventoryListener, slot, true, true, false, cursor)) {
            event.setCancelled(true);
        }
    }
}