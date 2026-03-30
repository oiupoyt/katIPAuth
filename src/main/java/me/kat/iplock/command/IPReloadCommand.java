package me.kat.iplock.command;

import me.kat.iplock.IPLockPlugin;
import org.bukkit.command.*;

public class IPReloadCommand implements CommandExecutor {

    private final IPLockPlugin plugin;

    public IPReloadCommand(IPLockPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!s.hasPermission("ipauth.admin")) {
            s.sendMessage("You don't have permission to use this command.");
            return true;
        }

        plugin.reloadConfig();
        s.sendMessage("KatIPAuth configuration reloaded successfully.");
        return true;
    }
}
