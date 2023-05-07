package me.m56738.easyarmorstands.property;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.context.CommandContext;
import me.m56738.easyarmorstands.command.EasCommandSender;
import me.m56738.easyarmorstands.command.EntityPreprocessor;
import me.m56738.easyarmorstands.command.Keys;
import me.m56738.easyarmorstands.session.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("rawtypes")
public class EntityPropertyRegistry {
    private final Map<String, EntityProperty<?, ?>> properties = new TreeMap<>();
    private final CommandManager<EasCommandSender> commandManager;
    private final Command.Builder<EasCommandSender> rootBuilder;

    public EntityPropertyRegistry(CommandManager<EasCommandSender> commandManager, Command.Builder<EasCommandSender> rootBuilder) {
        this.commandManager = commandManager;
        this.rootBuilder = rootBuilder;
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Entity getEntity(EntityProperty property, CommandContext<EasCommandSender> ctx) {
        Entity entity = EntityPreprocessor.getEntityOrNull(ctx);
        if (entity == null) {
            ctx.getSender().sendMessage(Component.text("You are not editing an entity.", NamedTextColor.RED));
            return null;
        }

        if (!property.getEntityType().isAssignableFrom(entity.getClass())) {
            ctx.getSender().sendMessage(Component.text("This property doesn't support this entity type.", NamedTextColor.RED));
            return null;
        }

        if (!property.isSupported(entity)) {
            ctx.getSender().sendMessage(Component.text("This property doesn't support this entity.", NamedTextColor.RED));
            return null;
        }
        return entity;
    }

    public void register(EntityProperty<?, ?> property) {
        String name = property.getName();
        EntityProperty old = properties.putIfAbsent(name, property);
        if (old != null) {
            throw new IllegalStateException("Duplicate property: " + name);
        }

        CommandArgument argument = property.getArgument();
        if (argument != null) {
            registerCommand(property, argument);
        }
    }

    @SuppressWarnings("unchecked")
    private void registerCommand(EntityProperty property, CommandArgument argument) {
        Command.Builder<EasCommandSender> builder = rootBuilder
                .literal(property.getName())
                .meta(Keys.SESSION_REQUIRED, true);

        String permission = property.getPermission();
        if (permission != null) {
            builder = builder.permission(permission);
        }

        commandManager.command(builder
                .handler(ctx -> {
                    Entity entity = getEntity(property, ctx);
                    if (entity == null) {
                        return;
                    }

                    ctx.getSender().sendMessage(Component.text()
                            .content("Current value of ")
                            .append(property.getDisplayName())
                            .append(Component.text(": "))
                            .append(property.getValueName(property.getValue(entity)))
                            .color(NamedTextColor.GREEN));
                }));

        builder = builder.argument(argument);

        commandManager.command(builder
                .handler(ctx -> {
                    Session session = ctx.get(Keys.SESSION);
                    Entity entity = getEntity(property, ctx);
                    if (entity == null) {
                        return;
                    }

                    Object value = ctx.get(argument);
                    session.setProperty(entity, property, value);
                    session.commit();
                    ctx.getSender().sendMessage(Component.text()
                            .content("Changed ")
                            .append(property.getDisplayName())
                            .append(Component.text(" to "))
                            .append(property.getValueName(value))
                            .color(NamedTextColor.GREEN));
                }));
    }

    public Map<String, EntityProperty<?, ?>> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @SuppressWarnings("unchecked")
    public <E extends Entity> Map<String, EntityProperty<? super E, ?>> getProperties(Class<E> type) {
        Map<String, EntityProperty<? super E, ?>> result = new TreeMap<>();
        for (Map.Entry<String, EntityProperty<?, ?>> entry : properties.entrySet()) {
            EntityProperty<?, ?> property = entry.getValue();
            if (property.getEntityType().isAssignableFrom(type)) {
                result.put(entry.getKey(), (EntityProperty<? super E, ?>) property);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <E extends Entity> EntityProperty<E, ?> getProperty(E entity, String name) {
        EntityProperty<?, ?> property = properties.get(name);
        if (property.getEntityType().isAssignableFrom(entity.getClass())) {
            return (EntityProperty<E, ?>) property;
        } else {
            return null;
        }
    }
}