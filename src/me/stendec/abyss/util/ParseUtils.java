package me.stendec.abyss.util;

import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import org.bukkit.material.Wool;

import java.util.*;

public class ParseUtils {

    public static String prettyName(final Material mat) {
        return WordUtils.capitalize(mat.name().toLowerCase().replaceAll("_+", " "));
    }

    public static String prettyName(final DyeColor color) {
        return WordUtils.capitalize(color.name().toLowerCase().replaceAll("_+", " "));
    }

    public static String prettyName(final ItemStack item) {
        final Material mat = item.getType();
        String out = prettyName(mat);

        if ( mat == Material.WOOL )
            out += " (" + prettyName(((Wool) item.getData()).getColor()) + ")";
        else if ( mat == Material.INK_SACK )
            out += " (" + prettyName(((Dye) item.getData()).getColor()) + ")";
        else if ( item.getDurability() != 0 )
            out += " (" + item.getDurability() + ")";

        return out;
    }

    public static Rotation matchRotation(final String value) {
        if (value == null)
            return null;

        Integer ival = null;

        try {
            ival = Integer.parseInt(value);
        } catch(NumberFormatException ex) { }

        if ( ival != null ) {
            ival = (ival % 360 + 360) % 360;
            switch(ival) {
                case 0:
                    return Rotation.NONE;
                case 1:
                case 90:
                    return Rotation.CLOCKWISE;
                case 2:
                case 180:
                    return Rotation.FLIPPED;
                case 3:
                case 270:
                    return Rotation.COUNTER_CLOCKWISE;
                default:
                    break;
            }
        } else {
            final String filtered = value.toUpperCase().replaceAll("\\s+", "_").replaceAll("\\W", "");
            if ("NORTH".startsWith(filtered) || filtered.equals("NONE"))
                return Rotation.NONE;
            else if ("EAST".startsWith(filtered) || filtered.equals("CLOCKWISE"))
                return Rotation.CLOCKWISE;
            else if ("SOUTH".startsWith(filtered) || filtered.equals("FLIPPED"))
                return Rotation.FLIPPED;
            else if ("WEST".startsWith(filtered) || filtered.equals("COUNTER_CLOCKWISE"))
                return Rotation.COUNTER_CLOCKWISE;
        }

        throw new IllegalArgumentException("rotation must be 0, 90, 180, 270, north, east, south, or west");
    }

    public static DyeColor matchColor(final String value) {
        if (value == null)
            return null;

        DyeColor result = null;

        try {
            result = DyeColor.getByWoolData((byte) Integer.parseInt(value));
        } catch(NumberFormatException ex) {}

        if (result == null) {
            final String filtered = value.toUpperCase().replaceAll("\\s+", "_").replaceAll("\\W", "");
            result = DyeColor.valueOf(filtered);
        }

        return result;
    }

    public static Location matchLocation(final String value) {
        return matchLocation(value, null);
    }

    public static Location matchLocation(final String value, World world) {
        if ( value == null || ! value.contains(",") )
            return null;

        final String[] pairs = value.split("\\s*,\\s*", 4);

        if ( pairs.length == 4 )
            world = Bukkit.getWorld(pairs[3]);

        if ( world == null )
            return null;

        try {
            return new Location(world,
                    Double.parseDouble(pairs[0]),
                    Double.parseDouble(pairs[1]),
                    Double.parseDouble(pairs[2]));

        } catch(NumberFormatException ex) {
            return null;
        }
    }


    public static ItemStack matchItem(final String value) {
        if (value == null)
            return null;

        final String[] pairs;
        if ( value.contains(":") ) pairs = value.split("\\s*:\\s*", 2);
        else pairs = new String[]{value, "0"};

        final Material material = Material.matchMaterial(pairs[0]);
        if ( material == null )
            return null;

        Short damage = -1;
        try { damage = Short.parseShort(pairs[1]); }
        catch(NumberFormatException ex) { }

        if ( damage < 0 )
            return null;

        return new ItemStack(material, 1, damage);
    }

    public static Map<String, String> tokenize(String item) {
        HashMap<String, String> out = new HashMap<String, String>();
        if ( item == null || item.length() == 0 )
            return out;

        for(final String pair: item.trim().split("\\s*;\\s*")) {
            if ( ! pair.contains(":") ) {
                out.put(pair.toLowerCase(), "");
                continue;
            }

            final String[] kv = pair.split("\\s*:\\s*", 2);
            out.put(kv[0].toLowerCase(), kv[1]);
        }

        return out;
    }

    public static Map<String, String> tokenizeLore(List<String> lore) {
        HashMap<String, String> out = new HashMap<String, String>();
        if ( lore == null || lore.size() == 0 )
            return out;

        for(String string: lore)
            out.putAll(tokenize(string));

        return out;
    }


    public static UUID tryUUID(final String string) {
        try {
            return UUID.fromString(string);
        } catch(IllegalArgumentException ex) {
            return null;
        }
    }

}
