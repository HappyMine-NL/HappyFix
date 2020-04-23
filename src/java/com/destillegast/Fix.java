package com.destillegast;

import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Fix extends JavaPlugin implements Listener {

    private HashMap<String, Date> delaymap = new HashMap<String, Date>();
    private String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "Fix" + ChatColor.DARK_GRAY + "] " + ChatColor.WHITE;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        prefix = col(getConfig().getString("prefix", prefix));
    }

    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (cmd.getName().equalsIgnoreCase("fix")) {
                if (player.hasPermission("fix.hand")) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (item.getType() == Material.AIR) return false;
                    if (!checkLore(item.getItemMeta().getLore())) {
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
//                                ((Damageable) item.getItemMeta()).setDamage(0);

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
            }

            if (cmd.getName().equalsIgnoreCase("setlore")) {
                if (player.getInventory().getItemInMainHand().getType() == Material.AIR) return false;
                if (!player.isOp()) {
                    player.sendMessage(col(prefix + getConfig().getString("geen permissie")));
                    return false;
                }

                if (args.length > 0) {
                    String lore = ChatColor.translateAlternateColorCodes('&', args[0]);
                    String[] lores = lore.split(",");
                    List<String> loresl = new ArrayList<>(Arrays.asList(lores));
                    ItemMeta m = player.getInventory().getItemInMainHand().getItemMeta();
                    m.setLore(loresl);
                    player.getInventory().getItemInMainHand().setItemMeta(m);
                    player.sendMessage(col(prefix + "Lore succesful set to '" + lore + "'"));
                } else {
                    player.sendMessage(col(prefix + "Voer een lore in"));
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            if (event.getInventory().getType().equals(InventoryType.ANVIL)) {
                try {
                    ItemStack item = event.getCurrentItem();
                    if (item != null && item.hasItemMeta()) {
                        if (checkLore(item.getItemMeta().getLore())) {
                            event.setCancelled(true);
                        }
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

    private String col(String string){
        return ChatColor.translateAlternateColorCodes('&', string);
    }

}