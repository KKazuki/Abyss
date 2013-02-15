package me.stendec.abyss.listeners;

import me.stendec.abyss.AbyssPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public class ServerListener implements Listener {

    private final AbyssPlugin plugin;

    public ServerListener(final AbyssPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        if ( "Multiverse-Core".equalsIgnoreCase(event.getPlugin().getName()) )
            plugin.enableMultiverse();
    }

    public void onPluginDisable(PluginDisableEvent event) {
        if ( "Multiverse-Core".equalsIgnoreCase(event.getPlugin().getName()) )
            plugin.disableMultiverse();
    }

}
