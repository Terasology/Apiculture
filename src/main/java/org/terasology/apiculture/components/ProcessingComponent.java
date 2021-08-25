// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.apiculture.components;

import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Indicates to screens that processes are occurring in an apiary, extractor, or injector, and at what game time they will
 * complete.
 */
public final class ProcessingComponent implements Component<ProcessingComponent> {
    public long finishTime;

    public ProcessingComponent() { }

    public ProcessingComponent(long finishTime) {
        this.finishTime = finishTime;
    }

    @Override
    public void copyFrom(ProcessingComponent other) {
        this.finishTime = other.finishTime;
    }
}
