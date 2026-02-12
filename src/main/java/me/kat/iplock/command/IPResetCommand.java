package me.kat.iplock.command;

import me.kat.iplock.storage.IPStorage;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class IPResetCommand implements CommandExecutor {

    private final IPStorage storage;

    public IPResetCommand(IPStorage storage) {
        this.storage = storage;
    }

    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!(s instanceof Player))
            return true;
        Player p = (Player) s;
        storage.remove(p.getName());
        p.kickPlayer("IP reset. Rejoin to bind new IP.");
        return true;
    }
}
