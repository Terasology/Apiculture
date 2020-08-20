// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.apiculture.ui;

import org.joml.Vector2i;
import org.terasology.nui.Canvas;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.LayoutConfig;

/**
 * A 1 pixel wide widget that fills vertical space in order to provide size hints to relative layouts.
 */
public class VerticalFiller extends CoreWidget {
    /**
     * The vertical height of this filler.
     */
    @LayoutConfig
    private int size = 1;

    @Override
    public void onDraw(Canvas canvas) { }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return new Vector2i(1, size);
    }
}
