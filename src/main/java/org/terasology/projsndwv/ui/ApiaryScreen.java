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
package org.terasology.projsndwv.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.projsndwv.TempBeeRegistry;
import org.terasology.projsndwv.components.ApiaryMatingComponent;
import org.terasology.projsndwv.components.BeeComponent;
import org.terasology.projsndwv.components.MatedComponent;
import org.terasology.projsndwv.genetics.components.GeneticsComponent;
import org.terasology.projsndwv.systems.ApiarySystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryGrid;

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

    private static final Logger logger = LoggerFactory.getLogger(ApiaryScreen.class);

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
        ApiaryMatingComponent matingComponent = interactionTarget.getComponent(ApiaryMatingComponent.class);
        if (matingComponent != null) {
            if (matingComponent.mateFinishTime <= time.getGameTimeInMs()) {
                interactionTarget.removeComponent(ApiaryMatingComponent.class);

                EntityManager entityManager = CoreRegistry.get(EntityManager.class);
                if (entityManager == null) {
                    logger.error("No EntityManager in registry");
                    return;
                }

                // TODO: Move to ApiarySystem
                EntityRef femaleBee = interactionTarget.getComponent(InventoryComponent.class).itemSlots.get(ApiarySystem.SLOT_FEMALE);
                GeneticsComponent femaleGenetics = femaleBee.getComponent(GeneticsComponent.class);
                EntityRef maleBee = interactionTarget.getComponent(InventoryComponent.class).itemSlots.get(ApiarySystem.SLOT_MALE);
                femaleBee.addComponent(new MatedComponent(maleBee.getComponent(GeneticsComponent.class), TempBeeRegistry.getLifespanFromGenome(femaleGenetics.activeGenes.get(ApiarySystem.LOCUS_LIFESPAN)), entityManager));
                BeeComponent beeComponent = femaleBee.getComponent(BeeComponent.class);
                beeComponent.type = BeeComponent.BeeType.QUEEN;
                femaleBee.saveComponent(beeComponent);
                ItemComponent itemComponent = femaleBee.getComponent(ItemComponent.class);
                itemComponent.icon = TempBeeRegistry.getTextureRegionAssetForSpeciesAndType(femaleBee.getComponent(GeneticsComponent.class).activeGenes.get(ApiarySystem.LOCUS_SPECIES), 2);
                femaleBee.saveComponent(itemComponent);
                femaleBee.saveComponent(TempBeeRegistry.getDisplayNameComponentForSpeciesAndType(femaleBee.getComponent(GeneticsComponent.class).activeGenes.get(ApiarySystem.LOCUS_SPECIES), 2));
                maleBee.destroy();

                DelayManager delayManager = CoreRegistry.get(DelayManager.class);
                if (delayManager == null) {
                    logger.error("No DelayManager in registry");
                    return;
                }

                delayManager.addDelayedAction(interactionTarget, ApiarySystem.LIFE_TICK_ACTION, TempBeeRegistry.getTickTimeFromGenome(femaleGenetics.activeGenes.get(ApiarySystem.LOCUS_SPEED)));
            }
            else {
                lifespanBar.setColor(Color.RED);
                lifespanBar.setFill((ApiarySystem.MATING_TIME + time.getGameTimeInMs() - matingComponent.mateFinishTime) / (float)ApiarySystem.MATING_TIME);
            }
        }
        else {
            EntityRef femaleBee = interactionTarget.getComponent(InventoryComponent.class).itemSlots.get(ApiarySystem.SLOT_FEMALE);
            if (femaleBee.hasComponent(MatedComponent.class)) {
                lifespanBar.setColor(Color.YELLOW);
                MatedComponent matedComponent = femaleBee.getComponent(MatedComponent.class);
                lifespanBar.setFill((float)matedComponent.ticksRemaining / matedComponent.lifespan);
            }
            else {
                lifespanBar.setFill(0f);
            }
        }
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
