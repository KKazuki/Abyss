# Abyss Configuration #

This document describes the various configuration options supported by the
Abyss plugin.


## General ##

#### ``auto-update`` ####

 * Default: ``true``
 * Possible Values: ``false``, ``check``, ``true``

Whether or not Abyss should automatically check for and download updates. Set
this to ``false`` to disable checking for updates and downloading them, or to
``check`` to merely inform you about available updates.

**Note:** You can still use the **/abyss update** command to check for updates.

&nbsp;

#### ``entity-type-whitelist`` ####

A list of entity types that are allowed to go through portals to other worlds.
Entity types are limited to prevent Abyss from destroying an entity that it
can not correctly recreate after the teleportation. Hopefully, this won't
be needed after Bukkit updates.

&nbsp;

#### ``use-worldguard`` ####

 * Default: ``true``
 * Possible Values: ``true``, ``false``

If Abyss is set to use WorldGuard, it will use WorldGuard to track portal
locations and thus take advantage of WorldGuard's spatial tree for fast
look-ups. Otherwise, or if WorldGuard is not available, Abyss will use a less
efficient fallback class.

&nbsp;


## Portal Wand ##

#### ``wand-name`` ####

 * Default: ``Portal Wand``

The name of the Portal Wand item in-game. This string must be present in the
display names of any items you wish to function as portal wands, and it will
be used when creating a portal wand with the **/abwand** command.

&nbsp;

#### ``wand-material`` ####

 * Default: ``STICK``
 * Possible Values: *material*

The base item to use for the Portal Wand. Make this something a player is
not likely to frequently right-click with for best results.

&nbsp;

#### ``wand-range`` ####

  * Default: ``256``

The maximum range of the Portal Wand. 256 is the maximum distance a player can
see when they're using the Far viewing distance.

&nbsp;


## Portals ##

### Default Network ###

#### ``network-key`` ####

 * Default: ``SKULL ITEM:3``
 * Possible Values: *material*[*:data*]

The default item to use as a portal's network, used for new portals or when a
player left-clicks a portal's network frame.

&nbsp;

#### ``network-color`` ####

 * Default: ``WHITE``
 * Possible Values: *color*

The default color for a newly created network.

&nbsp;


### Portal Dimensions ###

#### ``minimum-depth`` ####

 * Default: ``2``
 * Minimum Value: ``2``

The minimum depth required to create a portal.

&nbsp;

#### ``minimum-size`` ####

 * Default: ``2``
 * Minimum Value: ``2``

The minimum side length required to create a portal.

&nbsp;

#### ``maximum-size`` ####

 * Default: ``4``

The maximum side length allowed when creating a portal without a special tool.

&nbsp;


### Effects ###

#### ``use-portal-effect`` ####

 * Default: ``true``
 * Possible Values: ``true``, ``false``

Whether or not to create visual and audible effect when an entity passes
through a portal.

&nbsp;

#### ``portal-effect`` ####

 * Default: ``MOBSPAWNER FLAMES``

The visual effect to display when an entity is teleported through a portal.
This may be given as just the name of the effect, or as the effect and
a data value.

&nbsp;

#### ``portal-sound`` ####

 * Default: ``ENDERMAN TELEPORT``

The sound effect to play when an entity is teleported through a portal. This
may be given as just the name of the sound effect, or as the name, volume,
and pitch.

&nbsp;

#### ``use-static-effect`` ####

 * Default: ``true``
 * Possible Values: ``true``, ``false``

Whether or not portals should have ambient effects in the world, shown at
random time intervals.

&nbsp;

#### ``static-effect-centered`` ####

 * Default: ``false``
 * Possible Values: ``true``, ``false``

Whether or not the static effect should be played at random positions,
horizontally, within the portal. This leads to a more visually appealing
effect.

&nbsp;

#### ``static-effect-full-height`` ####

 * Default: ``true``
 * Possible Values: ``true, false``

Whether or not the static effect should be played throughout the entire
height of the portal, or just at the very top.

&nbsp;

#### ``static-effect`` ####

The visual effect to display at random intervals within a portal as
ambiance. This defaults to ``portal-effect`` if not given.

&nbsp;

#### ``static-sound`` ####

Default:

    static-sound:
        name: PORTAL
        volume: 0.2
        pitch: 1

The sound effect to play at random intervals from a portal as ambiance.

&nbsp;


### Frames ###

#### ``single-material`` ####

 * Default: ``false``
 * Possible Values: ``true``, ``false``

If this is true, all blocks in a portal's frame must match.

&nbsp;

#### ``corner-depth`` ####

 * Default: ``1``

Require at least ``corner-depth`` layers of frame down to have corner blocks.
If this is set to zero, then portals won't require corner blocks at all. By
corner blocks, I refer to frame blocks not directly adjacent to the central
shaft of the portal.

&nbsp;

#### ``frame-materials`` ####

 * Default: ``[occluding]``

A list of materials that portal frames can be made of. Each entry in the list
may be either the name of a block, the string ``solid`` to match *every* solid
block, or the string ``occluding`` to match every occluding block. Additionally,
you may prefix the name or string with a negative sign to instead remove the
material from the list.

&nbsp;


### Velocity ###

#### ``minimum-velocity`` ####

 * Default: ``0.15``

The minimum velocity to allow for an entity leaving a portal. If an entity is
not moving at least this fast, their velocity will be multiplied till it
reaches this value.

&nbsp;

#### ``maximum-velocity`` ####

 * Default: ``10``

The maximum velocity to allow for an entity leaving a portal. If an entity is
moving faster than this, their velocity will be reduced down to this value.

**Note:** The default value is ``10`` to prevent the "moved too quickly"
anti-cheat protection from kicking in.

&nbsp;


### Configuration ###

#### ``cooldown-ticks`` ####

 * Default: ``40``

The amount of time, in ticks, that an entity is locked out from using the
portal they arrived through upon their arrival somewhere. This is to prevent
an entity falling immediately back in and being teleported away.

&nbsp;

#### ``maximum-modifiers`` ####

The maximum number of modifiers allowed in a single portal. By default, this
mirrors ``maximum-size`` to create a full row of modifiers.

&nbsp;

#### ``smart-rails`` ####

 * Default: ``true``
 * Possible Values: ``true``, ``false``

When smart rails are enabled, rails placed around the top of a portal will
have their behavior modified to make them connect to that portal, making it
easier to send multiple rail lines through a single portal.

&nbsp;


## Distance Calculations ##

#### ``limit-distance`` ####

 * Default: ``true``
 * Possible Values: ``true``, ``false``

When this is true, a portal's destinations are limited to the portals within a
certain range. This range varies depending on the ``base-range``, the depth of
the portal, and any modifiers that increase range, such as the **Ender Pearl**
and the **Eye of Ender**.

&nbsp;

#### ``base-range`` ####

 * Default: ``50``

The base range of a portal. This is added to the portal's depth, multiplied by
the ``depth-modifier`` to determine how far a portal's range is. When determining
if a destination is too far away, both the destination's range and the portal's
range are taken into account.

&nbsp;

#### ``depth-multiplier`` ####

 * Default: ``25``

The amount of range added for each block of depth the portal has.
