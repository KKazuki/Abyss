package me.stendec.abyss.modifiers;

import me.stendec.abyss.ABPortal;
import me.stendec.abyss.ModInfo;
import me.stendec.abyss.PortalModifier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Random;

public class DispenserModifier extends PortalModifier {

    private final Random random;

    public DispenserModifier() {
        random = new Random();
    }

    public void postTeleport(final ABPortal from, final ABPortal portal, final ModInfo info, final Entity entity) {
        // Make sure this modifier belongs to the destination.
        final ABPortal owner = info.getPortal();
        if ( owner == null || owner.equals(from) )
            return;

        final Block block = info.location.getBlock();
        boolean sent = false;

        // If we don't have a chest,
        if (block.getType() != Material.CHEST || !(entity instanceof InventoryHolder))
            return;

        // Configuration Flags
        final boolean dispense = true;
        final boolean single = ! info.flags.containsKey("full stack");
        final boolean clone = info.flags.containsKey("clone");
        final boolean drop = ! info.flags.containsKey("never drop");
        final int equip = parseEquip(info.flags.get("equip"));
        final int stack = parseStack(info.flags.get("select"));

        final Location location = entity.getLocation();
        final Chest chest = (Chest) block.getState();
        final Inventory inv = chest.getInventory();

        // Whatever we do, we need a copy of the inventory.
        final ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        for(ItemStack i: inv) {
            if (i == null || i.getType() == Material.AIR)
                continue;

            i = i.clone();
            if (single)
                i.setAmount(1);

            items.add(i);

            if (stack == 1)
                break;
        }

        // Randomness
        if (stack == 0)
            if (items.size() > 1) {
                ItemStack i = items.get(random.nextInt(items.size()));
                items.clear();
                items.add(i);
            }

        final boolean player = entity instanceof Player;
        final Inventory einv = ((InventoryHolder) entity).getInventory();
        for(ItemStack i: items) {
            Material m = i.getType();
            boolean s = false;

            if (dispense) {
                if (m == Material.FLINT_AND_STEEL) {
                    // Set the entity on fire for 8 seconds.
                    if (entity.getFireTicks() < 160)
                        entity.setFireTicks(160);

                    // Update durability if necessary.
                    if (!clone) {
                        int dmg = i.getDurability() + 1;
                        if (dmg >= 65)
                            inv.removeItem(i);
                        else
                            i.setDurability((short) dmg);
                    }

                    // Don't do normal processing for this.
                    sent = true;
                    continue;

                } else if (player && equip != 3 && einv instanceof PlayerInventory) {
                    // Warning: This code is ridiculous.
                    PlayerInventory pinv = (PlayerInventory) einv;

                    if (isHelmet(m)) {
                        ItemStack helm = pinv.getHelmet();
                        if (equip == 2) {
                            for(ItemStack dnf: pinv.addItem(helm).values())
                                location.getWorld().dropItemNaturally(location, dnf);
                            helm = null;
                        }

                        if (helm == null || helm.getType() == Material.AIR) {
                            pinv.setHelmet(i);
                            s = true;
                        } else if ( equip == 1 )
                            continue;

                    } else if (isChestplate(m)) {
                        ItemStack cp = pinv.getChestplate();
                        if (equip == 2) {
                            for(ItemStack dnf: pinv.addItem(cp).values())
                                location.getWorld().dropItemNaturally(location, dnf);
                            cp = null;
                        }

                        if (cp == null || cp.getType() == Material.AIR) {
                            pinv.setChestplate(i);
                            s = true;
                        } else if ( equip == 1 )
                            continue;

                    } else if (isLegging(m)) {
                        ItemStack leg = pinv.getLeggings();
                        if (equip == 2) {
                            for(ItemStack dnf: pinv.addItem(leg).values())
                                location.getWorld().dropItemNaturally(location, dnf);
                            leg = null;
                        }

                        if (leg == null || leg.getType() == Material.AIR) {
                            pinv.setLeggings(i);
                            s = true;
                        } else if ( equip == 1 )
                            continue;

                    } else if (isBoots(m)) {
                        ItemStack b = pinv.getBoots();
                        if (equip == 2) {
                            for(ItemStack dnf: pinv.addItem(b).values())
                                location.getWorld().dropItemNaturally(location, dnf);
                            b = null;
                        }

                        if (b == null || b.getType() == Material.AIR) {
                            pinv.setBoots(i);
                            s = true;
                        } else if ( equip == 1 )
                            continue;
                    }
                }
            }

            if (!s) {
                if (drop) {
                    // Either give the item to the entity or drop it naturally.
                    for(ItemStack dnf: einv.addItem(i).values())
                        location.getWorld().dropItemNaturally(location, dnf);

                    s = true;
                } else {
                    s = einv.addItem(i).isEmpty();
                }
            }

            if (s) {
                sent = true;

                if (!clone) {
                    // Remove the item from the chest's inventory.
                    inv.removeItem(i);
                }
            }
        }

        // Play the appropriate sound.
        location.getWorld().playSound(location, Sound.CLICK, 1, sent ? 1 : 1.2f);
    }

    // Helpers

    private static boolean isHelmet(final Material material) {
        switch(material) {
            case GOLD_HELMET:
            case IRON_HELMET:
            case DIAMOND_HELMET:
            case LEATHER_HELMET:
            case CHAINMAIL_HELMET:
                return true;
            default:
                return false;
        }
    }

    private static boolean isBoots(final Material material) {
        switch(material) {
            case GOLD_BOOTS:
            case IRON_BOOTS:
            case DIAMOND_BOOTS:
            case LEATHER_BOOTS:
            case CHAINMAIL_BOOTS:
                return true;
            default:
                return false;
        }
    }

    private static boolean isChestplate(final Material material) {
        switch(material) {
            case GOLD_CHESTPLATE:
            case IRON_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
                return true;
            default:
                return false;
        }
    }

    private static boolean isLegging(final Material material) {
        switch(material) {
            case GOLD_LEGGINGS:
            case IRON_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case LEATHER_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
                return true;
            default:
                return false;
        }
    }

    private static int parseStack(final String stack) {
        if (stack == null)
            return 0;

        final String mode = stack.toUpperCase();

        if (mode.equals("RANDOM")) return 0;
        if (mode.equals("FIRST")) return 1;
        if (mode.equals("ALL")) return 2;

        int out = -1;
        try {
            out = Integer.parseInt(mode);
        } catch(NumberFormatException ex) {}

        if (out < 0 || out > 2 || mode.length() == 0 )
            throw new IllegalArgumentException("mode must be 'RANDOM', 'FIRST', 'ALL', 0, 1, or 2");

        return out;
    }

    private static int parseEquip(final String equip) {
        if (equip == null)
            return 0;

        final String mode = equip.toUpperCase();

        if (mode.equals("IF EMPTY")) return 0;
        if (mode.equals("ONLY")) return 1;
        if (mode.equals("FORCE")) return 2;
        if (mode.equals("NEVER")) return 3;

        int out = -1;
        try {
            out = Integer.parseInt(mode);
        } catch(NumberFormatException ex) {}

        if (out < 0 || out > 3 || mode.length() == 0 )
            throw new IllegalArgumentException("mode must be 'IF EMPTY', 'ONLY', 'FORCE', 'NEVER', 0, 1, 2, or 3");

        return out;
    }

}
