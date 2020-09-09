// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.apiculture.components;

import org.terasology.engine.entitySystem.Component;

/**
 * Indicates to screens that processes are occurring in an apiary, extractor, or injector, and at what game time they
 * will complete.
 */
public final class ProcessingComponent implements Component {
    public long finishTime;

    public ProcessingComponent() {
    }

    public ProcessingComponent(long finishTime) {
        this.finishTime = finishTime;
    }
}
