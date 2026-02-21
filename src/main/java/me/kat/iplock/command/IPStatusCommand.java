package me.kat.iplock.command;

import me.kat.iplock.storage.IPStorage;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class IPStatusCommand implements CommandExecutor {

    private final IPStorage storage;

    public IPStatusCommand(IPStorage storage) {
        this.storage = storage;
    }

    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!(s instanceof Player p)) {
            s.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!p.hasPermission("ipauth.admin")) {
            p.sendMessage("You don't have permission to use this command.");
            return true;
        }

        IPStorage.Entry e = storage.get(p.getName());
        if (e == null) {
            p.sendMessage("No IP bound. Join with a valid IP to bind it.");
        } else {
            p.sendMessage("Your IP is bound since: " + e.time());
        }
        return true;
    }
}
