/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.projsndwv.systems;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.projsndwv.components.ApiaryComponent;

@RegisterSystem(RegisterMode.ALWAYS) // TODO: Authority
public class ApiarySystem extends BaseComponentSystem {
    @ReceiveEvent
    public void onApiaryInitialized(OnAddedComponent event, EntityRef entity, ApiaryComponent component) {
        if (!entity.hasComponent(InventoryComponent.class)) {
            InventoryComponent inventoryComponent = new InventoryComponent(9);
            inventoryComponent.privateToOwner = false;
            entity.addComponent(inventoryComponent);
        }
    }
}
