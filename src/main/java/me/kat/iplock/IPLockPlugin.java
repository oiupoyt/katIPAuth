package me.kat.iplock;

import me.kat.iplock.listener.PreLoginListener;
import me.kat.iplock.listener.SubnetPreLoginListener;
import me.kat.iplock.storage.IPStorage;
import me.kat.iplock.util.ConfigUpdater;
import me.kat.iplock.util.LogManager;
import me.kat.iplock.util.VersionCheck;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class IPLockPlugin extends JavaPlugin {

    private static IPLockPlugin instance;
    private IPStorage storage;
    private LogManager logManager;
    private VersionCheck versionCheck;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Update config with any missing settings
        ConfigUpdater configUpdater = new ConfigUpdater(this);
        configUpdater.updateConfig();

        storage = new IPStorage(this);
        storage.initialize();

        logManager = new LogManager(this);

        versionCheck = new VersionCheck(this);
        versionCheck.checkForUpdates();

        // Register appropriate listener based on config settings
        if (getConfig().getBoolean("beta.subnet-locking", false)) {
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.RED + "[KatIPAuth] Subnet Locking is enabled!");
            Bukkit.getConsoleSender().sendMessage(
                    ChatColor.RED + "[KatIPAuth] This is a beta feature and can provoke bugs and connection problems.");
            Bukkit.getConsoleSender().sendMessage(
                    ChatColor.GOLD + "[KatIPAuth] Report any issues at: https://github.com/oiupoyt/katIPAuth/issues");
            getServer().getPluginManager().registerEvents(
                    new SubnetPreLoginListener(storage, logManager), this);
        } else {
            getServer().getPluginManager().registerEvents(
                    new PreLoginListener(storage, logManager), this);
        }

        getCommand("ipreset").setExecutor(new me.kat.iplock.command.IPResetCommand(storage));
        getCommand("ipstatus").setExecutor(new me.kat.iplock.command.IPStatusCommand(storage));
        getCommand("ipinfo").setExecutor(new me.kat.iplock.command.IPInfoCommand(storage));
        getCommand("ipforce").setExecutor(new me.kat.iplock.command.IPForceCommand(storage));
        getCommand("ipreload").setExecutor(new me.kat.iplock.command.IPReloadCommand(this));
    }

    @Override
    public void onDisable() {
        storage.shutdown();
    }

    public static IPLockPlugin get() {
        return instance;
    }
}
