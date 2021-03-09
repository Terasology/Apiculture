// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.apiculture.ui;

import org.terasology.apiculture.components.ProcessingComponent;
import org.terasology.apiculture.systems.ExtractorSystem;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.BaseInteractionScreen;
import org.terasology.engine.rendering.nui.layers.ingame.inventory.InventoryGrid;
import org.terasology.nui.databinding.ReadOnlyBinding;

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

    @Override
    public void update(float delta) {
        super.update(delta);

        EntityRef interactionTarget = getInteractionTarget();
        ProcessingComponent processingComponent = interactionTarget.getComponent(ProcessingComponent.class);
        if (processingComponent != null) {
            progressBar.setFill(Math.min(ExtractorSystem.EXTRACT_TIME + time.getGameTimeInMs() - processingComponent.finishTime, ExtractorSystem.EXTRACT_TIME)
                    / (float) ExtractorSystem.EXTRACT_TIME);
        } else {
            progressBar.setFill(0f);
        }
    }

    @Override
    public boolean isModal() {
        return false;
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
}
