/*
 * Copyright 2015-2016 Mark Vedder
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.javaru.iip.frc.ui.components;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.ui.components.panels.OpaquePanel;
import com.intellij.util.ui.UIUtil;



public class FrcIconComponent extends OpaquePanel
{
    protected BufferedImage myImage;


    public FrcIconComponent()
    {
    }
    
    public FrcIconComponent(@Nullable final Icon icon)
    {
        setIcon(icon);
    }


    @Override
    protected void paintChildren(@NotNull Graphics g)
    {
        if (myImage == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        setRenderingHints(g2);
        g2.drawImage(myImage, 0, 0, getWidth(), getHeight(), 0, 0, myImage.getWidth(), myImage.getHeight(), null);
    }

    public void setIcon(@Nullable final Icon icon)
    {
        if (icon != null)
        {
            myImage = UIUtil.createImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            final Graphics2D graphics2D = myImage.createGraphics();
            setRenderingHints(graphics2D);
            icon.paintIcon(this, graphics2D, 0, 0);
            graphics2D.dispose();
        }
        else
        {
            myImage = null;
        }
        revalidate();
        repaint();
    }


    private static void setRenderingHints(Graphics2D graphics2D)
    {
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
}
