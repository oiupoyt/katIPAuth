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

public class SubnetPreLoginListener implements Listener {

    private final IPStorage storage;
    private final LogManager logManager;

    public SubnetPreLoginListener(IPStorage storage, LogManager logManager) {
        this.storage = storage;
        this.logManager = logManager;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();
        String currentIp = event.getAddress().getHostAddress();
        String uuid = event.getUniqueId().toString();

        if (IPLockPlugin.get().getConfig().getBoolean("debug.verbose", false)) {
            IPLockPlugin.get().getLogger()
                    .info("[VERBOSE] Player " + name + " (UUID: " + uuid + ") attempting login from IP: " + currentIp);
        }

        boolean uuidIpBinding = IPLockPlugin.get().getConfig().getBoolean("security.uuid-ip-binding", false);

        if (uuidIpBinding) {
            // UUID-IP binding enabled with subnet logic
            IPStorage.Entry uuidEntry = storage.getByUUID(uuid);
            IPStorage.Entry ipEntry = storage.getByIP(currentIp);
            boolean allowMultipleAccounts = IPLockPlugin.get().getConfig()
                    .getBoolean("security.allow-multiple-accounts", true);
            int maxAccounts = IPLockPlugin.get().getConfig().getInt("security.max-accounts-per-ip", 5);

            // Check if UUID is already bound to a different IP
            if (uuidEntry != null && !isSameSubnet(uuidEntry.ip(), currentIp)) {
                String kickMessage = ChatColor.translateAlternateColorCodes('&',
                        IPLockPlugin.get().getConfig().getString("messages.blocked",
                                "UUID-IP subnet binding violation. Access denied. Contact the owner if you believe this is a mistake"));
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, kickMessage);
                logManager.log(name, currentIp, false,
                        "UUID bound to different subnet (Expected: " + logManager.formatIp(uuidEntry.ip()) + ")");

                if (IPLockPlugin.get().getConfig().getBoolean("debug.verbose", false)) {
                    IPLockPlugin.get().getLogger()
                            .info("[VERBOSE] UUID-IP subnet binding violation for " + name + " (UUID: " + uuid
                                    + "), expected subnet of: " + uuidEntry.ip() + ", got: " + currentIp
                                    + " - BLOCKED");
                }
                return;
            }

            // Check if IP is already used by a different account
            if (ipEntry != null && !ipEntry.uuid().equals(uuid)) {
                if (!allowMultipleAccounts) {
                    String kickMessage = ChatColor.translateAlternateColorCodes('&',
                            IPLockPlugin.get().getConfig().getString("messages.blocked",
                                    "This IP is already bound to another account. Access denied."));
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, kickMessage);
                    logManager.log(name, currentIp, false,
                            "IP subnet already bound to another UUID (Multi-account disabled)");

                    if (IPLockPlugin.get().getConfig().getBoolean("debug.verbose", false)) {
                        IPLockPlugin.get().getLogger()
                                .info("[VERBOSE] IP-UUID subnet binding violation for " + name + " (UUID: " + uuid
                                        + "), IP " + currentIp + " subnet is already bound to another UUID - BLOCKED");
                    }
                    return;
                } else {
                    // Multi-account enabled, check max accounts
                    int currentUsage = storage.getIPUsageCount(currentIp);
                    if (uuidEntry == null && currentUsage >= maxAccounts) {
                        String kickMessage = ChatColor.translateAlternateColorCodes('&',
                                IPLockPlugin.get().getConfig().getString("messages.blocked",
                                        "Maximum number of accounts (" + maxAccounts + ") reached for this IP."));
                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, kickMessage);
                        logManager.log(name, currentIp, false,
                                "Max accounts reached (" + currentUsage + "/" + maxAccounts + ")");

                        if (IPLockPlugin.get().getConfig().getBoolean("debug.verbose", false)) {
                            IPLockPlugin.get().getLogger()
                                    .info("[VERBOSE] Max accounts reached for IP " + currentIp + " ("
                                            + currentUsage + "/" + maxAccounts + ") - BLOCKED " + name);
                        }
                        return;
                    }
                }
            }

            // First login or valid binding
            if (uuidEntry == null) {
                storage.bind(name, uuid, currentIp);
                logManager.log(name, currentIp, true, "First login - UUID and IP subnet bound");
                if (IPLockPlugin.get().getConfig().getBoolean("debug.verbose", false)) {
                    IPLockPlugin.get().getLogger().info("[VERBOSE] First login for " + name + " (UUID: " + uuid
                            + "), binding UUID and IP subnet: " + currentIp);
                }
            } else {
                logManager.log(name, currentIp, true, "UUID-IP subnet match");
                if (IPLockPlugin.get().getConfig().getBoolean("debug.verbose", false)) {
                    IPLockPlugin.get().getLogger().info("[VERBOSE] Successful login for " + name + " (UUID: " + uuid
                            + ") from IP: " + currentIp + " (subnet match)");
                }
            }
        } else {
            // Original subnet-only binding logic
            IPStorage.Entry entry = storage.get(name);

            if (entry == null) {
                storage.bind(name, currentIp);
                logManager.log(name, currentIp, true, "First login - IP bound");
                if (IPLockPlugin.get().getConfig().getBoolean("debug.verbose", false)) {
                    IPLockPlugin.get().getLogger()
                            .info("[VERBOSE] First login for " + name + ", binding IP: " + currentIp);
                }
                return;
            }

            String storedIp = entry.ip();

            if (isSameSubnet(storedIp, currentIp)) {
                logManager.log(name, currentIp, true, "Subnet match (Stored: " + logManager.formatIp(storedIp) + ")");
                if (IPLockPlugin.get().getConfig().getBoolean("debug.verbose", false)) {
                    IPLockPlugin.get().getLogger()
                            .info("[VERBOSE] Successful login for " + name + " from IP: " + currentIp
                                    + " (subnet match with " + storedIp + ")");
                }
                return;
            }

            // Blocking login
            String kickMessage = ChatColor.translateAlternateColorCodes('&',
                    IPLockPlugin.get().getConfig().getString("messages.blocked",
                            "IP mismatch. Access denied. Contact the owner if you believe this is a mistake"));
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    kickMessage);

            logManager.log(name, currentIp, false,
                    "Subnet mismatch (Expected: " + logManager.formatIp(storedIp) + ".x)");

            if (IPLockPlugin.get().getConfig().getBoolean("debug.verbose", false)) {
                IPLockPlugin.get().getLogger().info("[VERBOSE] Subnet mismatch for " + name + ", expected subnet of: "
                        + storedIp + ", got: " + currentIp + " - BLOCKED");
            }

            DiscordWebhook.sendAlert(
                    name,
                    storedIp,
                    currentIp,
                    Instant.now());
        }
    }

    private boolean isSameSubnet(String ip1, String ip2) {
        if (ip1.equals(ip2))
            return true;

        // Implementation of /24 subnet check for IPv4
        if (ip1.contains(".") && ip2.contains(".")) {
            String[] parts1 = ip1.split("\\.");
            String[] parts2 = ip2.split("\\.");

            if (parts1.length >= 3 && parts2.length >= 3) {
                return parts1[0].equals(parts2[0]) &&
                        parts1[1].equals(parts2[1]) &&
                        parts1[2].equals(parts2[2]);
            }
        }

        // IPv6 (Beta: simple /64 check)
        if (ip1.contains(":") && ip2.contains(":")) {
            String[] parts1 = ip1.split(":");
            String[] parts2 = ip2.split(":");

            if (parts1.length >= 4 && parts2.length >= 4) {
                return parts1[0].equalsIgnoreCase(parts2[0]) &&
                        parts1[1].equalsIgnoreCase(parts2[1]) &&
                        parts1[2].equalsIgnoreCase(parts2[2]) &&
                        parts1[3].equalsIgnoreCase(parts2[3]);
            }
        }

        return false;
    }
}
