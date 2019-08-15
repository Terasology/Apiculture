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
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.events.BeforeItemPutInInventory;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.projsndwv.TempBeeRegistry;
import org.terasology.projsndwv.components.ApiaryComponent;
import org.terasology.projsndwv.components.ApiaryMatingComponent;
import org.terasology.projsndwv.components.BeeComponent;
import org.terasology.projsndwv.components.MatedComponent;
import org.terasology.projsndwv.genetics.Genome;
import org.terasology.projsndwv.genetics.components.GeneticsComponent;
import org.terasology.registry.In;
import org.terasology.world.generator.WorldGenerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Handles Apiary related events. Also contains a number of apiary related constants.
 */
@RegisterSystem(RegisterMode.ALWAYS) // TODO: Authority
public class ApiarySystem extends BaseComponentSystem {
    @In
    private DelayManager delayManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private EntityManager entityManager;

    @In
    private WorldGenerator worldGenerator;

    @In
    private Time time;

    private Genome genome;

    /**
     * The delayed action id for life ticks.
     */
    public static final String LIFE_TICK_EVENT = "life_tick";

    /**
     * The delayed action id for the completion of mating.
     */
    public static final String MATING_EVENT = "mating";



    /**
     * The slot index for the princess/queen bee slot in an apiary's inventory.
     */
    public static final int SLOT_FEMALE = 0;

    /**
     * The slot index for the drone bee slot in an apiary's inventory.
     */
    public static final int SLOT_MALE = 1;

    /**
     * A list of slot indices for the otuput slots in an apiary's inventory.
     */
    public static final List<Integer> SLOTS_OUT = Collections.unmodifiableList(Arrays.asList(2, 3, 4, 5, 6, 7, 8));



    /**
     * The locus in a bee's genetics indicating the species of a bee.
     */
    public static final int LOCUS_SPECIES = 0;

    /**
     * The locus in a bee's genetics indicating the speed at which a bee's life ticks down.
     */
    public static final int LOCUS_SPEED = 1;

    /**
     * The locus in a bee's genetics indicating the length of a bee's lifespan.
     */
    public static final int LOCUS_LIFESPAN = 2;

    /**
     * The locus in a bee's genetics indicating the number of drone offspring a bee will have.
     */
    public static final int LOCUS_OFFSPRING_COUNT = 3;

    /**
     * The time, in milliseconds, that mating takes in an apiary.
     */
    public static final long MATING_TIME = 1000L;

    @ReceiveEvent
    public void beforeItemPutIntoApiary(BeforeItemPutInInventory event, EntityRef entity, ApiaryComponent component) {
        if (event.getSlot() == SLOT_FEMALE) {
            if (!event.getItem().hasComponent(BeeComponent.class) || event.getItem().getComponent(BeeComponent.class).type == BeeComponent.BeeType.DRONE) {
                event.consume();
            }
        }
        else if (event.getSlot() == SLOT_MALE) {
            if (!event.getItem().hasComponent(BeeComponent.class) || event.getItem().getComponent(BeeComponent.class).type != BeeComponent.BeeType.DRONE) {
                event.consume();
            }
        }
        else if (event.getInstigator() != entity) {
            event.consume();
        }
    }

