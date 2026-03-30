package me.kat.iplock.util;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public class LogManager {

    private final JavaPlugin plugin;
    private final File logsFolder;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DateTimeFormatter fileDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public LogManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logsFolder = new File(plugin.getDataFolder(), "LOGS");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }
    }

    public void log(String playerName, String ip, boolean success, String extra) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(timeFormatter);
        String fileName = now.format(fileDateFormatter) + ".log";
        File logFile = new File(logsFolder, fileName);

        String displayIp = formatIp(ip);

        String status = success ? "SUCCESS" : "FAILED";
        String message = String.format("[%s] [%s] Player: %s | IP: %s%s",
                timestamp, status, playerName, displayIp, (extra != null ? " | " + extra : ""));

        try (FileWriter fw = new FileWriter(logFile, true);
                PrintWriter pw = new PrintWriter(fw)) {
            pw.println(message);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not write to log file: " + fileName, e);
        }

        // Also log to console for visibility
        if (success) {
            plugin.getLogger().info(message);
        } else {
            plugin.getLogger().warning(message);
        }
    }

    public String formatIp(String ip) {
        if (plugin.getConfig().getBoolean("privacy.mask-ips", false)) {
            return maskIp(ip);
        }
        return ip;
    }

    private String maskIp(String ip) {
        if (ip == null || ip.isEmpty())
            return ip;

        // Handle IPv4
        if (ip.contains(".")) {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < parts.length; i++) {
                    if (i < parts.length - 2) {
                        builder.append(parts[i]);
                    } else {
                        builder.append("xx");
                    }
                    if (i < parts.length - 1) {
                        builder.append(".");
                    }
                }
                return builder.toString();
            }
        }

        // Handle IPv6 (basic masking)
        if (ip.contains(":")) {
            String[] parts = ip.split(":");
            if (parts.length >= 2) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < parts.length; i++) {
                    if (i < parts.length - 2) {
                        builder.append(parts[i]);
                    } else {
                        builder.append("xxxx");
                    }
                    if (i < parts.length - 1) {
                        builder.append(":");
                    }
                }
                return builder.toString();
            }
        }

        return ip;
    }
}
