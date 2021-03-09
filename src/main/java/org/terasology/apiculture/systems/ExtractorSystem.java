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
package org.terasology.apiculture.systems;

import org.terasology.apiculture.TempBeeRegistry;
import org.terasology.apiculture.components.BeeComponent;
import org.terasology.apiculture.components.ExtractorComponent;
import org.terasology.apiculture.components.LocusSampleComponent;
import org.terasology.apiculture.components.ProcessingComponent;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.engine.logic.inventory.InventoryComponent;
import org.terasology.engine.logic.inventory.InventoryManager;
import org.terasology.engine.logic.inventory.events.BeforeItemPutInInventory;
import org.terasology.engine.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.random.MersenneRandom;
import org.terasology.genetics.components.GeneticsComponent;

@RegisterSystem(RegisterMode.ALWAYS) // TODO: Authority
public class ExtractorSystem extends BaseComponentSystem {
    /** The slot index for the input slot of the extractor. */
    public static final int SLOT_INPUT = 0;

    /** The slot index for the output slot of the extractor. */
    public static final int SLOT_OUTPUT = 1;


    /** The delayed action id for extraction completion. */
    public static final String EXTRACT_EVENT = "extract";

    /** The time, in milliseconds, that extraction takes. */
    public static final long EXTRACT_TIME = 60000L;

    @In
    private DelayManager delayManager;

    @In
    private Time time;

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    private MersenneRandom random = new MersenneRandom();

    /**
     * Consumes BeforeItemPutInInventory events, handling inventory access controls.
     *
     * Prevents non-bees from being placed into the extractor, and any items being placed into the output.
     */
    @ReceiveEvent
    public void beforeItemPutIntoExtractor(BeforeItemPutInInventory event, EntityRef entity, ExtractorComponent component) {
        if (event.getSlot() == SLOT_INPUT) {
            if (!event.getItem().hasComponent(BeeComponent.class)) {
                event.consume();
            }
        } else if (event.getInstigator() != entity) {
            event.consume();
        }
    }

    /**
     * Receives inventory change events, scheduling appropriate functional events.
     *
     * Schedules an extraction end event if a bee is placed in the input, and cancels appropriate events if the input is removed.
     */
    @ReceiveEvent
    public void onExtractorItemChanged(InventorySlotChangedEvent event, EntityRef entity, ExtractorComponent component) {
        if (event.getSlot() == SLOT_INPUT) {
            if (!event.getNewItem().hasComponent(BeeComponent.class)) {
                entity.removeComponent(ProcessingComponent.class);
                if (delayManager.hasDelayedAction(entity, EXTRACT_EVENT)) {
                    delayManager.cancelDelayedAction(entity, EXTRACT_EVENT);
                }
            } else {
                entity.addComponent(new ProcessingComponent(time.getGameTimeInMs() + EXTRACT_TIME));
                delayManager.addDelayedAction(entity, EXTRACT_EVENT, EXTRACT_TIME);
            }
        }
    }

    /**
     * Handles extraction end events, choosing a random locus from the input bees genetics, placing a sample of it in
     * the output, and destroys the bee.
     */
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
