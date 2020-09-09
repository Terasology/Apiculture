// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.apiculture.ui;

import org.terasology.apiculture.components.MatedComponent;
import org.terasology.apiculture.components.ProcessingComponent;
import org.terasology.apiculture.systems.ApiarySystem;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.BaseInteractionScreen;
import org.terasology.inventory.logic.InventoryComponent;
import org.terasology.inventory.rendering.nui.layers.ingame.InventoryGrid;
import org.terasology.nui.Color;
import org.terasology.nui.databinding.ReadOnlyBinding;

import java.util.Objects;

public class ApiaryScreen extends BaseInteractionScreen {
    private InventoryGrid inventory;
    private InventoryGrid female;
    private InventoryGrid male;
    private InventoryGrid out0;
    private InventoryGrid out1;
    private InventoryGrid out2;
    private LifespanBar lifespanBar;

    @In
    private Time time;

    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {
        if (inventory != null) {
            inventory.bindTargetEntity(new EntityRefBinding(Objects.requireNonNull(CoreRegistry.get(LocalPlayer.class)).getCharacterEntity()));
            inventory.setCellOffset(10);
            inventory.setMaxCellCount(30);
        }
        if (female != null) {
            female.bindTargetEntity(new EntityRefBinding(interactionTarget));
            female.setCellOffset(0);
            female.setMaxCellCount(1);
        }
        if (male != null) {
            male.bindTargetEntity(new EntityRefBinding(interactionTarget));
            male.setCellOffset(1);
            male.setMaxCellCount(1);
        }
        if (out0 != null) {
            out0.bindTargetEntity(new EntityRefBinding(interactionTarget));
            out0.setCellOffset(2);
            out0.setMaxCellCount(2);
        }
        if (out1 != null) {
            out1.bindTargetEntity(new EntityRefBinding(interactionTarget));
            out1.setCellOffset(4);
            out1.setMaxCellCount(3);
        }
        if (out2 != null) {
            out2.bindTargetEntity(new EntityRefBinding(interactionTarget));
            out2.setCellOffset(7);
            out2.setMaxCellCount(2);
        }
    }

    @Override
    public void initialise() {
        inventory = find("inventory", InventoryGrid.class);
        female = find("female", InventoryGrid.class);
        male = find("male", InventoryGrid.class);
        out0 = find("out0", InventoryGrid.class);
        out1 = find("out1", InventoryGrid.class);
        out2 = find("out2", InventoryGrid.class);
        lifespanBar = find("lifespanBar", LifespanBar.class);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        EntityRef interactionTarget = getInteractionTarget();
        ProcessingComponent matingComponent = interactionTarget.getComponent(ProcessingComponent.class);
        if (matingComponent != null) {
            lifespanBar.setColor(Color.RED);
            lifespanBar.setFill(Math.min(ApiarySystem.MATING_TIME + time.getGameTimeInMs() - matingComponent.finishTime, ApiarySystem.MATING_TIME)
                    / (float) ApiarySystem.MATING_TIME);
        } else {
            EntityRef femaleBee =
                    interactionTarget.getComponent(InventoryComponent.class).itemSlots.get(ApiarySystem.SLOT_FEMALE);
            if (femaleBee.hasComponent(MatedComponent.class)) {
                lifespanBar.setColor(Color.YELLOW);
                MatedComponent matedComponent = femaleBee.getComponent(MatedComponent.class);
                lifespanBar.setFill((float) matedComponent.ticksRemaining / matedComponent.lifespan);
            } else {
                lifespanBar.setFill(0f);
            }
        }
    }

    @Override
    public boolean isModal() {
        return false;
    }

    private static class EntityRefBinding extends ReadOnlyBinding<EntityRef> {
        private final EntityRef entityRef;

        public EntityRefBinding(EntityRef entityRef) {
            this.entityRef = entityRef;
        }

        @Override
        public EntityRef get() {
            return entityRef;
        }
    }
}
