package com.Lino.moneyGuard.listeners;

import com.Lino.moneyGuard.MoneyGuard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerTransactionListener implements Listener {

    private final MoneyGuard plugin;

    public PlayerTransactionListener(MoneyGuard plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        plugin.getEconomyManager().clearPlayerCache(player.getUniqueId());
        plugin.getDataManager().saveAllData();
    }
}