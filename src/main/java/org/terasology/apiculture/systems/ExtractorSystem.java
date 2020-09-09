// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.random.MersenneRandom;
import org.terasology.genetics.components.GeneticsComponent;
import org.terasology.inventory.logic.InventoryComponent;
import org.terasology.inventory.logic.InventoryManager;
import org.terasology.inventory.logic.events.BeforeItemPutInInventory;
import org.terasology.inventory.logic.events.InventorySlotChangedEvent;

@RegisterSystem(RegisterMode.ALWAYS) // TODO: Authority
public class ExtractorSystem extends BaseComponentSystem {
    /**
     * The slot index for the input slot of the extractor.
     */
    public static final int SLOT_INPUT = 0;

    /**
     * The slot index for the output slot of the extractor.
     */
    public static final int SLOT_OUTPUT = 1;


    /**
     * The delayed action id for extraction completion.
     */
    public static final String EXTRACT_EVENT = "extract";

    /**
     * The time, in milliseconds, that extraction takes.
     */
    public static final long EXTRACT_TIME = 60000L;
    private final MersenneRandom random = new MersenneRandom();
    @In
    private DelayManager delayManager;
    @In
    private Time time;
    @In
    private EntityManager entityManager;
    @In
    private InventoryManager inventoryManager;

    /**
     * Consumes BeforeItemPutInInventory events, handling inventory access controls.
     * <p>
     * Prevents non-bees from being placed into the extractor, and any items being placed into the output.
     */
    @ReceiveEvent
    public void beforeItemPutIntoExtractor(BeforeItemPutInInventory event, EntityRef entity,
                                           ExtractorComponent component) {
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
     * <p>
     * Schedules an extraction end event if a bee is placed in the input, and cancels appropriate events if the input is
     * removed.
     */
    @ReceiveEvent
    public void onExtractorItemChanged(InventorySlotChangedEvent event, EntityRef entity,
                                       ExtractorComponent component) {
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
        displayNameComponent.name = "Sample: " + TempBeeRegistry.getDisplayNameComponentForLocusAndGenotype(locus,
                genotype);
        sample.addComponent(displayNameComponent);

        inventoryManager.giveItem(entity, entity, sample, SLOT_OUTPUT);
    }
}
