package me.kat.iplock.command;

import me.kat.iplock.storage.IPStorage;
import org.bukkit.command.*;

public class IPInfoCommand implements CommandExecutor {

    private final IPStorage storage;

    public IPInfoCommand(IPStorage storage) {
        this.storage = storage;
    }

    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!s.hasPermission("ipauth.admin")) {
            s.sendMessage("You don't have permission to use this command.");
            return true;
        }

        if (a.length != 1) {
            s.sendMessage("Usage: /ipinfo <player>");
            return true;
        }

        IPStorage.Entry e = storage.get(a[0]);
        if (e == null) {
            s.sendMessage("No IP data found for player: " + a[0]);
        } else {
            s.sendMessage("Player: " + a[0] + " | IP: " + e.ip() + " | Bound: " + e.time());
        }
        return true;
    }
}
