package gg.bundlegroup.easyarmorstands.bukkit.addon.plotsquared.v6;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import gg.bundlegroup.easyarmorstands.bukkit.event.SessionMoveEvent;
import gg.bundlegroup.easyarmorstands.bukkit.event.SessionStartEvent;
import gg.bundlegroup.easyarmorstands.common.session.Session;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.joml.Vector3dc;

import java.util.Map;
import java.util.WeakHashMap;

public class PlotSquaredListener implements Listener {
    private final PlotAPI api;
    private final Map<Session, Boolean> bypassCache = new WeakHashMap<>();
    private final String bypassPermission = "easyarmorstands.plotsquared.bypass";

    public PlotSquaredListener(PlotAPI api) {
        this.api = api;
    }

    private boolean isAllowed(Player player, Location location) {
        PlotArea area = api.getPlotSquared().getPlotAreaManager().getPlotArea(location);
        if (area != null) {
            Plot plot = area.getPlot(location);
            if (plot != null) {
                return plot.isAdded(BukkitUtil.adapt(player).getUUID());
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStartSession(SessionStartEvent event) {
        ArmorStand armorStand = event.getArmorStand();
        Location location = BukkitUtil.adapt(armorStand.getLocation());
        if (isAllowed(event.getPlayer(), location)) {
            return;
        }
        if (event.getPlayer().hasPermission(bypassPermission)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMoveSession(SessionMoveEvent event) {
        Vector3dc pos = event.getPosition();
        Location location = BukkitUtil.adapt(new org.bukkit.Location(
                event.getArmorStand().getWorld(), pos.x(), pos.y(), pos.z()));
        if (isAllowed(event.getPlayer(), location)) {
            return;
        }
        if (bypassCache.computeIfAbsent(event.getSession(), this::canBypass)) {
            return;
        }
        event.setCancelled(true);
    }

    private boolean canBypass(Session session) {
        return session.getPlayer().hasPermission(bypassPermission);
    }
}