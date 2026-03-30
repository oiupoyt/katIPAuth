package me.kat.iplock.util;

import me.kat.iplock.IPLockPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigUpdater {

    private final IPLockPlugin plugin;

    public ConfigUpdater(IPLockPlugin plugin) {
        this.plugin = plugin;
    }

    public void updateConfig() {
        FileConfiguration config = plugin.getConfig();
        boolean updated = false;

        plugin.getLogger().info("Checking config for missing settings...");

        if (!config.isSet("discord.webhook")) {
            plugin.getLogger().info("Adding missing discord.webhook setting");
            config.set("discord.webhook", "PUT_WEBHOOK_URL_HERE");
            config.setComments("discord", List.of("Discord Webhook URL for logging IP changes (optional)"));
            updated = true;
        }

        if (!config.isSet("privacy.mask-ips")) {
            plugin.getLogger().info("Adding missing privacy.mask-ips setting");
            config.set("privacy.mask-ips", false);
            config.setComments("privacy", List.of("Privacy settings"));
            config.setInlineComments("privacy.mask-ips",
                    List.of("If true, last two octets of IPs will be replaced with x (e.g. 192.168.xx.xx)"));
            updated = true;
        }

        if (!config.isSet("debug.verbose")) {
            plugin.getLogger().info("Adding missing debug.verbose setting");
            config.set("debug.verbose", false);
            config.setComments("debug", List.of("Debug settings"));
            config.setInlineComments("debug.verbose",
                    List.of("If true, print debug info in console for player joins, blocks, IP changes, etc."));
            updated = true;
        }

        if (!config.isSet("messages.blocked")) {
            plugin.getLogger().info("Adding missing messages settings");
            config.set("messages.blocked",
                    "&cLogin blocked. If you believe this is a mistake contact the owner");
            config.set("messages.uuid-ip-mismatch",
                    "&cLogin blocked: UUID bound to different IP. Contact the owner if this is a mistake");
            config.set("messages.ip-uuid-mismatch",
                    "&cLogin blocked: IP already bound to another account");
            config.set("messages.max-accounts",
                    "&cLogin blocked: Maximum number of accounts reached for this IP");
            config.setComments("messages", List.of("Customizable messages"));
            updated = true;
        }

        if (!config.isSet("security.allow-multiple-accounts")) {
            plugin.getLogger().info("Adding missing security settings");

            boolean allowMulti = config.getBoolean("security.allow-multiple-accounts", true);
            int maxAccounts = config.getInt("security.max-accounts-per-ip", 2);

            config.set("security.allow-multiple-accounts", allowMulti);
            config.set("security.max-accounts-per-ip", maxAccounts);

            config.setComments("security", List.of("Security settings"));
            config.setInlineComments("security.allow-multiple-accounts",
                    List.of("If true, multiple UUIDs can bind to the same IP."));
            config.setInlineComments("security.max-accounts-per-ip",
                    List.of("Maximum number of accounts allowed per IP. Need allow-multiple-accounts=true for this to work."));

            // Remove legacy setting if it exists
            if (config.isSet("security.uuid-ip-binding")) {
                config.set("security.uuid-ip-binding", null);
            }
            updated = true;
        }

        if (!config.isSet("beta.subnet-locking")) {
            plugin.getLogger().info("Adding missing beta.subnet-locking setting");
            config.set("beta.subnet-locking", false);
            config.setComments("beta", List.of("Beta features (Use with caution)"));
            config.setInlineComments("beta.subnet-locking",
                    List.of("If true, players can join as long as they are in the same /24 subnet (e.g. 192.168.1.*)"));
            updated = true;
        }

        if (updated) {
            plugin.saveConfig();
            plugin.reloadConfig();
            plugin.getLogger().info("Config.yml has been updated with missing settings.");
        } else {
            plugin.getLogger().info("Config.yml is up to date.");
        }
    }
}