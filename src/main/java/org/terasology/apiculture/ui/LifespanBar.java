// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.apiculture.ui;

import org.joml.primitives.Rectanglei;
import org.joml.Vector2i;
import org.terasology.math.JomlUtil;
import org.terasology.nui.Canvas;
import org.terasology.nui.Color;
import org.terasology.nui.CoreWidget;

public class LifespanBar extends CoreWidget {
    private Color color = Color.RED;
    private float value = 1f;

    @Override
    public void onDraw(Canvas canvas) {
        Rectanglei canvasRegion = canvas.getRegion();
        canvas.drawFilledRectangle(JomlUtil.rectangleiFromMinAndSize(canvasRegion.minX, canvasRegion.maxY - (int) (value * canvasRegion.lengthY()), canvasRegion.lengthX(),
                (int) (canvasRegion.lengthY() * value)), color);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return new Vector2i(8, 48);
    }

    /**
     * Sets the color of the lifespan bar's fill.
     *
     * @param color The new color.
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Sets the amount the lifespan bar is filled, as a decimal.
     *
     * Fill fill is clamped between 0 and 1.
     *
     * @param fill The percentge of the lifespan bar fill, expressed as a decimal.
     */
    public void setFill(float fill) {
        if (fill > 1f) {
            value = 1f;
        } else {
            value = Math.max(fill, 0f);
        }
    }
}
