// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.apiculture.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.inventory.logic.ItemDifferentiating;

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

    public LocusSampleComponent() {
    }

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
