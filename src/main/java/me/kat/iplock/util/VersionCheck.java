package me.kat.iplock.util;

import me.kat.iplock.IPLockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionCheck {

    private final IPLockPlugin plugin;
    private static final String GITHUB_API_URL = "https://api.github.com/repos/oiupoyt/katIPAuth/releases/latest";
    private static final String MODRINTH_URL = "https://modrinth.com/plugin/katipauth";

    public VersionCheck(IPLockPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdates() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String latestVersion = fetchLatestVersion(GITHUB_API_URL);
                    String currentVersion = plugin.getDescription().getVersion();

                    if (isNewerVersion(latestVersion, currentVersion)) {
                        plugin.getLogger().info(
                                "A new version is available: " + latestVersion + " (current: " + currentVersion + ")");
                        plugin.getLogger().info("Download it from: " + MODRINTH_URL);
                    } else {
                        plugin.getLogger().info("You are running the latest version: " + currentVersion);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private String fetchLatestVersion(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("User-Agent", "KatIPAuth-VersionCheck");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // Parse JSON for "tag_name"
            String json = response.toString();
            int tagIndex = json.indexOf("\"tag_name\"");
            if (tagIndex != -1) {
                int start = json.indexOf("\"", tagIndex + 11) + 1;
                int end = json.indexOf("\"", start);
                return json.substring(start, end);
            } else {
                throw new Exception("Could not find tag_name in response");
            }
        } finally {
            connection.disconnect();
        }
    }

    private boolean isNewerVersion(String latest, String current) {
        // Simple version comparison, assuming semantic versioning like 1.0.0 or 1.1.0
        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");

        for (int i = 0; i < Math.max(latestParts.length, currentParts.length); i++) {
            int latestNum = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
            int currentNum = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;

            if (latestNum > currentNum) {
                return true;
            } else if (latestNum < currentNum) {
                return false;
            }
        }
        return false;
    }
}
