package me.kat.iplock.listener;

import me.kat.iplock.IPLockPlugin;
import me.kat.iplock.storage.IPStorage;
import me.kat.iplock.discord.DiscordWebhook;
import me.kat.iplock.util.LogManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.time.Instant;

public class PreLoginListener implements Listener {

    private final IPStorage storage;
    private final LogManager logManager;

    public PreLoginListener(IPStorage storage, LogManager logManager) {
        this.storage = storage;
        this.logManager = logManager;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();
        String ip = event.getAddress().getHostAddress();
        String uuid = event.getUniqueId().toString();

        if (IPLockPlugin.get().getConfig().getBoolean("debug.verbose", false)) {
            IPLockPlugin.get().getLogger()
                    .info("[VERBOSE] Player " + name + " (UUID: " + uuid + ") attempting login from IP: " + ip);
        }

        IPStorage.Entry uuidEntry = storage.getByUUID(uuid);
        IPStorage.Entry ipEntry = storage.getByIP(ip);
        boolean allowMultipleAccounts = IPLockPlugin.get().getConfig()
                .getBoolean("security.allow-multiple-accounts", true);
        int maxAccounts = IPLockPlugin.get().getConfig().getInt("security.max-accounts-per-ip", 5);

        if (uuidEntry != null && !uuidEntry.ip().equals(ip)) {
            String kickMessage = ChatColor.translateAlternateColorCodes('&',
                    IPLockPlugin.get().getConfig().getString("messages.uuid-ip-mismatch",
                            "UUID bound to different IP. Contact the owner if this is a mistake"));
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, kickMessage);
            logManager.log(name, ip, false,
                    "UUID bound to different IP (Expected: " + logManager.formatIp(uuidEntry.ip()) + ")");

            if (IPLockPlugin.get().getConfig().getBoolean("debug.verbose", false)) {
                IPLockPlugin.get().getLogger().info("[VERBOSE] UUID-IP binding violation for " + name + " (UUID: "
                        + uuid + "), expected IP: " + uuidEntry.ip() + ", got: " + ip + " - BLOCKED");
            }

            DiscordWebhook.sendAlert(
                    name,
                    uuidEntry.ip(),
                    ip,
                    Instant.now());

            return;
        }

        if (ipEntry != null && !ipEntry.uuid().equals(uuid)) {
            if (!allowMultipleAccounts) {
                String kickMessage = ChatColor.translateAlternateColorCodes('&',
                        IPLockPlugin.get().getConfig().getString("messages.ip-uuid-mismatch",
                                "IP already bound to another account"));
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, kickMessage);
                logManager.log(name, ip, false, "IP already bound to another UUID (Multi-account disabled)");

                if (IPLockPlugin.get().getConfig().getBoolean("debug.verbose", false)) {
                    IPLockPlugin.get().getLogger()
                            .info("[VERBOSE] IP-UUID binding violation for " + name + " (UUID: "
                                    + uuid + "), IP " + ip + " is already bound to another UUID - BLOCKED");
                }

                DiscordWebhook.sendAlert(
                        name,
                        ipEntry.uuid(), // Showing which UUID owns the IP
                        uuid, // Showing which UUID tried to use it
                        Instant.now());

                return;
            } else {
                int currentUsage = storage.getIPUsageCount(ip);
                if (uuidEntry == null && currentUsage >= maxAccounts) {
                    String kickMessage = ChatColor.translateAlternateColorCodes('&',
                            IPLockPlugin.get().getConfig().getString("messages.max-accounts",
                                    "Maximum number of accounts (" + maxAccounts + ") reached for this IP"));
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, kickMessage);
                    logManager.log(name, ip, false,
                            "Max accounts reached (" + currentUsage + "/" + maxAccounts + ")");

                    if (IPLockPlugin.get().getConfig().getBoolean("debug.verbose", false)) {
                        IPLockPlugin.get().getLogger().info("[VERBOSE] Max accounts reached for IP " + ip + " ("
                                + currentUsage + "/" + maxAccounts + ") - BLOCKED " + name);
                    }

                    DiscordWebhook.sendAlert(
                            name,
                            String.valueOf(currentUsage),
                            String.valueOf(maxAccounts),
                            Instant.now());

                    return;
                }
            }
        }

        if (uuidEntry == null) {
            storage.bind(name, uuid, ip);
            logManager.log(name, ip, true, "First login - UUID and IP bound");
            if (IPLockPlugin.get().getConfig().getBoolean("debug.verbose", false)) {
                IPLockPlugin.get().getLogger().info(
                        "[VERBOSE] First login for " + name + " (UUID: " + uuid + "), binding UUID and IP: " + ip);
            }
        } else {
            logManager.log(name, ip, true, "UUID-IP match");
            if (IPLockPlugin.get().getConfig().getBoolean("debug.verbose", false)) {
                IPLockPlugin.get().getLogger()
                        .info("[VERBOSE] Successful login for " + name + " (UUID: " + uuid + ") from IP: " + ip);
            }
        }
    }
}
