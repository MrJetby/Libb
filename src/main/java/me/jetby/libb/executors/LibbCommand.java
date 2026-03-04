package me.jetby.libb.executors;

import me.jetby.libb.Libb;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LibbCommand implements CommandExecutor {
    private final Libb plugin;

    public LibbCommand(Libb plugin) {
        this.plugin = plugin;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (sender instanceof Player player) {

            if (args[0].equalsIgnoreCase("reload")) {
                plugin.menusLoader.load();
                player.sendMessage("successfully reloaded");
                return true;
            }

        }

        return true;
    }
}
