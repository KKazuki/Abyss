package me.stendec.abyss.modifiers;

import me.stendec.abyss.ABPortal;
import me.stendec.abyss.ModInfo;
import me.stendec.abyss.PortalModifier;
import me.stendec.abyss.util.EntityUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BookModifier extends PortalModifier {

    public void postTeleport(final ABPortal from, final ABPortal portal, final ModInfo info, final Entity entity) {
        final ABPortal owner = info.getPortal();
        if ( owner == null || owner.equals(from) || !(entity instanceof Player))
            return;

        final Player player = (Player) entity;
        final Block block = info.location.getBlock();
        final Material m = block.getType();
        final ArrayList<String> message = new ArrayList<String>();
        final boolean color = ! info.flags.containsKey("no color");

        List<String> text = EntityUtils.getBookTextArray(info.item, true);
        if ( text != null )
            message.addAll(text);

        if ( message.isEmpty() && (m == Material.SIGN_POST || m == Material.WALL_SIGN) ) {
            final Sign sign = (Sign) block.getState();
            for(final String string: sign.getLines())
                message.add(color ? ChatColor.translateAlternateColorCodes('&', string) : string);
        }

        if ( message.isEmpty() ) {
            // Try for the ItemFrame
            final ItemFrame frame = EntityUtils.getItemFrameAt(info.location.getLocation());
            if ( frame != null ) {
                text = EntityUtils.getBookTextArray(frame.getItem(), true);
                if ( text != null )
                    message.addAll(text);
            }
        }

        // Did we find a message?
        if (message.isEmpty())
            throw new IllegalArgumentException("Unable to find message.");

        // Trim off trailing whitespace.
        while(!message.isEmpty() && message.get(message.size() - 1).trim().equals(""))
            message.remove(message.size() - 1);

        // If we've got a message, send it.
        if (!message.isEmpty())
            for(final String string: message)
                t().darkgray("[Abyss] ").reset(color ? string : ChatColor.stripColor(string)).
                        send(player);
    }

}
