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
package org.terasology.apiculture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.apiculture.components.BeeComponent;
import org.terasology.genetics.components.GeneticsComponent;
import org.terasology.apiculture.systems.ApiarySystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.texture.TextureRegionAsset;
import org.terasology.utilities.random.MersenneRandom;
import org.terasology.world.generator.WorldGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A temporary class providing helpers intended to be provided by a future bee registry.
 */
public final class TempBeeRegistry {
    private static Map<Integer, Integer> lifespans = new HashMap<>();
    private static Map<Integer, Long> tickspeeds = new HashMap<>();
    private static Map<Integer, Production> productions = new HashMap<>();
    private static Map<Integer, Map<Integer, String>> genotypeNames = new HashMap<>();

    private static MersenneRandom random;

    private static Logger logger = LoggerFactory.getLogger(TempBeeRegistry.class);

    private TempBeeRegistry() { }

    public static EntityRef modifyItemForSpeciesAndType(EntityRef entity) {
        if (!entity.hasComponent(BeeComponent.class)) {
            return entity;
        }

        int species = entity.getComponent(GeneticsComponent.class).activeGenes.get(ApiarySystem.LOCUS_SPECIES);
        BeeComponent.BeeType type = entity.getComponent(BeeComponent.class).type;

        String typeName = "NULL";
        switch(type) {
            case DRONE:
                typeName = "Drone";
                break;
            case PRINCESS:
                typeName = "Princess";
                break;
            case QUEEN:
                typeName = "Queen";
                break;
        }

        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        if (assetManager == null) {
            logger.error("No AssetManager in registry");
            return null;
        }

        // TODO: This call can probably be checked, but it'll take a serious refactor.
        Optional<TextureRegionAsset> textureRegionAsset =
                assetManager.getAsset("Apiculture:bee_" + new String[]{"a", "b", "c"}[species] + "_" + typeName.toLowerCase(), TextureRegionAsset.class);
        if (!textureRegionAsset.isPresent()) {
            logger.error("Texture not found for species " + species + " and type '" + typeName + "'");
            return null;
        }

        ItemComponent itemComponent = entity.getComponent(ItemComponent.class);

        if (type == BeeComponent.BeeType.DRONE) {
            itemComponent.stackId = "Apiculture:drone";
        }

        itemComponent.icon = textureRegionAsset.get();

        entity.addOrSaveComponent(itemComponent);

        DisplayNameComponent displayNameComponent = new DisplayNameComponent();
        displayNameComponent.name = new String[] {"A", "B", "C"}[species] + " " + typeName;

        entity.addOrSaveComponent(displayNameComponent);

        return entity;
    }

    public static int getLifespanFromGenome(int genome) {
        return lifespans.get(genome);
    }

    public static long getTickTimeFromGenome(int genome) {
        return tickspeeds.get(genome);
    }

    public static EntityRef getProduceForSpeciesWithChance(int species) {
        MersenneRandom rand = getRandom();
        Production production = productions.get(species);

        if (Objects.requireNonNull(rand).nextFloat() < production.chance) {
            EntityManager entityManager = CoreRegistry.get(EntityManager.class);
            if (entityManager == null) {
                logger.error("No EntityManager in registry");
                return null;
            }
            return entityManager.create(production.prefab);
        } else {
            return EntityRef.NULL;
        }
    }

    public static MersenneRandom getRandom() {
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

    public static String getDisplayNameComponentForLocusAndGenotype(int locus, int genotype) {
        return genotypeNames.get(locus).get(genotype);
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

        Map<Integer, String> map = new HashMap<>();
        map.put(0, "Short Life");
        map.put(1, "Normal Life");
        map.put(2, "Long Life");

        genotypeNames.put(ApiarySystem.LOCUS_LIFESPAN, map);

        tickspeeds.put(0, 100000L);
        tickspeeds.put(1, 75000L);
        tickspeeds.put(2, 50000L);

        map = new HashMap<>();
        map.put(0, "Slow Speed");
        map.put(1, "Normal Speed");
        map.put(2, "Fast Speed");

        genotypeNames.put(ApiarySystem.LOCUS_SPEED, map);

        map = new HashMap<>();
        map.put(1, "Single Offspring");
        map.put(2, "Double Offspring");
        map.put(4, "Quadruple Offspring");
        genotypeNames.put(ApiarySystem.LOCUS_OFFSPRING_COUNT, map);

        map = new HashMap<>();
        map.put(0, "Species A");
        map.put(1, "Species B");
        map.put(2, "Species C");
        genotypeNames.put(ApiarySystem.LOCUS_SPECIES, map);

        productions.put(0, new Production(0.25f, "Apiculture:comb"));
        productions.put(1, new Production(0.25f, "Apiculture:comb"));
        productions.put(2, new Production(0.5f, "Apiculture:comb"));
    }
}
