package me.jetby.libb.gui.parser;

import lombok.Getter;
import lombok.Setter;
import me.jetby.libb.action.record.ActionBlock;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Item {
    public Item(@Nullable ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public Item(@Nullable ItemStack itemStack, @Nullable String type, @Nullable String displayName, @Nullable List<String> lore, @NotNull Material material, @NotNull List<Integer> slots, @Nullable List<ItemFlag> flags, @Nullable List<Enchantment> enchantments, @NotNull Map<ClickType, ActionBlock> onClick) {
        this.itemStack = itemStack;
        this.type = type;
        this.displayName = displayName;
        this.lore = lore;
        this.material = material;
        this.slots = slots;
        this.flags = flags;
        this.enchantments = enchantments;
        this.onClick = onClick;
    }

    @Nullable ItemStack itemStack;
    @Nullable String type;
    @Nullable String displayName;
    @Nullable List<String> lore;
    @NotNull Material material;
    @NotNull List<Integer> slots;
    @Nullable List<ItemFlag> flags;
    @Nullable List<Enchantment> enchantments;
    @NotNull Map<ClickType, ActionBlock> onClick = new HashMap<>();


    public Item(@NotNull Material material, @NotNull List<Integer> slots) {
        this.material = material;
        this.slots = slots;
    }
}
