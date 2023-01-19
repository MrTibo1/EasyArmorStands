package gg.bundlegroup.easyarmorstands.platform.bukkit.v1_13;

import gg.bundlegroup.easyarmorstands.platform.bukkit.feature.EntityPersistenceSetter;
import org.bukkit.entity.Entity;

@SuppressWarnings("deprecation")
public class EntityPersistenceSetterImpl implements EntityPersistenceSetter {
    @Override
    public void setPersistent(Entity entity, boolean persistent) {
        entity.setPersistent(persistent);
    }

    public static class Provider implements EntityPersistenceSetter.Provider {
        @Override
        public boolean isSupported() {
            try {
                Entity.class.getDeclaredMethod("setPersistent", boolean.class);
                return true;
            } catch (NoSuchMethodException e) {
                return false;
            }
        }

        @Override
        public EntityPersistenceSetter create() {
            return new EntityPersistenceSetterImpl();
        }
    }
}