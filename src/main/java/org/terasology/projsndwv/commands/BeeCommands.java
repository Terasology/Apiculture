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
import org.terasology.logic.inventory.events.DropItemEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.network.ClientComponent;
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
        if (item.getComponent(BeeComponent.class) == null) {
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
        return sb.toString();
    }

    @Command(value = "beeMateTarget",
            shortDescription = "Sets a held bee as the target for the 'beeMate'",
            helpText = "Sets the held item as the target for the 'beeMate' command. It will become the first parent of a bee mated with that command.",
            runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String mateTarget(@Sender EntityRef client) {
        EntityRef item = client.getComponent(ClientComponent.class).character.getComponent(CharacterHeldItemComponent.class).selectedItem;
        if (item.getComponent(BeeComponent.class) == null) {
            return "Held item is not a bee.";
        }

        mateTarget = item;
        return "";
    }

    @Command(value = "beeMate",
            shortDescription = "Mates a held bee with the target.",
            helpText = "Mates a held bee with the target previously set by 'beeMateTarget.' The target will become the primary parent, and the currently held bee will be the secondary parent.",
            runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String mate(@Sender EntityRef client) {
        EntityRef item = client.getComponent(ClientComponent.class).character.getComponent(CharacterHeldItemComponent.class).selectedItem;
        if (item.getComponent(BeeComponent.class) == null) {
            return "Held item is not a bee."; // TODO: (Soundwave) Use constants / Make translatable?
        }
        if (item.equals(mateTarget)) {
            return "Cannot mate bee with iteself.";
        }
        if (mateTarget == null || mateTarget.getComponent(BeeComponent.class) == null) {
            return "Target invalid. Please use 'beeMateTarget' to set a target."; // TODO: (Soundwave) Use constants for command names
        }

        mateTarget.addOrSaveComponent(new MatedComponent(item.getComponent(GeneticsComponent.class), entityManager));
        item.destroy();
        client.getComponent(ClientComponent.class).character.send(new ChangeHeldItemRequest(item));

        return "";
    }

    @Command(value = "beeBirth",
            shortDescription = "Causes a held bee to give birth",
            helpText = "Causes a held mated bee to give birth to a new generation, according to the mechanics of genetics. If provided, the generation will have <count> offspring. Otherwise, it'll have 2.",
            runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String birth(@Sender EntityRef client, @CommandParam(value = "count", required = false)Integer count) {
        if (count == null) {
            count = 2;
        }

        EntityRef item = client.getComponent(ClientComponent.class).character.getComponent(CharacterHeldItemComponent.class).selectedItem;
        if (item.getComponent(BeeComponent.class) == null || item.getComponent(MatedComponent.class) == null) {
            return "Held item is not a mated bee.";
        }

        Iterator<GeneticsComponent> offspringGenetics = getGenome().combine(item.getComponent(GeneticsComponent.class), item.getComponent(MatedComponent.class).container.getComponent(GeneticsComponent.class));

        for (int i = 0; i < count; i++) {
            EntityRef drop = entityManager.create("Apiculture:bee");

            drop.addComponent(offspringGenetics.next());

            drop.send(new DropItemEvent(client.getComponent(ClientComponent.class).character.getComponent(LocationComponent.class).getWorldPosition()));
        }

        return "";
    }

    private Genome getGenome() {
        if (genome == null) {
            genome = new Genome(5, worldGenerator.getWorldSeed().hashCode());
        }
        return genome;
    }
}
