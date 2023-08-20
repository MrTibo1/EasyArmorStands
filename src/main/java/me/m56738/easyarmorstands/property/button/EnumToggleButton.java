package me.m56738.easyarmorstands.property.button;

import me.m56738.easyarmorstands.property.Property;
import me.m56738.easyarmorstands.property.PropertyContainer;
import me.m56738.easyarmorstands.item.ItemTemplate;

public class EnumToggleButton<T extends Enum<T>> extends ToggleButton<T> {
    private final T[] values;

    public EnumToggleButton(Property<T> property, PropertyContainer container, ItemTemplate item, T[] values) {
        super(property, container, item);
        this.values = values;
    }

    private T getNeighbour(int offset) {
        int index = property.getValue().ordinal() + offset;
        if (index >= values.length) {
            index -= values.length;
        } else if (index < 0) {
            index += values.length;
        }
        return values[index];
    }

    @Override
    public T getNextValue() {
        return getNeighbour(1);
    }

    @Override
    public T getPreviousValue() {
        return getNeighbour(-1);
    }
}
