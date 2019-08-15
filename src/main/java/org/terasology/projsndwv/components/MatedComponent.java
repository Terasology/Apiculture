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
package org.terasology.projsndwv.components;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.ItemDifferentiating;
import org.terasology.projsndwv.genetics.components.GeneticsComponent;

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

    public MatedComponent() {}

    public MatedComponent(GeneticsComponent geneticsComponent, int lifespan, EntityManager entityManager) {
        container = entityManager.create(geneticsComponent);
        this.lifespan = lifespan;
        ticksRemaining = lifespan;
    }

    public boolean equals(Object o) {
        if (!(o instanceof MatedComponent)) {
            return false;
        }
        MatedComponent matedComponent = ((MatedComponent)o);
        return ticksRemaining == matedComponent.ticksRemaining && lifespan == matedComponent.lifespan && container.getComponent(GeneticsComponent.class).equals(container.getComponent(GeneticsComponent.class));
    }
}
