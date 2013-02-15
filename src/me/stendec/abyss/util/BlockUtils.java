package me.stendec.abyss.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.material.Rails;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class BlockUtils {

    public static Material[] transparentMaterials;
    public static HashSet<Byte> transparentBytes;

    static {
        transparentMaterials = new Material[]{
                Material.AIR,
                Material.STONE_BUTTON, Material.WOOD_BUTTON,
                Material.WALL_SIGN, Material.LADDER,
                Material.VINE, Material.SKULL};

        transparentBytes = new HashSet<Byte>();
        for(final Material mat: transparentMaterials)
            transparentBytes.add((byte) mat.getId());
    }

    public static boolean isBusy(final Block block, final Block other) {
        // Check to see if both sides of a rail block have rails. If they do, assume we're connected.
        Rails data = (Rails) block.getState().getData();
        BlockFace bf = data.getDirection().getOppositeFace();

        BlockFace face1, face2;

        if ( bf == BlockFace.NORTH_EAST ) {
            face1 = BlockFace.NORTH;
            face2 = BlockFace.EAST;

        } else if ( bf == BlockFace.NORTH_WEST ) {
            face1 = BlockFace.NORTH;
            face2 = BlockFace.WEST;

        } else if ( bf == BlockFace.SOUTH_EAST ) {
            face1 = BlockFace.SOUTH;
            face2 = BlockFace.EAST;

        } else if ( bf == BlockFace.SOUTH_WEST ) {
            face1 = BlockFace.SOUTH;
            face2 = BlockFace.WEST;

        } else {
            face1 = bf;
            face2 = bf.getOppositeFace();
        }

        final Block block1 = block.getRelative(face1);
        final Block block2 = block.getRelative(face2);

        return !( block1.equals(other) || block2.equals(other) ) && isRail(block1) && isRail(block2);
    }

    public static boolean isRail(final Block block) {
        return isRail(block.getType());
    }

    public static boolean isRail(final Material mat) {
        return mat == Material.RAILS || mat == Material.POWERED_RAIL || mat == Material.DETECTOR_RAIL;
    }

    public static Location[] getBoundsBlocks(final List<Block> blocks) {
        if ( blocks == null || blocks.size() == 0 )
            return null;

        Iterator<Block> it = blocks.iterator();

        Block b = it.next();
        final World world = b.getWorld();

        int x1 = b.getX(), y1 = b.getY(), z1 = b.getZ();
        int x2 = x1, y2 = y1, z2 = z1;

        while( it.hasNext() ) {
            b = it.next();
            final int x = b.getX(), y = b.getY(), z = b.getZ();

            if ( x < x1 ) x1 = x;
            else if ( x > x2 ) x2 = x;

            if ( y < y1 ) y1 = y;
            else if ( y > y2 ) y2 = y;

            if ( z < z1 ) z1 = z;
            else if ( z > y2 ) z2 = z;
        }

        return new Location[]{new Location(world, x1, y1, z1), new Location(world, x2, y2, z2)};
    }

    public static Location[] getBounds(final List<BlockState> blocks) {
        if ( blocks == null || blocks.size() == 0 )
            return null;

        Iterator<BlockState> it = blocks.iterator();

        BlockState bs = it.next();
        final World world = bs.getWorld();

        int x1 = bs.getX(), y1 = bs.getY(), z1 = bs.getZ();
        int x2 = x1, y2 = y1, z2 = z1;

        while( it.hasNext() ) {
            bs = it.next();
            final int x = bs.getX(), y = bs.getY(), z = bs.getZ();

            if ( x < x1 ) x1 = x;
            else if ( x > x2 ) x2 = x;

            if ( y < y1 ) y1 = y;
            else if ( y > y2 ) y2 = y;

            if ( z < z1 ) z1 = z;
            else if ( z > y2 ) z2 = z;
        }

        return new Location[]{new Location(world, x1, y1, z1), new Location(world, x2, y2, z2)};
    }

    public static double getYaw(final Location from, final Location to) {
        return Math.toDegrees(Math.atan2(to.getZ() - from.getZ(), to.getX() - from.getX())) - 180;
    }


    public static BlockFace toBlockFace(double yaw) {
        while ( yaw < 0 )
            yaw += 360;
        yaw = yaw % 360;

        switch((byte) Math.round(yaw / 90f) & 0x3) {
            case 1:
                return BlockFace.EAST;
            case 2:
                return BlockFace.SOUTH;
            case 3:
                return BlockFace.WEST;
            default:
                return BlockFace.NORTH;
        }
    }


    public static BlockFace toLeft(BlockFace face) {
        switch(face) {
            case NORTH:
                return BlockFace.WEST;
            case EAST:
                return BlockFace.NORTH;
            case SOUTH:
                return BlockFace.EAST;
            default:
                return BlockFace.SOUTH;
        }
    }

    public static BlockFace toRight(BlockFace face) {
        switch(face) {
            case NORTH:
                return BlockFace.EAST;
            case EAST:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.WEST;
            default:
                return BlockFace.NORTH;
        }
    }

    public static BlockFace toLeftSub(BlockFace face) {
        switch(face) {
            case NORTH_WEST:
                return BlockFace.WEST;
            case NORTH:
                return BlockFace.NORTH_WEST;
            case NORTH_EAST:
                return BlockFace.NORTH;
            case EAST:
                return BlockFace.NORTH_EAST;
            case SOUTH_EAST:
                return BlockFace.EAST;
            case SOUTH:
                return BlockFace.SOUTH_EAST;
            case SOUTH_WEST:
                return BlockFace.SOUTH;
            default:
                return BlockFace.SOUTH_WEST;
        }
    }

    public static BlockFace toRightSub(BlockFace face) {
        switch(face) {
            case NORTH:
                return BlockFace.NORTH_EAST;
            case NORTH_EAST:
                return BlockFace.EAST;
            case EAST:
                return BlockFace.SOUTH_EAST;
            case SOUTH_EAST:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.SOUTH_WEST;
            case SOUTH_WEST:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.NORTH_WEST;
            default:
                return BlockFace.NORTH;
        }
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

    public static boolean isIris(final Material mat) {
        switch(mat) {
            case AIR:
            case WALL_SIGN:
            case WOOD_PLATE:
            case STONE_PLATE:
            case STATIONARY_WATER:
            case WATER:
            case LAVA:
            case STATIONARY_LAVA:
            case LADDER:
            case WEB:
            case TORCH:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
            case FIRE:
            case REDSTONE_WIRE:
            case LEVER:
            case WOOD_BUTTON:
            case STONE_BUTTON:
            case PORTAL:
            case ENDER_PORTAL:
            case TRIPWIRE:
            case TRIPWIRE_HOOK:
            case SKULL:
                return false;
            default:
                return true;
        }
    }

}
