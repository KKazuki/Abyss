package me.stendec.abyss.util;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.material.Colorable;
import org.bukkit.material.Directional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class EntityUtils {

    public static BlockFace getFacing(Entity entity) {
        return BlockUtils.toBlockFace(entity.getLocation().getYaw() - 180);
    }


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
        if ( loc == null )
            return null;

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


    public static Entity teleport(final Entity entity, final Location destination, final PlayerTeleportEvent.TeleportCause cause) {
        return teleport(entity, destination, cause, null);
    }

    public static Entity teleport(final Entity entity, final Location destination, final PlayerTeleportEvent.TeleportCause cause, final HashSet<EntityType> whitelist) {
        if ( entity instanceof Player || entity.getWorld().equals(destination.getWorld()) ) {
            if ( ! entity.teleport(destination, cause) )
                return null;
            return entity;
        }

        final EntityType type = entity.getType();
        Entity instance = null;

        // If the entity type isn't whitelisted, don't let them through.
        if ( whitelist != null && !whitelist.contains(type) )
            return null;

        // Clone the entity.
        if ( entity instanceof FallingBlock ) {
            final FallingBlock eb = (FallingBlock) entity;
            instance = destination.getWorld().spawnFallingBlock(destination,
                    eb.getMaterial(), eb.getBlockData());

        } else if ( entity instanceof Arrow ) {
            instance = destination.getWorld().spawnArrow(destination,
                    entity.getVelocity(), 0.6f, 12);

        } else {
            instance = destination.getWorld().spawn(destination, entity.getClass());
        }

        if ( instance == null )
            return null;

        instance.setFireTicks(entity.getFireTicks());
        instance.setTicksLived(entity.getTicksLived());

        if ( entity instanceof InventoryHolder) {
            final Inventory to = ((InventoryHolder) instance).getInventory();
            to.setContents(((InventoryHolder) entity).getInventory().getContents());
        }

        if ( entity instanceof Minecart) {
            final Minecart to = (Minecart) instance;
            to.setDamage(((Minecart) entity).getDamage());
            to.setDerailedVelocityMod(((Minecart) entity).getDerailedVelocityMod());
            to.setFlyingVelocityMod(((Minecart) entity).getFlyingVelocityMod());
            to.setMaxSpeed(((Minecart) entity).getMaxSpeed());
            to.setSlowWhenEmpty(((Minecart) entity).isSlowWhenEmpty());
        }

        if ( entity instanceof Boat ) {
            final Boat to = (Boat) instance;
            to.setMaxSpeed(((Boat) entity).getMaxSpeed());
            to.setOccupiedDeceleration(((Boat) entity).getOccupiedDeceleration());
            to.setUnoccupiedDeceleration(((Boat) entity).getUnoccupiedDeceleration());
            to.setWorkOnLand(((Boat) entity).getWorkOnLand());
        }

        if ( entity instanceof Damageable ) {
            final Damageable to = (Damageable) instance;
            to.setHealth(((Damageable) entity).getHealth());

            if ( ((Damageable) entity).getMaxHealth() != to.getMaxHealth() )
                to.setMaxHealth(((Damageable) entity).getMaxHealth());
        }

        if ( entity instanceof LivingEntity ) {
            final LivingEntity to = (LivingEntity) instance;
            to.addPotionEffects(((LivingEntity) entity).getActivePotionEffects());
            to.setCanPickupItems(((LivingEntity) entity).getCanPickupItems());
            to.setLastDamage(((LivingEntity) entity).getLastDamage());
            to.setMaximumAir(((LivingEntity) entity).getMaximumAir());
            to.setMaximumNoDamageTicks(((LivingEntity) entity).getMaximumNoDamageTicks());
            to.setNoDamageTicks(((LivingEntity) entity).getNoDamageTicks());
            to.setRemainingAir(((LivingEntity) entity).getRemainingAir());
            to.setRemoveWhenFarAway(((LivingEntity) entity).getRemoveWhenFarAway());

            final EntityEquipment te = to.getEquipment();
            final EntityEquipment fe = ((LivingEntity) entity).getEquipment();

            te.setArmorContents(fe.getArmorContents());
            te.setBootsDropChance(fe.getBootsDropChance());
            te.setChestplateDropChance(fe.getChestplateDropChance());
            te.setHelmetDropChance(fe.getHelmetDropChance());
            te.setItemInHandDropChance(fe.getItemInHandDropChance());
            te.setLeggingsDropChance(fe.getLeggingsDropChance());
        }

        if ( entity instanceof Creature ) {
            final Creature to = (Creature) instance;
            to.setTarget(((Creature) entity).getTarget());
        }

        if ( entity instanceof Ageable ) {
            final Ageable to = (Ageable) instance;
            if ( ((Ageable) entity).isAdult() )
                to.setAdult();
            else
                to.setBaby();

            to.setAge(((Ageable) entity).getAge());
            to.setAgeLock(((Ageable) entity).getAgeLock());
            to.setBreed(((Ageable) entity).canBreed());
        }

        if ( entity instanceof Creeper ) {
            final Creeper to = (Creeper) instance;
            to.setPowered(((Creeper) entity).isPowered());
        }

        if ( entity instanceof Directional ) {
            final Directional to = (Directional) instance;
            to.setFacingDirection(((Directional) entity).getFacing());
        }

        if ( entity instanceof Enderman ) {
            final Enderman to = (Enderman) instance;
            to.setCarriedMaterial(((Enderman) entity).getCarriedMaterial());
        }

        if ( entity instanceof ExperienceOrb ) {
            final ExperienceOrb to = (ExperienceOrb) instance;
            to.setExperience(((ExperienceOrb) entity).getExperience());
        }

        if ( entity instanceof Explosive ) {
            final Explosive to = (Explosive) instance;
            to.setIsIncendiary(((Explosive) entity).isIncendiary());
            to.setYield(((Explosive) entity).getYield());
        }

        if ( entity instanceof FallingBlock ) {
            final FallingBlock to = (FallingBlock) instance;
            to.setDropItem(((FallingBlock) entity).getDropItem());
        }

        if ( entity instanceof Fireball ) {
            final Fireball to = (Fireball) instance;
            to.setDirection(((Fireball) entity).getDirection());
        }

        if ( entity instanceof Firework ) {
            final Firework to = (Firework) instance;
            to.setFireworkMeta(((Firework) entity).getFireworkMeta());
        }

        if ( entity instanceof IronGolem ) {
            final IronGolem to = (IronGolem) instance;
            to.setPlayerCreated(((IronGolem) entity).isPlayerCreated());
        }

        if ( entity instanceof Item ) {
            final Item to = (Item) instance;
            to.setPickupDelay(((Item) entity).getPickupDelay());
            to.setItemStack(((Item) entity).getItemStack());
        }

        if ( entity instanceof ItemFrame ) {
            final ItemFrame to = (ItemFrame) instance;
            to.setItem(((ItemFrame) entity).getItem());
            to.setRotation(((ItemFrame) entity).getRotation());
        }

        if ( entity instanceof Ocelot ) {
            final Ocelot to = (Ocelot) instance;
            to.setCatType(((Ocelot) entity).getCatType());
            to.setSitting(((Ocelot) entity).isSitting());
        }

        if ( entity instanceof Painting ) {
            final Painting to = (Painting) instance;
            to.setArt(((Painting) entity).getArt(), true);
        }

        if ( entity instanceof Pig ) {
            final Pig to = (Pig) instance;
            to.setSaddle(((Pig) entity).hasSaddle());
        }

        if ( entity instanceof PigZombie ) {
            final PigZombie to = (PigZombie) instance;
            to.setAnger(((PigZombie) entity).getAnger());
        }

        if ( entity instanceof Projectile ) {
            final Projectile to = (Projectile) instance;
            to.setBounce(((Projectile) entity).doesBounce());
            to.setShooter(((Projectile) entity).getShooter());
        }

        if ( entity instanceof Sheep ) {
            final Sheep to = (Sheep) instance;
            to.setSheared(((Sheep) entity).isSheared());
        }

        if ( entity instanceof Colorable ) {
            final Colorable to = (Colorable) instance;
            to.setColor(((Colorable) entity).getColor());
        }

        if ( entity instanceof Skeleton ) {
            final Skeleton to = (Skeleton) instance;
            to.setSkeletonType(((Skeleton) entity).getSkeletonType());
        }

        if ( entity instanceof Slime ) {
            final Slime to = (Slime) instance;
            to.setSize(((Slime) entity).getSize());
        }

        if ( entity instanceof Tameable ) {
            final Tameable to = (Tameable) instance;
            to.setTamed(((Tameable) entity).isTamed());
            to.setOwner(((Tameable) entity).getOwner());
        }

        if ( entity instanceof TNTPrimed ) {
            final TNTPrimed to = (TNTPrimed) instance;
            to.setFuseTicks(((TNTPrimed) entity).getFuseTicks());
        }

        if ( entity instanceof Villager ) {
            final Villager to = (Villager) instance;
            to.setProfession(((Villager) entity).getProfession());
        }

        if ( entity instanceof Wolf ) {
            final Wolf to = (Wolf) instance;
            to.setCollarColor(((Wolf) entity).getCollarColor());
            to.setAngry(((Wolf) entity).isAngry());
            to.setSitting(((Wolf) entity).isSitting());
        }

        if ( entity instanceof Zombie ) {
            final Zombie to = (Zombie) instance;
            to.setBaby(((Zombie) entity).isBaby());
            to.setVillager(((Zombie) entity).isVillager());
        }

        // While we're at it, remove the old entity.
        entity.remove();
        return instance;
    }

}
