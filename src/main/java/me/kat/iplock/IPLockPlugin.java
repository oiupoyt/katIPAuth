package me.kat.iplock;

import me.kat.iplock.listener.PreLoginListener;
import me.kat.iplock.storage.IPStorage;
import me.kat.iplock.util.LogManager;
import me.kat.iplock.util.VersionCheck;
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

        storage = new IPStorage(this);
        storage.load();

        logManager = new LogManager(this);

        versionCheck = new VersionCheck(this);
        versionCheck.checkForUpdates();

        getServer().getPluginManager().registerEvents(
                new PreLoginListener(storage, logManager), this);

        getCommand("ipreset").setExecutor(new me.kat.iplock.command.IPResetCommand(storage));
        getCommand("ipstatus").setExecutor(new me.kat.iplock.command.IPStatusCommand(storage));
        getCommand("ipinfo").setExecutor(new me.kat.iplock.command.IPInfoCommand(storage));
        getCommand("ipforce").setExecutor(new me.kat.iplock.command.IPForceCommand(storage));
        getCommand("ipreload").setExecutor(new me.kat.iplock.command.IPReloadCommand(this, storage));
    }

    @Override
    public void onDisable() {
        storage.shutdown();
    }

    public static IPLockPlugin get() {
        return instance;
    }
}
