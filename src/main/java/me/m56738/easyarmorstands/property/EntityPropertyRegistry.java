package me.m56738.easyarmorstands.property;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParser;
import me.m56738.easyarmorstands.command.processor.Keys;
import me.m56738.easyarmorstands.command.sender.EasCommandSender;
import me.m56738.easyarmorstands.session.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;

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

    public void register(EntityProperty<?, ?> property) {
        String name = property.getName();
        EntityProperty old = properties.putIfAbsent(name, property);
        if (old != null) {
            throw new IllegalStateException("Duplicate property: " + name);
        }

        registerCommand(property);
    }

    @SuppressWarnings("unchecked")
    private void registerCommand(EntityProperty property) {
        ArgumentParser parser = property.getArgumentParser();
        if (parser == null) {
            return;
        }

        Command.Builder<EasCommandSender> builder = rootBuilder
                .literal(property.getName())
                .meta(Keys.SESSION_REQUIRED, true)
                .meta(Keys.ENTITY_REQUIRED,
                        e -> property.getEntityType().isAssignableFrom(e.getClass()) && property.isSupported(e));

        String permission = property.getPermission();
        if (permission != null) {
            builder = builder.permission(permission);
        }

        commandManager.command(builder
                .handler(ctx -> {
                    Entity entity = ctx.get(Keys.ENTITY);
                    ctx.getSender().sendMessage(Component.text()
                            .content("Current value of ")
                            .append(property.getDisplayName())
                            .append(Component.text(": "))
                            .append(property.getValueName(property.getValue(entity)))
                            .color(NamedTextColor.GREEN));
                }));

        CommandArgument.Builder argumentBuilder = CommandArgument.ofType(property.getValueType(), "value")
                .manager(commandManager)
                .withParser(parser);

        if (property.hasDefaultValue()) {
            argumentBuilder.asOptional();
        }

        CommandArgument argument = argumentBuilder.build();

        builder = builder.argument(argument);

        commandManager.command(builder
                .handler(ctx -> {
                    Session session = ctx.get(Keys.SESSION);
                    Entity entity = ctx.get(Keys.ENTITY);
                    Object value = ctx.getOrSupplyDefault(argument.getKey(), () -> property.getDefaultValue(ctx));
                    if (property.performChange(session, entity, value)) {
                        ctx.getSender().sendMessage(Component.text()
                                .content("Changed ")
                                .append(property.getDisplayName())
                                .append(Component.text(" to "))
                                .append(property.getValueName(value))
                                .color(NamedTextColor.GREEN));
                    } else {
                        ctx.getSender().sendMessage(Component.text("Unable to change property", NamedTextColor.RED));
                    }
                    session.commit();
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
        if (property != null && property.getEntityType().isAssignableFrom(entity.getClass())) {
            return (EntityProperty<E, ?>) property;
        } else {
            return null;
        }
    }
}
