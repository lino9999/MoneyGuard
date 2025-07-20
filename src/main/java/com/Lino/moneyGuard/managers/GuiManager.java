package com.Lino.moneyGuard.managers;

import com.Lino.moneyGuard.MoneyGuard;
import com.Lino.moneyGuard.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class GuiManager implements Listener {

    private final MoneyGuard plugin;
    private final Map<UUID, String> openGuis;

    public GuiManager(MoneyGuard plugin) {
        this.plugin = plugin;
        this.openGuis = new HashMap<>();
    }

    public void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "MoneyGuard Control Panel");

        gui.setItem(10, createItem(Material.EMERALD, "&aPlayer List",
                Arrays.asList("&7View all online players", "&7and their money stats")));

        gui.setItem(12, createItem(Material.BOOK, "&eSettings",
                Arrays.asList("&7Configure limits and", "&7monitoring options")));

        gui.setItem(14, createItem(Material.REDSTONE, "&cWarnings & Bans",
                Arrays.asList("&7View and manage", "&7player warnings")));

        gui.setItem(16, createItem(Material.PAPER, "&bReports",
                Arrays.asList("&7View transaction logs", "&7and statistics")));

        fillBorder(gui, Material.GRAY_STAINED_GLASS_PANE);

        player.openInventory(gui);
        openGuis.put(player.getUniqueId(), "main");
    }

    public void openPlayerList(Player viewer) {
        Inventory gui = Bukkit.createInventory(null, 54, "Online Players");

        int slot = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (slot >= 45) break;

            ItemStack head = createPlayerHead(player);
            gui.setItem(slot++, head);
        }

        gui.setItem(49, createItem(Material.ARROW, "&cBack", Collections.singletonList("&7Return to main menu")));

        viewer.openInventory(gui);
        openGuis.put(viewer.getUniqueId(), "players");
    }

    public void openPlayerInfo(Player viewer, Player target) {
        PlayerData data = plugin.getDataManager().getPlayerData(target.getUniqueId());
        double balance = plugin.getEconomyManager().getBalance(target);
        double gainedHour = plugin.getEconomyManager().getMoneyGainedInLastHour(target.getUniqueId());
        double gainedDay = data.getMoneyGainedToday();

        Inventory gui = Bukkit.createInventory(null, 45, "Player: " + target.getName());

        ItemStack info = createPlayerHead(target);
        ItemMeta meta = info.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add("&7Current Balance: &a$" + String.format("%.2f", balance));
        lore.add("&7Gained (Hour): &e$" + String.format("%.2f", gainedHour));
        lore.add("&7Gained (Day): &e$" + String.format("%.2f", gainedDay));
        lore.add("&7Warnings: &c" + data.getWarnings());
        lore.add("&7Status: " + (data.isBanned() ? "&cBanned" : "&aActive"));
        meta.setLore(colorize(lore));
        info.setItemMeta(meta);

        gui.setItem(13, info);

        gui.setItem(29, createItem(Material.YELLOW_WOOL, "&eWarn Player",
                Arrays.asList("&7Issue a warning to", "&7this player")));

        gui.setItem(31, createItem(Material.ORANGE_WOOL, "&6Reset Warnings",
                Arrays.asList("&7Clear all warnings", "&7for this player")));

        gui.setItem(33, createItem(Material.RED_WOOL, "&cBan Player",
                Arrays.asList("&7Temporarily ban", "&7this player")));

        gui.setItem(40, createItem(Material.ARROW, "&cBack", Collections.singletonList("&7Return to player list")));

        fillBorder(gui, Material.GRAY_STAINED_GLASS_PANE);

        viewer.openInventory(gui);
        openGuis.put(viewer.getUniqueId(), "player:" + target.getName());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String guiType = openGuis.get(player.getUniqueId());

        if (guiType == null) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        int slot = event.getRawSlot();

        if (guiType.equals("main")) {
            handleMainMenuClick(player, slot);
        } else if (guiType.equals("players")) {
            handlePlayerListClick(player, slot, event.getCurrentItem());
        } else if (guiType.startsWith("player:")) {
            handlePlayerInfoClick(player, slot, guiType.substring(7));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        openGuis.remove(event.getPlayer().getUniqueId());
    }

    private void handleMainMenuClick(Player player, int slot) {
        switch (slot) {
            case 10:
                openPlayerList(player);
                break;
            case 12:
                player.sendMessage(plugin.getMessageManager().getMessage("gui.settings-command"));
                player.closeInventory();
                break;
            case 14:
                player.sendMessage(plugin.getMessageManager().getMessage("gui.warnings-command"));
                player.closeInventory();
                break;
            case 16:
                player.sendMessage(plugin.getMessageManager().getMessage("gui.reports-command"));
                player.closeInventory();
                break;
        }
    }

    private void handlePlayerListClick(Player player, int slot, ItemStack item) {
        if (slot == 49) {
            openMainMenu(player);
            return;
        }

        if (slot < 45 && item.getType() == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            String targetName = meta.getDisplayName().replace("§a", "");
            Player target = Bukkit.getPlayer(targetName);

            if (target != null) {
                openPlayerInfo(player, target);
            }
        }
    }

    private void handlePlayerInfoClick(Player player, int slot, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("errors.player-offline"));
            player.closeInventory();
            return;
        }

        switch (slot) {
            case 29:
                plugin.getActionManager().warnPlayer(target, "Manual warning by " + player.getName());
                player.sendMessage(plugin.getMessageManager().getMessage("gui.warned-player", "{player}", targetName));
                openPlayerInfo(player, target);
                break;
            case 31:
                plugin.getActionManager().resetWarnings(target.getUniqueId());
                player.sendMessage(plugin.getMessageManager().getMessage("gui.reset-warnings", "{player}", targetName));
                openPlayerInfo(player, target);
                break;
            case 33:
                plugin.getActionManager().banPlayer(target, "Manual ban by " + player.getName());
                player.sendMessage(plugin.getMessageManager().getMessage("gui.banned-player", "{player}", targetName));
                openPlayerList(player);
                break;
            case 40:
                openPlayerList(player);
                break;
        }
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(colorize("&r" + name));
        meta.setLore(colorize(lore));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(colorize("&a" + player.getName()));
        head.setItemMeta(meta);
        return head;
    }

    private void fillBorder(Inventory inventory, Material material) {
        ItemStack glass = new ItemStack(material);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);

        int size = inventory.getSize();
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, glass);
            inventory.setItem(size - 9 + i, glass);
        }

        for (int i = 9; i < size - 9; i += 9) {
            inventory.setItem(i, glass);
            inventory.setItem(i + 8, glass);
        }
    }

    private String colorize(String text) {
        return text.replace("&", "§");
    }

    private List<String> colorize(List<String> list) {
        List<String> colored = new ArrayList<>();
        for (String line : list) {
            colored.add(colorize(line));
        }
        return colored;
    }
}