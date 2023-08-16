package me.m56738.easyarmorstands.history.action;

import me.m56738.easyarmorstands.EasyArmorStands;
import me.m56738.easyarmorstands.context.ChangeContext;
import me.m56738.easyarmorstands.element.DestroyableElement;
import me.m56738.easyarmorstands.element.Element;
import me.m56738.easyarmorstands.element.ElementReference;
import me.m56738.easyarmorstands.element.ElementType;
import me.m56738.easyarmorstands.element.EntityElementReference;
import me.m56738.easyarmorstands.property.PropertyContainer;
import me.m56738.easyarmorstands.property.PropertySnapshot;

import java.util.UUID;

abstract class ElementPresenceAction implements Action {
    private final ElementType type;
    private final PropertyContainer properties;
    private ElementReference reference;

    public ElementPresenceAction(Element element) {
        this.type = element.getType();
        this.properties = new PropertySnapshot(element.getProperties());
        this.reference = element.getReference();
    }

    protected boolean create(ChangeContext context) {
        if (!context.canCreateElement(type, properties)) {
            return false;
        }

        Element element = type.createElement(properties);
        if (element == null) {
            return false;
        }

        UUID oldId = getId(reference);
        reference = element.getReference();
        UUID newId = getId(reference);
        if (oldId != null && newId != null) {
            EasyArmorStands.getInstance().getHistoryManager().onEntityReplaced(oldId, newId);
        }

        return true;
    }

    protected boolean destroy(ChangeContext context) {
        if (reference == null) {
            return false;
        }

        Element element = reference.getElement();
        if (!(element instanceof DestroyableElement)) {
            return false;
        }

        DestroyableElement destroyableElement = (DestroyableElement) element;
        if (!context.canDestroyElement(destroyableElement)) {
            return false;
        }

        destroyableElement.destroy();
        return true;
    }

    @Override
    public void onEntityReplaced(UUID oldId, UUID newId) {
        if (reference != null) {
            reference.onEntityReplaced(oldId, newId);
        }
    }

    private UUID getId(ElementReference reference) {
        if (reference instanceof EntityElementReference) {
            return ((EntityElementReference<?>) reference).getId();
        }
        return null;
    }

    public ElementType getType() {
        return type;
    }
}
