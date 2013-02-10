package me.stendec.abyss;

import me.stendec.abyss.util.ColorBuilder;
import me.stendec.abyss.util.EntityUtils;
import me.stendec.abyss.util.ParseUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.ChatPaginator;

import java.util.*;

public abstract class ABCommand implements CommandExecutor {

    protected final AbyssPlugin plugin;
    public final String name;

    public ChatColor color;
    public String description;
    public String usage;
    public String help;

    public int minimumArguments;
    public int maximumArguments;

    public boolean try_block;
    public boolean require_portal;
    public boolean require_block;
    public boolean allow_wand;

    public ABCommand(final AbyssPlugin plugin, final String name) {
        this.plugin = plugin;
        this.name = name;

        // Store this command handler.
        plugin.commands.put(name.toLowerCase(), this);

        help = null;
        description = null;
        usage = null;
        minimumArguments = 0;
        maximumArguments = -1;
        require_portal = false;
        require_block = false;
        try_block = false;
        allow_wand = true;
        color = ChatColor.WHITE;
    }

    public Player getPlayer(final String name) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(name);
        if ( ! player.hasPlayedBefore() )
            throw new IllegalArgumentException(t().red("No such player: ").reset(name).toString());

        if ( player.isOnline() )
            return player.getPlayer();

