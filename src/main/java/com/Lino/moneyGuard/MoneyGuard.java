package com.Lino.moneyGuard;

import com.Lino.moneyGuard.commands.MoneyGuardCommand;
import com.Lino.moneyGuard.listeners.PlayerJoinListener;
import com.Lino.moneyGuard.listeners.PlayerTransactionListener;
import com.Lino.moneyGuard.managers.*;
import com.Lino.moneyGuard.tasks.EconomyCheckTask;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MoneyGuard extends JavaPlugin {

    private static MoneyGuard instance;
    private Economy economy;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private EconomyManager economyManager;
    private DataManager dataManager;
    private AlertManager alertManager;
    private ActionManager actionManager;
    private LogManager logManager;
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().severe("Vault dependency not found! Disabling MoneyGuard...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        initializeManagers();
        registerListeners();
        registerCommands();
        startTasks();

        getLogger().info("MoneyGuard v" + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        if (statsManager != null) {
            statsManager.saveStats();
        }
        getLogger().info("MoneyGuard has been disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    private void initializeManagers() {
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        economyManager = new EconomyManager(this);
        dataManager = new DataManager(this);
        alertManager = new AlertManager(this);
        actionManager = new ActionManager(this);
        logManager = new LogManager(this);
        statsManager = new StatsManager(this);

        configManager.loadConfig();
        messageManager.loadMessages();
        dataManager.loadData();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerTransactionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    private void registerCommands() {
        MoneyGuardCommand command = new MoneyGuardCommand(this);
        getCommand("moneyguard").setExecutor(command);
        getCommand("moneyguard").setTabCompleter(command);
        getCommand("mg").setExecutor(command);
        getCommand("mg").setTabCompleter(command);
    }

    private void startTasks() {
        long interval = configManager.getCheckInterval() * 20L;
        new EconomyCheckTask(this).runTaskTimer(this, interval, interval);
    }

    public void reload() {
        configManager.loadConfig();
        messageManager.loadMessages();
        statsManager.saveStats();
    }

    public static MoneyGuard getInstance() {
        return instance;
    }

    public Economy getEconomy() {
        return economy;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }
}