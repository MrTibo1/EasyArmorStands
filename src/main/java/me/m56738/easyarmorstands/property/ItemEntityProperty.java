package me.m56738.easyarmorstands.property;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.bukkit.parsers.ItemStackArgument;
import me.m56738.easyarmorstands.command.EasCommandSender;
import me.m56738.easyarmorstands.inventory.InventorySlot;
import me.m56738.easyarmorstands.menu.EntityItemSlot;
import me.m56738.easyarmorstands.menu.EntityMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class ItemEntityProperty<E extends Entity> implements ButtonEntityProperty<E, ItemStack> {

    @Override
    public @NotNull CommandArgument<EasCommandSender, ItemStack> getArgument() {
        ArgumentParser<EasCommandSender, ItemStack> parser = new ItemStackArgument.Parser<EasCommandSender>()
                .map((ctx, value) -> ArgumentParseResult.success(value.createItemStack(1, true)));
        return CommandArgument.<EasCommandSender, ItemStack>ofType(ItemStack.class, getName())
                .withParser(parser)
                .build();
    }

    @Override
    public @NotNull Component getValueName(ItemStack value) {
        return Component.text(value.getType().name());
    }

    @Override
    public InventorySlot createSlot(EntityMenu<? extends E> menu) {
        return new EntityItemSlot<>(menu, this);
    }
}