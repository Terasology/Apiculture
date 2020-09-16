// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.apiculture.systems;

import org.terasology.apiculture.TempBeeRegistry;
import org.terasology.apiculture.components.ApiaryComponent;
import org.terasology.apiculture.components.BeeComponent;
import org.terasology.apiculture.components.MatedComponent;
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
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.inventory.logic.InventoryComponent;
import org.terasology.inventory.logic.InventoryManager;
import org.terasology.inventory.logic.events.BeforeItemPutInInventory;
import org.terasology.inventory.logic.events.InventorySlotChangedEvent;
import org.terasology.soundygenetics.Genome;
import org.terasology.soundygenetics.components.GeneticsComponent;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Handles Apiary related events. Also contains a number of apiary related constants.
 */
@RegisterSystem(RegisterMode.ALWAYS) // TODO: Authority
public class ApiarySystem extends BaseComponentSystem {
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
     * Consumes BeforeItemPutInInventory events, handling inventory access controls.
     * <p>
     * Prevents non-bees from being placed into the apiary, bees being placed into the slot inappropriate for its sex,
     * and any items being placed into the output.
     */
    @ReceiveEvent
    public void beforeItemPutIntoApiary(BeforeItemPutInInventory event, EntityRef entity, ApiaryComponent component) {
        if (event.getSlot() == SLOT_FEMALE) {
            if (!event.getItem().hasComponent(BeeComponent.class) || event.getItem().getComponent(BeeComponent.class).type == BeeComponent.BeeType.DRONE) {
                event.consume();
            }
        } else if (event.getSlot() == SLOT_MALE) {
            if (!event.getItem().hasComponent(BeeComponent.class) || event.getItem().getComponent(BeeComponent.class).type != BeeComponent.BeeType.DRONE) {
                event.consume();
            }
        } else if (event.getInstigator() != entity) {
            event.consume();
        }
    }

    /**
     * Receives inventory change events, scheduling appropriate functional events.
     * <p>
     * Schedules a lifespan tick if a queen is placed into the top slot, a mating end event if a princess-drone pair is
     * completed, and cancels appropriate scheduled events if the queen is removed or the princess-drone pair is broken
     * before mating is complete.
     */
    @ReceiveEvent
    public void onApiaryInventoryChanged(InventorySlotChangedEvent event, EntityRef entity, ApiaryComponent component) {
        if (event.getSlot() == SLOT_FEMALE) {
            if (!event.getNewItem().hasComponent(BeeComponent.class)) {
                entity.removeComponent(ProcessingComponent.class);
                if (delayManager.hasDelayedAction(entity, MATING_EVENT)) {
                    delayManager.cancelDelayedAction(entity, MATING_EVENT);
                }
                if (delayManager.hasDelayedAction(entity, LIFE_TICK_EVENT)) {
                    delayManager.cancelDelayedAction(entity, LIFE_TICK_EVENT);
                }
            } else if (event.getNewItem().getComponent(BeeComponent.class).type == BeeComponent.BeeType.PRINCESS) {
                BeeComponent maleBee =
                        entity.getComponent(InventoryComponent.class).itemSlots.get(SLOT_MALE).getComponent(BeeComponent.class);
                if (maleBee != null) {
                    entity.addOrSaveComponent(new ProcessingComponent(time.getGameTimeInMs() + MATING_TIME));
                    delayManager.addDelayedAction(entity, MATING_EVENT, MATING_TIME);
                }
            } else {
                delayManager.addDelayedAction(entity, LIFE_TICK_EVENT,
                        TempBeeRegistry.getTickTimeFromGenome(event.getNewItem().getComponent(GeneticsComponent.class).activeGenes.get(LOCUS_SPEED)));
            }
        } else if (event.getSlot() == SLOT_MALE) {
            if (!event.getNewItem().hasComponent(BeeComponent.class)) {
                entity.removeComponent(ProcessingComponent.class);
                if (delayManager.hasDelayedAction(entity, MATING_EVENT)) {
                    delayManager.cancelDelayedAction(entity, MATING_EVENT);
                }
            } else {
                BeeComponent femaleBee =
                        entity.getComponent(InventoryComponent.class).itemSlots.get(SLOT_FEMALE).getComponent(BeeComponent.class);
                if (femaleBee != null) {
                    if (femaleBee.type == BeeComponent.BeeType.PRINCESS) {
                        entity.addOrSaveComponent(new ProcessingComponent(time.getGameTimeInMs() + MATING_TIME));
                        delayManager.addDelayedAction(entity, MATING_EVENT, MATING_TIME);
                    }
                }
            }
        }
    }

    /**
     * Receives and sorts Apiary-related delayed actions, namely lifespan ticks and mating end events.
     */
    @ReceiveEvent
    public void onApiaryEvent(DelayedActionTriggeredEvent event, EntityRef entity, ApiaryComponent component) {
        if (event.getActionId().equals(LIFE_TICK_EVENT)) {
            onLifeTick(entity);
        } else if (event.getActionId().equals(MATING_EVENT)) {
            onMatingFinished(entity);
        }
    }

