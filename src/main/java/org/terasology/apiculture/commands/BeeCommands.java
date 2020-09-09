// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.apiculture.commands;

import org.terasology.apiculture.components.BeeComponent;
import org.terasology.apiculture.components.MatedComponent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterHeldItemComponent;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.genetics.components.GeneticsComponent;

@RegisterSystem
public class BeeCommands extends BaseComponentSystem {
    @In
    private EntityManager entityManager;

    @Command(value = "beeDumpGenes",
            shortDescription = "Shows the genes of a held bee",
            helpText = "Displays the raw genetic data from a bee currently being held",
            runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String dumpGenes(@Sender EntityRef client) {
        EntityRef item =
                client.getComponent(ClientComponent.class).character.getComponent(CharacterHeldItemComponent.class).selectedItem;
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
}
