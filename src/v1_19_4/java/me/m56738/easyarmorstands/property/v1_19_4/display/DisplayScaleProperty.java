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
import org.joml.Vector3d;
import org.joml.Vector3fc;

public class DisplayScaleProperty implements EntityProperty<Display, Vector3fc> {
    private final JOMLMapper mapper;

    public DisplayScaleProperty(JOMLMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Vector3fc getValue(Display entity) {
        return mapper.getScale(entity.getTransformation());
    }

    @Override
    public TypeToken<Vector3fc> getValueType() {
        return TypeToken.get(Vector3fc.class);
    }

    @Override
    public void setValue(Display entity, Vector3fc value) {
        Transformation transformation = entity.getTransformation();
        entity.setTransformation(mapper.getTransformation(
                mapper.getTranslation(transformation),
                mapper.getLeftRotation(transformation),
                value,
                mapper.getRightRotation(transformation)));
    }

    @Override
    public @NotNull String getName() {
        return "scale";
    }

    @Override
    public @NotNull Class<Display> getEntityType() {
        return Display.class;
    }

    @Override
    public ArgumentParser<EasCommandSender, Vector3fc> getArgumentParser() {
        return null;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.text("Scale");
    }

    @Override
    public @NotNull Component getValueName(Vector3fc value) {
        return Util.formatOffset(new Vector3d(value));
    }

    @Override
    public @Nullable String getPermission() {
        return "easyarmorstands.property.display.scale";
    }
}