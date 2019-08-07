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
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.events.BeforeItemPutInInventory;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.projsndwv.TempBeeRegistry;
import org.terasology.projsndwv.components.ApiaryComponent;
import org.terasology.projsndwv.components.ApiaryMatingComponent;
import org.terasology.projsndwv.components.BeeComponent;
import org.terasology.projsndwv.components.MatedComponent;
import org.terasology.projsndwv.genetics.components.GeneticsComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.ALWAYS) // TODO: Authority
public class ApiarySystem extends BaseComponentSystem {
    @In
    DelayManager delayManager;

    @ReceiveEvent
    public void beforeItemPutIntoApiary(BeforeItemPutInInventory event, EntityRef entity, ApiaryComponent component) {
        if (!event.getItem().hasComponent(BeeComponent.class)) {
            event.consume();
        }
        else if (event.getSlot() == 0) {
            if (event.getItem().getComponent(BeeComponent.class).type == BeeComponent.BeeType.DRONE) {
                event.consume();
            }
        }
        else if (event.getSlot() == 1) {
            if (event.getItem().getComponent(BeeComponent.class).type != BeeComponent.BeeType.DRONE) {
                event.consume();
            }
        }
        else {
            event.consume();
        }
    }

    @ReceiveEvent
    public void onApiaryInventoryChanged(InventorySlotChangedEvent event, EntityRef entity, ApiaryComponent component) {
        DelayManager delayManager = CoreRegistry.get(DelayManager.class);
        if (delayManager == null) {
            // TODO: Log
            return;
        }

        if (event.getSlot() == 0) {
            if (!event.getNewItem().hasComponent(BeeComponent.class)) {
                entity.removeComponent(ApiaryMatingComponent.class);
                if (delayManager.hasDelayedAction(entity, "life_tick")) { // TODO: Action ID constant
                    delayManager.cancelDelayedAction(entity, "life_tick");
                }
            }
            else if (event.getNewItem().getComponent(BeeComponent.class).type == BeeComponent.BeeType.PRINCESS){
                BeeComponent maleBee = entity.getComponent(InventoryComponent.class).itemSlots.get(1).getComponent(BeeComponent.class);
                if (maleBee != null) {
                    entity.addOrSaveComponent(new ApiaryMatingComponent());
                }
            }
            else {
                delayManager.addDelayedAction(entity, "life_tick", TempBeeRegistry.getTickTimeFromGenome(event.getNewItem().getComponent(GeneticsComponent.class).activeGenes.get(1))); // TODO: Genome Constant
            }
        }
        else if (event.getSlot() == 1){
            if (!event.getNewItem().hasComponent(BeeComponent.class)) {
                entity.removeComponent(ApiaryMatingComponent.class);
            }
            else {
                BeeComponent femaleBee = entity.getComponent(InventoryComponent.class).itemSlots.get(0).getComponent(BeeComponent.class);
                if (femaleBee != null) {
                    if (femaleBee.type == BeeComponent.BeeType.PRINCESS) {
                        entity.addOrSaveComponent(new ApiaryMatingComponent());
                    }
                }
            }
        }
    }

    @ReceiveEvent
    public void onApiaryLifeTick(DelayedActionTriggeredEvent event, EntityRef entity, ApiaryComponent component) {
        EntityRef queenBee = entity.getComponent(InventoryComponent.class).itemSlots.get(0);
        MatedComponent matedComponent = queenBee.getComponent(MatedComponent.class);
        matedComponent.ticksRemaining--;
        if (matedComponent.ticksRemaining == 0) {
            queenBee.destroy();
        }
        else {
            queenBee.saveComponent(matedComponent);
            delayManager.addDelayedAction(entity, "life_tick", TempBeeRegistry.getTickTimeFromGenome(queenBee.getComponent(GeneticsComponent.class).activeGenes.get(1))); // TODO: Genome constant
        }
    }
}
