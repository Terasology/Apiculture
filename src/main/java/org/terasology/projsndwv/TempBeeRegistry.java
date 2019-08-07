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
package org.terasology.projsndwv;

import org.terasology.assets.management.AssetManager;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.TextureRegionAsset;

import java.util.HashMap;

/**
 * A temporary class providing helpers intended to be provided by a future bee registry.
 */
public class TempBeeRegistry {
    @In
    public static AssetManager assetManager;

    private static HashMap<Integer, Integer> lifespans = new HashMap<>();
    private static HashMap<Integer, Long> tickspeeds = new HashMap<>();

    public static TextureRegionAsset<?> getTextureRegionAssetForSpeciesAndType(int species, int type) {
        return (TextureRegionAsset<?>)CoreRegistry.get(AssetManager.class).getAsset("Apiculture:bee_" + new String[] {"a", "b", "c"}[species] + "_" + new String[] {"drone", "princess", "queen"}[type], TextureRegionAsset.class).get();
    }

    public static DisplayNameComponent getDisplayNameComponentForSpeciesAndType(int species, int type) {
        DisplayNameComponent displayNameComponent = new DisplayNameComponent();
        displayNameComponent.name = new String[] {"A", "B", "C"}[species] + " " + new String[] {"Drone", "Princess", "Queen"}[type];
        return displayNameComponent;
    }

    public static int getLifespanFromGenome(int genome) {
        return lifespans.get(genome);
    }

    public static long getTickTimeFromGenome(int genome) {
        return tickspeeds.get(genome);
    }

    static { // TODO: Down with static!
        lifespans.put(0, 3);
        lifespans.put(1, 6);
        lifespans.put(2, 9);

        tickspeeds.put(0, 100000L);
        tickspeeds.put(1, 75000L);
        tickspeeds.put(2, 50000L);
    }
}
