// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.apiculture.systems;

import org.terasology.apiculture.TempBeeRegistry;
import org.terasology.apiculture.components.BeeComponent;
import org.terasology.apiculture.components.InjectorComponent;
import org.terasology.apiculture.components.LocusSampleComponent;
import org.terasology.apiculture.components.ProcessingComponent;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.engine.registry.In;
import org.terasology.inventory.logic.InventoryComponent;
import org.terasology.inventory.logic.InventoryManager;
import org.terasology.inventory.logic.events.BeforeItemPutInInventory;
import org.terasology.inventory.logic.events.InventorySlotChangedEvent;
import org.terasology.soundygenetics.components.GeneticsComponent;

@RegisterSystem(RegisterMode.ALWAYS) // TODO: Authority
public class InjectorSystem extends BaseComponentSystem {
    /**
     * The slot index for the sample input slot.
     */
    public static final int SLOT_INPUT = 0;

    /**
     * The slot index for the bee input slot.
     */
    public static final int SLOT_BEE = 1;


    /**
     * The delayed action id for injection completion.
     */
    public static final String INJECT_EVENT = "inject";

    /**
     * The time, in milliseconds, that injection takes.
     */
    public static final long INJECT_TIME = 60000L;

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
     * Prevents non-bees from being placed into the bee input slot, and non-genetic sample items from being placed in
     * the sample input slot.
     */
    @ReceiveEvent
    public void beforeItemPutIntoInjector(BeforeItemPutInInventory event, EntityRef entity,
                                          InjectorComponent component) {
        if (event.getSlot() == SLOT_INPUT) {
            if (!event.getItem().hasComponent(LocusSampleComponent.class)) {
                event.consume();
            }
        } else {
            if (!event.getItem().hasComponent(BeeComponent.class)) {
                event.consume();
            }
        }
    }

    /**
     * Receives inventory change events, scheduling appropriate functional events.
     * <p>
     * Schedules an injection end event if a bee and genetic sample are present in their respective slots, and cancels
     * appropriate events if either input is removed.
     */
    @ReceiveEvent
    public void onInjectorItemChanged(InventorySlotChangedEvent event, EntityRef entity, InjectorComponent component) {
        if (event.getSlot() == SLOT_INPUT) {
            if (!event.getNewItem().hasComponent(LocusSampleComponent.class)) {
                entity.removeComponent(ProcessingComponent.class);
                if (delayManager.hasDelayedAction(entity, INJECT_EVENT)) {
                    delayManager.cancelDelayedAction(entity, INJECT_EVENT);
                }
            } else if (entity.getComponent(InventoryComponent.class).itemSlots.get(SLOT_BEE).hasComponent(BeeComponent.class)) {
                entity.addComponent(new ProcessingComponent(time.getGameTimeInMs() + INJECT_TIME));
                delayManager.addDelayedAction(entity, INJECT_EVENT, INJECT_TIME);
            }
        } else {
            if (!event.getNewItem().hasComponent(BeeComponent.class)) {
                entity.removeComponent(ProcessingComponent.class);
                if (delayManager.hasDelayedAction(entity, INJECT_EVENT)) {
                    delayManager.cancelDelayedAction(entity, INJECT_EVENT);
                }
            } else if (entity.getComponent(InventoryComponent.class).itemSlots.get(SLOT_INPUT).hasComponent(LocusSampleComponent.class)) {
                entity.addComponent(new ProcessingComponent(time.getGameTimeInMs() + INJECT_TIME));
                delayManager.addDelayedAction(entity, INJECT_EVENT, INJECT_TIME);
            }
        }
    }

    /**
     * Handles injection end events, replacing the corresponding gene of the input bee with the gene in the sample.
     */
    @ReceiveEvent
    public void onInjectorEvent(DelayedActionTriggeredEvent event, EntityRef entity, InjectorComponent component) {
        entity.removeComponent(ProcessingComponent.class);

        EntityRef bee = entity.getComponent(InventoryComponent.class).itemSlots.get(SLOT_BEE);
        GeneticsComponent geneticsComponent = bee.getComponent(GeneticsComponent.class);
        LocusSampleComponent locusSampleComponent =
                entity.getComponent(InventoryComponent.class).itemSlots.get(SLOT_INPUT).getComponent(LocusSampleComponent.class);

        geneticsComponent.activeGenes.set(locusSampleComponent.locus, locusSampleComponent.genotype);
        geneticsComponent.inactiveGenes.set(locusSampleComponent.locus, locusSampleComponent.genotype);

        bee.saveComponent(geneticsComponent);

        TempBeeRegistry.modifyItemForSpeciesAndType(bee);
    }
}
