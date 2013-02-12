package me.stendec.abyss.util;

import me.stendec.abyss.ABPortal;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;
import org.bukkit.material.Wool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ParseUtils {

    public static String[] rsplit(final String string, final String sep) {
        final int index = string.lastIndexOf(sep);
        if ( index == -1 )
            return new String[]{string, ""};
        return new String[]{string.substring(0, index), string.substring(index+1)};
    }

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

    public static Byte matchUpdate(final String value) {
        if ( value == null )
            return null;

        Byte result = null;

        try {
            result = Byte.parseByte(value);
        } catch(NumberFormatException ex) { }

        if ( result != null && (result > 2 || result < 0) )
            return null;

        if ( result == null ) {
            final String filtered = value.toLowerCase().replaceAll("\\W", "");
            if ( filtered.equals("false") || filtered.equals("no") )
                result = 0;
            else if ( filtered.equals("true") || filtered.equals("yes") )
                result = 2;
            else if ( filtered.equals("check") )
                result = 1;
        }

        return result;
    }

    public static String updateString(final Byte value) {
        if ( value == 0 )
            return "false";
        else if ( value == 1 )
            return "check";
        else if ( value == 2 )
            return "true";

        return null;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
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

    public static Effect matchEffect(final String value) {
        if ( value == null )
            return null;

        Effect result = null;

        try {
            result = Effect.getById(Integer.parseInt(value));
        } catch(NumberFormatException ex) {}

        if ( result == null ) {
            final String filtered = value.toUpperCase().replaceAll("\\s+", "_").replaceAll("\\W", "");
            try {
                result = Effect.valueOf(filtered);
            } catch(IllegalArgumentException ex) { }
        }

        return result;
    }


    public static EntityType matchEntityType(final String value) {
        if ( value == null )
            return null;

        EntityType result = null;

        try {
            result = EntityType.fromId(Integer.parseInt(value));
        } catch(NumberFormatException ex) {}

        if ( result == null )
            result = EntityType.fromName(value);

        if ( result == null ) {
            final String filtered = value.toUpperCase().replaceAll("\\s+", "_").replaceAll("\\W", "");
            result = EntityType.valueOf(filtered);
        }

        return result;
    }


    public static Sound matchSound(final String value) {
        if ( value == null )
            return null;

        final String filtered = value.toUpperCase().replaceAll("\\s+", "_").replaceAll("\\W", "");
        try {
            return Sound.valueOf(filtered);
        } catch(IllegalArgumentException ex) {
            return null;
        }
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
            try {
                result = DyeColor.valueOf(filtered);
            } catch(IllegalArgumentException ex) { }
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

        final Material material;
        try {
            material = Material.matchMaterial(pairs[0]);
        } catch(IllegalArgumentException ex) { return null; }

        if ( material == null )
            return null;

        Short damage = null;
        try { damage = Short.parseShort(pairs[1]); }
        catch(NumberFormatException ex) { }

        if ( damage == null )
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


    public static void configFromLore(final ABPortal portal, final Map<String, String> config) {
        if (config == null || portal == null || config.size() == 0)
            return;

        for(Map.Entry<String,String> entry: config.entrySet())
            loreConfig(portal, entry.getKey(), entry.getValue());
    }


    public static void configFromLore(final ABPortal portal, final ItemMeta meta) {
        // If there's no lore, obviously this does nothing.
        if (!meta.hasLore())
            return;

        configFromLore(portal, tokenizeLore(meta.getLore()));
    }


    public static void requireValue(final String key, final String value) {
        if ( value == null || value.length() == 0 )
            throw new IllegalArgumentException(key + " must have a value.");
    }


    public static void loreConfig(final ABPortal portal, final String key, final String value) {
        if (key.equals("owner")) {
            requireValue(key, value);
            portal.owner = value;

        } else if ( key.equals("name") ) {
            requireValue(key, value);

            // Check for collisions.
            final ABPortal p = portal.getPlugin().getManager().getByName(value);
            if ( p != null )
                throw new IllegalArgumentException("A portal already exists with the name: " + value);

            portal.setName(value);

        } else if (key.equals("color")) {
            requireValue(key, value);
            DyeColor color = ParseUtils.matchColor(value);
            if ( color == null )
                throw new IllegalArgumentException("Invalid color: " + value);

            portal.color = color;

        } else if (key.equals("id")) {
            requireValue(key, value);
            try {
                portal.id = Short.parseShort(value);
            } catch(NumberFormatException ex) {
                throw new IllegalArgumentException("id must be a number between " + Short.MIN_VALUE + " and " + Short.MAX_VALUE);
            }

        } else if (key.equals("dest") || key.equals("destination")) {
            requireValue(key, value);
            try {
                portal.destination = Short.parseShort(value);
            } catch(NumberFormatException ex) {
                throw new IllegalArgumentException("destination must be a number between " + Short.MIN_VALUE + " and " + Short.MAX_VALUE);
            }

        } else if (key.equals("velocity") || key.equals("speed")) {
            requireValue(key, value);
            try {
                portal.velocityMultiplier = Double.parseDouble(value);
            } catch(NumberFormatException ex) {
                throw new IllegalArgumentException("velocity must be a number");
            }

        } else if ( key.equals("range") ) {
            requireValue(key, value);
            try {
                portal.rangeMultiplier = Double.parseDouble(value);
            } catch(NumberFormatException ex) {
                throw new IllegalArgumentException("range must be a number");
            }

        } else if (key.equals("rot") || key.equals("rotation")) {
            requireValue(key, value);
            portal.setRotation(ParseUtils.matchRotation(value));

        } else if (key.equals("size")) {
            throw new IllegalArgumentException("size cannot be applied to existing portals");

        } else {
            throw new IllegalArgumentException("Invalid option: " + key);
        }
    }

}
