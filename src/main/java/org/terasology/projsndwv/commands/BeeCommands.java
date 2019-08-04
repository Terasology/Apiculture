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
package org.terasology.projsndwv.commands;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.characters.events.ChangeHeldItemRequest;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.events.DropItemEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.network.ClientComponent;
import org.terasology.projsndwv.TempBeeRegistry;
import org.terasology.projsndwv.components.BeeComponent;
import org.terasology.projsndwv.components.MatedComponent;
import org.terasology.projsndwv.genetics.Genome;
import org.terasology.projsndwv.genetics.components.GeneticsComponent;
import org.terasology.registry.In;
import org.terasology.world.generator.WorldGenerator;

import java.util.Iterator;

@RegisterSystem
public class BeeCommands extends BaseComponentSystem {
    @In
    private EntityManager entityManager;

    @In
    private WorldGenerator worldGenerator;

    private EntityRef mateTarget = null;

    private Genome genome;

    @Command(value = "beeDumpGenes",
            shortDescription = "Shows the genes of a held bee",
            helpText = "Displays the raw genetic data from a bee currently being held",
            runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String dumpGenes(@Sender EntityRef client) {
        EntityRef item = client.getComponent(ClientComponent.class).character.getComponent(CharacterHeldItemComponent.class).selectedItem;
        if (!item.hasComponent(BeeComponent.class)) {
            return "Held item is not a bee.";
        }

        GeneticsComponent genetics = item.getComponent(GeneticsComponent.class);
        StringBuilder sb = new StringBuilder();
        sb.append("Active: ");
        for (int i = 0; i < genetics.activeGenes.size(); i++) {
            sb.append(genetics.activeGenes.get(i));
            if (i != genetics.activeGenes.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("\nInactive: ");
        for (int i = 0; i < genetics.inactiveGenes.size(); i++) {
            sb.append(genetics.inactiveGenes.get(i));
            if (i != genetics.inactiveGenes.size() - 1) {
                sb.append(", ");
            }
        }
        if (item.hasComponent(MatedComponent.class)) {
            sb.append("\n\nMate:\nActive: ");
            genetics = item.getComponent(MatedComponent.class).container.getComponent(GeneticsComponent.class);
            for (int i = 0; i < genetics.activeGenes.size(); i++) {
                sb.append(genetics.activeGenes.get(i));
                if (i != genetics.activeGenes.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("\nInactive: ");
            for (int i = 0; i < genetics.inactiveGenes.size(); i++) {
                sb.append(genetics.inactiveGenes.get(i));
                if (i != genetics.inactiveGenes.size() - 1) {
                    sb.append(", ");
                }
            }
        }
        return sb.toString();
    }

    @Command(value = "beeMateTarget",
            shortDescription = "Sets a held princess as the target for the 'beeMate'",
            helpText = "Sets the held princess as the target for the 'beeMate' command.",
            runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String mateTarget(@Sender EntityRef client) {
        EntityRef item = client.getComponent(ClientComponent.class).character.getComponent(CharacterHeldItemComponent.class).selectedItem;
        BeeComponent beeComponent = item.getComponent(BeeComponent.class);
        if (beeComponent == null) {
            return "Held item is not a bee.";
        }
        if (beeComponent.type != BeeComponent.BeeType.PRINCESS) {
            return "Held item is not a princess";
        }

        mateTarget = item;
        return "";
    }

    @Command(value = "beeMate",
            shortDescription = "Mates a held drone with the target.",
            helpText = "Mates a held drone with the target previously set by 'beeMateTarget.'",
            runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String mate(@Sender EntityRef client) {
        EntityRef item = client.getComponent(ClientComponent.class).character.getComponent(CharacterHeldItemComponent.class).selectedItem;
        BeeComponent itemBeeComponent = item.getComponent(BeeComponent.class);
        if (itemBeeComponent == null) {
            return "Held item is not a bee."; // TODO: (Soundwave) Use constants / Make translatable?
        }
        if (itemBeeComponent.type != BeeComponent.BeeType.DRONE) {
            return "Held item is not a drone.";
        }
        if (mateTarget == null) {
            return "No mate target. Please use 'beeMateTarget' to set a valid target."; // TODO: (Soundwave) Use constants for command names
        }
        BeeComponent mateBeeComponent = mateTarget.getComponent(BeeComponent.class);
        if (mateBeeComponent == null || mateBeeComponent.type != BeeComponent.BeeType.PRINCESS) {
            return "Target invalid. Please use 'beeMateTarget' to set a valid target."; // TODO: (Soundwave) Use constants for command names
        }

        mateTarget.addComponent(new MatedComponent(item.getComponent(GeneticsComponent.class), entityManager));
        BeeComponent beeComponent = mateTarget.getComponent(BeeComponent.class);
        beeComponent.type = BeeComponent.BeeType.QUEEN;
        mateTarget.saveComponent(beeComponent);
        ItemComponent itemComponent = mateTarget.getComponent(ItemComponent.class);
        itemComponent.icon = TempBeeRegistry.getTextureRegionAssetForSpeciesAndType(mateTarget.getComponent(GeneticsComponent.class).activeGenes.get(0), 2);
        mateTarget.saveComponent(itemComponent);
        mateTarget.saveComponent(TempBeeRegistry.getDisplayNameComponentForSpeciesAndType(mateTarget.getComponent(GeneticsComponent.class).activeGenes.get(0), 2));
        item.destroy();
        client.getComponent(ClientComponent.class).character.send(new ChangeHeldItemRequest(item));

        return "";
    }

    @Command(value = "beeBirth",
            shortDescription = "Causes a held queen to give birth",
            helpText = "Causes a held queen to give birth to a new generation, according to the mechanics of genetics. If provided, the generation will have <count> drones. Otherwise, it'll have 2.",
            runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String birth(@Sender EntityRef client, @CommandParam(value = "count", required = false)Integer count) {
        if (count == null) {
            count = 2;
        }

        EntityRef item = client.getComponent(ClientComponent.class).character.getComponent(CharacterHeldItemComponent.class).selectedItem;
        BeeComponent beeComponent = item.getComponent(BeeComponent.class);
        if (beeComponent == null || !item.hasComponent(MatedComponent.class) || beeComponent.type != BeeComponent.BeeType.QUEEN) {
            return "Held item is not a queen.";
        }

        Iterator<GeneticsComponent> offspringGenetics = getGenome().combine(item.getComponent(GeneticsComponent.class), item.getComponent(MatedComponent.class).container.getComponent(GeneticsComponent.class));

        EntityRef drop = entityManager.create("Apiculture:bee_princess");
        drop.addComponent(offspringGenetics.next());
        ItemComponent itemComponent = drop.getComponent(ItemComponent.class);
        itemComponent.icon = TempBeeRegistry.getTextureRegionAssetForSpeciesAndType(drop.getComponent(GeneticsComponent.class).activeGenes.get(0), 1);
        drop.addComponent(itemComponent);
        drop.addComponent(TempBeeRegistry.getDisplayNameComponentForSpeciesAndType(drop.getComponent(GeneticsComponent.class).activeGenes.get(0), 1));
        drop.send(new DropItemEvent(client.getComponent(ClientComponent.class).character.getComponent(LocationComponent.class).getWorldPosition()));

        for (int i = 0; i < count; i++) {
            drop = entityManager.create("Apiculture:bee_drone");
            drop.addComponent(offspringGenetics.next());
            itemComponent = drop.getComponent(ItemComponent.class);
            itemComponent.icon = TempBeeRegistry.getTextureRegionAssetForSpeciesAndType(drop.getComponent(GeneticsComponent.class).activeGenes.get(0), 0);
            drop.addComponent(itemComponent);
            drop.addComponent(TempBeeRegistry.getDisplayNameComponentForSpeciesAndType(drop.getComponent(GeneticsComponent.class).activeGenes.get(0), 0));
            drop.send(new DropItemEvent(client.getComponent(ClientComponent.class).character.getComponent(LocationComponent.class).getWorldPosition()));
        }

        return "";
    }

    private Genome getGenome() {
        if (genome == null) {
            genome = new Genome(4, worldGenerator.getWorldSeed().hashCode());

            GeneticsComponent beeCComponent = new GeneticsComponent(4);

            beeCComponent.activeGenes.add(2); // TODO: (Soundwave) This is bad, both because the size isn't ensured, and because it's messy.
            beeCComponent.activeGenes.add(2);
            beeCComponent.activeGenes.add(2);
            beeCComponent.activeGenes.add(4);

            beeCComponent.inactiveGenes.add(2);
            beeCComponent.inactiveGenes.add(2);
            beeCComponent.inactiveGenes.add(2);
            beeCComponent.inactiveGenes.add(4);

            genome.registerMutation(0, 0, 1, beeCComponent, 0.05f);
        }
        return genome;
    }
}
