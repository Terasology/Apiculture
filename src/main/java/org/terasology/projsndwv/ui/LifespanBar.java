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
package org.terasology.projsndwv.ui;

import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreWidget;

public class LifespanBar extends CoreWidget {
    private Color color = Color.RED;
    private float value = 1f;

    @Override
    public void onDraw(Canvas canvas) {
        Rect2i canvasRegion = canvas.getRegion();
        canvas.drawFilledRectangle(Rect2i.createFromMinAndSize(canvasRegion.minX(), canvasRegion.maxY() - (int)(value * canvasRegion.height()), canvasRegion.width(), (int)(canvasRegion.height() * value)), color);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return new Vector2i(8, 48);
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setFill(float value) {
        if (value > 1f) {
            this.value = 1f;
        }
        else {
            this.value = Math.max(value, 0f);
        }
    }
}
