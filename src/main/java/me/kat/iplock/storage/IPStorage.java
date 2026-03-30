package me.kat.iplock.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.sql.*;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class IPStorage {

    public static class Entry {
        private final String ip;
        private final String uuid;
        private final long time;

        public Entry(String ip, String uuid, long time) {
            this.ip = ip;
            this.uuid = uuid;
            this.time = time;
        }

        public String ip() {
            return ip;
        }

        public String uuid() {
            return uuid;
        }

        public long time() {
            return time;
        }
    }

    private final JavaPlugin plugin;
    private final File databaseFile;
    private Connection connection;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public IPStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.databaseFile = new File(plugin.getDataFolder(), "ips");
    }

    public void initialize() {
        try {
            // Create data folder if it doesn't exist
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            // Explicitly load H2 driver if necessary
            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException e) {
                plugin.getLogger().log(Level.SEVERE, "H2 Driver not found! Make sure it is shaded into the JAR.", e);
            }

            // Connect to H2 database
            String url = "jdbc:h2:file:" + databaseFile.getAbsolutePath() + ";DB_CLOSE_ON_EXIT=FALSE";
            connection = DriverManager.getConnection(url, "sa", "");

            // Create table if it doesn't exist
            createTable();

            // Migrate from old JSON format if it exists
            migrateFromJson();

            plugin.getLogger().info("IPStorage database initialized successfully.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize IPStorage database", e);
        }
    }

    private void createTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS ip_bindings (
                    player_name VARCHAR(255) PRIMARY KEY,
                    ip_address VARCHAR(45) NOT NULL,
                    player_uuid VARCHAR(36),
                    bind_time BIGINT NOT NULL
                )
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }

        // Add UUID column if it doesn't exist (for migration)
        try {
            String alterSql = "ALTER TABLE ip_bindings ADD COLUMN IF NOT EXISTS player_uuid VARCHAR(36)";
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(alterSql);
            }
        } catch (SQLException e) {
            // Column might already exist, ignore
        }
    }

    private void migrateFromJson() {
        File oldJsonFile = new File(plugin.getDataFolder(), "ips.json");
        if (!oldJsonFile.exists()) {
            return;
        }

        plugin.getLogger().info("Migrating data from old JSON format to H2 database...");

        try (Reader r = new FileReader(oldJsonFile)) {
            JsonElement element = JsonParser.parseReader(r);
            if (element == null || !element.isJsonObject()) {
                return;
            }

            JsonObject obj = element.getAsJsonObject();
            String sql = """
                    MERGE INTO ip_bindings (player_name, player_uuid, ip_address, bind_time)
                    VALUES (?, ?, ?, ?)
                    """;

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                for (var entry : obj.entrySet()) {
                    try {
                        JsonObject v = entry.getValue().getAsJsonObject();
                        String playerName = entry.getKey();
                        String ipAddress = v.get("ip").getAsString();
                        long bindTime = v.get("time").getAsLong();

                        pstmt.setString(1, playerName);
                        pstmt.setString(2, null);
                        pstmt.setString(3, ipAddress);
                        pstmt.setLong(4, bindTime);
                        pstmt.executeUpdate();

                        plugin.getLogger().info("Migrated: " + playerName + " -> " + ipAddress);
                    } catch (Exception ex) {
                        plugin.getLogger().log(Level.WARNING, "Failed to migrate entry for " + entry.getKey(), ex);
                    }
                }
            }

            // Backup the old file and delete it
            File backupFile = new File(plugin.getDataFolder(), "ips.json.backup");
            if (oldJsonFile.renameTo(backupFile)) {
                plugin.getLogger().info("Old JSON file backed up as ips.json.backup");
            }

            plugin.getLogger().info("Migration completed successfully.");

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to migrate from JSON", e);
        }
    }

    public Entry get(String name) {
        String sql = "SELECT ip_address, player_uuid, bind_time FROM ip_bindings WHERE LOWER(player_name) = LOWER(?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Entry(rs.getString("ip_address"), rs.getString("player_uuid"), rs.getLong("bind_time"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get IP binding for " + name, e);
        }
        return null;
    }

    public void bind(String name, String uuid, String ip) {
        String sql = """
                MERGE INTO ip_bindings (player_name, player_uuid, ip_address, bind_time)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name.toLowerCase());
            pstmt.setString(2, uuid);
            pstmt.setString(3, ip);
            pstmt.setLong(4, Instant.now().toEpochMilli());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to bind IP for " + name, e);
        }
    }

    // Backward compatibility method
    public void bind(String name, String ip) {
        bind(name, null, ip);
    }

    public Entry getByUUID(String uuid) {
        String sql = "SELECT player_name, ip_address, bind_time FROM ip_bindings WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Entry(rs.getString("ip_address"), uuid, rs.getLong("bind_time"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get IP binding for UUID " + uuid, e);
        }
        return null;
    }

    public int getIPUsageCount(String ip) {
        String sql = "SELECT COUNT(*) FROM ip_bindings WHERE ip_address = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ip);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check IP usage count for " + ip, e);
        }
        return 0;
    }

    public boolean isIPInUse(String ip) {
        return getIPUsageCount(ip) > 0;
    }

    public Entry getByIP(String ip) {
        String sql = "SELECT player_name, player_uuid, bind_time FROM ip_bindings WHERE ip_address = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ip);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Entry(ip, rs.getString("player_uuid"), rs.getLong("bind_time"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get binding for IP " + ip, e);
        }
        return null;
    }

    public void remove(String name) {
        String sql = "DELETE FROM ip_bindings WHERE LOWER(player_name) = LOWER(?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to remove IP binding for " + name, e);
        }
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("IPStorage database connection closed.");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to close database connection", e);
            }
        }
    }
}
