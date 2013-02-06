package me.stendec.abyss.util;

import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EntityUtils {

    public static List<Entity> getNearbyEntities(final Location loc, final double distance) {
        return getNearbyEntities(loc, distance, distance, distance);
    }

    public static List<Entity> getNearbyEntities(final Location loc, final double x, final double y, final double z) {
        final Entity entity = loc.getWorld().spawnEntity(loc, EntityType.EXPERIENCE_ORB);
        if (entity == null)
            return null;

        final List<Entity> entities = entity.getNearbyEntities(x, y, z);
        entity.remove();
        return entities;
    }

    public static ItemFrame getItemFrameAt(final Location loc) {
        final int x = loc.getBlockX();
        final int y = loc.getBlockY();
        final int z = loc.getBlockZ();

        final List<Entity> entities = getNearbyEntities(loc, 1, 1, 1);
        for(Entity entity: entities) {
            if (!(entity instanceof ItemFrame))
                continue;

            final Location l = entity.getLocation();
            if (x == l.getBlockX() && y == l.getBlockY() && z == l.getBlockZ())
                return (ItemFrame) entity;
        }

        return null;
    }

    public static String getBookText(final ItemStack book, final boolean byline, final String separator) {
        final List<String> text = getBookTextArray(book, byline);
        if (text == null)
            return null;

        if (text.size() == 0)
            return "";

        final StringBuilder sb = new StringBuilder();
        final String sep = (separator != null) ? separator : "";
        boolean first = true;

        for(final String string: getBookTextArray(book, byline)) {
            if (!first)
                sb.append(sep);
            first = false;
            sb.append(string);
        }

        return sb.toString();
    }

    public static List<String> getBookTextArray(final ItemStack book, final boolean byline) {
        if (! book.hasItemMeta() || !(book.getItemMeta() instanceof BookMeta))
            return null;

        final BookMeta meta = (BookMeta) book.getItemMeta();
        final List<String> out = new ArrayList<String>(meta.getPages());

        if (byline && meta.hasTitle())
            out.add(0, meta.getTitle() + (meta.hasAuthor() ? " by " + meta.getAuthor() : ""));

        return out;
    }

    public static <T> List<T> getNearbyEntitiesOfType(final Class T, final Location loc, final double distance) {
        return getNearbyEntitiesOfType(T, loc, distance, distance, distance);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getNearbyEntitiesOfType(final Class T, final Location loc, final double x, final double y, final double z) {
        final List<Entity> entities = getNearbyEntities(loc, x, y, z);
        for(Iterator<Entity> it = entities.iterator(); it.hasNext(); ) {
            final Entity en = it.next();
            if (!T.isInstance(en))
                it.remove();
        }

        return (List<T>) entities;
    }


    public static BlockFace toBlockFace(final Rotation rotation) {
        switch(rotation) {
            case NONE:
                return BlockFace.NORTH;
            case FLIPPED:
                return BlockFace.SOUTH;
            case COUNTER_CLOCKWISE:
                return BlockFace.WEST;
            case CLOCKWISE:
            default:
                return BlockFace.EAST;
        }
    }


    public static BlockFace toBlockFace(final Vector delta) {
        if ( delta == null || delta.lengthSquared() == 0 )
            return BlockFace.SELF;

        final double x = delta.getX(), y = delta.getY(), z = delta.getZ();
        final double ax = Math.abs(x), ay = Math.abs(y), az = Math.abs(z);

        if ( ay >= ax && ay >= az )
            return ( y > 0 ) ? BlockFace.UP : BlockFace.DOWN;

        if ( ax >= az )
            return ( x > 0 ) ? BlockFace.EAST : BlockFace.WEST;

        return ( z > 0 ) ? BlockFace.SOUTH : BlockFace.NORTH;
    }


    public static Vector toVector(final BlockFace face, final double magnitude) {
        switch(face) {
            case NORTH:
                return new Vector(0, 0, -magnitude);
            case EAST:
                return new Vector(magnitude, 0, 0);
            case SOUTH:
                return new Vector(0, 0, magnitude);
            case WEST:
                return new Vector(-magnitude, 0, 0);
            case UP:
                return new Vector(0, magnitude, 0);
            case DOWN:
                return new Vector(0, -magnitude, 0);
            default:
                throw new IllegalArgumentException("Unsupported BlockFace.");
        }
    }


    public static Location[] fixCubeoid(final Location first, final Location second) {
        if ( first == null || second == null )
            return null;

        final World w = first.getWorld();
        if ( ! w.equals(second.getWorld()) )
            return null;

        final double x1 = first.getX(), y1 = first.getY(), z1 = first.getZ();
        final double x2 = second.getX(), y2 = second.getY(), z2 = second.getZ();

        return fixCubeoid(w, x1, y1, z1, x2, y2, z2);
    }


    public static Location[] fixCubeoid(final World world, final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
        final double ox1, oy1, oz1, ox2, oy2, oz2;

        if ( x1 > x2 ) {
            ox1 = x2; ox2 = x1;
        } else {
            ox1 = x1; ox2 = x2;
        }

        if ( y1 > y2 ) {
            oy1 = y2; oy2 = y1;
        } else {
            oy1 = y1; oy2 = y2;
        }

        if ( z1 > z2 ) {
            oz1 = z2; oz2 = z1;
        } else {
            oz1 = z1; oz2 = z2;
        }

        return new Location[]{new Location(world, ox1, oy1, oz1), new Location(world, ox2, oy2, oz2)};
    }

}
