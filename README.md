Libb - is a library for convenient and easy creation of plugins for Minecraft.

<details>
<summary>Gui</summary>

### Example GUI

```java
public class GuiTest extends AdvancedGui {

    public GuiTest() {
        super("Gui title");

        setItem("example", ItemWrapper.builder(Material.STONE)
                .slots(1, 5, 7)
                .displayName(Component.text("This is the name dude"))
                .onClick(event -> {
                    event.setCancelled(true);
                    player.sendMessage("Clicked on slot: " + event.getSlot());
                })
                .build());
    }
}
```

To open a GUI for a player, use `open()`
```java
new GuiTest().open(player)
```
</details>

<details>
<summary>Actions</summary>

<details>
<summary>All actions</summary>
- [message]
- [broadcast_message]
- [console]
- [player]
- [effect]
- [actionbar]
- [broadcast_actionbar]
- [title]
- [broadcast_title]
- [sound]
- [broadcast_sound]
- [open]
</details>

```java
// Simple run
ActionExecute.run(ActionContext.of(player), "[message] Hello!");
```
</details>