        throw new IllegalArgumentException(t().red(ChatColor.RESET, "The player %s is not online.").toString());
    }

    public Block parseBlock(final CommandSender sender, final String key) {
        if ( key.startsWith("@") ) {
            // Block Location
            final World world = (sender instanceof Player) ? ((Player) sender).getWorld() : null;
            Location loc = ParseUtils.matchLocation(key.substring(1), world);
            if ( loc == null )
                throw new IllegalArgumentException(t().red("Invalid block location: ").reset(key).toString());

            return loc.getBlock();

        } else if ( key.startsWith("+") ) {
            // Player Standing Location
            Player player = null;
            if ( key.length() == 1 )
                if ( sender instanceof Player )
                    player = (Player) sender;
                else
                    throw new IllegalArgumentException(t().red("You must specify a player name to use the player location option.").toString());
            else
                player = getPlayer(key.substring(1));

            return player.getLocation().getBlock();

        } else if ( key.startsWith("*") ) {
            // Player Target Location
            Player player = null;
            if ( key.length() == 1 )
                if ( sender instanceof Player )
                    player = (Player) sender;
                else
                    throw new IllegalArgumentException(t().red("You must specify a player name to use the player target option.").toString());
            else
                player = getPlayer(key.substring(1));

            return player.getTargetBlock(EntityUtils.transparentBytes, 100);
        }

        throw new IllegalArgumentException(t().red("Invalid block location: ").reset(key).toString());
    }

    public ABPortal parsePortal(final CommandSender sender, final String key) {
        ABPortal portal = null;
        if ( key.startsWith("@") || key.startsWith("+") || key.startsWith("*") ) {
            portal = plugin.getManager().getAt(parseBlock(sender, key));

            if ( portal == null ) {
                if ( key.equals("*") )
                    throw new IllegalArgumentException(t().red("Cannot find portal where you are looking.").toString());
                else if ( key.startsWith("*") )
                    throw new IllegalArgumentException(t().red("Cannot find portal where ").reset(key.substring(1)).red(" is looking.").toString());
                else if ( key.equals("+") )
                    throw new IllegalArgumentException(t().red("Cannot find portal where you are standing.").toString());
                else if ( key.startsWith("+") )
                    throw new IllegalArgumentException(t().red("Cannot find portal where ").reset(key.substring(1)).red(" is standing.").toString());
                else
                    throw new IllegalArgumentException(t().red("Cannot find portal at: ").reset(key).toString());
            }

            return portal;
        }

        // Try parsing it as a UUID first.
        try {
            portal = plugin.getManager().getById(UUID.fromString(key));
        } catch(IllegalArgumentException ex) { }

        if ( portal == null )
            portal = plugin.getManager().getByName(key);

        if ( portal == null )
            throw new IllegalArgumentException(t().red("Invalid portal name or id: ").reset(key).toString());

        return portal;
    }


    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] arguments) {
        // Convert the arguments to a list.
        final ArrayList<String> args = new ArrayList<String>(Arrays.asList(arguments));

        // Remove empty strings.
        for(Iterator<String> it = args.iterator(); it.hasNext(); )
            if ( it.next().trim().length() == 0 )
                it.remove();

        return onCommand(sender, command, label, args);
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final ArrayList<String> args) {
        // See if this is a help command.
        boolean help = false;
        boolean used_help = false;
        if ( args.size() > 0 && args.get(0).equalsIgnoreCase("help") ) {
            help = true;
            used_help = true;
            args.remove(0);
        }

        // If require_portal is true, try acquiring a portal somehow.
        Block block = null;
        ABPortal portal = null;
        String usage = null;

        if ( !help && require_portal ) {
            if ( args.size() > 0 ) {
                final String arg = args.remove(0);
                try {
                    portal = parsePortal(sender, arg);
                } catch(IllegalArgumentException ex) {
                    sender.sendMessage(ex.getMessage());
                    return true;
                }

                block = portal.getLocation().getBlock();

            } else
                help = true;
        }

        if ( !help && (try_block || require_block) && block == null ) {
            if ( args.size() > 0 ) {
                final String arg = args.remove(0);
                try {
                    block = parseBlock(sender, arg);
                } catch(IllegalArgumentException ex) {
                    if ( require_block ) {
                        sender.sendMessage(ex.getMessage());
                        return true;
                    }

                    // It's not a block, so put it back.
                    args.add(0, arg);
                }

                // Try getting the portal if we can.
                if ( portal == null && block != null )
                    portal = plugin.getManager().getAt(block);
            }
        }

        if ( !help && minimumArguments > args.size() ) {
            if ( args.size() != 0 ) {
                t().red("Not enough arguments.").send(sender);
                return true;
            }

            help = true;

        } else if ( !help && maximumArguments != -1 && args.size() > maximumArguments ) {
            t().red("Too many arguments.").send(sender);
            return true;
        }

        if ( !help ) {
            try {
                run(sender, null, block, portal, args);
            } catch(NeedsHelp ex) {
                help = true;
                final String msg = ex.getMessage();
                if ( msg != null && msg.length() > 0 )
                    usage = msg;
            }
        }

        // If we need help, display it.
        if ( !help )
            return true;

        final boolean isPlayer = sender instanceof Player;
        final String slash = isPlayer ? "/" : "";
        final String cmdString = String.format("%s%s %s", slash, command.getName(), name);

        ColorBuilder out;
        if ( used_help ) {
            out = t().yellow("--------- ").white("Help: ").append(cmdString).append(" ");

            final int needed = 50 - (out.length() - 4);
            if ( needed > 0 )
                out.yellow(StringUtils.repeat("-", needed));

            // Description
            if ( description != null )
                out.lf().gold("Description: ").reset(description);

            // Usage
            out.lf().gold("Usage: ").reset(cmdString);

        } else if ( label.equals("abyss") )
            out = t().red("Usage: ").reset(cmdString);
        else
            out = t().red("Usage: ").reset(slash).append(label);

        if ( require_portal || require_block || try_block ) {
            out.append((require_portal || require_block) ? " [" : " <");
            out.append(" [").append(isPlayer ? "@x,y,z|": "").append("@x,y,z,world|");
            out.append(isPlayer ? "+|+player|*|*player" : "+player|*player");

            if ( require_portal )
                out.append("|portal");

            out.append((require_portal || require_block) ? "]" : ">");
        }

        if ( usage != null )
            out.append(" ").append(usage);
        else if ( this.usage != null )
            out.append(" ").append(this.usage);

        if ( used_help ) {
            // Aliases
            out.lf().gold("Aliases: ").reset(slash).append("ab").append(name);
            for(final Map.Entry<String, String> entry: plugin.aliases.entrySet())
                if ( entry.getValue().equals(name) ) {
                    out.darkgray(", ").reset(slash).append("ab").append(entry.getKey());
                }

            if ( this.help != null )
                out.lf().reset(this.help);
        }

        out.sendByLine(sender);
        return true;
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