    @ReceiveEvent
    public void onApiaryInventoryChanged(InventorySlotChangedEvent event, EntityRef entity, ApiaryComponent component) {
        if (event.getSlot() == SLOT_FEMALE) {
            if (!event.getNewItem().hasComponent(BeeComponent.class)) {
                entity.removeComponent(ApiaryMatingComponent.class);
                if (delayManager.hasDelayedAction(entity, MATING_EVENT)) {
                    delayManager.cancelDelayedAction(entity, MATING_EVENT);
                }
                if (delayManager.hasDelayedAction(entity, LIFE_TICK_EVENT)) {
                    delayManager.cancelDelayedAction(entity, LIFE_TICK_EVENT);
                }
            }
            else if (event.getNewItem().getComponent(BeeComponent.class).type == BeeComponent.BeeType.PRINCESS){
                BeeComponent maleBee = entity.getComponent(InventoryComponent.class).itemSlots.get(SLOT_MALE).getComponent(BeeComponent.class);
                if (maleBee != null) {
                    entity.addOrSaveComponent(new ApiaryMatingComponent(time.getGameTimeInMs() + MATING_TIME));
                    delayManager.addDelayedAction(entity, MATING_EVENT, MATING_TIME);
                }
            }
            else {
                delayManager.addDelayedAction(entity, LIFE_TICK_EVENT, TempBeeRegistry.getTickTimeFromGenome(event.getNewItem().getComponent(GeneticsComponent.class).activeGenes.get(LOCUS_SPEED)));
            }
        }
        else if (event.getSlot() == SLOT_MALE){
            if (!event.getNewItem().hasComponent(BeeComponent.class)) {
                entity.removeComponent(ApiaryMatingComponent.class);
                if (delayManager.hasDelayedAction(entity, MATING_EVENT)) {
                    delayManager.cancelDelayedAction(entity, MATING_EVENT);
                }
            }
            else {
                BeeComponent femaleBee = entity.getComponent(InventoryComponent.class).itemSlots.get(SLOT_FEMALE).getComponent(BeeComponent.class);
                if (femaleBee != null) {
                    if (femaleBee.type == BeeComponent.BeeType.PRINCESS) {
                        entity.addOrSaveComponent(new ApiaryMatingComponent(time.getGameTimeInMs() + MATING_TIME));
                        delayManager.addDelayedAction(entity, MATING_EVENT, MATING_TIME);
                    }
                }
            }
        }
    }

    @ReceiveEvent
    public void onApiaryEvent(DelayedActionTriggeredEvent event, EntityRef entity, ApiaryComponent component) {
        if (event.getActionId().equals(LIFE_TICK_EVENT)) {
            onLifeTick(entity);
        }
        else if (event.getActionId().equals(MATING_EVENT)) {
            onMatingFinished(entity);
        }
    }

    private void onLifeTick(EntityRef entity) {
        EntityRef queenBee = entity.getComponent(InventoryComponent.class).itemSlots.get(SLOT_FEMALE);

        GeneticsComponent queenGenetics = queenBee.getComponent(GeneticsComponent.class);
        inventoryManager.giveItem(entity, entity, TempBeeRegistry.getProduceForSpeciesWithChance(queenGenetics.activeGenes.get(LOCUS_SPECIES)), SLOTS_OUT);

        MatedComponent matedComponent = queenBee.getComponent(MatedComponent.class);
        matedComponent.ticksRemaining--;

        if (matedComponent.ticksRemaining == 0) {
            birth(queenBee, entity);
        } else {
            queenBee.saveComponent(matedComponent);
            delayManager.addDelayedAction(entity, LIFE_TICK_EVENT, TempBeeRegistry.getTickTimeFromGenome(queenBee.getComponent(GeneticsComponent.class).activeGenes.get(LOCUS_SPEED)));
        }
    }

