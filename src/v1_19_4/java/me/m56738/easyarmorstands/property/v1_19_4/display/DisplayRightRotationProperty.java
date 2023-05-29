package me.m56738.easyarmorstands.property.v1_19_4.display;

import cloud.commandframework.arguments.parser.ArgumentParser;
import io.leangen.geantyref.TypeToken;
import me.m56738.easyarmorstands.command.sender.EasCommandSender;
import me.m56738.easyarmorstands.property.EntityProperty;
import me.m56738.easyarmorstands.util.Util;
import me.m56738.easyarmorstands.util.v1_19_4.JOMLMapper;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaternionfc;

public class DisplayRightRotationProperty implements EntityProperty<Display, Quaternionfc> {
    private final JOMLMapper mapper;

    public DisplayRightRotationProperty(JOMLMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Quaternionfc getValue(Display entity) {
        return mapper.getRightRotation(entity.getTransformation());
    }

    @Override
    public TypeToken<Quaternionfc> getValueType() {
        return TypeToken.get(Quaternionfc.class);
    }

    @Override
    public void setValue(Display entity, Quaternionfc value) {
        Transformation transformation = entity.getTransformation();
        entity.setTransformation(mapper.getTransformation(
                mapper.getTranslation(transformation),
                mapper.getLeftRotation(transformation),
                mapper.getScale(transformation),
                value));
    }

    @Override
    public @NotNull String getName() {
        return "shearing";
    }

    @Override
    public @NotNull Class<Display> getEntityType() {
        return Display.class;
    }

    @Override
    public ArgumentParser<EasCommandSender, Quaternionfc> getArgumentParser() {
        return null;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.text("shearing");
    }

    @Override
    public @NotNull Component getValueName(Quaternionfc value) {
        return Util.formatAngle(Util.toEuler(value.get(new Quaterniond())));
    }

    @Override
    public @Nullable String getPermission() {
        return "easyarmorstands.property.display.shearing";
    }
}