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

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.events.BeforeItemPutInInventory;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.projsndwv.TempBeeRegistry;
import org.terasology.projsndwv.components.BeeComponent;
import org.terasology.projsndwv.components.ExtractorComponent;
import org.terasology.projsndwv.components.LocusSampleComponent;
import org.terasology.projsndwv.components.ProcessingComponent;
import org.terasology.projsndwv.genetics.components.GeneticsComponent;
import org.terasology.registry.In;
import org.terasology.utilities.random.MersenneRandom;

@RegisterSystem(RegisterMode.ALWAYS) // TODO: Authority
public class ExtractorSystem extends BaseComponentSystem {
    @In
    private DelayManager delayManager;

    @In
    private Time time;

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;

    public static final String EXTRACT_EVENT = "extract";

    public static final long EXTRACT_TIME = 60000L;

    private MersenneRandom random = new MersenneRandom();

    @ReceiveEvent
    public void beforeItemPutIntoExtractor(BeforeItemPutInInventory event, EntityRef entity, ExtractorComponent component) {
        if (event.getSlot() == SLOT_INPUT) {
            if (!event.getItem().hasComponent(BeeComponent.class)) {
                event.consume();
            }
        }
        else if (event.getInstigator() != entity) {
            event.consume();
        }
    }

    @ReceiveEvent
    public void onExtractorItemChanged(InventorySlotChangedEvent event, EntityRef entity, ExtractorComponent component) {
        if (event.getSlot() == SLOT_INPUT) {
            if (!event.getNewItem().hasComponent(BeeComponent.class)) {
                entity.removeComponent(ProcessingComponent.class);
                if (delayManager.hasDelayedAction(entity, EXTRACT_EVENT)) {
                    delayManager.cancelDelayedAction(entity, EXTRACT_EVENT);
                }
            }
            else {
                entity.addComponent(new ProcessingComponent(time.getGameTimeInMs() + EXTRACT_TIME));
                delayManager.addDelayedAction(entity, EXTRACT_EVENT, EXTRACT_TIME);
            }
        }
    }

    @ReceiveEvent
    public void onExtractorEvent(DelayedActionTriggeredEvent event, EntityRef entity, ExtractorComponent component) {
        entity.removeComponent(ProcessingComponent.class);

        EntityRef bee = entity.getComponent(InventoryComponent.class).itemSlots.get(SLOT_INPUT);
        GeneticsComponent geneticsComponent = bee.getComponent(GeneticsComponent.class);

        bee.destroy();

        EntityRef sample = entityManager.create("Apiculture:genetic_sample");

        int locus = random.nextInt(geneticsComponent.size);
        int genotype = geneticsComponent.activeGenes.get(locus);

        sample.addComponent(new LocusSampleComponent(locus, genotype));

        DisplayNameComponent displayNameComponent = new DisplayNameComponent();
        displayNameComponent.name = "Sample: " + TempBeeRegistry.getDisplayNameComponentForLocusAndGenotype(locus, genotype);
        sample.addComponent(displayNameComponent);

        inventoryManager.giveItem(entity, entity, sample, SLOT_OUTPUT);
    }
}
