package me.jetby.libb.action;

import me.clip.placeholderapi.PlaceholderAPI;
import me.jetby.libb.action.record.ActionBlock;
import me.jetby.libb.action.record.Expression;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Entry point for executing actions.
 *
 * <pre>{@code
 * // Simple run
 * ActionExecute.run(ActionContext.of(player), "[message] Hello!");
 *
 * // Explicit namespace
 * ActionExecute.run(ActionContext.of(player), "[myplugin:spawn] some text");
 *
 * // With extra objects in context
 * ActionExecute.run(
 *     ActionContext.of(player).with(entity),
 *     "[myplugin:spawn] some text"
 * );
 * }</pre>
 */
public final class ActionExecute {

    private ActionExecute() {}

    public static void run(@NotNull ActionContext ctx, @NotNull String line) {
        String key = ActionRegistry.resolveKey(line);
        if (key == null) return;

        Action handler = ActionRegistry.resolve(line);
        if (handler == null) return;

        String rawText = ActionRegistry.extractText(line, key);
        String text = ctx.getPlayer() != null
                ? PlaceholderAPI.setPlaceholders(ctx.getPlayer(), rawText)
                : rawText;
        handler.execute(ctx, text);
    }

    public static void run(@NotNull ActionContext ctx, @NotNull ActionBlock block) {
        for (String line : block.staticActions()) {
            run(ctx, line);
        }
        for (Expression expression : block.expressions()) {
            run(ctx, expression);
        }
    }

    public static void run(@NotNull ActionContext ctx, @NotNull Expression expression) {
        boolean result = evaluate(ctx.getPlayer(), expression.expression());
        Iterable<String> lines = result ? expression.success() : expression.fail();
        for (String line : lines) {
            run(ctx, line);
        }
    }

    private static boolean evaluate(Player player, @NotNull String input) {
        String[] parts = input.split(" ", 3);
        if (parts.length < 3) return false;

        String left  = player != null ? PlaceholderAPI.setPlaceholders(player, parts[0]) : parts[0];
        String op    = parts[1];
        String right = player != null ? PlaceholderAPI.setPlaceholders(player, parts[2]) : parts[2];

        try {
            double l = Double.parseDouble(left);
            double r = Double.parseDouble(right);
            return switch (op) {
                case ">"  -> l > r;
                case ">=" -> l >= r;
                case "==" -> l == r;
                case "!=" -> l != r;
                case "<=" -> l <= r;
                case "<"  -> l < r;
                default   -> false;
            };
        } catch (NumberFormatException ignored) {}

        return switch (op) {
            case "==" -> left.equals(right);
            case "!=" -> !left.equals(right);
            default   -> false;
        };
    }
}