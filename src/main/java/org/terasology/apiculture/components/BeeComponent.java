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
 * Indicates that an item is a bee.
 */
public final class BeeComponent implements Component, ItemDifferentiating {
    /**
     * Bee type, that is, whether a bee is a drone, princess, or queen.
     */
    public BeeType type;

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
