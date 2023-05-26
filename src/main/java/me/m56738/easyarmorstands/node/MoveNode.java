package me.m56738.easyarmorstands.node;

import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.standard.DoubleArgument;
import me.m56738.easyarmorstands.bone.PositionBone;
import me.m56738.easyarmorstands.bone.RotationProvider;
import me.m56738.easyarmorstands.session.Session;
import me.m56738.easyarmorstands.util.Cursor3D;
import me.m56738.easyarmorstands.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.RGBLike;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;
import org.joml.Intersectiond;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class MoveNode extends EditNode implements Button, ValueNode<Double> {
    private final Session session;
    private final PositionBone bone;
    private final RotationProvider rotationProvider;
    private final Component name;
    private final Vector3d axis;
    private final Vector3d direction;
    private final RGBLike color;
    private final double length;
    private final boolean includeEnds;
    private final Cursor3D cursor;
    private final Vector3d negativeEnd = new Vector3d();
    private final Vector3d positiveEnd = new Vector3d();
    private final Vector3d initial = new Vector3d();
    private final Vector3d current = new Vector3d();
    private final Vector3d initialCursor = new Vector3d();
    private final Vector3d offset = new Vector3d();
    private double initialOffset;
    private Vector3d lookTarget;
    private Double manualValue;

    public MoveNode(Session session, PositionBone bone, RotationProvider rotationProvider, Component name, Vector3dc axis, RGBLike color, double length, boolean includeEnds) {
        super(session);
        this.session = session;
        this.bone = bone;
        this.rotationProvider = rotationProvider;
        this.name = name;
        this.axis = new Vector3d(axis);
        this.direction = new Vector3d(axis);
        this.color = color;
        this.length = length;
        this.cursor = new Cursor3D(session.getPlayer(), session);
        this.includeEnds = includeEnds;
    }

    public double getLength() {
        return length;
    }

    private void refreshDirection() {
        if (rotationProvider != null) {
            rotationProvider.getRotation().transform(axis, direction);
        }
    }

    @Override
    public void onEnter() {
        manualValue = null;
        refreshDirection();
        initial.set(bone.getPosition());
        initialOffset = initial.dot(direction);
        initialCursor.set(lookTarget != null ? lookTarget : initial);
        cursor.start(initialCursor);
    }

    @Override
    protected void abort() {
        bone.setPosition(initial);
    }

    @Override
    public void onUpdate(Vector3dc eyes, Vector3dc target) {
        refreshDirection();

        cursor.update(false);

        cursor.get().sub(initialCursor, offset);
        double t;
        if (manualValue != null) {
            t = manualValue;
            if (rotationProvider == null) {
                t -= initialOffset;
            }
        } else {
            t = session.snap(offset.dot(direction));
        }

        initial.fma(t, direction, current);
        bone.setPosition(current);

        double length = getLength();
        current.fma(-length, direction, negativeEnd);
        current.fma(length, direction, positiveEnd);

        current.add(initialCursor).sub(initial);
        session.showLine(negativeEnd, positiveEnd, color, true);
        session.showLine(current, cursor.get(), NamedTextColor.WHITE, false);

        TextComponent value;
        if (rotationProvider != null) {
            value = Component.text(Util.OFFSET_FORMAT.format(t));
        } else {
            value = Component.text(Util.POSITION_FORMAT.format(t + initialOffset));
        }
        session.sendActionBar(Component.text().append(name).append(Component.text(": ")).append(value));
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public Node createNode() {
        return this;
    }

    @Override
    public void update(Vector3dc eyes, Vector3dc target) {
        refreshDirection();
        Vector3dc position = bone.getPosition();
        double length = getLength();
        position.fma(-length, direction, negativeEnd);
        position.fma(length, direction, positiveEnd);
        Vector3d lookRayPoint = new Vector3d();
        Vector3d handlePoint = new Vector3d();
        double d = Intersectiond.findClosestPointsLineSegments(
                eyes.x(), eyes.y(), eyes.z(),
                target.x(), target.y(), target.z(),
                negativeEnd.x, negativeEnd.y, negativeEnd.z,
                positiveEnd.x, positiveEnd.y, positiveEnd.z,
                lookRayPoint,
                handlePoint
        );
        double threshold = session.getLookThreshold();
        if (d < threshold * threshold) {
            lookTarget = handlePoint;
        } else {
            lookTarget = null;
        }
    }

    @Override
    public @Nullable Vector3d getLookTarget() {
        return lookTarget;
    }

    @Override
    public void showPreview(boolean focused) {
        session.showLine(negativeEnd, positiveEnd, focused ? NamedTextColor.YELLOW : color, includeEnds);
    }

    @Override
    public Component getValueComponent(Double value) {
        return Component.text(Util.OFFSET_FORMAT.format(value), TextColor.color(color));
    }

    @Override
    public ArgumentParser<CommandSender, Double> getParser() {
        if (rotationProvider != null) {
            // Offset
            return new DoubleArgument.DoubleParser<>(-100, 100);
        } else {
            // Coordinate
            return new DoubleArgument.DoubleParser<>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        }
    }

    @Override
    public void setValue(Double value) {
        manualValue = value;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && bone.isValid();
    }
}
