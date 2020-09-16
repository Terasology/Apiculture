// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.apiculture.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.inventory.logic.ItemDifferentiating;
import org.terasology.soundygenetics.components.GeneticsComponent;

import java.util.Objects;

/**
 * Stores the genetic and lifespan information for a queen bee.
 */
public final class MatedComponent implements Component, ItemDifferentiating {
    /**
     * An entity containing the genetics component of the drone that mated with the queen.
     */
    public EntityRef container;

    /**
     * The number of ticks remaining in the lifespan of the queen.
     */
    public int ticksRemaining;

    /**
     * The total ticks in the entire lifespan of this queen.
     */
    public int lifespan;

    public MatedComponent() {
    }

    public MatedComponent(GeneticsComponent geneticsComponent, int lifespan, EntityManager entityManager) {
        container = entityManager.create(geneticsComponent);
        this.lifespan = lifespan;
        ticksRemaining = lifespan;
    }

    public boolean equals(Object o) {
        if (!(o instanceof MatedComponent)) {
            return false;
        }
        MatedComponent matedComponent = ((MatedComponent) o);
        return ticksRemaining == matedComponent.ticksRemaining && lifespan == matedComponent.lifespan
                && container.getComponent(GeneticsComponent.class).equals(container.getComponent(GeneticsComponent.class));
    }

    public int hashCode() {
        return Objects.hash(ticksRemaining, lifespan, container.getComponent(GeneticsComponent.class));
    }
}
