package com.Lino.moneyGuard.managers;

import com.Lino.moneyGuard.MoneyGuard;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final MoneyGuard plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;
    private final Map<String, String> cachedMessages;
    private String prefix;

    public MessageManager(MoneyGuard plugin) {
        this.plugin = plugin;
        this.cachedMessages = new HashMap<>();
    }

    public void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        InputStream defConfigStream = plugin.getResource("messages.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            messagesConfig.setDefaults(defConfig);
        }

        // Load prefix separately
        prefix = colorize(messagesConfig.getString("prefix", "&6[MoneyGuard] &r"));

        cacheMessages();
    }

    private void cacheMessages() {
        cachedMessages.clear();
        for (String key : messagesConfig.getKeys(true)) {
            if (!messagesConfig.isConfigurationSection(key) && !key.equals("prefix")) {
                String message = messagesConfig.getString(key);
                // Replace {prefix} placeholder with actual prefix
                message = message.replace("{prefix}", prefix);
                cachedMessages.put(key, colorize(message));
            }
        }
    }

    public String getMessage(String path) {
        return cachedMessages.getOrDefault(path, colorize("&cMessage not found: " + path));
    }

    public String getMessage(String path, Object... replacements) {
        String message = getMessage(path);

        if (replacements.length % 2 != 0) {
            return message;
        }

        for (int i = 0; i < replacements.length; i += 2) {
            String placeholder = String.valueOf(replacements[i]);
            String value = String.valueOf(replacements[i + 1]);
            message = message.replace(placeholder, value);
        }

        return message;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void saveMessages() {
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save messages.yml!");
        }
    }
}