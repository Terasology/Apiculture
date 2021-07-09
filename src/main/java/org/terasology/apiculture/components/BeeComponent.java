// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.apiculture.components;

import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.module.inventory.components.ItemDifferentiating;

import java.util.Objects;

/**
 * Indicates that an item is a bee.
 */
public final class BeeComponent implements Component<BeeComponent>, ItemDifferentiating {
    /**
     * Bee type, that is, whether a bee is a drone, princess, or queen.
     */
    public BeeType type;

    @Override
    public void copy(BeeComponent other) {
        this.type = other.type;
    }

    public enum BeeType {
        DRONE,
        PRINCESS,
        QUEEN
    }

    public boolean equals(Object o) {
        if (!(o instanceof BeeComponent)) {
            return false;
        }
        return ((BeeComponent) o).type == type;
    }

    public int hashCode() {
        return Objects.hash(type);
    }
}
