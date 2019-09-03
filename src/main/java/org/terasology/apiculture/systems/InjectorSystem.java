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

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.events.BeforeItemPutInInventory;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.apiculture.TempBeeRegistry;
import org.terasology.apiculture.components.BeeComponent;
import org.terasology.apiculture.components.InjectorComponent;
import org.terasology.apiculture.components.LocusSampleComponent;
import org.terasology.apiculture.components.ProcessingComponent;
import org.terasology.apiculture.genetics.components.GeneticsComponent;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.ALWAYS) // TODO: Authority
public class InjectorSystem extends BaseComponentSystem {
    @In
    private DelayManager delayManager;

    @In
    private Time time;

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    /** The slot index for the sample input slot. */
    public static final int SLOT_INPUT = 0;

    /** The slot index for the bee input slot. */
    public static final int SLOT_BEE = 1;


    /** The delayed action id for injection completion. */
    public static final String INJECT_EVENT = "inject";

    /** The time, in milliseconds, that injection takes. */
    public static final long INJECT_TIME = 60000L;


    /**
     * Consumes BeforeItemPutInInventory events, handling inventory access controls.
     *
     * Prevents non-bees from being placed into the bee input slot, and non-genetic sample items from being placed
     * in the sample input slot.
     */
    @ReceiveEvent
    public void beforeItemPutIntoInjector(BeforeItemPutInInventory event, EntityRef entity, InjectorComponent component) {
        if (event.getSlot() == SLOT_INPUT) {
            if (!event.getItem().hasComponent(LocusSampleComponent.class)) {
                event.consume();
            }
        }
        else {
            if (!event.getItem().hasComponent(BeeComponent.class)) {
                event.consume();
            }
        }
    }

    /**
     * Receives inventory change events, scheduling appropriate functional events.
     *
     * Schedules an injection end event if a bee and genetic sample are present in their respective slots,
     * and cancels appropriate events if either input is removed.
     */
    @ReceiveEvent
    public void onInjectorItemChanged(InventorySlotChangedEvent event, EntityRef entity, InjectorComponent component) {
        if (event.getSlot() == SLOT_INPUT) {
            if (!event.getNewItem().hasComponent(LocusSampleComponent.class)) {
                entity.removeComponent(ProcessingComponent.class);
                if (delayManager.hasDelayedAction(entity, INJECT_EVENT)) {
                    delayManager.cancelDelayedAction(entity, INJECT_EVENT);
                }
            }
            else if (entity.getComponent(InventoryComponent.class).itemSlots.get(SLOT_BEE).hasComponent(BeeComponent.class)){
                entity.addComponent(new ProcessingComponent(time.getGameTimeInMs() + INJECT_TIME));
                delayManager.addDelayedAction(entity, INJECT_EVENT, INJECT_TIME);
            }
        }
        else {
            if (!event.getNewItem().hasComponent(BeeComponent.class)) {
                entity.removeComponent(ProcessingComponent.class);
                if (delayManager.hasDelayedAction(entity, INJECT_EVENT)) {
                    delayManager.cancelDelayedAction(entity, INJECT_EVENT);
                }
            }
            else if (entity.getComponent(InventoryComponent.class).itemSlots.get(SLOT_INPUT).hasComponent(LocusSampleComponent.class)) {
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
        LocusSampleComponent locusSampleComponent = entity.getComponent(InventoryComponent.class).itemSlots.get(SLOT_INPUT).getComponent(LocusSampleComponent.class);

        geneticsComponent.activeGenes.set(locusSampleComponent.locus, locusSampleComponent.genotype);
        geneticsComponent.inactiveGenes.set(locusSampleComponent.locus, locusSampleComponent.genotype);

        bee.saveComponent(geneticsComponent);

        TempBeeRegistry.modifyItemForSpeciesAndType(bee);
    }
}
