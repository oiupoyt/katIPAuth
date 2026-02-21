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
        if (!s.hasPermission("ipauth.admin") || a.length != 1)
            return true;

        String targetPlayer = a[0];
        storage.remove(targetPlayer);

        // Try to kick the player if they're online
        Player target = s.getServer().getPlayer(targetPlayer);
        if (target != null) {
            target.kickPlayer("Your IP binding has been reset by an admin. Rejoin to bind new IP.");
        }

        s.sendMessage("IP binding reset for player: " + targetPlayer);
        return true;
    }
}
