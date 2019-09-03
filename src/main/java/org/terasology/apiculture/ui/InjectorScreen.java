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
package org.terasology.apiculture.ui;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.apiculture.components.ProcessingComponent;
import org.terasology.apiculture.systems.ExtractorSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryGrid;

import java.util.Objects;

public class InjectorScreen extends BaseInteractionScreen {
    private InventoryGrid inventory;
    private InventoryGrid input;
    private InventoryGrid bee;
    private LifespanBar progressBar;

    @In
    private Time time;

    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {
        if (inventory != null) {
            inventory.bindTargetEntity(new EntityRefBinding(Objects.requireNonNull(CoreRegistry.get(LocalPlayer.class)).getCharacterEntity()));
            inventory.setCellOffset(10);
            inventory.setMaxCellCount(30);
        }
        if (input != null) {
            input.bindTargetEntity(new EntityRefBinding(interactionTarget));
            input.setCellOffset(0);
            input.setMaxCellCount(1);
        }
        if (bee != null) {
            bee.bindTargetEntity(new EntityRefBinding(interactionTarget));
            bee.setCellOffset(1);
            bee.setMaxCellCount(1);
        }
    }

    @Override
    public void initialise() {
        inventory = find("inventory", InventoryGrid.class);
        input = find("input", InventoryGrid.class);
        bee = find("bee", InventoryGrid.class);
        progressBar = find("progressBar", LifespanBar.class);
    }

    private static class EntityRefBinding extends ReadOnlyBinding<EntityRef> {
        private EntityRef entityRef;

        public EntityRefBinding(EntityRef entityRef) {
            this.entityRef = entityRef;
        }

        @Override
        public EntityRef get() {
            return entityRef;
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        EntityRef interactionTarget = getInteractionTarget();
        ProcessingComponent processingComponent = interactionTarget.getComponent(ProcessingComponent.class);
        if (processingComponent != null) {
            progressBar.setFill(Math.min(ExtractorSystem.EXTRACT_TIME + time.getGameTimeInMs() - processingComponent.finishTime, ExtractorSystem.EXTRACT_TIME) / (float)ExtractorSystem.EXTRACT_TIME);
        }
        else {
            progressBar.setFill(0f);
        }
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
