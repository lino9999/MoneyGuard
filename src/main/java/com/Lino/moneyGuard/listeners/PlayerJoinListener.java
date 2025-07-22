package com.Lino.moneyGuard.listeners;

import com.Lino.moneyGuard.MoneyGuard;
import com.Lino.moneyGuard.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final MoneyGuard plugin;

    public PlayerJoinListener(MoneyGuard plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());

        if (data.checkBanExpiry()) {
            plugin.getDataManager().saveAllData();
        }

        plugin.getEconomyManager().checkBalanceChange(player);
    }
}