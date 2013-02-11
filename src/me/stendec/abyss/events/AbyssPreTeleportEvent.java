package me.stendec.abyss.events;

import me.stendec.abyss.ABPortal;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

/**
 *
 */
public class AbyssPreTeleportEvent extends Event implements Cancellable {

    // Event API
    private static final HandlerList handlers = new HandlerList();
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }

    // Cancellable API
    boolean cancelled;
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean b) { cancelled = b; }

    // Event Data
    protected final ABPortal from_portal;
    protected final ABPortal portal;
    protected final Entity entity;

    protected Location destination;
    protected Vector velocity;


    public AbyssPreTeleportEvent(final ABPortal from_portal, final ABPortal portal, final Entity entity, final Location destination, final Vector velocity) {
        this.from_portal = from_portal;
        this.portal = portal;
        this.entity = entity;

        this.cancelled = false;
        this.destination = destination;
        this.velocity = velocity;
    }

    /**
     * Gets the portal the entity is being teleported to.
     * @return The destination {@link ABPortal} instance.
     */
    public ABPortal getPortal() {
        return portal;
    }

    /**
     * Gets the portal the entity is teleporting from.
     * @return The departure {@link ABPortal} instance, or null if the entity isn't leaving from a portal.
     */
    public ABPortal getFromPortal() {
        return from_portal;
    }

    /**
     * Gets the entity being teleported.
     * @return {@link Entity} involved in the event.
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Gets the destination the entity will be teleported to.
     * @return {@link Location} the entity is to be teleported to.
     */
    public Location getDestination() {
        return destination;
    }

    public void setDestination(final Location destination) {
        Validate.notNull(destination);
        this.destination = destination;
    }

    /**
     * Gets the velocity the entity will have upon being teleported.
     * @return {@link Vector} of the entity's new velocity.
     */
    public Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(final Vector velocity) {
        if ( velocity == null )
            this.velocity = new Vector(0, 0, 0);
        else
            this.velocity = velocity;
    }

}