    /**
     * Handles a lifespan tick for a queen in a given apiary.
     * <p>
     * Generates produce, updates the remaining lifespan, and triggers birthing if the end of lifespan has been
     * reached.
     *
     * @param entity The apiary containing the queen for which a lifespan tick is to be completed.
     */
    private void onLifeTick(EntityRef entity) {
        EntityRef queenBee = entity.getComponent(InventoryComponent.class).itemSlots.get(SLOT_FEMALE);

        GeneticsComponent queenGenetics = queenBee.getComponent(GeneticsComponent.class);
        inventoryManager.giveItem(entity, entity,
                TempBeeRegistry.getProduceForSpeciesWithChance(queenGenetics.activeGenes.get(LOCUS_SPECIES)),
                SLOTS_OUT);

        MatedComponent matedComponent = queenBee.getComponent(MatedComponent.class);
        matedComponent.ticksRemaining--;

        if (matedComponent.ticksRemaining == 0) {
            birth(entity);
        } else {
            queenBee.saveComponent(matedComponent);
            delayManager.addDelayedAction(entity, LIFE_TICK_EVENT,
                    TempBeeRegistry.getTickTimeFromGenome(queenBee.getComponent(GeneticsComponent.class).activeGenes.get(LOCUS_SPEED)));
        }
    }

    /**
     * Handles queen birthing in a given apiary.
     * <p>
     * Generates offspring, placing them into the apiary's output if room is present, and destroys the queen.
     *
     * @param entity The apiary contianing the queen to give birth.
     */
    private void birth(EntityRef entity) {
        EntityRef queenBee = entity.getComponent(InventoryComponent.class).itemSlots.get(SLOT_FEMALE);
        Iterator<GeneticsComponent> offspringGenetics =
                getGenome().combine(queenBee.getComponent(GeneticsComponent.class),
                queenBee.getComponent(MatedComponent.class).container.getComponent(GeneticsComponent.class));

        EntityRef offspring = entityManager.create("Apiculture:bee_princess");
        GeneticsComponent geneticsComponent = offspringGenetics.next();
        offspring.addComponent(geneticsComponent);
        TempBeeRegistry.modifyItemForSpeciesAndType(offspring);
        boolean success = inventoryManager.giveItem(entity, entity, offspring, SLOTS_OUT);

        if (success) {
            for (int i = 0; i < queenBee.getComponent(GeneticsComponent.class).activeGenes.get(LOCUS_OFFSPRING_COUNT); i++) {
                offspring = entityManager.create("Apiculture:bee_drone");
                geneticsComponent = offspringGenetics.next();
                offspring.addComponent(geneticsComponent);
                TempBeeRegistry.modifyItemForSpeciesAndType(offspring);
                success = inventoryManager.giveItem(entity, entity, offspring, SLOTS_OUT);
                if (!success) {
                    break;
                }
            }
        }

        queenBee.destroy();
    }

    /**
     * Handles mating for a given apiary.
     * <p>
     * Turns the princess in an apiary into a queen containing the drone's genetics, destroying the drone, and schedules
     * the first lifespan tick.
     *
     * @param entity The apiary containing the princess and drone to mate.
     */
    private void onMatingFinished(EntityRef entity) {
        entity.removeComponent(ProcessingComponent.class);

        EntityRef femaleBee = entity.getComponent(InventoryComponent.class).itemSlots.get(ApiarySystem.SLOT_FEMALE);
        GeneticsComponent femaleGenetics = femaleBee.getComponent(GeneticsComponent.class);
        EntityRef maleBee = entity.getComponent(InventoryComponent.class).itemSlots.get(ApiarySystem.SLOT_MALE);
        femaleBee.addComponent(new MatedComponent(maleBee.getComponent(GeneticsComponent.class),
                TempBeeRegistry.getLifespanFromGenome(femaleGenetics.activeGenes.get(ApiarySystem.LOCUS_LIFESPAN)),
                entityManager));
        BeeComponent beeComponent = femaleBee.getComponent(BeeComponent.class);
        beeComponent.type = BeeComponent.BeeType.QUEEN;
        femaleBee.saveComponent(beeComponent);
        TempBeeRegistry.modifyItemForSpeciesAndType(femaleBee);
        maleBee.destroy();

        delayManager.addDelayedAction(entity, ApiarySystem.LIFE_TICK_EVENT,
                TempBeeRegistry.getTickTimeFromGenome(femaleGenetics.activeGenes.get(ApiarySystem.LOCUS_SPEED)));
    }

    private Genome getGenome() {
        if (genome == null) {
            genome = new Genome(4, worldGenerator.getWorldSeed().hashCode());

            GeneticsComponent beeCComponent = new GeneticsComponent(4);

            beeCComponent.activeGenes.add(2); // TODO: (Soundwave) This is bad, both because the size isn't ensured,
            // and because it's messy.
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
