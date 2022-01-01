package com.destillegast;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Fix extends JavaPlugin implements Listener {

    private final HashMap<String, Date> delaymap = new HashMap<>();
    private String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "Fix" + ChatColor.DARK_GRAY + "] " + ChatColor.WHITE;

    private final NamespacedKey antiRepairKey = new NamespacedKey(this, "denyRepair");

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        prefix = col(getConfig().getString("prefix", prefix));


        getCommand("fix").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("fix.hand")) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (item.getType() == Material.AIR) return false;
                    if (canItemBeRepaired(item)) {
                        if (item.getItemMeta() instanceof Damageable) {
                            if (delaymap.containsKey(player.getName())) {
                                Date d = delaymap.get(player.getName());
                                long differenceInMillis = Calendar.getInstance().getTime().getTime() - d.getTime();
                                long differenceInMin = differenceInMillis / 1000L / 60L;

                                boolean hasUpgrade = player.hasPermission("fix.hand.quicker");

                                long waitTime = hasUpgrade ? getConfig().getLong("Wait time fast", 30) : getConfig().getLong("Wait time", 60);

                                if (differenceInMin < waitTime) {
                                    int time = (int) (waitTime - differenceInMin);
                                    player.sendMessage(col(prefix + getConfig().getString("wait").replace("{time}", time + "")));
                                } else {
                                    delaymap.remove(player.getName());

                                    ItemMeta meta = item.getItemMeta();
                                    ((Damageable) meta).setDamage(0);
                                    item.setItemMeta(meta);

                                    player.sendMessage(col(prefix + getConfig().getString("fixed")));
                                    delaymap.put(player.getName(), Calendar.getInstance().getTime());
                                }
                            } else {
                                ItemMeta meta = item.getItemMeta();
                                ((Damageable) meta).setDamage(0);
                                item.setItemMeta(meta);

                                player.sendMessage(col(prefix + getConfig().getString("fixed")));
                                delaymap.put(player.getName(), Calendar.getInstance().getTime());
                            }
                        } else {
                            player.sendMessage(col(prefix + getConfig().getString("mag niet repareren")));
                        }
                    } else {
                        player.sendMessage(col(prefix + getConfig().getString("mag niet repareren")));
                    }
                } else {
                    player.sendMessage(col(prefix + getConfig().getString("geen permissie")));
                }
            } else {
                sender.sendMessage("Ingame command only");
            }

            return true;
        });

        getCommand("makeunrepairable").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (player.hasPermission("fix.create")) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if(item.getItemMeta() != null){
                        ItemMeta im = item.getItemMeta();

                        if(im.getPersistentDataContainer().has(antiRepairKey, PersistentDataType.BYTE)){
                            player.sendMessage(col(prefix + "Item has already been set as unrepairable"));
                            return true;
                        }



                        im.getPersistentDataContainer().set(antiRepairKey, PersistentDataType.BYTE, (byte)1);
                        if(im instanceof Repairable){
                            ((Repairable) im).setRepairCost(Integer.MAX_VALUE);
                        }

                        List<String> currentLore = im.getLore();
                        if(currentLore == null) currentLore = new ArrayList<>();

                        currentLore.add(ChatColor.DARK_RED + "Unfixable");
                        im.setLore(currentLore);


                        item.setItemMeta(im);
                        player.sendMessage(col(prefix + "Item has been set as unrepairable"));
                    }
                } else {
                    player.sendMessage(col(prefix + getConfig().getString("geen permissie")));
                }
            } else {
                sender.sendMessage("Ingame command only");
            }
            return false;
        });
    }

    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (cmd.getName().equalsIgnoreCase("fix")) {

            }

//            if (cmd.getName().equalsIgnoreCase("setlore")) {
//                if (player.getInventory().getItemInMainHand().getType() == Material.AIR) return false;
//                if (!player.isOp()) {
//                    player.sendMessage(col(prefix + getConfig().getString("geen permissie")));
//                    return false;
//                }
//
//                if (args.length > 0) {
//                    String lore = ChatColor.translateAlternateColorCodes('&', args[0]);
//                    String[] lores = lore.split(",");
//                    List<String> loresl = new ArrayList<>(Arrays.asList(lores));
//                    ItemMeta m = player.getInventory().getItemInMainHand().getItemMeta();
//                    m.setLore(loresl);
//                    player.getInventory().getItemInMainHand().setItemMeta(m);
//                    player.sendMessage(col(prefix + "Lore succesful set to '" + lore + "'"));
//                } else {
//                    player.sendMessage(col(prefix + "Voer een lore in"));
//                }
//            }


        }
        return false;
    }

//    @EventHandler
//    public void onInventoryClick(InventoryClickEvent event) {
//        if (event.getWhoClicked() instanceof Player) {
//            if (event.getInventory().getType().equals(InventoryType.ANVIL)) {
//                try {
//                    ItemStack item = event.getCurrentItem();
//                    if (item != null && item.hasItemMeta()) {
//                        if (checkLore(item.getItemMeta().getLore())) {
//                            event.setCancelled(true);
//                        }
//                    }
//                } catch (Exception ignored) {
//
//                }
//            }
//        }
//    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            if (event.getInventory().getType().equals(InventoryType.ANVIL)) {
                try {
                    ItemStack item = event.getCurrentItem();
                    if (!canItemBeRepaired(item)) {
                        event.setCancelled(true);
                    }
                } catch (Exception ignored) {

                }
            }
        }
    }

    //heeft het illegale lore ?
    private boolean checkLore(List<String> lores) {
        if (lores == null) {
            return false;
        }

        for (String lore : lores) {
            for (String nolore : getConfig().getStringList("disabled_lores")) {
                nolore = col(nolore);
                //lore = ChatColor.translateAlternateColorCodes('&', lore);
                //lore = ChatColor.stripColor(lore);//ChatColor.translateAlternateColorCodes('&', nolore);
                //System.out.pintln(nolore);
                if (lore.toLowerCase().contains(nolore.toLowerCase())) {
                    //System.out.println("found");
                    return true;
                }
            }

        }
        return false;
    }

    private boolean canItemBeRepaired(ItemStack itemStack) {
        if (itemStack == null || itemStack.getItemMeta() == null) return true;

        ItemMeta im = itemStack.getItemMeta();
        return !im.getPersistentDataContainer().has(antiRepairKey, PersistentDataType.BYTE);
    }

    private String col(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

}