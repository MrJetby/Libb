package me.jetby.libb.action.impl;

import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.gui.item.ItemWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Rebuild implements Action {


    @Override
    public void execute(@NotNull ActionContext ctx, @Nullable String line) {
        ItemWrapper wrapper = ctx.get(ItemWrapper.class);
        if (wrapper == null) return;
        wrapper.itemStack(wrapper.snapshot().build(ctx.getPlayer()));
    }
}
