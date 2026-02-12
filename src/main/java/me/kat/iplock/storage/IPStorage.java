package me.kat.iplock.storage;

import com.google.gson.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class IPStorage {

    public static class Entry {
        private final String ip;
        private final long time;

        public Entry(String ip, long time) {
            this.ip = ip;
            this.time = time;
        }

        public String ip() {
            return ip;
        }

        public long time() {
            return time;
        }
    }

    private final JavaPlugin plugin;
    private final File file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, Entry> data = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public IPStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ips.json");
    }

    public Entry get(String name) {
        return data.get(name.toLowerCase());
    }

    public void bind(String name, String ip) {
        data.put(name.toLowerCase(), new Entry(ip, Instant.now().toEpochMilli()));
        saveAsync();
    }

    public void remove(String name) {
        data.remove(name.toLowerCase());
        saveAsync();
    }

    public void load() {
        if (!file.exists())
            return;

        try (Reader r = new FileReader(file)) {
            JsonElement element = JsonParser.parseReader(r);
            if (element == null || !element.isJsonObject())
                return;

            JsonObject obj = element.getAsJsonObject();
            obj.entrySet().forEach(e -> {
                try {
                    JsonObject v = e.getValue().getAsJsonObject();
                    data.put(e.getKey(),
                            new Entry(v.get("ip").getAsString(), v.get("time").getAsLong()));
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load entry for " + e.getKey(), ex);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load ips.json", e);
        }
    }

    public void save() {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        File tempFile = new File(parent, "ips.json.tmp");
        try (Writer w = new FileWriter(tempFile)) {
            JsonObject obj = new JsonObject();
            data.forEach((k, v) -> {
                JsonObject o = new JsonObject();
                o.addProperty("ip", v.ip());
                o.addProperty("time", v.time());
                obj.add(k, o);
            });
            gson.toJson(obj, w);
            w.flush();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to write ips.json.tmp", e);
            return;
        }

        try {
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to move ips.json.tmp to ips.json", e);
        }
    }

    private void saveAsync() {
        if (!executor.isShutdown()) {
            executor.submit(this::save);
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
        save();
    }
}
