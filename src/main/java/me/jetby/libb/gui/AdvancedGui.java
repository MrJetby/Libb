package me.jetby.libb.gui;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.jetby.libb.Keys;
import me.jetby.libb.Libb;
import me.jetby.libb.gui.item.ItemWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AdvancedGui implements InventoryHolder {
    @Getter
    private final Inventory inventory;
    @Getter
    private final Map<String, ItemWrapper> items = new HashMap<>();
    private Consumer<InventoryClickEvent> onClick;
    private Consumer<InventoryDragEvent> onDrag;
    private Consumer<InventoryOpenEvent> onOpen;
    private Consumer<InventoryCloseEvent> onClose;
    public Player player;

    public AdvancedGui(Inventory inventory) {
        this.inventory = inventory;
    }
    public AdvancedGui(String title) {
        this.inventory = Bukkit.createInventory(this, InventoryType.CHEST, title);
    }
    public AdvancedGui(String title, int size) {
        this.inventory = Bukkit.createInventory(this, size, title);
    }
    public AdvancedGui(String title, InventoryType inventoryType) {
        this.inventory = Bukkit.createInventory(this, inventoryType, title);
    }

    public AdvancedGui(Component title) {
        this.inventory = Bukkit.createInventory(this, InventoryType.CHEST, title);
    }
    public AdvancedGui(Component title, int size) {
        this.inventory = Bukkit.createInventory(this, size, title);
    }
    public AdvancedGui(Component title, InventoryType inventoryType) {
        this.inventory = Bukkit.createInventory(this, inventoryType, title);
    }

    public void restore(Player player, @NotNull String key) {
        ItemWrapper wrapper = items.get(key);
        if (wrapper==null) return;

        ItemStack itemStack = wrapper.snapshot().getItemStack();
        if (itemStack==null) {
            itemStack = new ItemStack(wrapper.material());
            ItemWrapper restoredWrapper = new ItemWrapper(itemStack);
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.displayName(
                        Libb.MINI_MESSAGE.deserialize(
                                PlaceholderAPI.setPlaceholders(player,
                                        Libb.MINI_MESSAGE.serialize(restoredWrapper.displayName())
                                )));
                List<Component> lore = new ArrayList<>();
                for (Component str : restoredWrapper.lore()) {
                    lore.add(Libb.MINI_MESSAGE.deserialize(
                            PlaceholderAPI.setPlaceholders(player,
                                    Libb.MINI_MESSAGE.serialize(str)
                            )));
                }
                meta.lore(lore);
                meta.setCustomModelData(restoredWrapper.customModelData());
                if (restoredWrapper.enchanted()) {
                    meta.addEnchant(Enchantment.OXYGEN, 1, false);
                }
                if (restoredWrapper.flags() != null && !restoredWrapper.flags().isEmpty()) {
                    for (ItemFlag flag : restoredWrapper.flags()) {
                        meta.addItemFlags(flag);
                    }
                }
                meta.getPersistentDataContainer().set(Keys.GUI_ITEM, PersistentDataType.STRING, key);
                itemStack.setItemMeta(meta);
            }
            restoredWrapper.itemStack(itemStack);
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.getPersistentDataContainer().set(Keys.GUI_ITEM, PersistentDataType.STRING, key);
        itemStack.setItemMeta(meta);

        for (int slot : wrapper.slots()) {
            inventory.setItem(slot, itemStack);
        }

        items.put(key, wrapper);
    }

    public void setItem(@NotNull String key, @NotNull ItemWrapper wrapper) {
        if (wrapper.slots()==null) return;

        ItemStack itemStack = wrapper.itemStack();
        if (itemStack == null) {
            itemStack = new ItemStack(wrapper.material());
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.displayName(wrapper.displayName());
                meta.lore(wrapper.lore());
                meta.setCustomModelData(wrapper.customModelData());
                if (wrapper.enchanted()) {
                    meta.addEnchant(Enchantment.OXYGEN, 1, false);
                }
                if (wrapper.flags() != null && !wrapper.flags().isEmpty()) {
                    for (ItemFlag flag : wrapper.flags()) {
                        meta.addItemFlags(flag);
                    }
                }
                meta.getPersistentDataContainer().set(Keys.GUI_ITEM, PersistentDataType.STRING, key);
                itemStack.setItemMeta(meta);
            }
            wrapper.itemStack(itemStack);
        }

        ItemMeta meta = itemStack.getItemMeta();
        meta.getPersistentDataContainer().set(Keys.GUI_ITEM, PersistentDataType.STRING, key);
        itemStack.setItemMeta(meta);

        for (int slot : wrapper.slots()) {
            inventory.setItem(slot, itemStack);
        }

        items.put(key, wrapper);

    }

    public void open(@NotNull Player player) {
        this.player = player;
        player.openInventory(inventory);
    }

    public InventoryHolder getHolder() {
        return this;
    }
    public Consumer<InventoryClickEvent> onClick() {return onClick;}
    public Consumer<InventoryDragEvent> onDrag() {return onDrag;}
    public Consumer<InventoryOpenEvent> onOpen() {return onOpen;}
    public Consumer<InventoryCloseEvent> onClose() {return onClose;}
    public void onDrag(Consumer<InventoryDragEvent> event) {
        this.onDrag = event;
    }
    public void onOpen(Consumer<InventoryOpenEvent> event) {
        this.onOpen = event;
    }
    public void onClose(Consumer<InventoryCloseEvent> event) {
        this.onClose = event;
    }


}
