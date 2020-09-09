// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.apiculture.systems;

import org.terasology.apiculture.components.MatedComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;

@RegisterSystem(RegisterMode.ALWAYS) // TODO: Try to make AUTHORITY
public class MatedComponentLifetimeSystem extends BaseComponentSystem {
    @ReceiveEvent(components = {MatedComponent.class})
    public void onRemoved(BeforeRemoveComponent event, EntityRef ref) {
        ref.getComponent(MatedComponent.class).container.destroy();
    }
}
