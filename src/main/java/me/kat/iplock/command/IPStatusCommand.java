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
        if (!(s instanceof Player p))
            return true;

        IPStorage.Entry e = storage.get(p.getName());
        if (e == null) {
            p.sendMessage("No IP bound.");
        } else {
            p.sendMessage("IP bound since: " + e.time());
        }
        return true;
    }
}
