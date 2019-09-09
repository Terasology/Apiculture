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
package org.terasology.apiculture.components;

import org.terasology.entitySystem.Component;
import org.terasology.logic.inventory.ItemDifferentiating;

import java.util.Objects;

/**
 * Indicates an item is a genetic sample, and stores the locus and genotype of the gene it is a sample of.
 */
public class LocusSampleComponent implements Component, ItemDifferentiating {
    /**
     * The locus of the gene this sample is of.
     */
    public int locus;

    /**
     * The genotype of the gene this sample is of.
     */
    public int genotype;

    public LocusSampleComponent() { }

    public LocusSampleComponent(int locus, int genotype) {
        this.locus = locus;
        this.genotype = genotype;
    }

    public boolean equals(Object o) {
        if (!(o instanceof LocusSampleComponent)) {
            return false;
        }

        LocusSampleComponent sampleComponent = (LocusSampleComponent) o;
        return locus == sampleComponent.locus && genotype == sampleComponent.genotype;
    }

    public int hashCode() {
        return Objects.hash(locus, genotype);
    }
}
