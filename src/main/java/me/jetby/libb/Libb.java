package me.jetby.libb;

import lombok.Getter;
import me.jetby.libb.configuration.MenusLoader;
import me.jetby.libb.executors.LibbCommand;
import me.jetby.libb.gui.AdvancedGui;
import me.jetby.libb.gui.CommandRegistrar;
import me.jetby.libb.gui.GuiListener;
import me.jetby.libb.gui.parser.Gui;
import me.jetby.libb.papi.Test;
import me.jetby.libb.tools.Metrics;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class Libb extends JavaPlugin {


    public static final Map<String, Gui> PARSED_GUIS = new HashMap<>();

    public static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public MenusLoader menusLoader;

    @Getter
    private static Libb instance;

    @Override
    public void onEnable() {
        instance = this;
        new Metrics(this, 30288);

        getCommand("libb").setExecutor(new LibbCommand(this));

        menusLoader = new MenusLoader(this);
        menusLoader.load();
        CommandRegistrar.createCommands(this);

        getServer().getPluginManager().registerEvents(new GuiListener(), this);

        new Test().register();

    }

    @Override
    public void onDisable() {
        new Test().unregister();
        CommandRegistrar.unregisterAll(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory topInventory = player.getOpenInventory().getTopInventory();
            if (!(topInventory instanceof AdvancedGui)) continue;
            player.closeInventory();
        }
    }
}
