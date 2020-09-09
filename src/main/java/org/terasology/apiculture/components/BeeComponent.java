// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.apiculture.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.inventory.logic.ItemDifferentiating;

import java.util.Objects;

/**
 * Indicates that an item is a bee.
 */
public final class BeeComponent implements Component, ItemDifferentiating {
    /**
     * Bee type, that is, whether a bee is a drone, princess, or queen.
     */
    public BeeType type;

    public boolean equals(Object o) {
        if (!(o instanceof BeeComponent)) {
            return false;
        }
        return ((BeeComponent) o).type == type;
    }

    public int hashCode() {
        return Objects.hash(type);
    }

    public enum BeeType {
        DRONE,
        PRINCESS,
        QUEEN
    }
}
