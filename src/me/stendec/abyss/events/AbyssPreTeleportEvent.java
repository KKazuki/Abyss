package me.stendec.abyss.events;

import me.stendec.abyss.ABPortal;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

public class AbyssPreTeleportEvent extends Event implements Cancellable {

    // Event API
    private static final HandlerList handlers = new HandlerList();
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }

    // Cancellable API
    private boolean cancelled;
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean b) { cancelled = b; }

    // Event Data
    private final ABPortal from_portal;
    private final ABPortal portal;
    private final Entity entity;

    private Location destination;
    private Vector velocity;



    public AbyssPreTeleportEvent(final ABPortal from_portal, final ABPortal portal, final Entity entity, final Location destination, final Vector velocity) {
        this.from_portal = from_portal;
        this.portal = portal;
        this.entity = entity;

        this.cancelled = false;
        this.destination = destination;
        this.velocity = velocity;
    }


    public ABPortal getPortal() {
        return portal;
    }

    public ABPortal getFromPortal() {
        return from_portal;
    }

    public Entity getEntity() {
        return entity;
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(final Location destination) {
        Validate.notNull(destination);
        this.destination = destination;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(final Vector velocity) {
        Validate.notNull(velocity);
        this.velocity = velocity;
    }

}
