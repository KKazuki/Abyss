package me.stendec.abyss;

import com.sk89q.worldguard.blacklist.loggers.BlacklistLoggerHandler;
import me.stendec.abyss.util.ColorBuilder;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class ABCommand {

    protected final AbyssPlugin plugin;
    public String usage;
    public boolean require_portal;
    public boolean allow_wand;
    public ChatColor color;

    public ABCommand(final AbyssPlugin plugin) {
        this.plugin = plugin;
        usage = null;
        require_portal = false;
        allow_wand = true;
        color = ChatColor.WHITE;
    }

    public abstract boolean run(CommandSender sender, PlayerInteractEvent event, Block target, ABPortal portal, ArrayList<String> args) throws NeedsHelp;

    public List<String> tabComplete(CommandSender sender, Block target, ABPortal portal, ArrayList<String> args) {
        return null;
    }

    protected static ColorBuilder t(final String... text) {
        return new ColorBuilder(text);
    }


    public static class NeedsHelp extends Throwable {}

}
