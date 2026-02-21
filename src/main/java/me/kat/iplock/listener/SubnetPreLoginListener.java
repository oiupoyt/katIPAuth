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

        IPStorage.Entry entry = storage.get(name);

        if (entry == null) {
            storage.bind(name, currentIp);
            logManager.log(name, currentIp, true, "First login - IP bound");
            return;
        }

        String storedIp = entry.ip();

        if (isSameSubnet(storedIp, currentIp)) {
            logManager.log(name, currentIp, true, "Subnet match (Stored: " + logManager.formatIp(storedIp) + ")");
            return;
        }

        // Blocking login
        String kickMessage = ChatColor.translateAlternateColorCodes('&',
                IPLockPlugin.get().getConfig().getString("messages.blocked",
                        "IP mismatch. Access denied. Contact the owner if you believe this is a mistake"));
        event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                kickMessage);

        logManager.log(name, currentIp, false, "Subnet mismatch (Expected: " + logManager.formatIp(storedIp) + ".x)");

        DiscordWebhook.sendAlert(
                name,
                storedIp,
                currentIp,
                Instant.now());
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
