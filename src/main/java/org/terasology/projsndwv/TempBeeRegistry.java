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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.texture.TextureRegionAsset;
import org.terasology.utilities.random.MersenneRandom;
import org.terasology.world.generator.WorldGenerator;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

/**
 * A temporary class providing helpers intended to be provided by a future bee registry.
 */
public class TempBeeRegistry {
    private static HashMap<Integer, Integer> lifespans = new HashMap<>();
    private static HashMap<Integer, Long> tickspeeds = new HashMap<>();
    private static HashMap<Integer, Production> productions = new HashMap<>();

    private static MersenneRandom random;

    private static Logger logger = LoggerFactory.getLogger(TempBeeRegistry.class);

    public static TextureRegionAsset<?> getTextureRegionAssetForSpeciesAndType(int species, int type) {
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        if (assetManager == null) {
            logger.error("No AssetManager in registry");
            return null;
        }

        // TODO: This call can probably be checked, but it'll take a serious refactor.
        Optional<TextureRegionAsset> textureRegionAsset = assetManager.getAsset("Apiculture:bee_" + new String[]{"a", "b", "c"}[species] + "_" + new String[]{"drone", "princess", "queen"}[type], TextureRegionAsset.class);
        if (!textureRegionAsset.isPresent()) {
            logger.error("Texture not found for species " + species + " and type " + type);
            return null;
        }

        return textureRegionAsset.get();
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

    public static EntityRef getProduceForSpeciesWithChance(int species) {
        MersenneRandom random = getRandom();
        Production production = productions.get(species);

        if (Objects.requireNonNull(random).nextFloat() < production.chance) {
            EntityManager entityManager = CoreRegistry.get(EntityManager.class);
            if (entityManager == null) {
                logger.error("No EntityManager in registry");
                return null;
            }
            return entityManager.create(production.prefab);
        }
        else {
            return EntityRef.NULL;
        }
    }

    private static MersenneRandom getRandom() {
        if (random == null) {
            WorldGenerator worldGenerator = CoreRegistry.get(WorldGenerator.class);
            if (worldGenerator == null) {
                logger.error("No WorldGenerator in registry");
                return null;
            }
            random = new MersenneRandom(worldGenerator.getWorldSeed().hashCode());
        }
        return random;
    }

    private static class Production {
        public final String prefab;
        public final float chance;

        public Production(float chance, String prefab) {
            this.prefab = prefab;
            this.chance = chance;
        }
    }

    static { // TODO: Down with static!
        lifespans.put(0, 3);
        lifespans.put(1, 6);
        lifespans.put(2, 9);

        tickspeeds.put(0, 100000L);
        tickspeeds.put(1, 75000L);
        tickspeeds.put(2, 50000L);

        productions.put(0, new Production(0.25f, "Apiculture:comb"));
        productions.put(1, new Production(0.25f, "Apiculture:comb"));
        productions.put(2, new Production(0.5f, "Apiculture:comb"));
    }
}
