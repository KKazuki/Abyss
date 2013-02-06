package me.stendec.abyss.listeners;

import me.stendec.abyss.AbyssPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class VehicleListener implements Listener {

    private final AbyssPlugin plugin;

    public VehicleListener(AbyssPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onVehicleMove(final VehicleMoveEvent event) {
        // Do portal logic. We ignore the return value because we couldn't use it anyways.
        plugin.usePortal(event.getVehicle(), event.getFrom(), event.getTo());

    }
}
