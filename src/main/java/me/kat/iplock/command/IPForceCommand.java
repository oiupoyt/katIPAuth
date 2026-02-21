package me.kat.iplock.command;

import me.kat.iplock.storage.IPStorage;
import org.bukkit.command.*;

public class IPForceCommand implements CommandExecutor {

    private final IPStorage storage;

    public IPForceCommand(IPStorage storage) {
        this.storage = storage;
    }

    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!s.hasPermission("ipauth.admin")) {
            s.sendMessage("You don't have permission to use this command.");
            return true;
        }

        if (a.length != 1) {
            s.sendMessage("Usage: /ipforce <player>");
            return true;
        }

        storage.remove(a[0]);
        s.sendMessage("IP binding removed for player: " + a[0]);
        return true;
    }
}
