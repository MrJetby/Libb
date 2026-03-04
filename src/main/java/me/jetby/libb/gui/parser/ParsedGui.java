package me.jetby.libb.gui.parser;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.jetby.libb.Libb;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionExecute;
import me.jetby.libb.action.record.ActionBlock;
import me.jetby.libb.action.record.Expression;
import me.jetby.libb.gui.AdvancedGui;
import me.jetby.libb.gui.item.ItemWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class ParsedGui {


    @Getter
    private final AdvancedGui gui;
    private final ActionBlock onOpen;
    private final ActionBlock onClose;
    @Getter
    private final List<String> openCommands;
    private Player player;

    public ParsedGui(@Nullable Player player, Gui gui) {
        this.player = player;
        this.openCommands = gui.command();

        this.onOpen = gui.onOpen();
        this.onClose = gui.onClose();

        this.gui = new AdvancedGui(Libb.MINI_MESSAGE.deserialize(
                PlaceholderAPI.setPlaceholders(player, gui.title())),
                gui.size());

        this.gui.onOpen(event -> {
            if (onOpen == null) return;
            ActionExecute.run(ActionContext.of(player), onOpen);
        });
        this.gui.onClose(event -> {
            if (onClose == null) return;
            ActionExecute.run(ActionContext.of(player), onClose);
        });
        registerItems(gui.items());

    }

    public ParsedGui(FileConfiguration configuration) {

        String id = configuration.getString("id");
        String title = configuration.getString("title");
        int size = configuration.getInt("size");

        this.openCommands = configuration.getStringList("command");

        this.onOpen = ParseUtil.getActionBlock(configuration, "on_open");
        this.onClose = ParseUtil.getActionBlock(configuration, "on_close");

        this.gui = new AdvancedGui(title, size);

        gui.onOpen(event -> {
            if (onOpen == null) return;
            ActionExecute.run(ActionContext.of(player), onOpen);
        });
        gui.onClose(event -> {
            if (onClose == null) return;
            ActionExecute.run(ActionContext.of(player), onClose);
        });

        registerItems(ParseUtil.getItems(configuration));

    }

    private void registerItems(List<Item> items) {
        if (items != null) {
            for (Item item : items) {
                String key = UUID.randomUUID().toString();
                if (item.getItemStack() != null) {
                    ItemWrapper wrapper = new ItemWrapper(item.getItemStack());
                    wrapper.snapshot(new ItemWrapper.ItemSnapshot(wrapper));
                    wrapper.slots(item.getSlots().toArray(new Integer[0]));
                    if (item.getDisplayName() != null)
                        wrapper.displayName(PlaceholderAPI.setPlaceholders(player, item.getDisplayName()));

                    List<String> lore = new ArrayList<>();
                    for (String line : item.getLore()) {
                        lore.add(PlaceholderAPI.setPlaceholders(player, line));
                    }
                    wrapper.setLore(lore);
                    if (item.getFlags()!=null)
                        wrapper.flags(item.getFlags().toArray(new ItemFlag[0]));

                    wrapper.onClick(event -> {
                        event.setCancelled(true);
                        Player p = (Player) event.getWhoClicked();
                        // any click
                        if (item.getOnClick().containsKey(null))
                            ActionExecute.run(ActionContext.of(p).with(wrapper), item.getOnClick().get(null));
                        // other click
                        for (var entry : item.getOnClick().entrySet()) {
                            ClickType required = entry.getKey();
                            ActionBlock block = entry.getValue();

                            if (!event.getClick().equals(required)) continue;

                            ActionExecute.run(ActionContext.of(p).with(wrapper), block);
                        }

                    });

                    gui.setItem(key, wrapper);
                }
            }
        }
    }

    public static class ParseUtil {

        public static Map<ClickType, ActionBlock> getClicks(@NotNull ConfigurationSection section) {
            Map<ClickType, ActionBlock> clicks = new HashMap<>();

            ConfigurationSection onClickSec = section.getConfigurationSection("on_click");
            if (onClickSec == null) return clicks;

            for (String key : onClickSec.getKeys(false)) {
                switch (key) {
                    case "any" -> clicks.put(null, ParseUtil.getActionBlock(onClickSec, key));
                    case "left" -> clicks.put(ClickType.LEFT, ParseUtil.getActionBlock(onClickSec, key));
                    case "shift_left" -> clicks.put(ClickType.SHIFT_LEFT, ParseUtil.getActionBlock(onClickSec, key));
                    case "right" -> clicks.put(ClickType.RIGHT, ParseUtil.getActionBlock(onClickSec, key));
                    case "shift_right" -> clicks.put(ClickType.SHIFT_RIGHT, ParseUtil.getActionBlock(onClickSec, key));
                    case "middle" -> clicks.put(ClickType.MIDDLE, ParseUtil.getActionBlock(onClickSec, key));
                    case "drop" -> clicks.put(ClickType.DROP, ParseUtil.getActionBlock(onClickSec, key));
                    case "control_drop" ->
                            clicks.put(ClickType.CONTROL_DROP, ParseUtil.getActionBlock(onClickSec, key));
                    case "window_border_left" ->
                            clicks.put(ClickType.WINDOW_BORDER_LEFT, ParseUtil.getActionBlock(onClickSec, key));
                    case "window_border_right" ->
                            clicks.put(ClickType.WINDOW_BORDER_RIGHT, ParseUtil.getActionBlock(onClickSec, key));
                    case "double" -> clicks.put(ClickType.DOUBLE_CLICK, ParseUtil.getActionBlock(onClickSec, key));
                    case "num_1", "num_2", "num_3", "num_4", "num_5", "num_6", "num_7", "num_8", "num_9" ->
                            clicks.put(ClickType.NUMBER_KEY, ParseUtil.getActionBlock(onClickSec, key));
                }
            }
            return clicks;

        }

        private static List<Integer> parseSlots(Object slotObject) {
            List<Integer> slots = new ArrayList<>();

            if (slotObject instanceof Integer) {
                slots.add((Integer) slotObject);
            } else if (slotObject instanceof String) {
                String slotString = ((String) slotObject).trim();
                slots.addAll(parseSlotString(slotString));
            } else if (slotObject instanceof List<?>) {
                for (Object obj : (List<?>) slotObject) {
                    if (obj instanceof Integer) {
                        slots.add((Integer) obj);
                    } else if (obj instanceof String) {
                        slots.addAll(parseSlotString((String) obj));
                    }
                }
            } else {
                Bukkit.getLogger().warning("Unknown slot format: " + slotObject);
            }

            return slots;
        }

        private static List<Integer> parseSlotString(String slotString) {
            List<Integer> slots = new ArrayList<>();
            if (slotString.contains("-")) {
                try {
                    String[] range = slotString.split("-");
                    int start = Integer.parseInt(range[0].trim());
                    int end = Integer.parseInt(range[1].trim());
                    for (int i = start; i <= end; i++) {
                        slots.add(i);
                    }
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().warning("Error parsing slot range: " + slotString);
                }
            } else {
                try {
                    slots.add(Integer.parseInt(slotString));
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().warning("Error parsing single slot: " + slotString);
                }
            }
            return slots;
        }

        public static @Nullable List<Item> getItems(@NotNull FileConfiguration configuration) {
            ConfigurationSection items = configuration.getConfigurationSection("Items");
            if (items == null) return null;

            List<Item> itemList = new ArrayList<>();
            for (String key : items.getKeys(false)) {
                ConfigurationSection item = items.getConfigurationSection(key);
                if (item == null) continue;

                String type = item.getString("type");
                String displayName = item.getString("display_name");
                List<String> lore = item.getStringList("lore");

                String material = item.getString("material", "STONE").toUpperCase();
                ItemStack itemStack;
                if (material.startsWith("BASEHEAD-")) {
                    try {
                        itemStack = SkullCreator.itemFromBase64(material.replace("BASEHEAD-", ""));
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("Error creating custom skull: " + e.getMessage());
                        itemStack = new ItemStack(SkullCreator.createSkull());
                    }
                } else {
                    itemStack = new ItemStack(Material.valueOf(material));
                }

                List<Integer> slots = new ArrayList<>();
                if (item.getInt("slot") <= 0) {
                    slots.addAll(parseSlots(item.getStringList("slots")));
                } else {
                    slots.add(item.getInt("slot"));
                }

                List<ItemFlag> flags = new ArrayList<>();
                for (String flag : item.getStringList("flags")) {
                    flags.add(ItemFlag.valueOf(flag.toUpperCase()));
                }

                List<Enchantment> enchantments = new ArrayList<>();
                for (String enchantmentName : item.getStringList("enchantments")) {
                    NamespacedKey k = NamespacedKey.minecraft(enchantmentName.toLowerCase());
                    Enchantment enchantment = Registry.ENCHANTMENT.get(k);
                    if (enchantment != null) {
                        enchantments.add(enchantment);
                    }
                }

                Map<ClickType, ActionBlock> onClick = getClicks(item);

                itemList.add(new Item(itemStack, type, displayName, lore, itemStack.getType(), slots, flags, enchantments, onClick));
            }

            return itemList;
        }

        public static @Nullable ActionBlock getActionBlock(@NotNull FileConfiguration configuration, @NotNull String path) {
            List<String> staticActions = new ArrayList<>();
            List<Expression> expressions = new ArrayList<>();

            List<?> list = configuration.getList(path);
            if (list == null) {
                return null;
            }

            for (Object object : list) {

                if (object instanceof String string) {
                    staticActions.add(string);
                    continue;
                }

                // - example_check: { ... }
                if (object instanceof Map<?, ?> map) {
                    for (Map.Entry<?, ?> entry : map.entrySet()) {

                        String key = String.valueOf(entry.getKey());

                        if (!(entry.getValue() instanceof Map<?, ?> sectionMap)) {
                            continue;
                        }

                        ConfigurationSection section =
                                new MemoryConfiguration().createSection(key, sectionMap);

                        String expression = section.getString("if");
                        if (expression == null) {
                            continue;
                        }

                        List<String> success = section.getStringList("then");
                        List<String> fail = section.getStringList("else");

                        expressions.add(new Expression(expression, success, fail));
                    }
                }
            }

            return new ActionBlock(staticActions, expressions);
        }

        public static @Nullable ActionBlock getActionBlock(@NotNull ConfigurationSection configuration, @NotNull String path) {
            List<String> staticActions = new ArrayList<>();
            List<Expression> expressions = new ArrayList<>();

            List<?> list = configuration.getList(path);
            if (list == null) {
                return null;
            }

            for (Object object : list) {

                if (object instanceof String string) {
                    staticActions.add(string);
                    continue;
                }

                // - example_check: { ... }
                if (object instanceof Map<?, ?> map) {
                    for (Map.Entry<?, ?> entry : map.entrySet()) {

                        String key = String.valueOf(entry.getKey());

                        if (!(entry.getValue() instanceof Map<?, ?> sectionMap)) {
                            continue;
                        }

                        ConfigurationSection section =
                                new MemoryConfiguration().createSection(key, sectionMap);

                        String expression = section.getString("if");
                        if (expression == null) {
                            continue;
                        }

                        List<String> success = section.getStringList("then");
                        List<String> fail = section.getStringList("else");

                        expressions.add(new Expression(expression, success, fail));
                    }
                }
            }

            return new ActionBlock(staticActions, expressions);
        }
    }


}