    private void birth(EntityRef queen, EntityRef apiary) {
        Iterator<GeneticsComponent> offspringGenetics = getGenome().combine(queen.getComponent(GeneticsComponent.class), queen.getComponent(MatedComponent.class).container.getComponent(GeneticsComponent.class));

        EntityRef offspring = entityManager.create("Apiculture:bee_princess");
        GeneticsComponent geneticsComponent = offspringGenetics.next();
        offspring.addComponent(geneticsComponent);
        ItemComponent itemComponent = offspring.getComponent(ItemComponent.class);
        int species = geneticsComponent.activeGenes.get(LOCUS_SPECIES);
        itemComponent.icon = TempBeeRegistry.getTextureRegionAssetForSpeciesAndType(species, 1);
        offspring.addComponent(itemComponent);
        offspring.addComponent(TempBeeRegistry.getDisplayNameComponentForSpeciesAndType(species, 1));
        boolean success = inventoryManager.giveItem(apiary, apiary, offspring, SLOTS_OUT);

        if (success) {
            for (int i = 0; i < queen.getComponent(GeneticsComponent.class).activeGenes.get(LOCUS_OFFSPRING_COUNT); i++) {
                offspring = entityManager.create("Apiculture:bee_drone");
                geneticsComponent = offspringGenetics.next();
                offspring.addComponent(geneticsComponent);
                itemComponent = offspring.getComponent(ItemComponent.class);
                species = geneticsComponent.activeGenes.get(LOCUS_SPECIES);
                itemComponent.icon = TempBeeRegistry.getTextureRegionAssetForSpeciesAndType(species, 0);
                offspring.addComponent(itemComponent);
                offspring.addComponent(TempBeeRegistry.getDisplayNameComponentForSpeciesAndType(species, 0));
                success = inventoryManager.giveItem(apiary, apiary, offspring, SLOTS_OUT);
                if (!success) {
                    break;
                }
            }
        }

        queen.destroy();
    }

    private void onMatingFinished(EntityRef entity) {
        entity.removeComponent(ApiaryMatingComponent.class);

        EntityRef femaleBee = entity.getComponent(InventoryComponent.class).itemSlots.get(ApiarySystem.SLOT_FEMALE);
        GeneticsComponent femaleGenetics = femaleBee.getComponent(GeneticsComponent.class);
        EntityRef maleBee = entity.getComponent(InventoryComponent.class).itemSlots.get(ApiarySystem.SLOT_MALE);
        femaleBee.addComponent(new MatedComponent(maleBee.getComponent(GeneticsComponent.class), TempBeeRegistry.getLifespanFromGenome(femaleGenetics.activeGenes.get(ApiarySystem.LOCUS_LIFESPAN)), entityManager));
        BeeComponent beeComponent = femaleBee.getComponent(BeeComponent.class);
        beeComponent.type = BeeComponent.BeeType.QUEEN;
        femaleBee.saveComponent(beeComponent);
        ItemComponent itemComponent = femaleBee.getComponent(ItemComponent.class);
        itemComponent.icon = TempBeeRegistry.getTextureRegionAssetForSpeciesAndType(femaleBee.getComponent(GeneticsComponent.class).activeGenes.get(ApiarySystem.LOCUS_SPECIES), 2);
        femaleBee.saveComponent(itemComponent);
        femaleBee.saveComponent(TempBeeRegistry.getDisplayNameComponentForSpeciesAndType(femaleBee.getComponent(GeneticsComponent.class).activeGenes.get(ApiarySystem.LOCUS_SPECIES), 2));
        maleBee.destroy();

        delayManager.addDelayedAction(entity, ApiarySystem.LIFE_TICK_EVENT, TempBeeRegistry.getTickTimeFromGenome(femaleGenetics.activeGenes.get(ApiarySystem.LOCUS_SPEED)));
    }

    private Genome getGenome() {
        if (genome == null) {
            genome = new Genome(4, worldGenerator.getWorldSeed().hashCode());

            GeneticsComponent beeCComponent = new GeneticsComponent(4);

            beeCComponent.activeGenes.add(2); // TODO: (Soundwave) This is bad, both because the size isn't ensured, and because it's messy.
            beeCComponent.activeGenes.add(2);
            beeCComponent.activeGenes.add(2);
            beeCComponent.activeGenes.add(4);

            beeCComponent.inactiveGenes.add(2);
            beeCComponent.inactiveGenes.add(2);
            beeCComponent.inactiveGenes.add(2);
            beeCComponent.inactiveGenes.add(4);

            genome.registerMutation(0, 0, 1, beeCComponent, 0.05f);
        }
        return genome;
    }
}
