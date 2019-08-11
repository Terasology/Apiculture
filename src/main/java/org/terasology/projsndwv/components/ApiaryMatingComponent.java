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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.entitySystem.Component;
import org.terasology.projsndwv.systems.ApiarySystem;
import org.terasology.registry.CoreRegistry;

public final class ApiaryMatingComponent implements Component {
    public long mateFinishTime;

    private static final Logger logger = LoggerFactory.getLogger(ApiaryMatingComponent.class);

    public ApiaryMatingComponent() {
        Time time = CoreRegistry.get(Time.class);
        if (time == null) {
            logger.error("No Time in registry");
            return;
        }
        mateFinishTime = time.getGameTimeInMs() + ApiarySystem.MATING_TIME;
    }
}
